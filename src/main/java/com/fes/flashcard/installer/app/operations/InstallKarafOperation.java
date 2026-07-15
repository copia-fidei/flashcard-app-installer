package com.fes.flashcard.installer.app.operations;


import com.fes.flashcard.installer.app.Dir;
import com.fes.flashcard.installer.app.Resources;
import com.fes.flashcard.installer.app.TarGz;
import com.fes.flashcard.installer.app.operations.DecisionDialog.Option;
import com.fes.flashcard.installer.app.pages.KarafPageData;
import com.fes.flashcard.installer.operation.Operation;
import io.github.compress4j.archivers.tar.TarGzArchiveExtractor;
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

import static io.github.compress4j.archivers.ArchiveExtractor.ErrorHandlerChoice.RETRY;
import static io.github.compress4j.archivers.ArchiveExtractor.EscapingSymlinkPolicy.DISALLOW;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.setPosixFilePermissions;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


public class InstallKarafOperation extends Operation {

	private final Path directoryToInstallKarafInto;

	public InstallKarafOperation(KarafPageData karafPageData) {
		super("Karaf", "Installiere Apache Karaf 4.4.11");

		directoryToInstallKarafInto = karafPageData.getKarafInstallationDir();
	}

	public String getDescription() {
		var description = new TextBuilder();
		description.line("Installiere Apache Karaf 4.4.11");
		description.line("Prüfe, ob in " + directoryToInstallKarafInto + " bereits eine Karaf-Installation vorhanden ist");
		description.line("Entferne den bestehenden Inhalt von " + directoryToInstallKarafInto + ", falls vorhanden");
		description.line("Entpacke " + Resources.KARAF_ZIP_NAME + " nach " + directoryToInstallKarafInto);
		return description.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		setProgress(0);
		DangerousPaths.check(directoryToInstallKarafInto);
		setProgress(1);
		publish("Prüfe ob Karaf bereits installiert ist...");
		Installed state = isKarafInstalled();
		setProgress(2);
		switch (state) {
			case FULLY -> {
				publish("Karaf ist bereits vollständig installiert.");
				handleExistingInstallation(true);
			}
			case PARTIALLY -> {
				publish("Karaf ist teilweise installiert.");
				handleExistingInstallation(false);
			}
			case NOT -> {
				publish("Installiere Karaf...");
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
			publish("Entferne vorhandene Installation...");
			removeKaraf();
			publish("Installiere Karaf neu...");
			installKaraf();
		} else {
			publish("Vorhandene Karaf-Installation wird wiederverwendet.");
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
					""".formatted(directoryToInstallKarafInto);
		} else {
			return """
					Im Verzeichnis %s wurde eine unvollständige Karaf-Installation gefunden.
					Möglicherweise wurde die Installation einmal gestartet, aber nicht abgeschlossen.
					
					Es kann entweder eine Neuinstallation durchgeführt (empfohlen) oder die vorhandene unvollständige Installation weiterverwendet werden.
					
					Achtung: Bei einer Neuinstallation werden alle vorhandenen Daten im Verzeichnis gelöscht.
					""".formatted(directoryToInstallKarafInto);
		}
	}

	int karafTarEntriesCount = 0;
	int karafDirFileCount    = 0;

	private Installed isKarafInstalled() throws IOException {
		if (!exists(directoryToInstallKarafInto)) {
			return Installed.NOT;
		}
		Set<String> karafTarEntries = TarGz.entriesWithoutTopLevelDirectory(Resources.KARAF.openStream());
		Set<String> karafDirFiles   = Dir.descendantsOf(directoryToInstallKarafInto);
		karafTarEntriesCount = karafTarEntries.size();
		karafDirFileCount = karafDirFiles.size();

		if (karafDirFiles.containsAll(karafTarEntries) && karafDirFiles.size() >= karafTarEntries.size()) {
			return Installed.FULLY;
		} else if (!karafDirFiles.isEmpty()) {
			return Installed.PARTIALLY;
		}
		return Installed.NOT;
	}

	private void installKaraf() throws IOException {
		publish("Erstelle Installationsverzeichnis: " + directoryToInstallKarafInto);
		createDirectories(directoryToInstallKarafInto);
		publish("Entpacke Karaf: " + Resources.KARAF_ZIP_NAME);
		extractTarGz(Resources.KARAF.openStream(), directoryToInstallKarafInto);
		publish("Karaf entpackt nach " + directoryToInstallKarafInto);
		publish("Karaf erfolgreich installiert.");
	}


	private void removeKaraf() throws IOException {
		var counter = new Counter(getProgress(), 50, karafDirFileCount);

		if (exists(directoryToInstallKarafInto)) {
			publish("Lösche Verzeichnis: " + directoryToInstallKarafInto);
			walkFileTree(directoryToInstallKarafInto, new SimpleFileVisitor<>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					publish("Datei " + file + " wird gelöscht");
					delete(file);
					setProgress(counter.up());

					return CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (exc == null) {
						publish("Verzeichnis " + dir + " wird gelöscht");
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
				publish("Entpackt: " + name);
			}
		}
	}

	// TODO remove compress4j dependency
	private void extractTarGz_old(InputStream tarGz, Path destination) throws IOException {
		Counter counter = new Counter(getProgress(), 98, karafTarEntriesCount);

		try (var extractor = TarGzArchiveExtractor.builder(tarGz).errorHandler((entry, exception) -> {
			publish(entry.name() + " konnte nicht entpackt werden");
			return RETRY;
		}).escapingSymlinkPolicy(DISALLOW).postProcessor((entry, path) -> {
			setProgress(counter.up());
			publish(entry.name());
		}).overwrite(true).build()) {
			extractor.extract(destination);
		}
	}

	private enum Installed {
		NOT, PARTIALLY, FULLY
	}

	private enum Choice {
		REUSE, REINSTALL
	}
}



