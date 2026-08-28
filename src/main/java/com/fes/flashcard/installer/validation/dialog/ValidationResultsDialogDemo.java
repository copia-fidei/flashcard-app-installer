package com.fes.flashcard.installer.validation.dialog;

import com.fes.flashcard.installer.validation.Severity;
import com.fes.flashcard.installer.validation.ValidationResult;
import com.fes.flashcard.installer.validation.ValidationResults;

import static javax.swing.SwingUtilities.invokeLater;

/// A validation results dialog with dummy data
class ValidationResultsDialogDemo {

	static void main() {
		var results = new ValidationResults();

		results.add(new ValidationResult(
				"Connection successful",
				"The connection to the PostgreSQL database was successfully established.",
				Severity.INFO
		));
		results.add(new ValidationResult(
				"Wrong user password",
				"The specified user password does not match the password of the existing database. During the installation, the password will be replaced.",
				Severity.WARNING
		));
		results.addError(
				"Invalid port",
				"Port should be between 1 and 65535.",
				0
		);
		results.addError(
				"Unable to login administrator",
				"The installer supports only the following authentication methods for the administrator role 'postgres': peer, trust, scram-sha-256, and md5. The pg_hba.conf authentication file must be adjusted manually.",
				1
		);
		results.addError(
				"Unable to connect",
				"Only connections with the connection type \"host\" and one of the authentication methods trust, md5, or scram-sha-256 are supported for the user flashcards. The pg_hba.conf authentication file must be adjusted during installation.",
				2
		);
		results.add(
				"Unable to connect",
				"""
				Only connections with the connection type "host" and one of the authentication methods trust, md5, or scram-sha-256 are supported for the user flashcards.
				
				The following entries will be added to pg_hba.conf during the installation:
				
				host    collections    flashcards    127.0.0.1/32    scram-sha-256
				host    collections    flashcards    ::1/128         scram-sha-256
				""",
				Severity.WARNING
		);
		invokeLater(() -> new ValidationResultsDialog(null, results).setVisible(true));
	}
}
