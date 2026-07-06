package com.fes.flashcard.installer.app.pages;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

record DocumentAdapter(Runnable runnable) implements DocumentListener {

	@Override
	public void insertUpdate(DocumentEvent e) { runnable.run(); }

	@Override
	public void removeUpdate(DocumentEvent e) { runnable.run(); }

	@Override
	public void changedUpdate(DocumentEvent e) { runnable.run(); }
}
