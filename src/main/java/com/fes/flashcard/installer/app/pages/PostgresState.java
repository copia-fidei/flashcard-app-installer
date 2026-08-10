package com.fes.flashcard.installer.app.pages;

public enum PostgresState {
	NOT_INSTALLED,
	/// program has been installed or reinstalled by the installer
	INSTALLED_BY_INSTALLER,
	/// program had been already installed before this installer started
	ALREADY_INSTALLED
}
