package com.fes.flashcard.installer.validation;

import java.util.ArrayList;
import java.util.List;

import static com.fes.flashcard.installer.validation.Severity.ERROR;

public record ValidationResults(List<ValidationResult> list) {

	public ValidationResults() {
		this(new ArrayList<>());
	}

	public void add(ValidationResult result) {
		list.add(result);
	}

	public void addError(String title, String description, int priority) {
		this.add(new ValidationResult(title, description, ERROR, priority));
	}

	public void add(String title, String description, Severity severity) {
		this.add(new ValidationResult(title, description, severity, 0));
	}

	public boolean contains(Severity severity) {
		return list.stream().anyMatch(result -> result.severity() == severity);
	}
}
