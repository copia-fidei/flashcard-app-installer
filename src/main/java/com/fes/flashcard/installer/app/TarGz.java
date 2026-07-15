package com.fes.flashcard.installer.app;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

public interface TarGz {

	Logger LOG = getLogger(TarGz.class.getName());

	/**
	 * @return the entries of the tar.gz file
	 */
	static Set<String> entriesOf(Path zipFile) throws IOException {
		return entriesOf(Files.newInputStream(zipFile));
	}

	/**
	 * Gets all entries of the tar.gz file.
	 * The top level directory that all entries have is removed.
	 *
	 * @return the entries without the top level directory
	 */
	static Set<String> entriesWithoutTopLevelDirectory(Path tarGzFile) throws IOException {
		return entriesWithoutTopLevelDirectory(Files.newInputStream(tarGzFile));
	}

	static Set<String> entriesWithoutTopLevelDirectory(InputStream zipFile) throws IOException {
		var entries = new HashSet<String>();
		try (var tarGzArchive = newTarGzArchiveInputStream(zipFile)) {
			TarArchiveEntry entry;
			while ((entry = tarGzArchive.getNextEntry()) != null) {
				entries.add(entryWithoutTopLevelDirectory(entry));
			}
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to read tar.gz file", e);
			throw e;
		}
		return entries;
	}

	static Set<String> entriesOf(InputStream input) throws IOException {
		var entries = new HashSet<String>();
		try (var tarGzArchive = newTarGzArchiveInputStream(input)) {
			TarArchiveEntry entry;
			while ((entry = tarGzArchive.getNextEntry()) != null) {
				entries.add(entry.getName());
			}
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to read tar.gz file", e);
			throw e;
		}
		return entries;
	}

	private static TarArchiveInputStream newTarGzArchiveInputStream(InputStream tarGzFile) throws IOException {
		return new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(tarGzFile)));
	}

	private static String entryWithoutTopLevelDirectory(TarArchiveEntry entry) {
		String name  = entry.getName();
		int    slash = name.indexOf('/');
		if (slash >= 0) {
			name = name.substring(slash + 1);
		}
		return name;
	}

	static void main() throws IOException {
		entriesOf(Resources.KARAF.openStream()).forEach(IO::println);
	}
}
