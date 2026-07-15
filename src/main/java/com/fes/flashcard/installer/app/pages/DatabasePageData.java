package com.fes.flashcard.installer.app.pages;

import com.fes.flashcard.installer.PageDataPool;
import com.fes.flashcard.installer.app.Database;
import com.fes.flashcard.installer.page.PageData;
import com.fes.flashcard.installer.validation.Severity;
import com.fes.flashcard.installer.validation.ValidationResult;
import com.fes.flashcard.installer.validation.ValidationResults;

import java.util.ArrayList;
import java.util.List;

public class DatabasePageData extends PageData {

	// Preferences keys
	private static final String KEY_TYPE                        = "db.type";
	private static final String KEY_SELECTED_POSTGRESQL_VERSION = "db.postgresql.selected.version";
	private static final String KEY_VERSION_H2                  = "db.h2.selected.version";
	private static final String KEY_POSTGRESQL_USERNAME         = "db.postgresql.username";
	private static final String KEY_POSTGRESQL_PASSWORD         = "db.postgresql.password";
	private static final String KEY_POSTGRESQL_PORT             = "db.postgresql.port";
	private static final String KEY_POSTGRESQL_HOST             = "db.postgresql.host";
	private static final String KEY_H2_USERNAME                 = "db.h2.username";
	private static final String KEY_H2_PASSWORD                 = "db.h2.password";
	private static final String KEY_NAME                        = "db.name";

	// Default values
	private static final String DEFAULT_SELECTED_POSTGRESQL_VERSION = "14";
	private static final String DEFAULT_POSTGRESQL_USERNAME         = "postgres";
	private static final String DEFAULT_POSTGRESQL_PASSWORD         = "";
	private static final String DEFAULT_POSTGRESQL_PORT             = "5432";
	private static final String DEFAULT_POSTGRESQL_HOST             = "localhost";
	public static final  String DEFAULT_SELECTED_H2_VERSION         = "2.4";
	private static final String DEFAULT_H2_USERNAME                 = "sa";
	private static final String DEFAULT_H2_PASSWORD                 = "";
	private static final String DEFAULT_DB_NAME                     = "collections";

	// version options
	private static final String[] POSTGRESQL_VERSIONS = {"14", "15", "16", "17", "18"};
	private static final String[] H2_VERSIONS         = {"2.2", "2.3", "2.4"};

	// Values
	private Database database                  = Database.PostgreSQL;
	private String   selectedPostgresqlVersion = DEFAULT_SELECTED_POSTGRESQL_VERSION;
	private String   selectedH2Version         = DEFAULT_SELECTED_H2_VERSION;
	private String   postgresqlUsername        = DEFAULT_POSTGRESQL_USERNAME;
	private String   postgresqlPassword        = DEFAULT_POSTGRESQL_PASSWORD;
	private String   postgresqlPort            = DEFAULT_POSTGRESQL_PORT;
	private String   postgresqlHost            = DEFAULT_POSTGRESQL_HOST;
	private String   h2Username                = DEFAULT_H2_USERNAME;
	private String   h2Password                = DEFAULT_H2_PASSWORD;
	private String   dbName                    = DEFAULT_DB_NAME;

	public DatabasePageData(PageDataPool pageDataPool) {
		super(pageDataPool);
	}

	@Override
	public void load() {
		database = Database.valueOf(preferences.get(KEY_TYPE, Database.PostgreSQL.name()));
		selectedPostgresqlVersion = preferences.get(KEY_SELECTED_POSTGRESQL_VERSION, DEFAULT_SELECTED_POSTGRESQL_VERSION);
		selectedH2Version = preferences.get(KEY_VERSION_H2, DEFAULT_SELECTED_H2_VERSION);
		postgresqlUsername = preferences.get(KEY_POSTGRESQL_USERNAME, DEFAULT_POSTGRESQL_USERNAME);
		postgresqlPassword = preferences.get(KEY_POSTGRESQL_PASSWORD, DEFAULT_POSTGRESQL_PASSWORD);
		postgresqlPort = preferences.get(KEY_POSTGRESQL_PORT, DEFAULT_POSTGRESQL_PORT);
		postgresqlHost = preferences.get(KEY_POSTGRESQL_HOST, DEFAULT_POSTGRESQL_HOST);
		h2Username = preferences.get(KEY_H2_USERNAME, DEFAULT_H2_USERNAME);
		h2Password = preferences.get(KEY_H2_PASSWORD, DEFAULT_H2_PASSWORD);
		dbName = preferences.get(KEY_NAME, DEFAULT_DB_NAME);
	}

	@Override
	public void save() {
		preferences.put(KEY_TYPE, database.name());
		preferences.put(KEY_SELECTED_POSTGRESQL_VERSION, selectedPostgresqlVersion);
		preferences.put(KEY_VERSION_H2, selectedH2Version);
		preferences.put(KEY_POSTGRESQL_USERNAME, postgresqlUsername);
		preferences.put(KEY_POSTGRESQL_PASSWORD, postgresqlPassword);
		preferences.put(KEY_POSTGRESQL_PORT, postgresqlPort);
		preferences.put(KEY_POSTGRESQL_HOST, postgresqlHost);
		preferences.put(KEY_H2_USERNAME, h2Username);
		preferences.put(KEY_H2_PASSWORD, h2Password);
		preferences.put(KEY_NAME, dbName);
	}

	@Override
	public void loadDefaults() {
		database = Database.PostgreSQL;
		selectedPostgresqlVersion = DEFAULT_SELECTED_POSTGRESQL_VERSION;
		selectedH2Version = DEFAULT_SELECTED_H2_VERSION;
		postgresqlUsername = DEFAULT_POSTGRESQL_USERNAME;
		postgresqlPassword = DEFAULT_POSTGRESQL_PASSWORD;
		postgresqlPort = DEFAULT_POSTGRESQL_PORT;
		postgresqlHost = DEFAULT_POSTGRESQL_HOST;
		h2Username = DEFAULT_H2_USERNAME;
		h2Password = DEFAULT_H2_PASSWORD;
		dbName = DEFAULT_DB_NAME;

		preferences.remove(KEY_TYPE);
		preferences.remove(KEY_SELECTED_POSTGRESQL_VERSION);
		preferences.remove(KEY_VERSION_H2);
		preferences.remove(KEY_POSTGRESQL_USERNAME);
		preferences.remove(KEY_POSTGRESQL_PASSWORD);
		preferences.remove(KEY_POSTGRESQL_PORT);
		preferences.remove(KEY_POSTGRESQL_HOST);
		preferences.remove(KEY_H2_USERNAME);
		preferences.remove(KEY_H2_PASSWORD);
		preferences.remove(KEY_NAME);
	}

	@Override
	public ValidationResults validate() {
		List<ValidationResult> validationResults = new ArrayList<>();

		// TODO instead of isEmpty isBlank
		if (database == Database.PostgreSQL) {
			if (!postgresqlUsername.equals(DEFAULT_POSTGRESQL_USERNAME)) {
				validationResults.add(new ValidationResult("Datenbanknutzer nicht typisch", "Für den Datenbanknutzer wird per Konvention „postgres“ verwendet.", Severity.WARNING));
			}
			if (postgresqlUsername.isEmpty()) {
				validationResults.add(new ValidationResult("Datenbank Nutzer", "Benutzername darf nicht leer sein", 0));
			}
			if (postgresqlPort.isEmpty()) {
				validationResults.add(new ValidationResult("Datenbank Port", "Port darf nicht leer sein", 1));
			} else {
				try {
					int portNum = Integer.parseInt(postgresqlPort);
					if (portNum < 1 || portNum > 65535) {
						validationResults.add(new ValidationResult("Datenbank Port", "Port muss zwischen 1 und 65535 liegen", 0));
					}
				} catch (NumberFormatException e) {
					validationResults.add(new ValidationResult("Datenbank Port", "Port muss eine gültige Zahl sein", 0));
				}
			}
			if (postgresqlHost.isEmpty()) {
				validationResults.add(new ValidationResult("Datenbank Host", "Host darf nicht leer sein", 1));
			}
		} else {
			if (h2Username.isEmpty()) {
				validationResults.add(new ValidationResult("Datenbank Nutzer", "Benutzername darf nicht leer sein", 1));
			}
		}

		if (dbName.isEmpty()) {
			validationResults.add(new ValidationResult("Datenbank Name", "Datenbankname darf nicht leer sein", 1));
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

	public String getSelectedPostgresqlVersion() {
		return selectedPostgresqlVersion;
	}

	public void setSelectedPostgresqlVersion(String selectedPostgresqlVersion) {
		this.selectedPostgresqlVersion = selectedPostgresqlVersion;
	}

	public String getSelectedH2Version() {
		return selectedH2Version;
	}

	public void setSelectedH2Version(String selectedH2Version) {
		this.selectedH2Version = selectedH2Version;
	}

	public String getPostgresqlUsername() {
		return postgresqlUsername;
	}

	public void setPostgresqlUsername(String postgresqlUsername) {
		this.postgresqlUsername = postgresqlUsername;
	}

	public String getPostgresqlPassword() {
		return postgresqlPassword;
	}

	public void setPostgresqlPassword(String postgresqlPassword) {
		this.postgresqlPassword = postgresqlPassword;
	}

	public String getPostgresqlPort() {
		return postgresqlPort;
	}

	public void setPostgresqlPort(String postgresqlPort) {
		this.postgresqlPort = postgresqlPort;
	}

	public String getPostgresqlHost() {
		return postgresqlHost;
	}

	public void setPostgresqlHost(String postgresqlHost) {
		this.postgresqlHost = postgresqlHost;
	}

	public String getH2Username() {
		return h2Username;
	}

	public void setH2Username(String h2Username) {
		this.h2Username = h2Username;
	}

	public String getH2Password() {
		return h2Password;
	}

	public void setH2Password(String h2Password) {
		this.h2Password = h2Password;
	}

	public String getDbName() {
		return dbName;
	}

	public String[] getPostgresqlVersions() {
		return POSTGRESQL_VERSIONS;
	}

	public String[] getH2Versions() {
		return H2_VERSIONS;
	}
}
