package com.fes.flashcard.installer.app.pages;

import com.fes.flashcard.installer.PageDataPool;
import com.fes.flashcard.installer.app.AuthMethod;
import com.fes.flashcard.installer.app.Database;
import com.fes.flashcard.installer.app.Postgres;
import com.fes.flashcard.installer.page.PageData;
import com.fes.flashcard.installer.validation.Severity;
import com.fes.flashcard.installer.validation.ValidationResult;
import com.fes.flashcard.installer.validation.ValidationResults;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.stream;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;

// TODO show only postgres versions that can be installed on the current Ubuntu
public class DatabasePageData extends PageData {


	// Preferences keys
	private static final String KEY_TYPE                      = "db.type";
	private static final String KEY_SELECTED_POSTGRES_VERSION = "db.postgres.selected.version";
	private static final String KEY_VERSION_H2                = "db.h2.selected.version";
	private static final String KEY_POSTGRES_ADMIN            = "db.postgres.admin";
	private static final String KEY_POSTGRES_ADMIN_PASSWORD   = "db.postgres.admin.password";
	private static final String KEY_POSTGRES_PORT             = "db.postgres.port";
	private static final String KEY_POSTGRES_HOST             = "db.postgres.host";
	private static final String KEY_POSTGRES_USER             = "db.postgres.user";
	private static final String KEY_POSTGRES_USER_PASSWORD    = "db.postgres.user.password";
	private static final String KEY_H2_ADMIN                  = "db.h2.admin";
	private static final String KEY_H2_ADMIN_PASSWORD         = "db.h2.admin.password";

	// default values
	private static final String DEFAULT_SELECTED_POSTGRES_VERSION = "14";
	private static final String DEFAULT_POSTGRES_ADMIN            = "postgres";
	private static final String DEFAULT_POSTGRES_ADMIN_PASSWORD   = "";
	private static final String DEFAULT_POSTGRES_PORT             = "5432";
	private static final String DEFAULT_POSTGRES_USER_PASSWORD    = "";
	private static final String DEFAULT_SELECTED_H2_VERSION       = "2.4";

	// version options
	private static final String[] POSTGRES_VERSIONS_ALL       = {"10", "11", "12", "13", "14", "15", "16", "17", "18"};
	private static final String[] POSTGRES_VERSIONS_AVAILABLE = getAvailablePostgresVersions();
	private static final String[] H2_VERSIONS                 = {"2.2.224"};

	// values
	private       Database database                = Database.PostgreSQL;
	private       String   selectedPostgresVersion = DEFAULT_SELECTED_POSTGRES_VERSION;
	private       String   selectedH2Version       = DEFAULT_SELECTED_H2_VERSION;
	private       String   postgresAdmin           = DEFAULT_POSTGRES_ADMIN;
	private       String   postgresAdminPassword   = DEFAULT_POSTGRES_ADMIN_PASSWORD;
	private       String   postgresPort            = DEFAULT_POSTGRES_PORT;
	private final String   postgresHost            = "localhost";
	private final String   postgresUser            = "flashcards";
	private       String   postgresUserPassword    = DEFAULT_POSTGRES_USER_PASSWORD;
	private final String   h2User                  = "sa";
	private final String   dbName                  = "collections";

	private final String dataSourceName = "collections";


	public DatabasePageData(PageDataPool pageDataPool) {
		super(pageDataPool);
	}

	private static String[] getAvailablePostgresVersions() {
		return stream(POSTGRES_VERSIONS_ALL).filter(DatabasePageData::isPostgresVersionAvailable)
		                                    .toArray(String[]::new);
	}

	private static boolean isPostgresVersionAvailable(String version) {
		try {
			Process process = new ProcessBuilder("apt-cache", "policy", "postgresql-" + version).start();
			String  output;
			try (var reader = process.inputReader()) {
				output = reader.readAllAsString();
			}
			process.waitFor();
			return output.lines().count() > 1; // If the package does not exist the output will have only one line
		} catch (IOException | InterruptedException e) {
			getLogger(DatabasePageData.class.getName()).log(SEVERE, "Could not determine availability of PostgreSQL " + version, e);
			return false;
		}
	}

	@Override
	public void load() {
		database = Database.valueOf(preferences.get(KEY_TYPE, Database.PostgreSQL.name()));
		selectedPostgresVersion = preferences.get(KEY_SELECTED_POSTGRES_VERSION, DEFAULT_SELECTED_POSTGRES_VERSION);
		selectedH2Version = preferences.get(KEY_VERSION_H2, DEFAULT_SELECTED_H2_VERSION);
		postgresAdmin = preferences.get(KEY_POSTGRES_ADMIN, DEFAULT_POSTGRES_ADMIN);
		postgresAdminPassword = preferences.get(KEY_POSTGRES_ADMIN_PASSWORD, DEFAULT_POSTGRES_ADMIN_PASSWORD);
		postgresPort = preferences.get(KEY_POSTGRES_PORT, DEFAULT_POSTGRES_PORT);
		postgresUserPassword = preferences.get(KEY_POSTGRES_USER_PASSWORD, DEFAULT_POSTGRES_USER_PASSWORD);
	}


	@Override
	public void save() {
		preferences.put(KEY_TYPE, database.name());
		preferences.put(KEY_SELECTED_POSTGRES_VERSION, selectedPostgresVersion);
		preferences.put(KEY_VERSION_H2, selectedH2Version);
		preferences.put(KEY_POSTGRES_ADMIN, postgresAdmin);
		preferences.put(KEY_POSTGRES_ADMIN_PASSWORD, postgresAdminPassword);
		preferences.put(KEY_POSTGRES_PORT, postgresPort);
		preferences.put(KEY_POSTGRES_HOST, postgresHost);
		preferences.put(KEY_POSTGRES_USER, postgresUser);
		preferences.put(KEY_POSTGRES_USER_PASSWORD, postgresUserPassword);
		preferences.put(KEY_H2_ADMIN, h2User);
	}

	@Override
	public void loadDefaults() {
		database = Database.PostgreSQL;
		selectedPostgresVersion = DEFAULT_SELECTED_POSTGRES_VERSION;
		selectedH2Version = DEFAULT_SELECTED_H2_VERSION;
		postgresAdmin = DEFAULT_POSTGRES_ADMIN;
		postgresAdminPassword = DEFAULT_POSTGRES_ADMIN_PASSWORD;
		postgresPort = DEFAULT_POSTGRES_PORT;
		postgresUserPassword = DEFAULT_POSTGRES_USER_PASSWORD;

		preferences.remove(KEY_TYPE);
		preferences.remove(KEY_SELECTED_POSTGRES_VERSION);
		preferences.remove(KEY_VERSION_H2);
		preferences.remove(KEY_POSTGRES_ADMIN);
		preferences.remove(KEY_POSTGRES_ADMIN_PASSWORD);
		preferences.remove(KEY_POSTGRES_PORT);
		preferences.remove(KEY_POSTGRES_HOST);
		preferences.remove(KEY_POSTGRES_USER);
		preferences.remove(KEY_POSTGRES_USER_PASSWORD);
		preferences.remove(KEY_H2_ADMIN);
		preferences.remove(KEY_H2_ADMIN_PASSWORD);
	}

	@Override
	public ValidationResults validate() {
		var results = new ValidationResults();

		if (database == Database.PostgreSQL) {
			if (postgresPort.isBlank()) {
				results.addError("Leerer Port", "Port darf nicht leer sein", 1);
			} else {
				try {
					int portNum = Integer.parseInt(postgresPort);
					if (portNum < 1 || portNum > 65535) {
						results.addError("Ungültiger Port", "Port muss zwischen 1 und 65535 liegen", 0);
					}
				} catch (NumberFormatException e) {
					results.addError("Ungültiger Port", "Port muss eine gültige Zahl sein", 0);
				}
			}
			if (isPostgresInstalled()) {
				validateAdminPassword(results);
				validateUserPassword(results);
			}
		}

		return results;
	}

	private boolean isPostgresInstalled() {
		return postgresInstallStates.get(selectedPostgresVersion) == PostgresState.ALREADY_INSTALLED;
	}

	private Postgres postgres;

	private void validateAdminPassword(ValidationResults validationResults) {
		postgres = new Postgres(selectedPostgresVersion);

		try {
			Optional<String> adminAuthMethodOpt = postgres.getAuthMethod(postgresAdmin, "local", "postgres");
			if (adminAuthMethodOpt.isEmpty()) {
				log.severe("Keine Authentifizierungsmöglichkeit gefunden für user=" + postgresAdmin + " connectionType=local" + " database=postgres in PostgreSQL Datenbank Version " + selectedPostgresVersion);
				return;
			}
			String adminAuthMethod = adminAuthMethodOpt.get();
			if (adminAuthMethod.equals("reject")) {
				log.log(Level.INFO, "Authentifizierungsmethode ist 'reject' - Zugriff verweigert");
			}
			if (!AuthMethod.isSupportedForAdmin(adminAuthMethod)) {
				validationResults.addError("Administrator kann sich nicht anmelden", "Der Installer unterstützt ausschließlich die Authentifizierungsmethoden: peer, trust, scram-sha-256 und md5 für die Administratorrolle postgres. Die Authentifizierungsdatei pg_hba.conf muss manuell angepasst werden.", 1);
				return;
			}
			if (adminAuthMethod.equals("peer")) {
				validationResults.add("Kein Administratorpasswort erforderlich.", "Die Authentifizierungsmethode peer ist aktiv, welche kein Passwort erfordert. Die Korrektheit des Administratorpassworts kann daher nicht ermittelt werden. Das Administratorpasswort bleibt jedoch unverändert.", Severity.INFO);
				return;
			}
			if (AuthMethod.isPasswordBased(adminAuthMethod)) {
				if (!canConnectToPostgresAdminByPassword()) {
					validationResults.add("Falsches Administratorpasswort", "Das Administratorpasswort ist falsch.", Severity.ERROR);
				}
			}
		} catch (IOException | InterruptedException e) {
			log.log(SEVERE, "Fehler beim Validieren des Administratorpassworts", e);
		}
	}

	private void validateUserPassword(ValidationResults validationResults) {
		if (postgresUserPassword.isBlank()) {
			validationResults.add("Leeres Benutzerpasswort", "Ein Benutzerpasswort ist erforderlich.", Severity.ERROR);
		}
		try {
			Optional<String> userAuthMethodOpt = postgres.getAuthMethod(postgresUser, "host", dataSourceName);
			if (userAuthMethodOpt.isEmpty()) {
				validationResults.add(unableToConnect());
				return;
			}
			String userAuthMethod = userAuthMethodOpt.get();
			if (!AuthMethod.isSupported(userAuthMethod)) {
				validationResults.add(unableToConnect());
			}
			if (AuthMethod.isPasswordBased(userAuthMethod)) {
				if (!connectUser()) {
					validationResults.add("Falsches Benutzerpasswort", "Das angegebene Benutzerpasswort stimmt nicht mit dem Passwort der vorhandenen Datenbank überein. Während der Installation wird das Passwort mit '" + postgresUserPassword + "' ersetzt.", Severity.WARNING);
				}
			}
		} catch (IOException | InterruptedException e) {
			log.log(Level.WARNING, "Passwort Validierung unterbrochen", e);
		}
	}

	private ValidationResult unableToConnect() {
		String description = String.format("""
				Nur Verbindungen mit dem Verbindungstyp "host" und einer der Authentifizierungsmethoden trust, md5 oder scram-sha-256 werden für den Benutzer %s unterstützt. 
				Während der Installation werden folgende Einträge in die pg_hba.conf aufgenommen:		
				host    collections             flashcards             127.0.0.1/32            scram-sha-256
				host    collections             flashcards             ::1/128                 scram-sha-256
				""", postgresUser);
		return new ValidationResult("Kein Verbindungsaufbau möglich.", description, Severity.WARNING);
	}

	private boolean canConnectToPostgresAdminByPassword() {
		return execute(new String[]{"sudo", "--preserve-env=PGPASSWORD", "-u", postgresAdmin, "psql", "-c", "SELECT 1;"}, postgresAdminPassword, "Verbindung zu " + postgresAdmin + " fehlgeschlagen");
	}

	private boolean connectUser() {
		return execute(new String[]{"psql", "-U", postgresUser, "-h", "localhost", "-d", dataSourceName, "-c", "SELECT 1;"}, postgresUserPassword, "Verbindung zu " + postgresUser + " fehlgeschlagen");
	}

	private boolean execute(String[] command, String password, String error) {
		try {
			var pb = new ProcessBuilder(command);
			pb.environment().put("PGPASSWORD", password);
			var process = pb.start();
			if (!process.waitFor(2, SECONDS)) {
				process.destroy();
				log.warning(error + ": Zeitüberschreitung");
				return false;
			}
			return process.exitValue() == 0;
		} catch (IOException e) {
			log.warning(error + ": " + e.getMessage());
			return false;
		} catch (InterruptedException e) {
			currentThread().interrupt();
			log.warning(error + ": Vorgang wurde unterbrochen");
			return false;
		}
	}

	// Getters and Setters
	public Database getDatabaseImplementation() {
		return database;
	}

	public void setDatabaseImplementation(Database database) {
		this.database = database;
	}

	public String getSelectedPostgresVersion() {
		return selectedPostgresVersion;
	}

	public void setSelectedPostgresVersion(String selectedPostgresVersion) {
		this.selectedPostgresVersion = selectedPostgresVersion;
	}

	public String getSelectedH2Version() {
		return selectedH2Version;
	}

	public void setSelectedH2Version(String selectedH2Version) {
		this.selectedH2Version = selectedH2Version;
	}

	public String getPostgresAdmin() {
		return postgresAdmin;
	}

	public void setPostgresAdmin(String postgresAdmin) {
		this.postgresAdmin = postgresAdmin;
	}

	public String getPostgresAdminPassword() {
		return postgresAdminPassword;
	}

	public void setPostgresAdminPassword(String postgresAdminPassword) {
		this.postgresAdminPassword = postgresAdminPassword;
	}

	public String getPostgresUser() {
		return postgresUser;
	}

	public String getPostgresUserPassword() {
		return postgresUserPassword;
	}

	public void setPostgresUserPassword(String postgresUserPassword) {
		this.postgresUserPassword = postgresUserPassword;
	}

	public String getPostgresPort() {
		return postgresPort;
	}

	public void setPostgresPort(String postgresPort) {
		this.postgresPort = postgresPort;
	}

	public String getPostgresHost() {
		return postgresHost;
	}

	public String getH2User() {
		return h2User;
	}

	public String getDbName() {
		return dataSourceName;
	}

	public String[] getPostgresVersions() {
		return POSTGRES_VERSIONS_AVAILABLE;
	}

	public String[] getH2Versions() {
		return H2_VERSIONS;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	private final Map<String, PostgresState> postgresInstallStates = new HashMap<>();

	public Map<String, PostgresState> getPostgresInstallStates() {
		return postgresInstallStates;
	}
}
