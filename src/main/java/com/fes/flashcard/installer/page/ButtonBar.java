package com.fes.flashcard.installer.page;

import com.fes.flashcard.installer.swing.TestFrames;
import com.fes.flashcard.installer.utilities.Nls;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import static javax.swing.Box.createHorizontalGlue;
import static javax.swing.Box.createHorizontalStrut;
import static javax.swing.BoxLayout.LINE_AXIS;

public class ButtonBar extends JPanel {

	private static final Nls nls = new Nls(ButtonBar.class);

	// Button titles
	public static final String  CANCEL         = nls.get("ButtonBar.Cancel");
	public static final String  DEFAULTS       = nls.get("ButtonBar.Defaults");
	public static final String  BACK           = "←";
	public static final String  NEXT           = "→";
	public static final String  APPLY          = nls.get("ButtonBar.Apply");

	private final JButton cancelButton   = new JButton(CANCEL);
	private final JButton defaultsButton = new JButton(DEFAULTS);
	private final JButton backButton     = new JButton(BACK);
	private final JButton nextButton     = new JButton(NEXT);
	private final JButton applyButton    = new JButton(APPLY);

	public ButtonBar() {
		setLayout(new BoxLayout(this, LINE_AXIS));

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

	public JButton getCancelButton() {
		return cancelButton;
	}

	public JButton getDefaultsButton() {
		return defaultsButton;
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

	// Demo
	void main() {
		TestFrames.showComponent("Buttons Bar", new ButtonBar()); //$NON-NLS
	}
}
