package com.epau.app.flashcard.app.installer.pages;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import static com.epau.app.flashcard.app.installer.Runner.getOutputOf;
import static com.epau.app.flashcard.app.installer.pages.PostgresState.ALREADY_INSTALLED;
import static com.epau.app.flashcard.app.installer.pages.PostgresState.NOT_INSTALLED;
import static java.lang.IO.println;
import static java.util.Arrays.stream;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

interface PostgresVersions {

	static String[] filterAvailable(String... candidates) {
		return stream(candidates).filter(PostgresVersions::isAvailable).toArray(String[]::new);
	}

	private static boolean isAvailable(String version) {
		try {
			// If the package does not exist the output will have only one line
			return getOutputOf("apt-cache", "policy", "postgresql-" + version).lines().count() > 1;
		} catch (IOException | InterruptedException e) {
			log.log(SEVERE, "Could not determine availability of PostgreSQL " + version, e); //$NON-NLS
			return false;
		}
	}

	/// Finds out the installation states of the provided versions and puts them into the given map.
	static void findOutInstallStates(Map<String, PostgresState> states, String... versions) {
		for (String version : versions) {
			try {
				states.put(version, getInstallState(version));
			} catch (IOException | InterruptedException e) {
				log.log(WARNING, "Failed to find out if Postgres packages are installed", e); //$NON-NLS
			}
		}
	}

	private static PostgresState getInstallState(String version) throws InterruptedException, IOException {
		String status = getOutputOf("dpkg-query", "-f=${db:Status-Abbrev}", "-W", "postgresql" + "-" + version); //$NON-NLS

		if (status.startsWith("ii")) { //$NON-NLS
			return ALREADY_INSTALLED;
		} else {
			return NOT_INSTALLED;
		}
	}

	// For testing
	static void main() throws IOException, InterruptedException {
		String[] candidates = {"13", "14", "15", "16", "17", "18"};
		for (String candidate : candidates) {
			println(candidate + ": " + getInstallState(candidate));
		}
		println("Available:"); //NON-NLS
		for (String available : filterAvailable(candidates)) {
			println(available);
		}
	}

	Logger log = getLogger(PostgresVersions.class.getName());
}
