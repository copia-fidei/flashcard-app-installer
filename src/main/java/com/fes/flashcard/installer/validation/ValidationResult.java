package com.fes.flashcard.installer.validation;

public record ValidationResult(String title, String description, Severity severity, int priority) {

	public ValidationResult(String title, String description, int priority) {
		this(title, description, Severity.ERROR, priority);
	}

	public ValidationResult(String title, String description, Severity severity) {
		this(title, description, severity, 0);
	}
}
