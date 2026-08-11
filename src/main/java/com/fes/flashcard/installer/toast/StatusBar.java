package com.fes.flashcard.installer.toast;

import com.fes.flashcard.installer.Colors;
import com.fes.flashcard.installer.Icons;

public record StatusBar(Toast toast) {

	private static final int ICON_SIZE = 20;

	public StatusBar() {
		this(new Toast());
	}

	public void displayError(String message) {
		toast.display(Icons.error(ICON_SIZE), message, Colors.ERROR);
	}

	public void displayWarning(String message) {
		toast.display(Icons.warning(ICON_SIZE), message, Colors.WARNING);
	}

	public void displayInfo(String message) {
		toast.display(Icons.info(ICON_SIZE), message, Colors.INFO);
	}
}
