package apply;

import javax.swing.*;
import java.awt.*;

public class ConfirmationDialog extends JDialog {

	private boolean confirmed = false;

	public ConfirmationDialog(Frame parent, String message) {
		super(parent, "Abbrechen", true);
		initialize(message);
	}

	public ConfirmationDialog(Dialog parent, String message) {
		super(parent, "Abbrechen", true);
		initialize(message);
	}

	private void initialize(String message) {
		setLayout(new BorderLayout());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		JLabel messageLabel = new JLabel(message);
		messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		add(messageLabel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton yesButton = new JButton("Ja");
		JButton noButton = new JButton("Nein");

		yesButton.addActionListener(_ -> {
			confirmed = true;
			dispose();
		});

		noButton.addActionListener(_ -> dispose());

		buttonPanel.add(yesButton);
		buttonPanel.add(noButton);
		add(buttonPanel, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(noButton);

		pack();
		setLocationRelativeTo(getParent());
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public static boolean showConfirmDialog(Component parent, String message) {
		ConfirmationDialog dialog;
		if (parent instanceof Frame frame) {
			dialog = new ConfirmationDialog(frame, message);
		} else if (parent instanceof Dialog dialog1) {
			dialog = new ConfirmationDialog(dialog1, message);
		} else {
			dialog = new ConfirmationDialog((Frame) null, message);
		}
		dialog.setVisible(true);
		return dialog.isConfirmed();
	}
}
