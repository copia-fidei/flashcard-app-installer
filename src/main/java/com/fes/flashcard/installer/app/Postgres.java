package com.fes.flashcard.installer.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.nio.file.Files.exists;
import static java.util.concurrent.TimeUnit.SECONDS;

// TODO localize exception messages
public interface Postgres {

	static String getAuthFile(String postgresVersion) throws FileNotFoundException {
		var authFile = Path.of("/etc/postgresql", postgresVersion, "main", "pg_hba.conf");
		if (!exists(authFile)) {
			throw new FileNotFoundException("pg_hba.conf not found at " + authFile);
		}
		return authFile.toString();
	}

	static String getConfigurationFile(String postgresVersion) throws FileNotFoundException {
		var confPath = Path.of("/etc/postgresql", postgresVersion, "main", "postgresql.conf");
		if (!Files.exists(confPath)) {
			throw new FileNotFoundException("postgresql.conf not found at " + confPath);
		}
		return confPath.toString();
	}

	static Optional<String> getAuthMethod(String postgresVersion, String user, String connectionType, String database) throws IOException, InterruptedException {
		var process = new ProcessBuilder("sudo", "grep", "-E", "all|" + user, getAuthFile(postgresVersion)).start();
		var output = new StringWriter();
		try (var stdout = process.inputReader()) {
			stdout.transferTo(output);
		}
		process.waitFor(2, SECONDS);
		List<String> lines = output.toString().lines()
			.filter(line -> !line.isBlank())
			.filter(line -> !line.trim().startsWith("#"))
			.filter(line -> !line.contains("replication"))
			.toList();

		for (String line : lines) {
			String[] parts = line.split("\\s+");
			if (parts.length >= 4) {
				String foundConnectionType = parts[0];
				String foundDatabase       = parts[1];
				String foundUser           = parts[2];
				String foundAuthMethod     = parts[parts.length - 1];

				if (connectionType.equals(foundConnectionType)
				&& (foundUser.equals(user) || foundUser.equals("all"))
				&& (foundDatabase.equals(database) || foundDatabase.equals("all"))) {
					return Optional.of(foundAuthMethod);
				}
			}
		}
		return Optional.empty();
	}


}
