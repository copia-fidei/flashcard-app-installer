package com.fes.flashcard.installer.app.operations;


import com.fes.flashcard.installer.DangerousPaths;
import com.fes.flashcard.installer.Directory;
import com.fes.flashcard.installer.PosixConverter;
import com.fes.flashcard.installer.TarGzFile;
import com.fes.flashcard.installer.TextBuilder;
import com.fes.flashcard.installer.app.Resources;
import com.fes.flashcard.installer.app.pages.KarafPageData;
import com.fes.flashcard.installer.operation.Counter;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.swing.DecisionDialog;
import com.fes.flashcard.installer.swing.DecisionDialog.Option;
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

	/// the directory where Karaf will be installed into
	private final Path karafDir;

	public InstallKarafOp(KarafPageData karafPageData) {
		super("Karaf Installation", "Installiere Apache Karaf 4.4.11");

		karafDir = karafPageData.getKarafInstallationDir();
	}

	public String getDescription() {
		var test = new TextBuilder();
		test.line("Installiere Apache Karaf 4.4.11");
		test.line("Prüfe, ob in " + karafDir + " bereits eine Karaf-Installation vorhanden ist");
		test.line("Entferne den bestehenden Inhalt von " + karafDir + ", falls vorhanden");
		test.line("Entpacke " + KARAF_ZIP_NAME + " nach " + karafDir);
		return test.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		setProgress(0);
		DangerousPaths.check(karafDir);
		setProgress(1);
		println("Prüfe ob Karaf bereits installiert ist...");
		Installed state = isKarafInstalled();
		setProgress(2);
		switch (state) {
			case FULLY -> {
				println("Karaf ist bereits vollständig installiert.");
				handleExistingInstallation(true);
			}
			case PARTIALLY -> {
				println("Karaf ist teilweise installiert.");
				handleExistingInstallation(false);
			}
			case NOT -> {
				println("Installiere Karaf...");
				installKaraf();
			}
		}
		setProgress(100);
		return "Installation durchgeführt";
	}

	private void handleExistingInstallation(boolean isFullyInstalled) throws Exception {
		setProgress(3);
		Choice choice = (Choice) showConflictDialog(isFullyInstalled).id();
		setProgress(4);
		if (choice == Choice.REINSTALL) {
			setProgress(5);
			println("Entferne vorhandene Installation...");
			removeKaraf();
			println("Installiere Karaf neu...");
			installKaraf();
		} else {
			println("Vorhandene Karaf-Installation wird wiederverwendet.");
		}
		setProgress(99);
	}

	private DecisionDialog.Option showConflictDialog(boolean isFullyInstalled) throws Exception {
		var reuseOption     = new Option(Choice.REUSE, "Karaf wiederverwenden", "Die vorhandene Installation wird verwendet");
		var reinstallOption = new Option(Choice.REINSTALL, "Karaf neu installieren", "Die vorhandene Installation wird entfernt und neu installiert");

		return DecisionDialog.showDialog("Karaf ist " + (isFullyInstalled ? "bereits installiert" : "teilweise installiert"), getDescription(isFullyInstalled), List.of(reuseOption, reinstallOption), isFullyInstalled ? reuseOption : reinstallOption);
	}

	private String getDescription(boolean isFullyInstalled) {
		if (isFullyInstalled) {
			return """
					Im Verzeichnis %s wurde eine Karaf-Installation gefunden.
					
					Es kann entweder eine Neuinstallation durchgeführt oder die vorhandene Installation weiterverwendet werden (empfohlen).
					
					Achtung: Bei einer Neuinstallation werden alle vorhandenen Daten gelöscht.
					""".formatted(karafDir);
		} else {
			return """
					Im Verzeichnis %s wurde eine unvollständige Karaf-Installation gefunden.
					Möglicherweise wurde die Installation einmal gestartet, aber nicht abgeschlossen.
					
					Es kann entweder eine Neuinstallation durchgeführt (empfohlen) oder die vorhandene unvollständige Installation weiterverwendet werden.
					
					Achtung: Bei einer Neuinstallation werden alle vorhandenen Daten im Verzeichnis gelöscht.
					""".formatted(karafDir);
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
		println("Erstelle Installationsverzeichnis: " + karafDir);
		createDirectories(karafDir);
		println("Entpacke Karaf: " + KARAF_ZIP_NAME);
		extractTarGz(Resources.KARAF.openStream(), karafDir);
		println("Karaf entpackt nach " + karafDir);
		println("Karaf erfolgreich installiert.");
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



