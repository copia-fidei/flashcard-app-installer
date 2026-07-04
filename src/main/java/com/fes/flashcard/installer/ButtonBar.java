package com.fes.flashcard.installer;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import static javax.swing.Action.NAME;
import static javax.swing.Box.createHorizontalGlue;
import static javax.swing.Box.createHorizontalStrut;
import static javax.swing.BoxLayout.LINE_AXIS;

public class ButtonBar extends JPanel {

	// Button titles
	public static final String CANCEL   = "Abbrechen";
	public static final String DEFAULTS = "Standardwerte";
	public static final String BACK     = "←";
	public static final String NEXT     = "→";
	public static final String APPLY    = "Anwenden";

	private final JButton cancelButton;
	private final JButton defaultsButton;
	private final JButton backButton;
	private final JButton nextButton;
	private final JButton applyButton;

	public ButtonBar() {
		setLayout(new BoxLayout(this, LINE_AXIS));

		cancelButton = new JButton(CANCEL);
		defaultsButton = new JButton(DEFAULTS);
		backButton = new JButton(BACK);
		nextButton = new JButton(NEXT);
		applyButton = new JButton(APPLY);

		add(cancelButton);
		add(createHorizontalStrut(10));
		add(defaultsButton);
		add(createHorizontalGlue());
		add(backButton);
		add(createHorizontalStrut(10));
		add(nextButton);
		add(createHorizontalStrut(10));
		add(applyButton);
	}

	// Demo
	void main() {
		TestFrames.showComponent("Buttons Bar", new ButtonBar());
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
