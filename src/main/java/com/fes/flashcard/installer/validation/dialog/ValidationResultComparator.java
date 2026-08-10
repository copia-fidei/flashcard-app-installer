package com.fes.flashcard.installer.validation.dialog;

import com.fes.flashcard.installer.validation.ValidationResult;

import java.util.Comparator;

class ValidationResultComparator implements Comparator<ValidationResult> {

	@Override
	public int compare(ValidationResult result1, ValidationResult result2) {
		int severityComparison = Integer.compare(result2.severity().ordinal(), result1.severity().ordinal());
		if (severityComparison != 0) {
			return severityComparison;
		}
		return Integer.compare(result2.priority(), result1.priority());
	}
}
