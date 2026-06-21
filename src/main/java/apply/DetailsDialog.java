package apply;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DetailsDialog extends JDialog {

	private final Operation operation;
	private final JLabel    statusLabel;
	private final JTextArea progressTextArea;
	private final JButton cancelButton;
	private final JButton closeButton;

	public DetailsDialog(Frame parent, Operation operation) {
		super(parent, operation.getDescription(), false);
		this.operation = operation;
		this.statusLabel = new JLabel();
		this.progressTextArea = new JTextArea();
		this.cancelButton = new JButton("Abbrechen");
		this.closeButton = new JButton("Schließen");

		initialize();
		addListeners();
		updateUI();
	}

	public DetailsDialog(Dialog parent, Operation operation) {
		super(parent, operation.getDescription(), false);
		this.operation = operation;
		this.statusLabel = new JLabel();
		this.progressTextArea = new JTextArea();
		this.cancelButton = new JButton("Abbrechen");
		this.closeButton = new JButton("Schließen");

		initialize();
		addListeners();
		updateUI();
	}

	private void initialize() {
		setLayout(new BorderLayout());

		JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel descriptionPanel = new JPanel(new BorderLayout());
		JLabel descriptionLabel = new JLabel("Beschreibung:");
		JTextArea descriptionTextArea = new JTextArea(operation.getDescription());
		descriptionTextArea.setEditable(false);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setBackground(getBackground());
		descriptionPanel.add(descriptionLabel, BorderLayout.NORTH);
		descriptionPanel.add(new JScrollPane(descriptionTextArea), BorderLayout.CENTER);

		JPanel statusPanel = new JPanel(new BorderLayout());
		JLabel statusTitleLabel = new JLabel("Status:");
		statusPanel.add(statusTitleLabel, BorderLayout.NORTH);
		statusPanel.add(statusLabel, BorderLayout.CENTER);

		JPanel progressPanel = new JPanel(new BorderLayout());
		JLabel progressTitleLabel = new JLabel("Fortschritt:");
		progressTextArea.setEditable(false);
		progressTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		progressPanel.add(progressTitleLabel, BorderLayout.NORTH);
		progressPanel.add(new JScrollPane(progressTextArea), BorderLayout.CENTER);

		JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
		centerPanel.add(descriptionPanel, BorderLayout.NORTH);
		centerPanel.add(statusPanel, BorderLayout.CENTER);
		centerPanel.add(progressPanel, BorderLayout.SOUTH);

		contentPanel.add(centerPanel, BorderLayout.CENTER);
		add(contentPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		cancelButton.setToolTipText("Bricht diese apply.Operation ab.");
		closeButton.setToolTipText("Schließt den Dialog, bricht die apply.Operation nicht ab.");
		buttonPanel.add(cancelButton);
		buttonPanel.add(closeButton);
		add(buttonPanel, BorderLayout.SOUTH);

		pack();
		setSize(600, 500);
		setLocationRelativeTo(getParent());
	}

	private void addListeners() {
		cancelButton.addActionListener(_ -> {
			if (operation.getStatus() == OperationStatus.RUNNING) {
				if (ConfirmationDialog.showConfirmDialog(this, "Soll die apply.Operation wirklich abgebrochen werden?")) {
					operation.cancel(true);
				}
			}
		});

		closeButton.addActionListener(_ -> dispose());

		operation.addListener(new OperationListener() {
			@Override
			public void statusChanged(OperationStatus status) {
				SwingUtilities.invokeLater(DetailsDialog.this::updateUI);
			}

			@Override
			public void progressChanged(int progress) {
				SwingUtilities.invokeLater(DetailsDialog.this::updateUI);
			}
		});
	}

	private void updateUI() {
		OperationStatus status = operation.getStatus();
		statusLabel.setText(status.getDisplayText());

		StringBuilder progressText = new StringBuilder();
		for (String log : operation.getLogOutput()) {
			progressText.append(log).append("\n");
		}
		progressTextArea.setText(progressText.toString());
		progressTextArea.setCaretPosition(progressTextArea.getDocument().getLength());

		cancelButton.setEnabled(status == OperationStatus.RUNNING);
	}
}
