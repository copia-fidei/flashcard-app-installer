package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.TextBuilder;
import com.fes.flashcard.installer.app.AuthMethod;
import com.fes.flashcard.installer.app.Postgres;
import com.fes.flashcard.installer.app.pages.DatabasePageData;
import com.fes.flashcard.installer.app.pages.PostgresState;
import com.fes.flashcard.installer.operation.ErrorCode;
import com.fes.flashcard.installer.operation.Operation;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.function.Supplier;

import static com.fes.flashcard.installer.Temporary.use;
import static com.fes.flashcard.installer.app.Resources.POSTGRES_INIT_SQL;
import static com.fes.flashcard.installer.app.Resources.getTmpFile;
import static com.fes.flashcard.installer.app.pages.PostgresState.INSTALLED_BY_INSTALLER;
import static com.fes.flashcard.installer.app.pages.PostgresState.NOT_INSTALLED;
import static java.sql.DriverManager.getConnection;
import static java.text.MessageFormat.format;

public class ConfigurePostgresOp extends Operation {

	private final DatabasePageData databasePageData;

	private final String postgresVersion;
	private final String user;
	private final String userPassword;
	private final String adminPassword;
	private final String dbName;
	private final String port;

	private final Postgres postgres;

	public ConfigurePostgresOp(DatabasePageData databasePageData) {
		super("PostgreSQL Konfiguration", "Prüfe PostgreSQL Authentifizierungsmethode");

		this.databasePageData = databasePageData;

		postgresVersion = databasePageData.getSelectedPostgresVersion();
		user = databasePageData.getPostgresUser();
		userPassword = databasePageData.getPostgresUserPassword();
		adminPassword = databasePageData.getPostgresAdminPassword();
		dbName = databasePageData.getDbName();
		port = databasePageData.getPostgresPort();
		postgres = new Postgres(postgresVersion);
	}

	public String getDescription() {
		var text = new TextBuilder();
		text.line("Finde die Authentifizierungsmethode für den Nutzer postgres heraus");
		text.line("Prüfe ob Benutzer " + user + " sich verbinden kann");
		text.line("Ermögliche eine Verbindungsmöglichkeit für den Nutzer " + user);
		text.line("Setze PostgreSQL Port auf " + port);
		text.line("Starte PostgreSQL neu");
		text.line("Erstelle Benutzer " + user);
		text.line("Setze Passwort für " + user);
		text.line("Erstelle Datenbank " + dbName + " und Tabellen");
		text.line("Erteile " + user + " Eigentumsrechte an Datenbank " + dbName);
		text.line("Teste JDBC Verbindung");
		return text.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		if (getPostgresState() == NOT_INSTALLED) {
			throw new Exception("PostgreSQL " + postgresVersion + " ist nicht installiert");
		}
		progress(2);
		println("Suche eine Authentifizierungsmethode für postgres");
		progress(2);
		String authMethod = postgres.getAuthMethod(databasePageData.getPostgresAdmin(), "local", "postgres").orElseThrow(() ->
				// Unsupported authentification methods should have been prevented in database page (next button disabled).
				// There is a minimal risks if the user changes the database configuration while using the installer,
				// but this is considered his own fault.
				new Exception("pg_hba.conf enthält keine Authentifizierungsmethode für die Rolle postgres"));
		progress(2);
		Supplier<ProcessBuilder> loginCommand = setupLoginMethod(authMethod);
		progress(2);
		ensureUserCanConnect();
		progress(2);
		setPort();
		progress(2);
		restartPostgres();
		progress(2);
		createUser(loginCommand);
		progress(2);
		setUserPassword(loginCommand);
		progress(2);
		createDatabase(loginCommand);
		progress(2);
		grantUserOwnershipOfDatabase(loginCommand);
		progress(2);
		testUserConnection();
		progress(2);

		setProgress(100);

		return "Konfiguration abgeschlossen";
	}

	private PostgresState getPostgresState() {
		// needs to fetch the latest value
		return databasePageData.getPostgresInstallStates().get(postgresVersion);
	}

	private void setPort() throws IOException, InterruptedException, ErrorCode {
		println("Setze PostgreSQL Port auf " + port);
		progress(2);
		execute(new ProcessBuilder("sudo", "sed", "-Ei", "s/^([[:space:]]*port[[:space:]]*=?[[:space:]]*)[0-9]+/\\1" + port + "/", postgres.getConfigurationFile()).start()).throwIfNonZeroExit("Setzen des Ports auf " + port + " fehlgeschlagen");
	}


	private Supplier<ProcessBuilder> setupLoginMethod(String authMethod) throws IOException, InterruptedException, ErrorCode {
		Supplier<ProcessBuilder> processBuilder;
		if (authMethod.equals("peer")) {
			processBuilder = () -> new ProcessBuilder("sudo", "-u", "postgres", "psql");
			if (getPostgresState() == INSTALLED_BY_INSTALLER) {
				setAdminPassword();
			}
		} else if (AuthMethod.isPasswordBased(authMethod)) {
			processBuilder = () -> {
				var pb = new ProcessBuilder("psql", "-U", "postgres");
				pb.environment().put("PGPASSWORD", adminPassword);
				return pb;
			};
		} else {
			// we do not expect to land here, except if the user modifies the database while using the installer
			throw new IllegalStateException("The postgres user cannot login. Was the the database modified during installation?");
		}
		println("Authentifizierungsmethode eingerichtet: " + authMethod);
		progress(2);
		return processBuilder;
	}

	private void setAdminPassword() throws IOException, InterruptedException, ErrorCode {
		if (adminPassword.isBlank()) {
			return;
		}
		println("Setze das Passwort für postgres");
		progress(2);
		var pb = new ProcessBuilder("sudo", "-u", "postgres", "psql");
		pb.command().add("-c");
		pb.command().add("ALTER USER postgres WITH PASSWORD '" + adminPassword + "';");
		var process = pb.start();
		execute(process).throwIfNonZeroExit("Setzen des Passworts fehlgeschlagen");
		println("Admin Passwort erfolgreich gesetzt");
		progress(2);
	}

	private void ensureUserCanConnect() throws Exception {
		if (userCanConnect()) {
			println("Die Rolle " + user + " kann sich bereits verbinden");
			progress(2);
			return;
		}
		progress(2);
		addToPgHbaConf();
	}

	private boolean userCanConnect() throws IOException, InterruptedException {
		return postgres.getAuthMethod(user, "host", dbName).filter(AuthMethod::isSupported).isPresent();
	}

	private void addToPgHbaConf() throws IOException, InterruptedException {
		println("Ermögliche eine Verbindungsmöglichkeit für die Rolle " + user);
		progress(2);

		String entries = MessageFormat.format("""
				host    {0}     {1}      127.0.0.1/32            scram-sha-256
				host    {0}     {1}      ::1/128                 scram-sha-256
				""", dbName, user);

		String authFile = postgres.getAuthFile();
		println("Füge " + entries + " in " + authFile + " ein");
		progress(2);

		var process = new ProcessBuilder("sudo", "tee", "--append", postgres.getAuthFile()).start();
		try (var writer = process.outputWriter()) {
			writer.write(entries);
		}
		process.waitFor();
		println("Einträge erfolgreich zu pg_hba.conf hinzugefügt");
		progress(2);
	}

	// the only command that takes time
	private void restartPostgres() throws IOException, InterruptedException, ErrorCode {
		println("Starte PostgreSQL neu");
		progress(2);
		execute(new ProcessBuilder("sudo", "systemctl", "restart", "postgresql.service").start()).throwIfNonZeroExit("PostgreSQL Neustart fehlgeschlagen");
		println("PostgreSQL neu gestartet");
		setProgress(40);
	}

	private void createUser(Supplier<ProcessBuilder> loginCommand) throws Exception {
		println("Prüfe ob Rolle " + user + " existiert");
		progress(2);

		var checkUsersExists = loginCommand.get();
		checkUsersExists.command().add("-tA");
		checkUsersExists.command().add("-c");
		checkUsersExists.command().add("SELECT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '" + user + "');");

		var checkResult = execute(checkUsersExists.start());
		checkResult.throwIfNonZeroExit("Prüfen ob Rolle " + user + " existiert fehlgeschlagen");
		if ("t".equals(checkResult.output().trim())) {
			println("Rolle " + user + " existiert bereits");
			progress(2);
			return;
		}
		println("Erstelle Rolle " + user);
		progress(2);

		var createUser = loginCommand.get();
		createUser.command().add("-c");
		createUser.command().add("CREATE USER " + user + ";");

		execute(createUser.start()).throwIfNonZeroExit("Rolle erstellen fehlgeschlagen");

		println("Rolle " + user + " erfolgreich erstellt");
		progress(2);
	}

	private void setUserPassword(Supplier<ProcessBuilder> loginCommand) throws Exception {
		if (userPassword.isBlank()) {
			println("Kein Passwort für " + user + " gesetzt");
			progress(2);
			return;
		}
		println("Setze Passwort für " + user);
		progress(2);
		var pb = loginCommand.get();
		pb.command().add("-c");
		pb.command().add("ALTER USER " + user + " WITH PASSWORD '" + userPassword + "';");
		execute(pb.start()).throwIfNonZeroExit("Passwort setzen fehlgeschlagen");

		println("Passwort für " + user + " erfolgreich gesetzt");
		progress(2);
	}

	private void createDatabase(Supplier<ProcessBuilder> loginCommand) throws Exception {
		println("Erstelle Datenbank und Tabellen");
		progress(2);

		use(getTmpFile(POSTGRES_INIT_SQL), script -> {
			println("Führe aus:");
			println(Files.readString(script));
			var pb = loginCommand.get();
			pb.command().add("-f");
			pb.command().add(script.toString());
			execute(pb.start()).throwIfNonZeroExit("Datenbankerstellung fehlgeschlagen");

			println("Datenbank und Tabellen erfolgreich erstellt");
			progress(2);
		});
	}

	private void grantUserOwnershipOfDatabase(Supplier<ProcessBuilder> loginCommand) throws Exception {
		println("Erteile " + user + " Eigentumsrechte an Datenbank " + dbName);
		progress(2);

		String sql = format("""
				GRANT ALL PRIVILEGES ON DATABASE {0} TO {1};
				GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO {1};
				""", dbName, user);

		println("Führe aus:");
		println(sql);

		var pb = loginCommand.get();
		pb.command().add("-d");
		pb.command().add(dbName);
		pb.command().add("-c");
		pb.command().add(sql);
		execute(pb.start()).throwIfNonZeroExit("Rechte vergeben fehlgeschlagen");

		println("Eigentumsrechte erfolgreich vergeben");
		progress(2);
	}

	private void testUserConnection() throws SQLException {
		String host = databasePageData.getPostgresHost();
		println("Teste JDBC Verbindung an " + user);
		progress(2);

		String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
		try (Connection _ = getConnection(url, user, userPassword)) {
			println("JDBC Verbindung an " + user + " erfolgreich");
			progress(2);
		}
	}
}
