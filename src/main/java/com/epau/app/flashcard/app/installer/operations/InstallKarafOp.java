package com.epau.app.flashcard.app.installer.operations;


import com.epau.app.flashcard.app.installer.Resources;
import com.epau.app.flashcard.app.installer.pages.KarafPageData;
import com.epau.util.io.directory.Directory;
import com.epau.util.io.file.tar.gz.TarGzFile;
import com.epau.util.nls.Nls;
import com.epau.util.swing.operation.Counter;
import com.epau.util.swing.operation.Operation;
import com.epau.util.swing.option_dialog.Option;
import com.epau.util.swing.option_dialog.OptionDialog;
import com.epau.util.text.MultilineText;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;

import static com.epau.app.flashcard.app.installer.Resources.KARAF_ZIP_NAME;
import static com.epau.util.io.file.unix.PosixFilePermissionsConverter.getPermissionsFromDecimal;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.setPosixFilePermissions;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.concurrent.TimeUnit.SECONDS;

public class InstallKarafOp extends Operation {

	private final static Nls nls = new Nls(InstallKarafOp.class);

	/// the directory where Karaf will be installed into
	private final Path karafDir;

	public InstallKarafOp(KarafPageData karafPageData) {
		super(nls.get("InstallKarafOp.title"), nls.get("InstallKarafOp.description"));

		karafDir = karafPageData.getKarafInstallationDir();
	}

	public String getDescription() {
		var test = new MultilineText();
		test.add(nls.get("InstallKarafOp.description.Install_Apache_Karaf"));
		test.add(nls.get("InstallKarafOp.description.Check_if_Karaf_is_already_installed_in_{0}", karafDir));
		test.add(nls.get("InstallKarafOp.description.Remove_existing_content_from_{0}", karafDir));
		test.add(nls.get("InstallKarafOp.description.Extract_{0}_to_{1}", KARAF_ZIP_NAME, karafDir));
		return test.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		setProgress(0);
		IllegalPaths.check(karafDir);
		setProgress(1);
		println(nls.get("InstallKarafOp.println.Check_if_Karaf_is_already_installed"));
		Installed state = isKarafInstalled();
		setProgress(2);
		switch (state) {
			case FULLY -> {
				println(nls.get("InstallKarafOp.println.Karaf_is_already_fully_installed"));
				handleExistingInstallation(true);
			}
			case PARTIALLY -> {
				println(nls.get("InstallKarafOp.println.Karaf_is_partially_installed"));
				handleExistingInstallation(false);
			}
			case NOT -> {
				println(nls.get("InstallKarafOp.println.Extracting_Karaf"));
				installKaraf();
			}
		}
		SECONDS.sleep(1);
		setProgress(100);
		return nls.get("InstallKarafOp.println.Installation_completed");
	}

	private void handleExistingInstallation(boolean isFullyInstalled) throws Exception {
		setProgress(3);
		Choice choice = (Choice) showConflictDialog(isFullyInstalled).id();
		setProgress(4);
		if (choice == Choice.REINSTALL) {
			setProgress(5);
			println(nls.get("InstallKarafOp.println.Remove_existing_installation"));
			removeKaraf();
			println(nls.get("InstallKarafOp.println.Reinstall_Karaf"));
			installKaraf();
		} else {
			println(nls.get("InstallKarafOp.println.Existing_Karaf_installation_will_be_reused"));
		}
		setProgress(99);
	}

	private Option showConflictDialog(boolean isFullyInstalled) throws Exception {
		var reuseOption     = new Option(Choice.REUSE, nls.get("InstallKarafOp.button.Reuse_Karaf"), nls.get("InstallKarafOp.tooltip.The_existing_installation_will_be_used"));
		var reinstallOption = new Option(Choice.REINSTALL, nls.get("InstallKarafOp.button.Reinstall_Karaf"), nls.get("InstallKarafOp.tooltip.The_existing_installation_will_be_removed_and_reinstalled"));

		String title = isFullyInstalled ? nls.get("InstallKarafOp.title.Karaf_is_already_installed") : nls.get("InstallKarafOp.title.Karaf_is_partially_installed");
		return OptionDialog.showDialog(title, getDescription(isFullyInstalled), List.of(reuseOption, reinstallOption), isFullyInstalled ? reuseOption : reinstallOption);
	}

	private String getDescription(boolean isFullyInstalled) {
		if (isFullyInstalled) {
			return nls.get("InstallKarafOp.description.A_Karaf_installation_was_found_in_directory_{0}", karafDir);
		} else {
			return nls.get("InstallKarafOp.description.An_incomplete_Karaf_installation_was_found_in_directory_{0}", karafDir);
		}
	}

	int karafTarEntriesCount = 0;
	int karafDirFileCount    = 0;

	private Installed isKarafInstalled() throws IOException {
		if (!exists(karafDir)) {
			return Installed.NOT;
		}
		Set<String> karafTarEntries  = new TarGzFile(Resources.KARAF).getEntriesWithoutTopLevelDirectory();
		Set<String> karafDirChildren = new Directory(karafDir).getDescendants();
		karafTarEntriesCount = karafTarEntries.size();
		karafDirFileCount = karafDirChildren.size();

		if (karafDirChildren.containsAll(karafTarEntries) && karafDirChildren.size() >= karafTarEntries.size()) {
			return Installed.FULLY;
		} else if (!karafDirChildren.isEmpty()) {
			return Installed.PARTIALLY;
		}
		return Installed.NOT;
	}

	private void installKaraf() throws IOException {
		println(nls.get("InstallKarafOp.println.Create_installation_directory_{0}", karafDir));
		createDirectories(karafDir);
		println(nls.get("InstallKarafOp.println.Extract_Karaf_{0}", KARAF_ZIP_NAME));
		extractTarGz(Resources.KARAF.openStream(), karafDir);
		println(nls.get("InstallKarafOp.println.Karaf_extracted_to_{0}", karafDir));
		println(nls.get("InstallKarafOp.println.Karaf_successfully_installed"));
	}


	private void removeKaraf() throws IOException {
		var counter = new Counter(getProgress(), 50, karafDirFileCount);

		if (exists(karafDir)) {
			println(nls.get("InstallKarafOp.println.Delete_directory_{0}", karafDir));
			walkFileTree(karafDir, new SimpleFileVisitor<>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (isCancelled()) {
						return FileVisitResult.TERMINATE;
					}
					println(nls.get("InstallKarafOp.println.File_{0}_is_being_deleted", file));
					delete(file);
					setProgress(counter.up());
					return CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (isCancelled()) {
						return FileVisitResult.TERMINATE;
					}
					if (exc == null) {
						println(nls.get("InstallKarafOp.println.Directory_{0}_is_being_deleted", dir));
						delete(dir);
						setProgress(counter.up());
						return CONTINUE;
					} else {
						throw exc;
					}
				}
			});
		}
	}

	private void extractTarGz(InputStream tarGz, Path destination) throws IOException {
		var counter = new Counter(getProgress(), 98, karafTarEntriesCount);

		try (var tarGzArchive = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(tarGz)))) {
			TarArchiveEntry entry;
			while ((entry = tarGzArchive.getNextEntry()) != null) {
				if(isCancelled()) {
					return;
				}
				String name = entry.getName();
				// Strip top-level directory
				int slash = name.indexOf('/');
				if (slash >= 0) {
					name = name.substring(slash + 1);
				}
				Path outputPath = destination.resolve(name);
				if (entry.isFile()) {
					if (!exists(outputPath.getParent())) {
						createDirectories(outputPath.getParent());
					}
					if (!exists(outputPath)) {
						createFile(outputPath);
					}
					copy(tarGzArchive, outputPath, REPLACE_EXISTING);
					setPosixFilePermissions(outputPath, getPermissionsFromDecimal(entry.getMode()));
				}
				setProgress(counter.up());
				println(nls.get("InstallKarafOp.println.Extracted_{0}", name));
			}
		}
	}

	private enum Installed {
		NOT, PARTIALLY, FULLY
	}
}