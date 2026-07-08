package com.fes.flashcard.installer.app.pages;

import com.fes.flashcard.installer.PageDataPool;
import com.fes.flashcard.installer.page.PageData;
import com.fes.flashcard.installer.validation.Severity;
import com.fes.flashcard.installer.validation.ValidationResult;
import com.fes.flashcard.installer.validation.ValidationResults;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RootPasswordPageData extends PageData {

	private char[] rootPassword = new char[0];

	public RootPasswordPageData(PageDataPool pageDataPool) {
		super(pageDataPool);
	}

	@Override
	public void load() {
		// Do not store the root password of the user!
	}

	@Override
	public void save() {
		// Do not store the root password of the user!
	}

	@Override
	public void loadDefaults() {
		rootPassword = new char[0];
	}

	@Override
	public ValidationResults validate() {
		var validationResults = new ArrayList<ValidationResult>();
		try {
			// Clear sudo cache to ensure fresh authentication
			new ProcessBuilder("sudo", "-k").start().waitFor();
			var process = new ProcessBuilder("sudo", "-S", "true").start();
			try (var writer = process.outputWriter()) {
				writer.write(rootPassword);
				writer.newLine();
			}
			int exitCode = process.waitFor();
			if (exitCode != 0) {
				validationResults.add(new ValidationResult("Falsches Passwort", "Das Passwort für Rootuser ist falsch.", Severity.ERROR));
			}
		} catch (IOException e) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed to validate root password", e);
		} catch (InterruptedException e) {
			Logger.getLogger(getClass().getName()).log(Level.INFO, "Validation interrupted", e);
		}
		return new ValidationResults(validationResults);
	}

	public char[] getRootPassword() {
		return rootPassword;
	}

	public void setRootPassword(char[] rootPassword) {
		this.rootPassword = rootPassword;
	}
}
