package com.fes.flashcard.installer.app.pages;

import com.fes.flashcard.installer.page.PageData;
import com.fes.flashcard.installer.page.PageDataPool;
import com.fes.flashcard.installer.utilities.Nls;
import com.fes.flashcard.installer.validation.Severity;
import com.fes.flashcard.installer.validation.ValidationResults;

import java.io.IOException;

import static java.lang.Thread.currentThread;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

public class RootPasswordPageData extends PageData {

	private final Nls nls = new Nls(this);

	// Do not store the root password of the user!!!
	private char[] rootPassword = new char[0];

	public RootPasswordPageData(PageDataPool pageDataPool) {
		super(pageDataPool);
	}

	@Override
	public void load() {
		// Do not store the root password of the user!!!
	}

	@Override
	public void save() {
		// Do not store the root password of the user!!!
	}

	@Override
	public void loadDefaults() {
		rootPassword = new char[0];
	}

	@Override

	public ValidationResults validate() {
		var validationResults = new ValidationResults();
		try {
			// Clear sudo cache to ensure fresh authentication
			new ProcessBuilder("sudo", "-k").start().waitFor(); //NON-NLS
			var process = new ProcessBuilder("sudo", "-S", "true").start(); //NON-NLS
			try (var writer = process.outputWriter()) {
				writer.write(rootPassword);
				writer.newLine();
			}
			if (process.waitFor() != 0) {
				validationResults.add(nls.get("RootPasswordPageData.error.title.incorrectPassword"), nls.get("RootPasswordPageData.error.description.incorrectPassword"), Severity.ERROR);
			}
		} catch (IOException e) {
			log.log(WARNING, "Failed to validate root password", e);
		} catch (InterruptedException e) {
			log.log(INFO, "Validation interrupted", e);
			currentThread().interrupt();
		}
		return validationResults;
	}

	public char[] getRootPassword() {
		return rootPassword;
	}

	public void setRootPassword(char[] rootPassword) {
		this.rootPassword = rootPassword;
	}
}
