package com.fes.flashcard.installer.app.pages;

import com.fes.flashcard.installer.page.Page;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;

public class RootPasswordPage extends Page {

	private final RootPasswordPageData rootpasswordPageData;

	private final JPasswordField passwordField = new JPasswordField(20);

	private final DelayedDocumentListener documentListener = new DelayedDocumentListener();

	public RootPasswordPage(RootPasswordPageData pageData, Runnable onValidationChanged) {
		super(pageData, onValidationChanged);
		this.rootpasswordPageData = pageData;
	}

	@Override
	public String getTitle() {
		return "Rootpasswort";
	}

	@Override
	public String getDescription() {
		return "Das Rootpasswort des Systems";
	}

	@Override
	public void build() {
		var description = new JTextArea("Die Installation und Einrichtung der Datenbank muss als Root (sudo) durchgeführt werden.");
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.setEditable(false);
		description.setOpaque(false);
		var label = new JLabel("Passwort (sudo)");

		content.add(description, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
		content.add(label, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(passwordField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		// Filler
		content.add(new JPanel(), new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0, LINE_START, BOTH, new Insets(10, 10, 10, 10), 0, 0));
	}

	@Override
	protected void addListeners() {
		passwordField.getDocument().addDocumentListener(documentListener);
	}

	@Override
	protected void removeListeners() {
		passwordField.getDocument().removeDocumentListener(documentListener);
	}

	@Override
	protected void updatePageData() {
		rootpasswordPageData.setRootPassword(passwordField.getPassword());
	}

	@Override
	protected void fillGUI() {}

	@Override
	public void updateGUI() {}

	@Override
	public void updateDependantValues() {}

	class DelayedDocumentListener implements DocumentListener {

		private final Timer timer;

		DelayedDocumentListener() {
			this.timer = new Timer(1500, _ -> pageChanged());
			this.timer.setRepeats(false);
		}

		private void handleEvent() {
			isValid = false;
			onValidationChanged.run();
			timer.restart();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			handleEvent();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			handleEvent();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			handleEvent();
		}
	}

}
