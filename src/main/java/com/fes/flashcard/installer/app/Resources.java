package com.fes.flashcard.installer.app;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import static java.lang.IO.println;
import static java.util.Objects.requireNonNull;

public interface Resources {

	String KARAF_ZIP_NAME = "apache-karaf-4.4.11.tar.gz";

	URL KARAF = getResource(KARAF_ZIP_NAME);

	// TODO more resources

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
