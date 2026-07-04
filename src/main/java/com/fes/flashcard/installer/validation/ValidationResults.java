package com.fes.flashcard.installer.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResults {

	private final List<ValidationResult> results = new ArrayList<>();

	public ValidationResults(List<ValidationResult> results) {
		this.results.addAll(results);
	}

	public List<ValidationResult> list() {
		return results;
	}

	public boolean contains(Severity severity) {
		return results.stream().anyMatch(result -> result.getSeverity() == severity);
	}

	// FIXME remove maybe
	public boolean isValid() {
		return results.isEmpty() || results.stream().noneMatch(result -> result.getSeverity() != Severity.ERROR);
	}
}
