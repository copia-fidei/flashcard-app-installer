package com.fes.flashcard.installer.validation.dialog;

import com.fes.flashcard.installer.validation.Severity;
import com.fes.flashcard.installer.validation.ValidationResult;
import com.fes.flashcard.installer.validation.ValidationResults;

import static javax.swing.SwingUtilities.invokeLater;

/// A validation results dialog with dummy data
// TODO use English
class ValidationResultsDialogDemo {

	static void main() {
		var results = new ValidationResults();
		results.add(new ValidationResult(
				"Verbindung erfolgreich",
				"Die Verbindung zur PostgreSQL-Datenbank konnte erfolgreich hergestellt werden.",
				Severity.INFO
		));
		results.add(new ValidationResult(
				"Benutzerpasswort",
				"Das angegebene Benutzerpasswort stimmt nicht mit dem Passwort der vorhandenen Datenbank überein.",
				Severity.WARNING
		));
		results.addError(
				"Ungültiger Port",
				"Der PostgreSQL-Port muss zwischen 1 und 65535 liegen.",
				0
		);
		results.addError(
				"Administrator kann sich nicht anmelden",
				"Die Authentifizierungsmethode des PostgreSQL-Administrators wird vom Installer nicht unterstützt.",
				1
		);
		results.addError(
				"Datenbank nicht erreichbar",
				"Die Datenbank 'collections' konnte nicht erreicht werden. Bitte überprüfen Sie die PostgreSQL-Konfiguration.",
				2
		);
		results.add("Kein Verbindungsaufbau möglich.", """
				Nur Verbindungen mit dem Verbindungstyp "host" und einer der Authentifizierungsmethoden trust, md5 oder scram-sha-256 werden für den Benutzer flashcards unterstützt. 
				Während der Installation werden folgende Einträge in die pg_hba.conf aufgenommen:		
				host    collections             flashcards             127.0.0.1/32            scram-sha-256
				host    collections             flashcards             ::1/128                 scram-sha-256
				""", Severity.WARNING);
		invokeLater(() -> new ValidationResultsDialog(null, results).setVisible(true));
	}
}
