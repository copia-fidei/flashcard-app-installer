package com.fes.flashcard.installer.page;

import com.fes.flashcard.installer.validation.Severity;
import com.fes.flashcard.installer.validation.ValidationResult;
import com.fes.flashcard.installer.validation.ValidationResults;

import java.util.stream.Stream;

import static com.fes.flashcard.installer.validation.Severity.ERROR;
import static java.util.Comparator.comparing;

/**
 * Utility class to get the most important validation messages.
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
record ValidationSummary(ValidationResults results) {

	String mostImportantError() {
		return filter(ERROR).max(comparing(ValidationResult::getPriority)).get().getDescription();
	}

	// does not consider priority
	String first(Severity severity) {
		return filter(severity).findFirst().get().getDescription();
	}

	private Stream<ValidationResult> filter(Severity severity) {
		return results.list().stream().filter(result -> result.getSeverity() == severity);
	}
}
