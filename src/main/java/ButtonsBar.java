import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.*;

import static javax.swing.Action.NAME;

public class ButtonsBar extends JPanel {

	// Button titles
	public static final String CANCEL   = "Abbrechen";
	public static final String DEFAULTS = "Standardwerte";
	public static final String BACK = "Zurück";
	public static final String NEXT = "Weiter";
	public static final String APPLY = "Anwenden";

	private final JButton cancelButton;
	private final JButton defaultsButton;
	private final JButton backButton;
	private final JButton nextButton;
	private final JButton applyButton;

	public ButtonsBar() {
		setLayout(new FlowLayout(FlowLayout.RIGHT));

		cancelButton = new JButton(CANCEL);
		defaultsButton = new JButton(DEFAULTS);
		backButton = new JButton(BACK);
		nextButton = new JButton(NEXT);
		applyButton = new JButton(APPLY);

		add(cancelButton);
		add(defaultsButton);
		add(backButton);
		add(nextButton);
		add(applyButton);
	}

	// Test
	void main() {
		ComponentTestFrame.show("Buttons Bar", new ButtonsBar());
	}

	public JButton getCancelButton() {
		return cancelButton;
	}

	public JButton getDefaultsButton() {
		return defaultsButton;
	}

	// Overkill?
	public void setNextButtonEnabled(boolean enabled) {
		nextButton.setEnabled(enabled);
	}

	public void setNextActionButKeepOriginalTitle(Action action) {
		action.putValue(NAME, nextButton.getText());
		nextButton.setAction(action);
	}

	public void setBackButtonEnabled(boolean enabled) {
		backButton.setEnabled(enabled);
	}

	public void setBackAction(Action action) {
		action.putValue(NAME, backButton.getText());
		backButton.setAction(action);
	}


	public JButton getBackButton() {
		return backButton;
	}

	public JButton getNextButton() {
		return nextButton;
	}

	public JButton getApplyButton() {
		return applyButton;
	}
}
