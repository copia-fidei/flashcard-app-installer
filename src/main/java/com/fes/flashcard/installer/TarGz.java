package com.fes.flashcard.installer;

import com.fes.flashcard.installer.app.Resources;
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
	static Set<String> getEntries(Path zipFile) throws IOException {
		return getEntries(Files.newInputStream(zipFile));
	}

	/**
	 * Gets all entries of the tar.gz file.
	 * The top level directory that all entries have is removed.
	 *
	 * @return the entries without the top level directory
	 */
	static Set<String> getEntriesWithoutTopLevelDirectory(Path tarGzFile) throws IOException {
		return getEntriesWithoutTopLevelDirectory(Files.newInputStream(tarGzFile));
	}

	static Set<String> getEntriesWithoutTopLevelDirectory(InputStream zipFile) throws IOException {
		var entries = new HashSet<String>();
		try (var tarGzArchive = newTarGzArchiveInputStream(zipFile)) {
			TarArchiveEntry entry;
			while ((entry = tarGzArchive.getNextEntry()) != null) {
				entries.add(getEntryWithoutTopLevelDirectory(entry));
			}
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to read tar.gz file", e);
			throw e;
		}
		return entries;
	}

	static Set<String> getEntries(InputStream input) throws IOException {
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

	private static String getEntryWithoutTopLevelDirectory(TarArchiveEntry entry) {
		String name  = entry.getName();
		int    slash = name.indexOf('/');
		if (slash >= 0) {
			name = name.substring(slash + 1);
		}
		return name;
	}

	static void main() throws IOException {
		getEntries(Resources.KARAF.openStream()).forEach(IO::println);
	}
}
