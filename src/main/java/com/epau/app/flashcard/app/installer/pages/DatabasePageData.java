package com.epau.app.flashcard.app.installer.pages;

import com.epau.app.flashcard.app.installer.AuthMethod;
import com.epau.app.flashcard.app.installer.Database;
import com.epau.app.flashcard.app.installer.PgHba;
import com.epau.app.flashcard.app.installer.Postgres;
import com.epau.installer.page.PageData;
import com.epau.installer.page.PageDataPool;
import com.epau.installer.validation.Severity;
import com.epau.installer.validation.ValidationResult;
import com.epau.installer.validation.ValidationResults;
import com.epau.utilities.nls.Nls;
import org.jetbrains.annotations.NonNls;

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

public class DatabasePageData extends PageData {

	private final Nls nls = new Nls(this);

	// Preferences keys
	private static final String KEY_DB_TYPE                   = "db.type";
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
	private static final         String DEFAULT_SELECTED_POSTGRES_VERSION = "14";
	private static final @NonNls String DEFAULT_POSTGRES_ADMIN            = "postgres";
	private static final         String DEFAULT_POSTGRES_ADMIN_PASSWORD   = "";
	private static final         String DEFAULT_POSTGRES_PORT             = "5432";
	private static final         String DEFAULT_POSTGRES_USER_PASSWORD    = "";
	private static final         String DEFAULT_SELECTED_H2_VERSION       = "2.4";

	// version options
	private static final String[] POSTGRES_VERSIONS_ALL       = {"10", "11", "12", "13", "14", "15", "16", "17", "18"};
	private static final String[] POSTGRES_VERSIONS_AVAILABLE = filterAvailablePostgresVersions();
	private static final String[] H2_VERSIONS                 = {"2.2.224"};

	// values
	private               Database database                = Database.PostgreSQL;
	private               String   selectedPostgresVersion = DEFAULT_SELECTED_POSTGRES_VERSION;
	private               String   selectedH2Version       = DEFAULT_SELECTED_H2_VERSION;
	private               String   postgresAdmin           = DEFAULT_POSTGRES_ADMIN;
	private               String   postgresAdminPassword   = DEFAULT_POSTGRES_ADMIN_PASSWORD;
	private               String   postgresPort            = DEFAULT_POSTGRES_PORT;
	private final @NonNls String   postgresHost            = "localhost";
	private final @NonNls String   postgresUser            = "flashcards";
	private               String   postgresUserPassword    = DEFAULT_POSTGRES_USER_PASSWORD;
	private final @NonNls String   h2User                  = "sa";
	private final @NonNls String   dbName                  = "collections";

	private final @NonNls String dataSourceName = "collections";


	public DatabasePageData(PageDataPool pageDataPool) {
		super(pageDataPool);
	}

	private static String[] filterAvailablePostgresVersions() {
		return stream(POSTGRES_VERSIONS_ALL).filter(DatabasePageData::isPostgresVersionAvailable).toArray(String[]::new);
	}

	private static boolean isPostgresVersionAvailable(String version) {
		try {
			Process process = new ProcessBuilder("apt-cache", "policy", "postgresql-" + version).start(); //$NON-NLS
			String  output;
			try (var reader = process.inputReader()) {
				output = reader.readAllAsString();
			}
			process.waitFor();
			return output.lines().count() > 1; // If the package does not exist the output will have only one line
		} catch (IOException | InterruptedException e) {
			getLogger(DatabasePageData.class.getName()).log(SEVERE, "Could not determine availability of PostgreSQL " + version, e); //$NON-NLS
			return false;
		}
	}

	@Override
	public void load() {
		database                = Database.valueOf(preferences.get(KEY_DB_TYPE, Database.PostgreSQL.name()));
		selectedPostgresVersion = preferences.get(KEY_SELECTED_POSTGRES_VERSION, DEFAULT_SELECTED_POSTGRES_VERSION);
		selectedH2Version       = preferences.get(KEY_VERSION_H2, 				 DEFAULT_SELECTED_H2_VERSION);
		postgresAdmin           = preferences.get(KEY_POSTGRES_ADMIN, 			 DEFAULT_POSTGRES_ADMIN);
		postgresAdminPassword   = preferences.get(KEY_POSTGRES_ADMIN_PASSWORD,   DEFAULT_POSTGRES_ADMIN_PASSWORD);
		postgresPort            = preferences.get(KEY_POSTGRES_PORT, 			 DEFAULT_POSTGRES_PORT);
		postgresUserPassword    = preferences.get(KEY_POSTGRES_USER_PASSWORD, 	 DEFAULT_POSTGRES_USER_PASSWORD);
	}


	@Override
	public void save() {
		preferences.put(KEY_DB_TYPE, database.name());
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
		database                = Database.PostgreSQL;
		selectedPostgresVersion = DEFAULT_SELECTED_POSTGRES_VERSION;
		selectedH2Version       = DEFAULT_SELECTED_H2_VERSION;
		postgresAdmin           = DEFAULT_POSTGRES_ADMIN;
		postgresAdminPassword   = DEFAULT_POSTGRES_ADMIN_PASSWORD;
		postgresPort            = DEFAULT_POSTGRES_PORT;
		postgresUserPassword    = DEFAULT_POSTGRES_USER_PASSWORD;

		preferences.remove(KEY_DB_TYPE);
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
				results.addError(nls.get("DatabasePageData.error.title.Empty_port"), nls.get("DatabasePageData.error.description.Port_must_not_be_empty"), 1);
			} else {
				try {
					int portNum = Integer.parseInt(postgresPort);
					if (portNum < 1 || portNum > 65535) {
						results.addError(nls.get("DatabasePageData.error.title.Invalid_port"), nls.get("DatabasePageData.error.description.Port_should_be_between_1_and_65535"), 0);
					}
				} catch (NumberFormatException e) {
					results.addError(nls.get("DatabasePageData.error.title.Invalid_port"), nls.get("DatabasePageData.error.description.Port_must_be_a_number"), 0);
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
				log.severe("PostgreSQL " + selectedPostgresVersion + ": No authentication method found for user=" + postgresAdmin + ", connectionType=local, databaseName=postgres");
				return;
			}
			String adminAuthMethod = adminAuthMethodOpt.get();
			if (adminAuthMethod.equals("reject")) { //$NON-NLS
				log.log(Level.INFO, "Authentification Method is 'reject'. Role " + postgresAdmin + " cannot log in");
			}
			if (!AuthMethod.isSupportedForAdmin(adminAuthMethod)) {
				validationResults.addError(nls.get("DatabasePageData.error.title.Unable_to_login_administrator"), nls.get("DatabasePageData.error.description.The_installer_supports_only_the_following_authentication_methods_for_the_administrator_role_postgres"), 1);
				return;
			}
			if (adminAuthMethod.equals("peer")) { //$NON-NLS
				validationResults.add(nls.get("DatabasePageData.info.title.Password_correctness_cannot_be_determined"), nls.get("DatabasePageData.info.description.The_authentication_method_peer_is_active"), Severity.INFO);
				return;
			}
			if (AuthMethod.isPasswordBased(adminAuthMethod)) {
				if (!canConnectToPostgresAdminByPassword()) {
					validationResults.add(nls.get("DatabasePageData.error.title.Wrong_admin_password"), nls.get("DatabasePageData.error.description.The_admin_password_is_wrong"), Severity.ERROR);
				}
			}
		} catch (IOException | InterruptedException e) {
			log.log(SEVERE, "Error during validation of the admin password.", e);
		}
	}

	private void validateUserPassword(ValidationResults validationResults) {
		if (postgresUserPassword.isBlank()) {
			validationResults.add(nls.get("DatabasePageData.error.title.Empty_user_password"), nls.get("DatabasePageData.error.description.A_user_password_is_required"), Severity.ERROR);
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
					validationResults.add(nls.get("DatabasePageData.warning.title.Wrong_user_password"), nls.get("DatabasePageData.warning.description.The_specified_user_password_does_not_match_the_password_of_the_existing_database", postgresUserPassword), Severity.WARNING);
				}
			}
		} catch (IOException | InterruptedException e) {
			log.log(Level.WARNING, "Error during validation of user " + postgresUser, e);
		}
	}

	private ValidationResult unableToConnect() {
		return new ValidationResult(
				nls.get("DatabasePageData.warning.title.Unable_to_connect"),
				nls.get("DatabasePageData.warning.description.Only_connections_with_the_connection_type_host_and_one_of_the_authentication_methods_trust_md5_or_scram_sha_256_are_supported_for_the_user_{0}",
						postgresUser,
						PgHba.getEntries(dbName, postgresUser)),
				Severity.WARNING);
	}

	private boolean canConnectToPostgresAdminByPassword() {
		return execute(new String[]{"sudo", "--preserve-env=PGPASSWORD", "-u", postgresAdmin, "psql", "-c", "SELECT 1;"}, postgresAdminPassword, "Connection to" + postgresAdmin + " failed");
	}

	private boolean connectUser() {
		return execute(new String[]{"psql", "-U", postgresUser, "-h", "localhost", "-d", dataSourceName, "-c", "SELECT 1;"}, postgresUserPassword, "Connection to" + postgresUser + " failed");
	}

	private boolean execute(@NonNls String[] command, String password, @NonNls String error) {
		try {
			var pb = new ProcessBuilder(command);
			pb.environment().put("PGPASSWORD", password);
			var process = pb.start();
			if (!process.waitFor(2, SECONDS)) {
				process.destroy();
				log.warning(error + ": Timeout");
				return false;
			}
			return process.exitValue() == 0;
		} catch (IOException e) {
			log.warning(error + ": " + e.getMessage());
			return false;
		} catch (InterruptedException e) {
			currentThread().interrupt();
			log.warning(error + ": Interrupted");
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
