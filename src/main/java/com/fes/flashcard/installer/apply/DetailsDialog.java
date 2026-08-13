package com.fes.flashcard.installer.apply;

import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.operation.OperationListener;
import com.fes.flashcard.installer.operation.OperationStatus;
import com.fes.flashcard.installer.utilities.Nls;
import org.jetbrains.annotations.NonNls;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.List;

import static com.fes.flashcard.installer.operation.OperationStatusPresentation.getDescription;
import static com.fes.flashcard.installer.operation.OperationStatusPresentation.getIcon;
import static java.awt.Font.PLAIN;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;
import static javax.swing.Box.createRigidArea;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.SwingUtilities.windowForComponent;

@NonNls
public class DetailsDialog extends JDialog {

	private final Nls nls = new Nls(DetailsDialog.class);

	private final Operation operation;
	private final JLabel    statusLabel      = new JLabel();
	private final JTextArea progressTextArea = new JTextArea();
	private final JButton   cancelButton     = new JButton(nls.get("DetailsDialog.button.Cancel"));
	private final JButton   closeButton      = new JButton(nls.get("DetailsDialog.button.Close"));

	public DetailsDialog(Window parent, Operation operation) {
		super(parent, operation.getTitle());
		this.operation = operation;

		buildGUI();
		addListeners();
		updateUI();
	}

	private void buildGUI() {
		setLayout(new GridBagLayout());

		var descriptionLabel      = new JLabel(nls.get("DetailsDialog.label.Description"));
		var descriptionTextArea   = new JTextArea(3, 20);
		var descriptionScrollPane = new JScrollPane(descriptionTextArea);
		var statusTitleLabel      = new JLabel(nls.get("DetailsDialog.label.Status"));
		var progressTitleLabel    = new JLabel(nls.get("DetailsDialog.label.Progress"));
		var scrollPane            = new JScrollPane(progressTextArea);
		var separator             = new JSeparator(SwingConstants.HORIZONTAL);
		var buttonPanel           = new JPanel();

		descriptionTextArea.setText(operation.getDescription());
		descriptionTextArea.setEditable(false);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		statusLabel.setFont(statusTitleLabel.getFont().deriveFont(PLAIN));
		progressTextArea.setEditable(false);
		cancelButton.setToolTipText(nls.get("DetailsDialog.tooltip.Cancel_{0}", operation.getTitle()));
		closeButton.setToolTipText(nls.get("DetailsDialog.tooltip.Close_the_dialog"));

		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

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
			if (showConfirmDialog(windowForComponent(this), nls.get("DetailsDialog.description.Cancel_this_operation"), nls.get("DetailsDialog.title.Cancel_this_operation"), YES_NO_OPTION) == YES_OPTION) {
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
		statusLabel.setIcon(getIcon(status));
		statusLabel.setText(getDescription(status));

		progressTextArea.setText(getText(operation.getLogs()));
		progressTextArea.setCaretPosition(progressTextArea.getDocument().getLength());

		cancelButton.setEnabled(status == OperationStatus.RUNNING);
	}

	// An improvement could be to use jediterm
	private static String getText(List<String> logs) {
		StringBuilder result = new StringBuilder();
		StringBuilder currentLine = new StringBuilder();
		int cursorX = 0;
		boolean pendingCR = false;

		for (String log : logs) {
			for (int i = 0; i < log.length(); i++) {
				char c = log.charAt(i);
				if (pendingCR) {
					pendingCR = false;
					if (c == '\n') {
						// \r\n → flush current line and start a new one
						result.append(currentLine).append('\n');
						currentLine.setLength(0);
						cursorX = 0;
						continue;
					}
					// bare \r → move cursor to column 0, but do NOT clear
					cursorX = 0;
				}
				if (c == '\r') {
					pendingCR = true;
				} else if (c == '\n') {
					result.append(currentLine).append('\n');
					currentLine.setLength(0);
					cursorX = 0;
				} else {
					// overwrite at cursor position, pad with spaces if cursor jumped ahead
					if (cursorX < currentLine.length()) {
						currentLine.setCharAt(cursorX, c);
					} else {
						while (cursorX > currentLine.length()) {
							currentLine.append(' ');
						}
						currentLine.append(c);
					}
					cursorX++;
				}
			}
		}
		result.append(currentLine);
		return result.toString();
	}
}
