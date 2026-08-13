package com.fes.flashcard.installer.validation;

import com.fes.flashcard.installer.utilities.Nls;

public enum Severity {

	// ordinal order is used for sorting
	INFO("Severity.info"),
	WARNING("Severity.warning"),
	ERROR("Severity.error");

	private final Nls nls = new Nls(this);

	private final String label;

	Severity(String key) {
		this.label = nls.get(key);
	}

	public String label() {
		return label;
	}
}
