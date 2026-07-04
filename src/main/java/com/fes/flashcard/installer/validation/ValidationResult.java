package com.fes.flashcard.installer.validation;

public class ValidationResult {

	private final String   title;
	private final String   description;
	private final Severity severity;
	private final int      priority;

	public ValidationResult(String title, String description, int priority) {
		this.title = title;
		this.description = description;
		this.severity = Severity.ERROR;
		this.priority = priority;
	}

	public ValidationResult(String title, String description, Severity severity) {
		this.title = title;
		this.description = description;
		this.severity = severity;
		this.priority = 0;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public Severity getSeverity() {
		return severity;
	}

	public int getPriority() {
		return priority;
	}

}
