package com.epau.app.flashcard.app.installer;

import org.jetbrains.annotations.NonNls;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.nio.file.Files.exists;
import static java.util.concurrent.TimeUnit.SECONDS;

/// Represents a PostgreSQL installation.
@NonNls
public class Postgres {

	private final String version;

	public Postgres(String version) {
		this.version = version;
	}

	public String getAuthFile() throws FileNotFoundException {
		return find("/etc", "postgresql", version, "main", "pg_hba.conf");
	}

	public String getConfigurationFile() throws FileNotFoundException {
		return find("/etc", "postgresql", version, "main", "postgresql.conf");
	}

	@SuppressWarnings("SameParameterValue")
	private static String find(String first, String... more) throws FileNotFoundException {
		Path path = Path.of(first, more);
		if (!exists(path)) {
			throw new FileNotFoundException("Datei nicht gefunden: " + path);
		}
		return path.toString();
	}

	public Optional<String> getAuthMethod(String user, String connectionType, String database) throws IOException, InterruptedException {
		var process = new ProcessBuilder("sudo", "grep", "-E", "all|" + user, getAuthFile()).start(); //NON-NLS
		var output = new StringWriter();
		try (var stdout = process.inputReader()) {
			stdout.transferTo(output);
		}
		process.waitFor(2, SECONDS);
		List<String> lines = output.toString().lines()
			.filter(line -> !line.isBlank())
			.filter(line -> !line.trim().startsWith("#"))
			.filter(line -> !line.contains("replication"))  //NON-NLS
			.toList();

		for (String line : lines) {
			String[] parts = line.split("\\s+"); //NON-NLS
			if (parts.length >= 4) {
				String foundConnectionType = parts[0];
				String foundDatabase       = parts[1];
				String foundUser           = parts[2];
				String foundAuthMethod     = parts[parts.length - 1];

				if (connectionType.equals(foundConnectionType)
				&& (foundUser.equals(user) || foundUser.equals("all")) //NON-NLS
				&& (foundDatabase.equals(database) || foundDatabase.equals("all"))) { //NON-NLS
					return Optional.of(foundAuthMethod);
				}
			}
		}
		return Optional.empty();
	}


}
