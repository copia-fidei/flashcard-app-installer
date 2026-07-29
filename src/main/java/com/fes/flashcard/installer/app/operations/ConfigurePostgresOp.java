package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.app.pages.DatabasePageData;
import com.fes.flashcard.installer.app.pages.DatabasePageData.PostgresState;
import com.fes.flashcard.installer.operation.Operation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

// TODO decide how to act, based on if Postgres was installed by the installer or not
public class ConfigurePostgresOp extends Operation {

	private final DatabasePageData databasePageData;

	public ConfigurePostgresOp(DatabasePageData databasePageData) {
		super("pg_hba.conf", "Prüfe PostgreSQL Authentifizierungsmethode");

		this.databasePageData = databasePageData;
	}

	// TODO do we need to throw exceptions?
	@Override
	protected String doInBackground() throws Exception {
		String        postgresVersion = databasePageData.getSelectedPostgresVersion();
		PostgresState postgresState   = databasePageData.getPostgresInstallStates().get(postgresVersion);
		if (postgresState == PostgresState.NOT_INSTALLED) {
			throw new Exception("PostgreSQL " + postgresVersion + " ist nicht installiert");
		}

		progress();
		publishLn("Suche pg_hba.conf Datei");
		progress();
		Path authFile = getAuthFilePath();
		publishLn("Gefunden: " + authFile);
		progress();
		publishLn("Lese pg_hba.conf mit sudo");
		progress();
		String output = redirectOutputs(new ProcessBuilder("sudo", "grep", "-E", "'all|postgres'", authFile.toString()).start()).output();
		if (output.isBlank()) {
			throw new IOException("sudo cat command produced no output"); // TODO localize
		}
		progress();
		publishLn("Suche Authentifizierungsmethode für postgres Benutzer");
		progress();

		// TODO this should already be found at the database page, the user should be informed, the next button should be disabled
		// TODO the user should be informed to change the authMethod to password, md5 or scram-sha-256
		// TODO if password is empty peer is assumed, which should be in pg_hba.conf
		AuthMethod authMethod = getPostgresAuthMethod(output.lines().toList()).orElseThrow(() -> new Exception("Keine Authentifizierungsmethode für postgres Benutzer gefunden"));
		if (authMethod.string.equals("reject")) {
			throw new Exception("Authentifizierungsmethode ist 'reject' - Zugriff verweigert");
		}
		if (!authMethod.isSupported()) {
			throw new Exception("Authentifizierungsmethode " + getPostgresAuthMethod(output.lines().toList()) + " ist nicht unterstützt");
		}
		publishLn("Authentifizierungsmethode: " + authMethod);
		progress();
		publishLn("Verbinde zu PostgreSQL mit sudo -u postgres psql");
		progress();

		if (postgresState == PostgresState.ALREADY_INSTALLED) {
			// TODO do nothing?
		}
		else if(postgresState == PostgresState.INSTALLED_BY_INSTALLER) {
			// peer is active
			createAdminPassword();

			// TODO necessary? why not keep it at peer?
			modifyAuthFile();
		}

		// TODO already created?


		// TODO create user flashcards
		// TODO set password
		// TODO create database collections
		// TODO flashcards rights to database


		connectToPostgres(authMethod);
		setProgress(100);

		return "Konfiguration abgeschlossen";
	}

	private void modifyAuthFile() {
		// we change from peer to password
		// TODO really necessary?


	}

	private void createDatabase() throws IOException, InterruptedException {
		publishLn("Erstelle Datenbank und Tabellen aus postgresql-init.sql");
		progress();

		var sqlScript = getClass().getResourceAsStream("/postgres-init.sql");
		if (sqlScript == null) {
			throw new IOException("postgres-init.sql nicht gefunden");
		}

		String sqlContent = new String(sqlScript.readAllBytes());
		Path tempFile = Files.createTempFile("postgres-init", ".sql");
		Files.writeString(tempFile, sqlContent);

		try {
			var pb = new ProcessBuilder("sudo", "-u", "postgres", "psql", "-f", tempFile.toString());
			var process = pb.start();

			int exitCode = redirectOutputs(process).exitCode();
			if (exitCode != 0) {
				throw new IOException("Datenbankerstellung fehlgeschlagen mit Exit-Code " + exitCode);
			}

			publishLn("Datenbank und Tabellen erfolgreich erstellt");
			progress();
		} finally {
			Files.deleteIfExists(tempFile);
		}
	}

	private void createUser() {

	}

	private void setUserPassword() {

	}




	// TODO is a password change necessary?
	// sudo -u postgres psql -c 'ALTER USER postgres WITH PASSWORD ***;'
	private void createAdminPassword() throws IOException, InterruptedException {


		String password = databasePageData.getPostgresAdminPassword();
		if (password.isBlank()) {
			// Peer connection will be used
			return;
		}
		publishLn("Setze Admin Passwort für PostgreSQL");
		progress();
		var process = new ProcessBuilder("sudo", "-u", "postgres", "psql", "-c", "'ALTER USER postgres WITH PASSWORD " + password + ";'").start();
		int exitCode = redirectOutputs(process).exitCode();
		if (exitCode != 0) {
			throw new IOException("Admin Passwort setzen fehlgeschlagen mit Exit-Code " + exitCode);
		}
		publishLn("Admin Passwort erfolgreich gesetzt");
		progress();
	}

	private Path getAuthFilePath() throws IOException {
		String version   = databasePageData.getSelectedPostgresVersion();
		Path   pgHbaPath = Path.of("/etc/postgresql", version, "main", "pg_hba.conf");
		if (!Files.exists(pgHbaPath)) {
			throw new IOException("pg_hba.conf nicht gefunden");
		}
		return pgHbaPath;
	}


	private Optional<AuthMethod> getPostgresAuthMethod(List<String> lines) {
		for (String line : lines) {
			String trimmed = line.trim();
			if (trimmed.isEmpty() || trimmed.startsWith("#")) {
				continue;
			}
			String[] parts = trimmed.split("\\s+");
			if (parts.length >= 4) {
				String connectionType = parts[0];
				String database       = parts[1];
				String user           = parts[2];
				String method         = parts[parts.length - 1];
				if (user.equals("postgres") || user.equals("all")) {
					if (database.equals("postgres") || database.equals("all")) {
						return Optional.of(new AuthMethod(method));
					}
				}
			}
		}
		return Optional.empty();
	}


	/// TODO maybe ask user if authMethod should be changed
	private void connectToPostgres(AuthMethod authMethod) throws IOException, InterruptedException {
		var pb = new ProcessBuilder("sudo", "--preserve-env=PGPASSWORD", "-u", "postgres", "psql");
		if (authMethod.isPasswordBased()) {
			String password = databasePageData.getPostgresAdminPassword();

			if (password.isBlank()) {
				setAdminPassword();
			}

			// TODO password can be empty, does a password exist?
			// TODO find out, can you log in if md5 is on, but password is not set?
			if (password.isEmpty()) {
				throw new IOException("PostgreSQL Passwort erforderlich für " + authMethod + " Authentifizierung");
			}

			pb.environment().put("PGPASSWORD", password);
		}
		var process  = pb.start();
		int exitCode = redirectOutputs(process).exitCode();
		if (exitCode != 0) {
			throw new IOException("psql Verbindung fehlgeschlagen mit Exit-Code " + exitCode);
		}

		publishLn("Erfolgreich mit PostgreSQL verbunden");
	}


	/// Admin password has not been set yet, it needs to be set
	private void setAdminPassword() {
		databasePageData.getPostgresInstallStates().get(databasePageData.getSelectedPostgresVersion());


	}

	private record AuthMethod(String string) {

		boolean isPasswordBased() {
			return string.equals("md5") || string.equals("scram-sha-256") || string.equals("password");
		}

		boolean isSupported() {
			return isPasswordBased() || isPeer();
		}

		// unnecessary
		boolean isPeer() {
			return string.equals("peer");
		}

		@Override
		public String toString() {
			return string;
		}
	}
}
