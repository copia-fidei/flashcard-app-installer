package com.fes.flashcard.installer.apply;

import com.fes.flashcard.installer.TestFrames;
import com.fes.flashcard.installer.operation.BlockingOperation;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.operation.OperationStatus;
import com.fes.flashcard.installer.operation.WaitingOperation;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.Box.createHorizontalGlue;
import static javax.swing.Box.createRigidArea;
import static javax.swing.BoxLayout.LINE_AXIS;
import static javax.swing.BoxLayout.PAGE_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingConstants.HORIZONTAL;

public class ApplyDialog extends JDialog {

	private final List<Operation>       operations;
	private final JTextArea             descriptionTextArea = new JTextArea(4, 20);
	private final JButton               cancelButton        = new JButton("Abbrechen");
	private final JButton               closeButton         = new JButton("Schließen");
	private final JButton               startButton         = new JButton("Start");

	public ApplyDialog(Frame parent, List<Operation> operations) {
		super(parent, "Ausführung", true);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.operations = operations;

		buildGUI();
		addListeners();
	}

	// Demo
	static void main() {
		TestFrames.showDialog("Apply Dialog", frame -> new ApplyDialog(frame,
				List.of(
					new WaitingOperation("Test 1"),
					new WaitingOperation("Test 2", 5, true),
					new WaitingOperation("Test 3", 4, false),
					new BlockingOperation("Block", "Block forever"),
					new BlockingOperation("Block", "Block forever")
				)
		));
	}

	private void buildGUI() {
		setLayout(new BoxLayout(getContentPane(), PAGE_AXIS));

		descriptionTextArea.setEditable(false);
		var scrollPane  = new JScrollPane(descriptionTextArea);
		var emptyBorder = createEmptyBorder(10, 10, 10, 10);
		scrollPane.setBorder(emptyBorder);
		add(scrollPane);
		descriptionTextArea.setText(operations.stream().map(Operation::getDescription).collect(joining("\n")));
		var progressPanel = new JPanel();
		progressPanel.setLayout(new BoxLayout(progressPanel, Y_AXIS));
		var progressScrollPane = new JScrollPane(progressPanel);
		progressScrollPane.setBorder(null);
		for (var operation : operations) {
			var segment = new ProgressSegment(operation) {
				@Override
				public Dimension getMaximumSize() {
					return new Dimension(super.getMaximumSize().width, super.getPreferredSize().height);
				}
			};
			progressPanel.add(segment);
			progressPanel.add(createRigidArea(new Dimension(0, 5)));
		}
		var panelPreferredSize = progressPanel.getPreferredSize();
		progressScrollPane.setPreferredSize(new Dimension(panelPreferredSize.width, panelPreferredSize.height + operations.size() * 10));
		progressScrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, panelPreferredSize.height));
		progressScrollPane.setMinimumSize(panelPreferredSize);
		progressScrollPane.setBorder(emptyBorder);

		add(progressScrollPane);

		add(new JSeparator(HORIZONTAL));

		var buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, LINE_AXIS));
		cancelButton.setToolTipText("Bricht alle Operationen ab.");
		closeButton.setToolTipText("Schließt den Dialog.");
		startButton.setToolTipText("Führt alle Operationen aus.");
		buttonPanel.add(cancelButton);
		buttonPanel.add(createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(closeButton);
		buttonPanel.add(createHorizontalGlue());
		buttonPanel.add(startButton);

		buttonPanel.setBorder(emptyBorder);

		add(buttonPanel);

		cancelButton.setEnabled(false);
		closeButton.setEnabled(true);
		startButton.setEnabled(true);

		pack();
		setSize(700, 500);
		setLocationRelativeTo(getParent());
	}

	private void addListeners() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				if (isProcessing()) {
					showMessageDialog(ApplyDialog.this, "Die Installation ist noch im Gange. Der Dialog kann nicht geschlossen werden.", "Warnung", WARNING_MESSAGE);
				} else {
					dispose();
				}
			}
		});

		cancelButton.addActionListener(_ -> {
			if (showConfirmDialog(this, "Sollen alle Operationen wirklich abgebrochen werden?", "Abbrechen", YES_NO_OPTION) == YES_OPTION) {
				cancel();
			}
		});

		closeButton.addActionListener(_ -> dispose());

		startButton.addActionListener(_ -> {
			startButton.setEnabled(false);
			closeButton.setEnabled(false);
			cancelButton.setEnabled(true);
			operationsExecutor.execute();
		});
	}

	private boolean isProcessing() {
		return operations.stream().anyMatch(operation -> operation.getStatus() == OperationStatus.RUNNING);
	}

	private final SwingWorker<Void, Void> operationsExecutor = new OperationsExecutor();

	private class OperationsExecutor extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() {
			for (var operation : operations) {
				operation.run();
			}
			return null;
		}

		@Override
		protected void done() {
			closeButton.setEnabled(true);
			cancelButton.setEnabled(false);
		}
	}

	private void cancel() {
		for (var operation : operations) {
			operation.cancel(true);
		}
	}

}
