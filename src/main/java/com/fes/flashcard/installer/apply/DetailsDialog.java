package com.fes.flashcard.installer.apply;

import com.fes.flashcard.installer.TestFrames;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.operation.OperationListener;
import com.fes.flashcard.installer.operation.OperationStatus;
import com.fes.flashcard.installer.operation.WaitingOperation;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.*;
import java.util.List;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;
import static javax.swing.Box.createRigidArea;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.SwingUtilities.windowForComponent;

// TODO do not show exception type
public class DetailsDialog extends JDialog {

	private final Operation operation;
	private final JLabel    statusLabel      = new JLabel();
	private final JTextArea progressTextArea = new JTextArea();
	private final JButton   cancelButton     = new JButton("Abbrechen");
	private final JButton   closeButton      = new JButton("Schließen");

	public DetailsDialog(Window parent, Operation operation) {
		super(parent, operation.getTitle());
		this.operation = operation;

		buildGUI();
		addListeners();
		updateUI();
	}

	static void main() {
		TestFrames.showDialog("Details Dialog", frame -> {
			var dialog = new DetailsDialog(frame, new WaitingOperation("Wait 10 seconds", 10));
			dialog.setModal(true);
			dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			return dialog;
		});
	}

	private void buildGUI() {
		setLayout(new GridBagLayout());
		var descriptionLabel    = new JLabel("Beschreibung:");
		var descriptionTextArea = new JTextArea(3, 20);
		descriptionTextArea.setText(operation.getDescription());
		descriptionTextArea.setEditable(false);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		var descriptionScrollPane = new JScrollPane(descriptionTextArea);
		var statusTitleLabel      = new JLabel("Status:");
		var progressTitleLabel    = new JLabel("Fortschritt:");
		progressTextArea.setEditable(false);
		var scrollPane = new JScrollPane(progressTextArea);
		var separator  = new JSeparator(SwingConstants.HORIZONTAL);

		var buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		cancelButton.setToolTipText("Bricht diese Operation ab.");
		closeButton.setToolTipText("Schließt den Dialog, bricht die Operation nicht ab.");
		buttonPanel.add(cancelButton);
		buttonPanel.add(createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(closeButton);

		add(descriptionLabel, 		new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, LINE_START, NONE, 		 new Insets(10, 10,  0, 10), 0, 0));
		add(descriptionScrollPane, 	new GridBagConstraints(0, 1, 1, 1, 1.0, 0.5, LINE_START, BOTH, 		 new Insets(10, 10,  0, 10), 0, 0));
		add(statusTitleLabel, 		new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, LINE_START, NONE, 		 new Insets(10, 10,  0, 10), 0, 0));
		add(statusLabel, 			new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, LINE_START, NONE, 		 new Insets(10, 10,  0, 10), 0, 0));
		add(progressTitleLabel, 	new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, LINE_START, NONE, 		 new Insets(10, 10,  0, 10), 0, 0));
		add(scrollPane, 			new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0, LINE_START, BOTH, 		 new Insets(10, 10,  0, 10), 0, 0));
		add(separator, 				new GridBagConstraints(0, 6, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10,  0,  0,  0), 0, 0));
		add(buttonPanel, 			new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, LINE_START, NONE, 		 new Insets(10, 10, 10, 10), 0, 0));

		pack();
		setSize(600, 500);
		setLocationRelativeTo(getParent());
	}

	private void addListeners() {
		cancelButton.addActionListener(_ -> {
			if (showConfirmDialog(windowForComponent(this), "Soll die Operation wirklich abgebrochen werden?", "Abbrechen", YES_NO_OPTION) == YES_OPTION) {
				operation.cancel(true);
			}
		});

		closeButton.addActionListener(_ -> dispose());

		operation.addListener(new OperationListener() {
			@Override
			public void statusChanged(OperationStatus status) {
				updateUI();
			}

			@Override
			public void progressChanged(int progress) {
				updateUI();
			}

			@Override
			public void intermediateResults(List<String> intermediateResults) {
				 updateUI();
			}
		});
	}

	private void updateUI() {
		OperationStatus status = operation.getStatus();
		statusLabel.setText(status.getDisplayText());

		String progressText = String.join("\n", operation.getLogs());
		progressTextArea.setText(progressText);
		progressTextArea.setCaretPosition(progressTextArea.getDocument().getLength());

		cancelButton.setEnabled(status == OperationStatus.RUNNING);
	}
}
