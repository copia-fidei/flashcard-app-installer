import java.util.ArrayList;
import java.util.List;

public class DatabasePageData extends PageData {

	// Database types
	public enum Database {
		PostgreSQL,
		H2
	}

	// Preferences keys
	private static final String DB_TYPE = "db.type";
	private static final String DB_VERSION_POSTGRESQL = "db.version.postgresql";
	private static final String DB_VERSION_H2 = "db.version.h2";
	private static final String DB_POSTGRESQL_USERNAME = "db.postgresql.username";
	private static final String DB_POSTGRESQL_PASSWORD = "db.postgresql.password";
	private static final String DB_POSTGRESQL_PORT = "db.postgresql.port";
	private static final String DB_POSTGRESQL_HOST = "db.postgresql.host";
	private static final String DB_H2_USERNAME = "db.h2.username";
	private static final String DB_H2_PASSWORD = "db.h2.password";
	private static final String DB_NAME = "db.name";

	// Default values
	private static final String DEFAULT_POSTGRESQL_USERNAME = "postgres";
	private static final String DEFAULT_POSTGRESQL_PASSWORD = "";
	private static final String DEFAULT_POSTGRESQL_PORT = "5432";
	private static final String DEFAULT_POSTGRESQL_HOST = "localhost";
	private static final String DEFAULT_H2_USERNAME = "sa";
	private static final String DEFAULT_H2_PASSWORD = "";
	private static final String DEFAULT_DB_NAME = "collections";

	// PostgreSQL versions
	private static final String[] POSTGRESQL_VERSIONS = {"14", "15", "16", "17", "18"};
	// H2 versions
	private static final String[] H2_VERSIONS = {"2.2", "2.3", "2.4"};

	// Values
	private Database database = Database.PostgreSQL;
	private String postgresqlVersion = "14";
	private String h2Version = "2.4";
	private String postgresqlUsername = DEFAULT_POSTGRESQL_USERNAME;
	private String postgresqlPassword = DEFAULT_POSTGRESQL_PASSWORD;
	private String postgresqlPort = DEFAULT_POSTGRESQL_PORT;
	private String postgresqlHost = DEFAULT_POSTGRESQL_HOST;
	private String h2Username = DEFAULT_H2_USERNAME;
	private String h2Password = DEFAULT_H2_PASSWORD;
	private String dbName = DEFAULT_DB_NAME;

	public DatabasePageData(PageDataPool pageDataPool) {
		super(pageDataPool);
	}

	@Override
	public void load() {
		String typeStr = preferences.get(DB_TYPE, Database.PostgreSQL.name());
		database = Database.valueOf(typeStr);
		postgresqlVersion = preferences.get(DB_VERSION_POSTGRESQL, "14");
		h2Version = preferences.get(DB_VERSION_H2, "2.4");
		postgresqlUsername = preferences.get(DB_POSTGRESQL_USERNAME, DEFAULT_POSTGRESQL_USERNAME);
		postgresqlPassword = preferences.get(DB_POSTGRESQL_PASSWORD, DEFAULT_POSTGRESQL_PASSWORD);
		postgresqlPort = preferences.get(DB_POSTGRESQL_PORT, DEFAULT_POSTGRESQL_PORT);
		postgresqlHost = preferences.get(DB_POSTGRESQL_HOST, DEFAULT_POSTGRESQL_HOST);
		h2Username = preferences.get(DB_H2_USERNAME, DEFAULT_H2_USERNAME);
		h2Password = preferences.get(DB_H2_PASSWORD, DEFAULT_H2_PASSWORD);
		dbName = preferences.get(DB_NAME, DEFAULT_DB_NAME);
	}

	@Override
	public void save() {
		preferences.put(DB_TYPE, database.name());
		preferences.put(DB_VERSION_POSTGRESQL, postgresqlVersion);
		preferences.put(DB_VERSION_H2, h2Version);
		preferences.put(DB_POSTGRESQL_USERNAME, postgresqlUsername);
		preferences.put(DB_POSTGRESQL_PASSWORD, postgresqlPassword);
		preferences.put(DB_POSTGRESQL_PORT, postgresqlPort);
		preferences.put(DB_POSTGRESQL_HOST, postgresqlHost);
		preferences.put(DB_H2_USERNAME, h2Username);
		preferences.put(DB_H2_PASSWORD, h2Password);
		preferences.put(DB_NAME, dbName);
	}

	@Override
	void loadDefaults() {
		database = Database.PostgreSQL;
		postgresqlVersion = "14";
		h2Version = "2.4";
		postgresqlUsername = DEFAULT_POSTGRESQL_USERNAME;
		postgresqlPassword = DEFAULT_POSTGRESQL_PASSWORD;
		postgresqlPort = DEFAULT_POSTGRESQL_PORT;
		postgresqlHost = DEFAULT_POSTGRESQL_HOST;
		h2Username = DEFAULT_H2_USERNAME;
		h2Password = DEFAULT_H2_PASSWORD;
		dbName = DEFAULT_DB_NAME;
		preferences.remove(DB_TYPE);
		preferences.remove(DB_VERSION_POSTGRESQL);
		preferences.remove(DB_VERSION_H2);
		preferences.remove(DB_POSTGRESQL_USERNAME);
		preferences.remove(DB_POSTGRESQL_PASSWORD);
		preferences.remove(DB_POSTGRESQL_PORT);
		preferences.remove(DB_POSTGRESQL_HOST);
		preferences.remove(DB_H2_USERNAME);
		preferences.remove(DB_H2_PASSWORD);
		preferences.remove(DB_NAME);
	}

	@Override
	public ValidationResults validate() {
		List<ValidationResult> validationResults = new ArrayList<>();

		if (database == Database.PostgreSQL) {
			if (postgresqlUsername == null || postgresqlUsername.trim().isEmpty()) {
				validationResults.add(new ValidationResult("Datenbank Nutzer", "Benutzername darf nicht leer sein", ValidationResult.Severity.ERROR));
			}

			if (postgresqlPort == null || postgresqlPort.trim().isEmpty()) {
				validationResults.add(new ValidationResult("Datenbank Port", "Port darf nicht leer sein", ValidationResult.Severity.ERROR));
			} else {
				try {
					int portNum = Integer.parseInt(postgresqlPort);
					if (portNum < 1 || portNum > 65535) {
						validationResults.add(new ValidationResult("Datenbank Port", "Port muss zwischen 1 und 65535 liegen", ValidationResult.Severity.ERROR));
					}
				} catch (NumberFormatException e) {
					validationResults.add(new ValidationResult("Datenbank Port", "Port muss eine gültige Zahl sein", ValidationResult.Severity.ERROR));
				}
			}

			if (postgresqlHost == null || postgresqlHost.trim().isEmpty()) {
				validationResults.add(new ValidationResult("Datenbank Host", "Host darf nicht leer sein", ValidationResult.Severity.ERROR));
			}
		} else {
			if (h2Username == null || h2Username.trim().isEmpty()) {
				validationResults.add(new ValidationResult("Datenbank Nutzer", "Benutzername darf nicht leer sein", ValidationResult.Severity.ERROR));
			}
		}

		if (dbName == null || dbName.trim().isEmpty()) {
			validationResults.add(new ValidationResult("Datenbank Name", "Datenbankname darf nicht leer sein", ValidationResult.Severity.ERROR));
		}

		return new ValidationResults(validationResults);
	}

	// Getters and Setters
	public Database getDatabaseType() {
		return database;
	}

	public void setDatabaseType(Database database) {
		this.database = database;
	}

	public String getPostgresqlVersion() {
		return postgresqlVersion;
	}

	public void setPostgresqlVersion(String postgresqlVersion) {
		this.postgresqlVersion = postgresqlVersion;
	}

	public String getH2Version() {
		return h2Version;
	}

	public void setH2Version(String h2Version) {
		this.h2Version = h2Version;
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

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String[] getVersionsForType(Database type) {
		return type == Database.PostgreSQL ? POSTGRESQL_VERSIONS : H2_VERSIONS;
	}

	public String getCurrentVersion() {
		return database == Database.PostgreSQL ? postgresqlVersion : h2Version;
	}

	public void setCurrentVersion(String version) {
		if (database == Database.PostgreSQL) {
			postgresqlVersion = version;
		} else {
			h2Version = version;
		}
	}
}
