package com.fes.flashcard.installer.validation;

public enum Severity {

	// ordinal order is used for sorting
	INFO("Info"),
	WARNING("Warnung"),
	ERROR("Fehler");

	private final String displayName;

	Severity(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
