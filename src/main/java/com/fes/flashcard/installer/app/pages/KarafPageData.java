package com.fes.flashcard.installer.app.pages;

import com.fes.flashcard.installer.page.PageData;
import com.fes.flashcard.installer.PageDataPool;
import com.fes.flashcard.installer.validation.Severity;
import com.fes.flashcard.installer.validation.ValidationResult;
import com.fes.flashcard.installer.validation.ValidationResults;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.lang.IO.println;
import static java.util.Objects.requireNonNull;

public class KarafPageData extends PageData {

	// Constants
	private static final Path HOME_PATH = Path.of(System.getProperty("user.home"));

	// Preferences keys
	private static final String KARAF_INSTALLATION_DIR = "karaf.installation.dir";

	// Default values
	private static final Path defaultKarafInstallationDir = HOME_PATH.resolve(".local/bin/apache-karaf-4.4.8");

	// Values
	private Path karafInstallationDir = defaultKarafInstallationDir;

	public KarafPageData(PageDataPool pageDataPool) {
		super(pageDataPool);
	}

	// Test
	static void main() {
		println(HOME_PATH);
		println(defaultKarafInstallationDir);
	}

	@Override
	public void load() {
		String karafInstallationDir = preferences.get(KARAF_INSTALLATION_DIR, defaultKarafInstallationDir.toString());
		println("Loading karafInstallationDir: " + karafInstallationDir);
		this.karafInstallationDir = Path.of(karafInstallationDir);
	}

	@Override
	public void save() {
		preferences.put(KARAF_INSTALLATION_DIR, karafInstallationDir.toString());
	}

	@Override
	public void loadDefaults() {
		karafInstallationDir = defaultKarafInstallationDir;
		preferences.remove(KARAF_INSTALLATION_DIR);
	}

	@Override
	public ValidationResults validate() {
		List<ValidationResult> results = new ArrayList<>();

		if (!karafInstallationDir.startsWith(HOME_PATH)) {
			results.add(new ValidationResult("Karaf Installation Directory", "Karaf installation directory must be inside home directory", Severity.ERROR));
		}

		return new ValidationResults(results);
	}

	public Path getKarafInstallationDir() {
		return karafInstallationDir;
	}

	public void setKarafInstallationDir(Path karafInstallationDir) {
		requireNonNull(karafInstallationDir);
		this.karafInstallationDir = karafInstallationDir;
	}
}
