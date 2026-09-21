package com.epau.app.flashcard.app.installer.pages;

import com.epau.lib.swing.wizard.page.Page;
import com.epau.util.nls.Nls;

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

	private final Nls nls = new Nls(this);

	private final RootPasswordPageData rootpasswordPageData;

	private final JPasswordField passwordField = new JPasswordField(20);

	private final DelayedDocumentListener documentListener = new DelayedDocumentListener();

	public RootPasswordPage(RootPasswordPageData pageData) {
		super(pageData);
		this.rootpasswordPageData = pageData;
	}

	@Override
	public String getTitle() {
		return nls.get("RootPasswordPage.page.title.Root_password");
	}

	@Override
	public String getDescription() {
		return nls.get("RootPasswordPage.page.description.Root_password");
	}

	@Override
	public void build() {
		var description = new JTextArea(nls.get("RootPasswordPage.description.The_installation_and_configuration_of_the_database_must_be_done_as_Root_sudo"));
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.setEditable(false);
		description.setOpaque(false);
		description.setFocusable(false);
		var label = new JLabel(nls.get("RootPasswordPage.label.Sudo_password"));

		content.add(description,   new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
		content.add(label, 		   new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, LINE_START, NONE, 		new Insets(10, 10, 10, 10), 0, 0));
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

		private static final int DELAY = 1500;

		private final Timer timer;

		DelayedDocumentListener() {
			this.timer = new Timer(DELAY, _ -> pageChanged());
			this.timer.setRepeats(false);
		}

		private void handleEvent() {
			fireValidationStarted(); // this will disable the next button
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
