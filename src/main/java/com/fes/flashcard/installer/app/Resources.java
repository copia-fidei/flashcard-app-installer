package com.fes.flashcard.installer.app;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import static java.lang.IO.println;
import static java.util.Objects.requireNonNull;

public interface Resources {

	String KARAF_ZIP_NAME = "apache-karaf-4.4.11.tar.gz";

	String FLASHCARDS_APP_BUNDLE_NAME = "flashcards.jar";

	String PURGE_AND_INSTALL_POSTGRES_SCRIPT_NAME = "purge_and_install_postgres.sh";

	String APT_PURGE_POSTGRES_EXP_NAME = "apt_purge_postgres.exp";

	URL KARAF = getResource(KARAF_ZIP_NAME);

	URL FLASHCARDS_APP_BUNDLE = getResource(FLASHCARDS_APP_BUNDLE_NAME);

	URL PURGE_AND_INSTALL_POSTGRES_SCRIPT = getResource(PURGE_AND_INSTALL_POSTGRES_SCRIPT_NAME);

	URL APT_PURGE_POSTGRES_EXP = getResource(APT_PURGE_POSTGRES_EXP_NAME);

	static void main() {
		println(KARAF);
	}

	// this should throw exceptions on startup
	private static Path pathOf(String resourceName) {
		try {
			URL resource = Resources.class.getResource(resourceName);

			requireNonNull(resource);
			return Path.of(resource.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private static URL getResource(String name) {
		URL resource = Resources.class.getResource("/" + name);
		requireNonNull(resource);
		return resource;
	}

}
