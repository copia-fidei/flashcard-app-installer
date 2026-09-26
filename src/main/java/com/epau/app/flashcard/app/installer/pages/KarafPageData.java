package com.epau.app.flashcard.app.installer.pages;

import com.epau.app.flashcard.app.installer.Home;
import com.epau.lib.swing.wizard.page.PageData;
import com.epau.lib.swing.wizard.page.PageDataPool;
import com.epau.lib.validation.ValidationResults;
import com.epau.util.io.directory.Directory;
import com.epau.util.nls.Nls;
import org.jetbrains.annotations.NonNls;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.epau.lib.validation.Severity.WARNING;
import static java.lang.IO.println;
import static java.nio.file.Files.isRegularFile;
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
			results.addError(nls.get("KarafPageData.error.title.Invalid_parent_directory"), nls.get("KarafPageData.error.description.The_selected_parent_directory_must_be_under_home"), 0);
		}
		if (isRegularFile(parentDir)) {
			results.addError(nls.get("KarafPageData.error.title.Invalid_parent_directory"), nls.get("KarafPageData.error.description.A_file_is_selected_Only_directories_can_be_selected"), 1);
		}
		if (folderName.isBlank()) {
			results.addError(nls.get("KarafPageData.error.title.Invalid_folder_name"), nls.get("KarafPageData.error.description.The_folder_name_must_not_be_empty"), 0);
		}
		if (folderName.contains("/") || folderName.contains("\\")) {
			results.addError(nls.get("KarafPageData.error.title.Invalid_folder_name"), nls.get("KarafPageData.error.description.The_folder_name_must_not_contain_path_separators"), 1);
		}
		if (folderName.equals(".") || folderName.equals("..")) {
			results.addError(nls.get("KarafPageData.error.title.Invalid_folder_name"), nls.get("KarafPageData.error.description.The_folder_name_must_not_be_relative"), 1);
		}
		if (parentDir.getFileName().toString().equals(folderName)) {
			results.add(nls.get("KarafPageData.warning.title.Suspicious_directory_path"), nls.get("KarafPageData.warning.description.Folder_and_parent_folder_have_the_same_name"), WARNING);
		}
		if (Files.exists(getKarafInstallationDir()) && !new Directory(getKarafInstallationDir()).isEmpty()) {
			results.add(nls.get("KarafPageData.warning.title.Folder_is_not_empty"), nls.get("KarafPageData.warning.description.The_selected_folder_is_not_empty_Its_contents_will_be_deleted_during_installation"), WARNING);
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
