package com.fes.flashcard.installer.apply;

import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.operation.OperationAdapter;
import com.fes.flashcard.installer.operation.OperationStatus;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.*;

import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.SwingUtilities.windowForComponent;

public class ProgressSegment extends JPanel {

	private final Operation operation;
	private final JButton   cancelButton  = new SVGButton("icons/svgrepo/cancel-svgrepo-com.svg", 20);
	private final JProgressBar progressBar   = new JProgressBar(0, 100);
	private final JButton      detailsButton = new JButton("...");

	public ProgressSegment(Operation operation) {
		this.operation = operation;

		buildGUI();
		addListeners();
		updateGUI();
	}

	private void buildGUI() {
		setLayout(new BorderLayout(5, 0));
		cancelButton.setToolTipText(operation.getDescription() + " abbrechen");

		progressBar.setStringPainted(true);

		add(cancelButton, BorderLayout.WEST);
		add(progressBar, BorderLayout.CENTER);
		add(detailsButton, BorderLayout.EAST);
	}

	private void addListeners() {
		cancelButton.addActionListener(_ -> {
			if (showConfirmDialog(windowForComponent(this), "Soll die Operation wirklich abgebrochen werden?", "Abbrechen", YES_NO_OPTION) == YES_OPTION) {
				operation.cancel(true);
			}
		});
		detailsButton.addActionListener(_ -> new DetailsDialog(windowForComponent(this), operation).setVisible(true));

		operation.addListener(new OperationAdapter() {
			@Override
			public void statusChanged(OperationStatus status) {
				updateGUI();
			}

			@Override
			public void progressChanged(int progress) {
				updateGUI();
			}
		});
	}

	private void updateGUI() {
		OperationStatus status   = operation.getStatus();
		int             progress = operation.getProgress();

		progressBar.setValue(progress);
		progressBar.setString(status.getIcon() + " " + status.getDescription() + " (" + progress + "%)");
		cancelButton.setEnabled(status == OperationStatus.RUNNING);
	}
}
