package com.fes.flashcard.installer.app.pages;

import com.fes.flashcard.installer.Home;
import com.fes.flashcard.installer.PageDataPool;
import com.fes.flashcard.installer.page.PageData;
import com.fes.flashcard.installer.validation.ValidationResults;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import static com.fes.flashcard.installer.validation.Severity.WARNING;
import static java.lang.IO.println;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Logger.getLogger;

public class KarafPageData extends PageData {

	// Preferences keys
	private static final String KARAF_PARENT_DIR  = "karaf.parent.dir";
	private static final String KARAF_FOLDER_NAME = "karaf.folder.name";

	// Default values
	private static final Path   DEFAULT_PARENT_DIR             = Home.PATH.resolve(".local/bin");
	private static final String DEFAULT_FOLDER_NAME            = "apache-karaf-4.4.11";
	private static final Path   DEFAULT_KARAF_INSTALLATION_DIR = DEFAULT_PARENT_DIR.resolve(DEFAULT_FOLDER_NAME);

	// Values
	private Path   parentDir  = DEFAULT_PARENT_DIR;
	private String folderName = DEFAULT_FOLDER_NAME;

	public KarafPageData(PageDataPool pageDataPool) {
		super(pageDataPool);
	}

	// Test
	static void main() {
		println(DEFAULT_KARAF_INSTALLATION_DIR);
	}

	@Override
	public void load() {
		this.parentDir = Path.of(preferences.get(KARAF_PARENT_DIR, DEFAULT_PARENT_DIR.toString()));
		this.folderName = preferences.get(KARAF_FOLDER_NAME, DEFAULT_FOLDER_NAME);
	}

	@Override
	public void save() {
		preferences.put(KARAF_PARENT_DIR, parentDir.toString());
		preferences.put(KARAF_FOLDER_NAME, folderName);
	}

	@Override
	public void loadDefaults() {
		parentDir = DEFAULT_PARENT_DIR;
		folderName = DEFAULT_FOLDER_NAME;

		preferences.remove(KARAF_PARENT_DIR);
		preferences.remove(KARAF_FOLDER_NAME);
	}

	@Override
	public ValidationResults validate() {
		var results = new ValidationResults();

		if (!parentDir.startsWith(Home.PATH)) {
			results.addError("Ungültiges übergeordnetes Verzeichnis", "Das ausgewählte übergeordnete Verzeichnis muss sich in einem Pfad unter /home befinden.", 0);
		}
		if (Files.isRegularFile(parentDir)) {
			results.addError("Ungültiges übergeordnetes Verzeichnis", "Eine Datei ist ausgewählt. Nur Verzeichnisse dürfen ausgewählt werden.", 1);
		}
		if (folderName.isBlank()) {
			results.addError("Ungültiger Ordnername", "Der Ordnername darf nicht leer sein.", 0);
		}
		if (folderName.contains("/") || folderName.contains("\\")) {
			results.addError("Ungültiger Ordnername", "Der Ordnername darf keine Pfadtrenner enthalten.", 1);
		}
		if (folderName.equals(".") || folderName.equals("..")) {
			results.addError("Ungültiger Ordnername", "Der Ordnername darf nicht relativ sein.", 1);
		}
		if (parentDir.getFileName().toString().equals(folderName)) {
			results.add("Verdächtiger Verzeichnispfad", "Ordner und übergeordneter Ordner haben den gleichen Namen.", WARNING);
		}
		if (!isDirEmpty(getKarafInstallationDir())) {
			results.add("Ordner ist nicht leer", "Der ausgewählte Ordner ist nicht leer. Sein Inhalt wird bei der Installation gelöscht.", WARNING);
		}
		return results;
	}

	private static boolean isDirEmpty(Path directory) {
		if (!Files.exists(directory)) {
			return true;
		}
		try (var entries = Files.newDirectoryStream(directory)) {
			return !entries.iterator().hasNext();
		} catch (IOException e) {
			getLogger(KarafPageData.class.getName()).log(Level.WARNING, "Could not check if directory is empty", e);
			return false;
		}
	}

	public Path getParentDir() {
		return parentDir;
	}

	public void setParentDir(Path parentDir) {
		requireNonNull(parentDir);
		this.parentDir = parentDir.normalize();
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		requireNonNull(folderName);
		this.folderName = folderName;
	}


	public Path getKarafInstallationDir() {
		return parentDir.resolve(folderName);
	}
}
