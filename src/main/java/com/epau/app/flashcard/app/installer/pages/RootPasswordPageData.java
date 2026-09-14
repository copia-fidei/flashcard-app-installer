package com.epau.app.flashcard.app.installer.pages;

import com.epau.lib.swing.installer.page.PageData;
import com.epau.lib.swing.installer.page.PageDataPool;
import com.epau.lib.validation.Severity;
import com.epau.lib.validation.ValidationResults;
import com.epau.util.nls.Nls;

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
		var results = new ValidationResults();
		try {
			// Clear sudo cache to ensure fresh authentication
			new ProcessBuilder("sudo", "-k").start().waitFor(); //NON-NLS
			var process = new ProcessBuilder("sudo", "-S", "true").start(); //NON-NLS
			try (var writer = process.outputWriter()) {
				writer.write(rootPassword);
				writer.newLine();
			}
			if (process.waitFor() != 0) {
				results.add(nls.get("RootPasswordPageData.error.title.Incorrect_password"), nls.get("RootPasswordPageData.error.description.The_password_for_the_root_user_is_incorrect"), Severity.ERROR);
			}
		} catch (IOException e) {
			log.log(WARNING, "Failed to validate root password", e);
		} catch (InterruptedException e) {
			log.log(INFO, "Validation interrupted", e);
			currentThread().interrupt();
		}
		return results;
	}

	public char[] getRootPassword() {
		return rootPassword;
	}

	public void setRootPassword(char[] rootPassword) {
		this.rootPassword = rootPassword;
	}
}
