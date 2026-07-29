package com.fes.flashcard.installer.app.pages;

import com.fes.flashcard.installer.PageDataPool;
import com.fes.flashcard.installer.app.Database;
import com.fes.flashcard.installer.page.PageData;
import com.fes.flashcard.installer.validation.Severity;
import com.fes.flashcard.installer.validation.ValidationResult;
import com.fes.flashcard.installer.validation.ValidationResults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private static final String DEFAULT_POSTGRES_HOST             = "localhost";
	private static final String DEFAULT_POSTGRES_USER             = "flashcards";
	private static final String DEFAULT_POSTGRES_USER_PASSWORD    = "";
	private static final String DEFAULT_SELECTED_H2_VERSION       = "2.4";
	private static final String DEFAULT_H2_ADMIN                  = "sa";
	private static final String DEFAULT_H2_ADMIN_PASSWORD         = "";

	// version options
	private static final String[] POSTGRES_VERSIONS = {"14", "15", "16", "17", "18"};
	private static final String[] H2_VERSIONS       = {"2.2", "2.3", "2.4"};

	// values
	private Database database                = Database.PostgreSQL;
	private String   selectedPostgresVersion = DEFAULT_SELECTED_POSTGRES_VERSION;
	private String   selectedH2Version       = DEFAULT_SELECTED_H2_VERSION;
	private String   postgresAdmin           = DEFAULT_POSTGRES_ADMIN;
	private String   postgresAdminPassword   = DEFAULT_POSTGRES_ADMIN_PASSWORD;
	private String   postgresPort            = DEFAULT_POSTGRES_PORT;
	private String   postgresHost            = DEFAULT_POSTGRES_HOST;
	private String   postgresUser            = DEFAULT_POSTGRES_USER;
	private String   postgresUserPassword    = DEFAULT_POSTGRES_USER_PASSWORD;
	private String   h2Admin                 = DEFAULT_H2_ADMIN;
	private String   h2AdminPassword         = DEFAULT_H2_ADMIN_PASSWORD;

	// unchanged values
	private final String dbName = "collections";

	public DatabasePageData(PageDataPool pageDataPool) {
		super(pageDataPool);
	}

	@Override
	public void load() {
		database = Database.valueOf(preferences.get(KEY_TYPE, Database.PostgreSQL.name()));
		selectedPostgresVersion = preferences.get(KEY_SELECTED_POSTGRES_VERSION, DEFAULT_SELECTED_POSTGRES_VERSION);
		selectedH2Version = preferences.get(KEY_VERSION_H2, DEFAULT_SELECTED_H2_VERSION);
		postgresAdmin = preferences.get(KEY_POSTGRES_ADMIN, DEFAULT_POSTGRES_ADMIN);
		postgresAdminPassword = preferences.get(KEY_POSTGRES_ADMIN_PASSWORD, DEFAULT_POSTGRES_ADMIN_PASSWORD);
		postgresPort = preferences.get(KEY_POSTGRES_PORT, DEFAULT_POSTGRES_PORT);
		postgresHost = preferences.get(KEY_POSTGRES_HOST, DEFAULT_POSTGRES_HOST);
		postgresUser = preferences.get(KEY_POSTGRES_USER, DEFAULT_POSTGRES_USER);
		postgresUserPassword = preferences.get(KEY_POSTGRES_USER_PASSWORD, DEFAULT_POSTGRES_USER_PASSWORD);
		h2Admin = preferences.get(KEY_H2_ADMIN, DEFAULT_H2_ADMIN);
		h2AdminPassword = preferences.get(KEY_H2_ADMIN_PASSWORD, DEFAULT_H2_ADMIN_PASSWORD);
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
		preferences.put(KEY_H2_ADMIN, h2Admin);
		preferences.put(KEY_H2_ADMIN_PASSWORD, h2AdminPassword);
	}

	@Override
	public void loadDefaults() {
		database = Database.PostgreSQL;
		selectedPostgresVersion = DEFAULT_SELECTED_POSTGRES_VERSION;
		selectedH2Version = DEFAULT_SELECTED_H2_VERSION;
		postgresAdmin = DEFAULT_POSTGRES_ADMIN;
		postgresAdminPassword = DEFAULT_POSTGRES_ADMIN_PASSWORD;
		postgresPort = DEFAULT_POSTGRES_PORT;
		postgresHost = DEFAULT_POSTGRES_HOST;
		postgresUser = DEFAULT_POSTGRES_USER;
		postgresUserPassword = DEFAULT_POSTGRES_USER_PASSWORD;
		h2Admin = DEFAULT_H2_ADMIN;
		h2AdminPassword = DEFAULT_H2_ADMIN_PASSWORD;

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

	// TODO validate Postgres database password, if postgres is installed (the right version
	@Override
	public ValidationResults validate() {
		List<ValidationResult> validationResults = new ArrayList<>();

		if (database == Database.PostgreSQL) {
			if (!postgresAdmin.equals(DEFAULT_POSTGRES_ADMIN)) {
				validationResults.add(new ValidationResult("Datenbanknutzer nicht typisch", "Für den Datenbanknutzer wird per Konvention „postgres“ verwendet.", Severity.WARNING));
			}
			// TODO the Leerstrings is not user friendly
			// TODO validate only what is in the specification
			// TODO validate postgresAdmin Password
			//			if (postgresAdmin.isBlank()) {
			//				validationResults.add(new ValidationResult("Leerstring", "Für den Admin sind keine Leerstrings erlaubt.", Severity.ERROR));
			//			}
			//			if (postgresAdminPassword.isBlank()) {
			//				validationResults.add(new ValidationResult("Leerstring", "Für das Admin-Passwort sind keine Leerstrings erlaubt.", Severity.ERROR));
			//			}
			//			if (postgresUser.isBlank()) {
			//				validationResults.add(new ValidationResult("Leerstring", "Für den Benutzernamen sind keine Leerstrings erlaubt.", Severity.ERROR));
			//			}
			//			if (postgresUserPassword.isBlank()) {
			//				validationResults.add(new ValidationResult("Leerstring", "Für das Benutzerpasswort sind keine Leerstrings erlaubt.", Severity.ERROR));
			//			}
			if (postgresPort.isBlank()) {
				validationResults.add(new ValidationResult("Datenbank Port", "Port darf nicht leer sein", 1));
			} else {
				try {
					int portNum = Integer.parseInt(postgresPort);
					if (portNum < 1 || portNum > 65535) {
						validationResults.add(new ValidationResult("Datenbank Port", "Port muss zwischen 1 und 65535 liegen", 0));
					}
				} catch (NumberFormatException e) {
					validationResults.add(new ValidationResult("Datenbank Port", "Port muss eine gültige Zahl sein", 0));
				}
			}
			if (postgresHost.isBlank()) {
				validationResults.add(new ValidationResult("Datenbank Host", "Host darf nicht leer sein", 1));
			}
		} else {
			if (h2Admin.isBlank()) {
				validationResults.add(new ValidationResult("Datenbank Nutzer", "Benutzername darf nicht leer sein", 1));
			}
		}

		return new ValidationResults(validationResults);
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

	public void setPostgresUser(String postgresUser) {
		this.postgresUser = postgresUser;
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

	public void setPostgresHost(String postgresHost) {
		this.postgresHost = postgresHost;
	}

	public String getH2Admin() {
		return h2Admin;
	}

	public void setH2Admin(String h2Admin) {
		this.h2Admin = h2Admin;
	}

	public String getH2AdminPassword() {
		return h2AdminPassword;
	}

	public void setH2AdminPassword(String h2AdminPassword) {
		this.h2AdminPassword = h2AdminPassword;
	}

	public String getDbName() {
		return dbName;
	}

	public String[] getPostgresVersions() {
		return POSTGRES_VERSIONS;
	}

	public String[] getH2Versions() {
		return H2_VERSIONS;
	}

	// TODO reset after apply
	// TODO threading issues?
	private final Map<String, PostgresState> postgresInstallStates = new HashMap<>();

	public Map<String, PostgresState> getPostgresInstallStates() {
		return postgresInstallStates;
	}

	// TODO add reinstalled?
	public enum PostgresState {
		NOT_INSTALLED,
		/// program has been installed or reinstalled by the installer
		INSTALLED_BY_INSTALLER,
		/// program had been already installed before this installer started
		ALREADY_INSTALLED
	}

}
