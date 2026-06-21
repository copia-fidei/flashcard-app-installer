package apply;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.currentThread;
import static javax.swing.SwingUtilities.invokeLater;

public class ApplyDialog extends JDialog {

	private final List<Operation>       operations;
	private final List<ProgressSegment> progressSegments    = new ArrayList<>();
	private final JTextArea             descriptionTextArea = new JTextArea();
	private final JButton               cancelButton        = new JButton("Abbrechen");
	private final JButton               closeButton         = new JButton("Schließen");
	private final JButton               startButton         = new JButton("Start");
	private       boolean               processing          = false;

	public ApplyDialog(Frame parent, List<Operation> operations) {
		super(parent, "Ausführung", true);
		this.operations = operations;

		initialize();
		addListeners();
	}

	public ApplyDialog(Dialog parent, List<Operation> operations) {
		super(parent, "Ausführung", true);
		this.operations = operations;

		initialize();
		addListeners();
	}

	static void main() {
		invokeLater(() -> {
			var frame = new JFrame("Test");
			frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

			ApplyDialog applyDialog = new ApplyDialog(frame, List.of());

			frame.pack();
			frame.setLocationRelativeTo(null);
			//frame.setVisible(true);
			applyDialog.setVisible(true);

		});
	}

	private void initialize() {
		setLayout(new BorderLayout());

		JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel descriptionPanel = new JPanel(new BorderLayout());
		JLabel descriptionLabel = new JLabel("Beschreibung:");
		descriptionTextArea.setEditable(false);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		descriptionPanel.add(descriptionLabel, BorderLayout.NORTH);
		descriptionPanel.add(new JScrollPane(descriptionTextArea), BorderLayout.CENTER);

		StringBuilder descriptionBuilder = new StringBuilder();
		for (Operation operation : operations) {
			descriptionBuilder.append("- ").append(operation.getDescription()).append("\n");
		}
		descriptionTextArea.setText(descriptionBuilder.toString());

		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
		JScrollPane progressScrollPane = new JScrollPane(progressPanel);

		for (Operation operation : operations) {
			ProgressSegment segment = new ProgressSegment(operation, this);
			progressSegments.add(segment);
			progressPanel.add(segment);
			progressPanel.add(Box.createVerticalStrut(5));
		}

		JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
		centerPanel.add(descriptionPanel, BorderLayout.NORTH);
		centerPanel.add(progressScrollPane, BorderLayout.CENTER);

		contentPanel.add(centerPanel, BorderLayout.CENTER);
		add(contentPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		cancelButton.setToolTipText("Bricht alle Operationen ab.");
		closeButton.setToolTipText("Schließt den Dialog.");
		startButton.setToolTipText("Führt alle Operationen aus.");
		buttonPanel.add(cancelButton);
		buttonPanel.add(closeButton);
		buttonPanel.add(startButton);
		add(buttonPanel, BorderLayout.SOUTH);

		cancelButton.setEnabled(false);
		closeButton.setEnabled(true);
		startButton.setEnabled(true);

		pack();
		setSize(700, 500);
		setLocationRelativeTo(getParent());

		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (processing) {
					JOptionPane.showMessageDialog(ApplyDialog.this, "Die Installation ist noch im Gange. Der Dialog kann nicht geschlossen werden.", "Warnung", JOptionPane.WARNING_MESSAGE);
				} else {
					dispose();
				}
			}
		});
	}

	private void addListeners() {
		cancelButton.addActionListener(_ -> {
			if (ConfirmationDialog.showConfirmDialog(this, "Sollen alle Operationen wirklich abgebrochen werden?")) {
				cancelAllOperations();
			}
		});

		closeButton.addActionListener(_ -> {
			if (!processing) {
				dispose();
			}
		});

		startButton.addActionListener(_ -> {
			startButton.setEnabled(false);
			closeButton.setEnabled(false);
			cancelButton.setEnabled(true);
			processing = true;
			executeOperations();
		});
	}

	private void executeOperations() {
		for (Operation operation : operations) {
			operation.setStatus(OperationStatus.RUNNING);
			operation.execute();
		}

		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() {
				while (true) {
					boolean allComplete = true;
					for (Operation operation : operations) {
						OperationStatus status = operation.getStatus();
						if (status == OperationStatus.RUNNING || status == OperationStatus.NOT_STARTED) {
							allComplete = false;
							break;
						}
					}
					if (allComplete) {
						break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						currentThread().interrupt();
						break;
					}
				}
				return null;
			}

			@Override
			protected void done() {
				processing = false;
				closeButton.setEnabled(true);
				cancelButton.setEnabled(false);
			}
		}.execute();
	}

	private void cancelAllOperations() {
		for (Operation operation : operations) {
			if (operation.getStatus() == OperationStatus.RUNNING) {
				operation.cancel(true);
			}
		}
	}
}
