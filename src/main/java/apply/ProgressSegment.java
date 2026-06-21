package apply;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProgressSegment extends JPanel {

	private final Operation operation;
	private final JButton   cancelButton;
	private final JProgressBar progressBar;
	private final JButton detailsButton;
	private final Window parentWindow;

	public ProgressSegment(Operation operation, Window parentWindow) {
		this.operation = operation;
		this.parentWindow = parentWindow;
		this.cancelButton = new JButton("🚫");
		this.progressBar = new JProgressBar(0, 100);
		this.detailsButton = new JButton("...");

		initialize();
		addListeners();
		updateUI();
	}

	private void initialize() {
		setLayout(new BorderLayout(5, 0));
		setBorder(new EmptyBorder(5, 5, 5, 5));

		cancelButton.setPreferredSize(new Dimension(30, 25));
		cancelButton.setToolTipText(operation.getDescription() + " abbrechen");

		progressBar.setStringPainted(true);

		detailsButton.setPreferredSize(new Dimension(40, 25));

		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		leftPanel.add(cancelButton);

		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		rightPanel.add(detailsButton);

		add(leftPanel, BorderLayout.WEST);
		add(progressBar, BorderLayout.CENTER);
		add(rightPanel, BorderLayout.EAST);
	}

	private void addListeners() {
		cancelButton.addActionListener(_ -> {
			if (operation.getStatus() == OperationStatus.RUNNING) {
				if (ConfirmationDialog.showConfirmDialog(parentWindow, "Soll die apply.Operation wirklich abgebrochen werden?")) {
					operation.cancel(true);
				}
			}
		});

		detailsButton.addActionListener(_ -> {
			DetailsDialog detailsDialog;
			if (parentWindow instanceof Frame frame) {
				detailsDialog = new DetailsDialog(frame, operation);
			} else if (parentWindow instanceof Dialog dialog) {
				detailsDialog = new DetailsDialog(dialog, operation);
			} else {
				detailsDialog = new DetailsDialog((Frame) null, operation);
			}
			detailsDialog.setVisible(true);
		});

		operation.addListener(new OperationListener() {
			@Override
			public void statusChanged(OperationStatus status) {
				SwingUtilities.invokeLater(ProgressSegment.this::updateGUI);
			}

			@Override
			public void progressChanged(int progress) {
				SwingUtilities.invokeLater(ProgressSegment.this::updateGUI);
			}
		});
	}

	private void updateGUI() {
		OperationStatus status = operation.getStatus();
		int progress = operation.getOperationProgress();

		progressBar.setValue(progress);
		progressBar.setString(status.getIcon() + " " + status.getDescription() + " (" + progress + "%)");

		cancelButton.setEnabled(status == OperationStatus.RUNNING);
	}
}
