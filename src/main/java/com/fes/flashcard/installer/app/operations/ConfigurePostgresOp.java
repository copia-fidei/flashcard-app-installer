package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.app.AuthMethod;
import com.fes.flashcard.installer.app.Postgres;
import com.fes.flashcard.installer.app.pages.DatabasePageData;
import com.fes.flashcard.installer.app.pages.PostgresState;
import com.fes.flashcard.installer.operation.ErrorCode;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.utilities.Nls;
import com.fes.flashcard.installer.utilities.TextBuilder;
import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.function.Supplier;

import static com.fes.flashcard.installer.app.Resources.POSTGRES_INIT_SQL;
import static com.fes.flashcard.installer.app.Resources.getTmpFile;
import static com.fes.flashcard.installer.app.pages.PostgresState.INSTALLED_BY_INSTALLER;
import static com.fes.flashcard.installer.app.pages.PostgresState.NOT_INSTALLED;
import static com.fes.flashcard.installer.utilities.Temporary.use;
import static java.sql.DriverManager.getConnection;
import static java.text.MessageFormat.format;

public class ConfigurePostgresOp extends Operation {

	private static final Nls nls = new Nls(ConfigurePostgresOp.class);

	private final DatabasePageData databasePageData;

	private final String postgresVersion;
	private final String user;
	private final String userPassword;
	private final String adminPassword;
	private final String dbName;
	private final String port;

	private final Postgres postgres;

	public ConfigurePostgresOp(DatabasePageData databasePageData) {
		super(nls.get("ConfigurePostgresOp.title"), nls.get("ConfigurePostgresOp.description"));

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
		text.line(nls.get("ConfigurePostgresOp.description.findAuthMethodForPostgres"));
		text.line(nls.get("ConfigurePostgresOp.description.checkIfUserCanConnect", user));
		text.line(nls.get("ConfigurePostgresOp.description.enableConnectionForUser", user));
		text.line(nls.get("ConfigurePostgresOp.description.setPostgreSQLPort", port));
		text.line(nls.get("ConfigurePostgresOp.description.restartPostgreSQL"));
		text.line(nls.get("ConfigurePostgresOp.description.createUser", user));
		text.line(nls.get("ConfigurePostgresOp.description.setPasswordForUser", user));
		text.line(nls.get("ConfigurePostgresOp.description.createDatabaseAndTables", dbName));
		text.line(nls.get("ConfigurePostgresOp.description.grantOwnershipOfDatabase", user, dbName));
		text.line(nls.get("ConfigurePostgresOp.description.testJDBCConnection"));
		return text.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		if (getPostgresState() == NOT_INSTALLED) {
			throw new Exception(nls.get("ConfigurePostgresOp.println.postgresNotInstalled", postgresVersion));
		}
		progress(2);
		println(nls.get("ConfigurePostgresOp.println.searchAuthMethodForPostgres"));
		progress(2);
		String authMethod = postgres.getAuthMethod(databasePageData.getPostgresAdmin(), "local", "postgres").orElseThrow(() ->
				// Unsupported authentification methods should have been prevented in database page (next button disabled).
				// There is a minimal risks if the user changes the database configuration while using the installer,
				// but this is considered his own fault.
				new Exception(nls.get("ConfigurePostgresOp.println.pgHbaConfNoAuthMethod")));
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

		return nls.get("ConfigurePostgresOp.println.configurationCompleted");
	}

	private PostgresState getPostgresState() {
		// needs to fetch the latest value
		return databasePageData.getPostgresInstallStates().get(postgresVersion);
	}

	private void setPort() throws IOException, InterruptedException, ErrorCode {
		println(nls.get("ConfigurePostgresOp.description.setPostgreSQLPort", port));
		progress(2);
		execute(new ProcessBuilder("sudo", "sed", "-Ei", "s/^([[:space:]]*port[[:space:]]*=?[[:space:]]*)[0-9]+/\\1" + port + "/", postgres.getConfigurationFile()).start()) //NON-NLS
				.throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.setPortFailed", port));
	}


	private Supplier<ProcessBuilder> setupLoginMethod(String authMethod) throws IOException, InterruptedException, ErrorCode {
		Supplier<ProcessBuilder> processBuilder;
		if (authMethod.equals("peer")) {
			processBuilder = () -> new ProcessBuilder("sudo", "-u", "postgres", "psql"); //NON-NLS
			if (getPostgresState() == INSTALLED_BY_INSTALLER) {
				setAdminPassword();
			}
		} else if (AuthMethod.isPasswordBased(authMethod)) {
			processBuilder = () -> {
				var pb = new ProcessBuilder("psql", "-U", "postgres"); //NON-NLS
				pb.environment().put("PGPASSWORD", adminPassword);
				return pb;
			};
		} else {
			// we do not expect to land here, except if the user modifies the database while using the installer
			throw new IllegalStateException("The postgres user cannot login. Was the the database modified during installation?");
		}
		println(nls.get("ConfigurePostgresOp.println.authMethodSetup", authMethod));
		progress(2);
		return processBuilder;
	}

	private void setAdminPassword() throws IOException, InterruptedException, ErrorCode {
		if (adminPassword.isBlank()) {
			return;
		}
		println(nls.get("ConfigurePostgresOp.println.setPasswordForPostgres"));
		progress(2);
		var pb = new ProcessBuilder("sudo", "-u", "postgres", "psql"); //NON-NLS
		pb.command().add("-c"); //NON-NLS
		pb.command().add("ALTER USER postgres WITH PASSWORD '" + adminPassword + "';"); //NON-NLS
		var process = pb.start();
		execute(process).throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.setPasswordFailed"));
		println(nls.get("ConfigurePostgresOp.println.adminPasswordSetSuccessfully"));
		progress(2);
	}

	private void ensureUserCanConnect() throws Exception {
		if (userCanConnect()) {
			println(nls.get("ConfigurePostgresOp.println.roleCanAlreadyConnect", user));
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
		println(nls.get("ConfigurePostgresOp.println.enableConnectionForRole", user));
		progress(2);

		String entries = MessageFormat.format("""
				host    {0}     {1}      127.0.0.1/32            scram-sha-256
				host    {0}     {1}      ::1/128                 scram-sha-256
				""", dbName, user);

		String authFile = postgres.getAuthFile();
		println(nls.get("ConfigurePostgresOp.println.addEntriesToAuthFile", entries, authFile));
		progress(2);

		var process = new ProcessBuilder("sudo", "tee", "--append", postgres.getAuthFile()).start(); //NON-NLS
		try (var writer = process.outputWriter()) {
			writer.write(entries);
		}
		process.waitFor();
		println(nls.get("ConfigurePostgresOp.println.entriesAddedSuccessfully"));
		progress(2);
	}

	// the only command that takes time
	private void restartPostgres() throws IOException, InterruptedException, ErrorCode {
		println(nls.get("ConfigurePostgresOp.println.restartPostgreSQL"));
		progress(2);
		execute(new ProcessBuilder("sudo", "systemctl", "restart", "postgresql.service").start()) //NON-NLS
				.throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.restartPostgreSQLFailed"));
		println(nls.get("ConfigurePostgresOp.println.postgreSQLRestarted"));
		setProgress(40);
	}

	private void createUser(Supplier<ProcessBuilder> loginCommand) throws Exception {
		println(nls.get("ConfigurePostgresOp.println.checkIfRoleExists", user));
		progress(2);

		var checkUsersExists = loginCommand.get();
		checkUsersExists.command().add("-tA"); //NON-NLS
		checkUsersExists.command().add("-c"); //NON-NLS
		checkUsersExists.command().add("SELECT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '" + user + "');"); //NON-NLS

		var checkResult = execute(checkUsersExists.start());
		checkResult.throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.checkRoleExistsFailed", user));
		if ("t".equals(checkResult.output().trim())) { //NON-NLS
			println(nls.get("ConfigurePostgresOp.println.roleAlreadyExists", user));
			progress(2);
			return;
		}
		println(nls.get("ConfigurePostgresOp.println.createRole", user));
		progress(2);

		var createUser = loginCommand.get();
		createUser.command().add("-c"); //NON-NLS
		createUser.command().add("CREATE USER " + user + ";"); //NON-NLS

		execute(createUser.start()).throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.createRoleFailed"));

		println(nls.get("ConfigurePostgresOp.println.roleCreatedSuccessfully", user));
		progress(2);
	}

	private void setUserPassword(Supplier<ProcessBuilder> loginCommand) throws Exception {
		if (userPassword.isBlank()) {
			println(nls.get("ConfigurePostgresOp.println.noPasswordSetForUser", user));
			progress(2);
			return;
		}
		println(nls.get("ConfigurePostgresOp.println.setPasswordForUser", user));
		progress(2);
		var pb = loginCommand.get();
		pb.command().add("-c"); //NON-NLS
		pb.command().add("ALTER USER " + user + " WITH PASSWORD '" + userPassword + "';"); //NON-NLS
		execute(pb.start()).throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.setPasswordFailed"));

		println(nls.get("ConfigurePostgresOp.println.passwordSetSuccessfully", user));
		progress(2);
	}

	private void createDatabase(Supplier<ProcessBuilder> loginCommand) throws Exception {
		println(nls.get("ConfigurePostgresOp.println.createDatabaseAndTables"));
		progress(2);

		use(getTmpFile(POSTGRES_INIT_SQL), script -> {
			println(nls.get("ConfigurePostgresOp.println.execute"));
			println(Files.readString(script));
			var pb = loginCommand.get();
			pb.command().add("-f"); //NON-NLS
			pb.command().add(script.toString());
			execute(pb.start()).throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.createDatabaseFailed"));

			println(nls.get("ConfigurePostgresOp.println.databaseAndTablesCreatedSuccessfully"));
			progress(2);
		});
	}

	private void grantUserOwnershipOfDatabase(Supplier<ProcessBuilder> loginCommand) throws Exception {
		println(nls.get("ConfigurePostgresOp.println.grantOwnershipOfDatabase", user, dbName));
		progress(2);

		@NonNls String sql = format("""
				GRANT ALL PRIVILEGES ON DATABASE {0} TO {1};
				GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO {1};
				""", dbName, user);

		println(nls.get("ConfigurePostgresOp.println.executeGrant"));
		println(sql);

		var pb = loginCommand.get();
		pb.command().add("-d"); //NON-NLS
		pb.command().add(dbName);
		pb.command().add("-c"); //NON-NLS
		pb.command().add(sql);
		execute(pb.start()).throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.grantOwnershipFailed"));

		println(nls.get("ConfigurePostgresOp.println.ownershipGrantedSuccessfully"));
		progress(2);
	}

	private void testUserConnection() throws SQLException {
		String host = databasePageData.getPostgresHost();
		println(nls.get("ConfigurePostgresOp.println.testJDBCConnection", user));
		progress(2);

		@NonNls String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
		try (Connection _ = getConnection(url, user, userPassword)) {
			println(nls.get("ConfigurePostgresOp.println.jdbcConnectionSuccessful", user));
			progress(2);
		}
	}
}
