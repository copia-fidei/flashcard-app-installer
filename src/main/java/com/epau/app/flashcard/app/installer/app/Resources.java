package com.epau.app.flashcard.app.installer.app;

import com.epau.installer.utilities.TarGzFile;
import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.IO.println;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.setPosixFilePermissions;
import static java.nio.file.attribute.PosixFilePermissions.fromString;
import static java.util.Objects.requireNonNull;

/// all files in /resources
@NonNls
public interface Resources {

	String KARAF_ZIP_NAME             = "apache-karaf-4.4.11.tar.gz";
	String FLASHCARDS_APP_BUNDLE_NAME = "flashcards.jar";
	String POSTGRES_INIT_SQL_NAME     = "postgres-init.sql";
	String H2_INIT_SQL_NAME           = "h2-init.sql";

	URL    KARAF                      = getResource(KARAF_ZIP_NAME);
	URL    FLASHCARDS_APP_BUNDLE      = getResource(FLASHCARDS_APP_BUNDLE_NAME);
	URL    POSTGRES_INIT_SQL          = getResource(POSTGRES_INIT_SQL_NAME);
	URL    H2_INIT_SQL                = getResource(H2_INIT_SQL_NAME);

	private static URL getResource(String name) {
		return requireNonNull(Resources.class.getResource("/" + name));
	}

	// TODO decide for a tmp folder
	static Path getTmpFile(URL resource) throws IOException {
		String path   = resource.getPath();
		String name   = path.substring(path.lastIndexOf('/') + 1);
		String prefix = name;
		String suffix = null;
		if (name.contains(".")) {
			int extIdx = name.lastIndexOf('.');
			prefix = name.substring(0, extIdx);
			suffix = name.substring(extIdx);
		}
		Path tmp = createTempFile(prefix, suffix);
		try (var is = resource.openStream()) {
			Files.write(tmp, is.readAllBytes());
		}
		// Make file readable by others
		try {
			setPosixFilePermissions(tmp, fromString("rw-r--r--")); //$NON-NLS
		} catch (UnsupportedOperationException e) {
			// On non-POSIX systems (e.g., Windows), skip permission setting
		}
		return tmp;
	}

	// For testing
	static void main() throws IOException {
		println(POSTGRES_INIT_SQL);
		Path tempFile = getTmpFile(POSTGRES_INIT_SQL);
		println(tempFile);
		Files.deleteIfExists(tempFile);
		new TarGzFile(Resources.KARAF.openStream()).getEntries().forEach(IO::println);
	}
}
