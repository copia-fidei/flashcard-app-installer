package com.fes.flashcard.installer.app.pages;

import com.fes.flashcard.installer.Home;
import com.fes.flashcard.installer.page.PageData;
import com.fes.flashcard.installer.page.PageDataPool;
import com.fes.flashcard.installer.utilities.Directory;
import com.fes.flashcard.installer.utilities.Nls;
import com.fes.flashcard.installer.validation.ValidationResults;
import org.jetbrains.annotations.NonNls;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.fes.flashcard.installer.validation.Severity.WARNING;
import static java.lang.IO.println;
import static java.util.Objects.requireNonNull;

public class KarafPageData extends PageData {

	private final Nls nls = new Nls(this);

	// Preferences keys
	private static final String KARAF_PARENT_DIR  = "karaf.parent.dir";
	private static final String KARAF_FOLDER_NAME = "karaf.folder.name";

	// Default values
	private static final         Path   DEFAULT_PARENT_DIR             = Home.PATH.resolve(".local/bin");
	private static final @NonNls String DEFAULT_FOLDER_NAME            = "apache-karaf-4.4.11";

	// Values
	private Path   parentDir  = DEFAULT_PARENT_DIR;
	private String folderName = DEFAULT_FOLDER_NAME;

	public KarafPageData(PageDataPool pageDataPool) {
		super(pageDataPool);
	}

	// Test
	static void main() {
		println(DEFAULT_PARENT_DIR.resolve(DEFAULT_FOLDER_NAME));
	}

	@Override
	public void load() {
		this.parentDir  = Path.of(preferences.get(KARAF_PARENT_DIR, DEFAULT_PARENT_DIR.toString()));
		this.folderName = preferences.get(KARAF_FOLDER_NAME, DEFAULT_FOLDER_NAME);
	}

	@Override
	public void save() {
		preferences.put(KARAF_PARENT_DIR, parentDir.toString());
		preferences.put(KARAF_FOLDER_NAME, folderName);
	}

	@Override
	public void loadDefaults() {
		parentDir  = DEFAULT_PARENT_DIR;
		folderName = DEFAULT_FOLDER_NAME;

		preferences.remove(KARAF_PARENT_DIR);
		preferences.remove(KARAF_FOLDER_NAME);
	}

	@Override
	public ValidationResults validate() {
		var results = new ValidationResults();

		if (!parentDir.startsWith(Home.PATH)) {
			results.addError(nls.get("KarafPageData.error.title.invalidParentDirectory"), nls.get("KarafPageData.error.description.invalidParentDirectory"), 0);
		}
		if (Files.isRegularFile(parentDir)) {
			results.addError(nls.get("KarafPageData.error.title.parentDirectoryIsFile"), nls.get("KarafPageData.error.description.parentDirectoryIsFile"), 1);
		}
		if (folderName.isBlank()) {
			results.addError(nls.get("KarafPageData.error.title.invalidFolderName"), nls.get("KarafPageData.error.description.emptyFolderName"), 0);
		}
		if (folderName.contains("/") || folderName.contains("\\")) {
			results.addError(nls.get("KarafPageData.error.title.invalidFolderName"), nls.get("KarafPageData.error.description.separatorNotAllowedInFolderName"), 1);
		}
		if (folderName.equals(".") || folderName.equals("..")) {
			results.addError(nls.get("KarafPageData.error.title.invalidFolderName"), nls.get("KarafPageData.error.description.relativeFolderNameNotAllowed"), 1);
		}
		if (parentDir.getFileName().toString().equals(folderName)) {
			results.add(nls.get("KarafPageData.warning.title.suspiciousDirectoryPath"), nls.get("KarafPageData.warning.description.suspiciousDirectoryPath"), WARNING);
		}
		if (!new Directory(getKarafInstallationDir()).isEmpty()) {
			results.add(nls.get("KarafPageData.warning.title.folderNotEmpty"), nls.get("KarafPageData.warning.description.folderNotEmpty"), WARNING);
		}
		return results;
	}

	public Path getParentDir() {
		return parentDir;
	}

	public void setParentDir(Path parentDir) {
		this.parentDir = requireNonNull(parentDir).normalize();
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = requireNonNull(folderName);
	}


	public Path getKarafInstallationDir() {
		return parentDir.resolve(folderName);
	}
}
