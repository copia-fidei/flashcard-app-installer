package com.fes.flashcard.installer.app.pages;

import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class DelayedDocumentListener implements DocumentListener {

	private final Timer timer;

	DelayedDocumentListener(Runnable runnable) {
		this.timer = new Timer(1000, _ -> runnable.run());
		this.timer.setRepeats(false);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		timer.restart();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		timer.restart();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		timer.restart();
	}
}
