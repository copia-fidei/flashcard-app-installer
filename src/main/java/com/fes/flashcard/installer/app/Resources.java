package com.fes.flashcard.installer.app;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.IO.println;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.setPosixFilePermissions;
import static java.nio.file.attribute.PosixFilePermissions.fromString;
import static java.util.Objects.requireNonNull;

public interface Resources {

	String KARAF_ZIP_NAME = "apache-karaf-4.4.11.tar.gz";

	String FLASHCARDS_APP_BUNDLE_NAME = "flashcards.jar";

	String POSTGRES_INIT_SQL_NAME = "postgres-init.sql";

	URL KARAF = getResource(KARAF_ZIP_NAME);

	URL FLASHCARDS_APP_BUNDLE = getResource(FLASHCARDS_APP_BUNDLE_NAME);

	URL POSTGRES_INIT_SQL = getResource(POSTGRES_INIT_SQL_NAME);

	static void main() throws IOException {
		println(POSTGRES_INIT_SQL);
		Path tempFile = copyToTmp(POSTGRES_INIT_SQL);
		println(tempFile);
		Files.deleteIfExists(tempFile);
	}

	private static URL getResource(String name) {
		URL resource = Resources.class.getResource("/" + name);
		requireNonNull(resource);
		return resource;
	}

	// TODO decide for a tmp folder
	static Path copyToTmp(URL resource) throws IOException {
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
			setPosixFilePermissions(tmp, fromString("rw-r--r--"));
		} catch (UnsupportedOperationException e) {
			// On non-POSIX systems (e.g., Windows), skip permission setting
		}
		return tmp;
	}
}
