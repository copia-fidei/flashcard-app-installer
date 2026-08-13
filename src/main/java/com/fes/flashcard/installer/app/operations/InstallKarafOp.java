package com.fes.flashcard.installer.app.operations;


import com.fes.flashcard.installer.app.Resources;
import com.fes.flashcard.installer.app.pages.KarafPageData;
import com.fes.flashcard.installer.operation.Counter;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.swing.DecisionDialog;
import com.fes.flashcard.installer.swing.DecisionDialog.Option;
import com.fes.flashcard.installer.utilities.Directory;
import com.fes.flashcard.installer.utilities.Nls;
import com.fes.flashcard.installer.utilities.PosixConverter;
import com.fes.flashcard.installer.utilities.TarGzFile;
import com.fes.flashcard.installer.utilities.TextBuilder;
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

import static com.fes.flashcard.installer.app.Resources.KARAF_ZIP_NAME;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.setPosixFilePermissions;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class InstallKarafOp extends Operation {

	private final static Nls nls = new Nls(InstallKarafOp.class);

	/// the directory where Karaf will be installed into
	private final Path karafDir;

	public InstallKarafOp(KarafPageData karafPageData) {
		super(nls.get("InstallKarafOp.title"), nls.get("InstallKarafOp.description"));

		karafDir = karafPageData.getKarafInstallationDir();
	}

	public String getDescription() {
		var test = new TextBuilder();
		test.line(nls.get("InstallKarafOp.description.installApacheKaraf"));
		test.line(nls.get("InstallKarafOp.description.checkIfKarafAlreadyInstalled", karafDir));
		test.line(nls.get("InstallKarafOp.description.removeExistingContent", karafDir));
		test.line(nls.get("InstallKarafOp.description.extractKarafZip", KARAF_ZIP_NAME, karafDir));
		return test.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		setProgress(0);
		IllegalPaths.check(karafDir);
		setProgress(1);
		println(nls.get("InstallKarafOp.println.checkIfKarafAlreadyInstalled"));
		Installed state = isKarafInstalled();
		setProgress(2);
		switch (state) {
			case FULLY -> {
				println(nls.get("InstallKarafOp.println.karafIsAlreadyFullyInstalled"));
				handleExistingInstallation(true);
			}
			case PARTIALLY -> {
				println(nls.get("InstallKarafOp.println.karafIsPartiallyInstalled"));
				handleExistingInstallation(false);
			}
			case NOT -> {
				println(nls.get("InstallKarafOp.println.extractingKaraf"));
				installKaraf();
			}
		}
		setProgress(100);
		return nls.get("InstallKarafOp.println.installationCompleted");
	}

	private void handleExistingInstallation(boolean isFullyInstalled) throws Exception {
		setProgress(3);
		Choice choice = (Choice) showConflictDialog(isFullyInstalled).id();
		setProgress(4);
		if (choice == Choice.REINSTALL) {
			setProgress(5);
			println(nls.get("InstallKarafOp.println.removeExistingInstallation"));
			removeKaraf();
			println(nls.get("InstallKarafOp.println.reinstallKaraf"));
			installKaraf();
		} else {
			println(nls.get("InstallKarafOp.println.existingKarafInstallationWillBeReused"));
		}
		setProgress(99);
	}

	private DecisionDialog.Option showConflictDialog(boolean isFullyInstalled) throws Exception {
		var reuseOption     = new Option(Choice.REUSE, nls.get("InstallKarafOp.dialog.optionReuse"), nls.get("InstallKarafOp.dialog.optionReuse.tooltip"));
		var reinstallOption = new Option(Choice.REINSTALL, nls.get("InstallKarafOp.dialog.optionReinstall"), nls.get("InstallKarafOp.dialog.optionReinstall.tooltip"));

		String title = isFullyInstalled ? nls.get("InstallKarafOp.dialog.alreadyInstalled.title") : nls.get("InstallKarafOp.dialog.partiallyInstalled.title");
		return DecisionDialog.showDialog(title, getDescription(isFullyInstalled), List.of(reuseOption, reinstallOption), isFullyInstalled ? reuseOption : reinstallOption);
	}

	private String getDescription(boolean isFullyInstalled) {
		if (isFullyInstalled) {
			return nls.get("InstallKarafOp.dialog.descriptionFullyInstalled", karafDir);
		} else {
			return nls.get("InstallKarafOp.dialog.descriptionPartiallyInstalled", karafDir);
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
		println(nls.get("InstallKarafOp.println.createInstallationDirectory", karafDir));
		createDirectories(karafDir);
		println(nls.get("InstallKarafOp.println.extractKaraf", KARAF_ZIP_NAME));
		extractTarGz(Resources.KARAF.openStream(), karafDir);
		println("Karaf extracted to " + karafDir);
		println("Karaf successfully installed.");
	}


	private void removeKaraf() throws IOException {
		var counter = new Counter(getProgress(), 50, karafDirFileCount);

		if (exists(karafDir)) {
			println("Lösche Verzeichnis: " + karafDir);
			walkFileTree(karafDir, new SimpleFileVisitor<>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (isCancelled()) {
						return FileVisitResult.TERMINATE;
					}
					println("Datei " + file + " wird gelöscht");
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
						println("Verzeichnis " + dir + " wird gelöscht");
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
					setPosixFilePermissions(outputPath, PosixConverter.posixPermissionsFromDecimal(entry.getMode()));
				}
				setProgress(counter.up());
				println("Entpackt: " + name);
			}
		}
	}

	private enum Installed {
		NOT, PARTIALLY, FULLY
	}
}



