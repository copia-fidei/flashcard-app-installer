package com.epau.app.flashcard.app.installer.operations;

import com.epau.app.flashcard.app.installer.AuthMethod;
import com.epau.app.flashcard.app.installer.PgHba;
import com.epau.app.flashcard.app.installer.Postgres;
import com.epau.app.flashcard.app.installer.pages.DatabasePageData;
import com.epau.app.flashcard.app.installer.pages.PostgresState;
import com.epau.util.nls.Nls;
import com.epau.util.swing.operation.ErrorCode;
import com.epau.util.swing.operation.Operation;
import com.epau.util.text.MultilineText;
import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import static com.epau.app.flashcard.app.installer.Resources.POSTGRES_INIT_SQL;
import static com.epau.app.flashcard.app.installer.Resources.getTmpFile;
import static com.epau.app.flashcard.app.installer.pages.PostgresState.INSTALLED_BY_INSTALLER;
import static com.epau.app.flashcard.app.installer.pages.PostgresState.NOT_INSTALLED;
import static com.epau.util.stream.io.TmpLocation.deleteAfterUse;
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
		var text = new MultilineText();
		text.add(nls.get("ConfigurePostgresOp.description.Find_authentication_method_for_user_postgres"));
		text.add(nls.get("ConfigurePostgresOp.description.Check_if_user_{0}_can_connect", user));
		text.add(nls.get("ConfigurePostgresOp.description.Enable_connection_for_user_{0}", user));
		text.add(nls.get("ConfigurePostgresOp.description.Set_PostgreSQL_port_to_{0}", port));
		text.add(nls.get("ConfigurePostgresOp.description.Restart_PostgreSQL"));
		text.add(nls.get("ConfigurePostgresOp.description.Create_user_{0}", user));
		text.add(nls.get("ConfigurePostgresOp.description.Set_password_for_{0}", user));
		text.add(nls.get("ConfigurePostgresOp.description.Create_database_{0}_and_tables", dbName));
		text.add(nls.get("ConfigurePostgresOp.description.Grant_{0}_ownership_of_database_{1}", user, dbName));
		text.add(nls.get("ConfigurePostgresOp.description.Test_JDBC_connection"));
		return text.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		if (getPostgresState() == NOT_INSTALLED) {
			throw new Exception(nls.get("ConfigurePostgresOp.println.PostgreSQL_{0}_is_not_installed", postgresVersion));
		}
		progress(2);
		println(nls.get("ConfigurePostgresOp.println.Search_authentication_method_for_PostgreSQL"));
		progress(2);
		String authMethod = postgres.getAuthMethod(databasePageData.getPostgresAdmin(), "local", "postgres").orElseThrow(() ->
				// Unsupported authentification methods should have been prevented in database page (next button disabled).
				// There is a minimal risks if the user changes the database configuration while using the installer,
				// but this is considered his own fault.
				new Exception(nls.get("ConfigurePostgresOp.println.pg_hba_conf_has_no_authentication_method")));
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

		return nls.get("ConfigurePostgresOp.println.Configuration_completed");
	}

	private PostgresState getPostgresState() {
		// needs to fetch the latest value
		return databasePageData.getPostgresInstallStates().get(postgresVersion);
	}

	private void setPort() throws IOException, InterruptedException, ErrorCode {
		println(nls.get("ConfigurePostgresOp.description.Set_PostgreSQL_port_to_{0}", port));
		progress(2);
		execute(new ProcessBuilder("sudo", "sed", "-Ei", "s/^([[:space:]]*port[[:space:]]*=?[[:space:]]*)[0-9]+/\\1" + port + "/", postgres.getConfigurationFile())) //NON-NLS
				.throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.Set_port_failed_for_{0}", port));
	}


	private Supplier<ProcessBuilder> setupLoginMethod(String authMethod) throws IOException, InterruptedException, ErrorCode {
		Supplier<ProcessBuilder> processBuilder;
		if (authMethod.equals("peer")) { //NON-NLS
			processBuilder = () -> new ProcessBuilder("sudo", "-u", "postgres", "psql"); //NON-NLS
			if (getPostgresState() == INSTALLED_BY_INSTALLER && !adminPassword.isBlank()) {
				setAdminPassword(processBuilder);
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
		println(nls.get("ConfigurePostgresOp.println.Authentication_method_setup_{0}", authMethod));
		progress(2);
		return processBuilder;
	}

	private void setAdminPassword(Supplier<ProcessBuilder> loginCommand) throws IOException, InterruptedException, ErrorCode {
		println(nls.get("ConfigurePostgresOp.println.Set_password_for_Postgres"));
		progress(2);
		var pb = loginCommand.get();
		pb.command().add("-c"); //NON-NLS
		pb.command().add("ALTER USER postgres WITH PASSWORD '" + adminPassword + "';"); //NON-NLS
		execute(pb).throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.Set_password_failed"));
		println(nls.get("ConfigurePostgresOp.println.Admin_password_set_successfully"));
		progress(2);
	}

	private void ensureUserCanConnect() throws Exception {
		if (userCanConnect()) {
			println(nls.get("ConfigurePostgresOp.println.Role_{0}_can_already_connect", user));
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
		println(nls.get("ConfigurePostgresOp.println.Enable_connection_for_role_{0}", user));
		progress(2);

		String entries = PgHba.getEntries(dbName, user);

		String authFile = postgres.getAuthFile();
		println(nls.get("ConfigurePostgresOp.println.Add_entries_to_auth_file_{0}_{1}", entries, authFile));
		progress(2);

		var process = new ProcessBuilder("sudo", "tee", "--append", postgres.getAuthFile()).start(); //NON-NLS
		try (var writer = process.outputWriter()) {
			writer.write(entries);
		}
		process.waitFor();
		println(nls.get("ConfigurePostgresOp.println.Entries_added_successfully"));
		progress(2);
	}

	// the only command that takes time
	private void restartPostgres() throws IOException, InterruptedException, ErrorCode {
		println(nls.get("ConfigurePostgresOp.println.Restart_PostgreSQL"));
		progress(2);
		execute(new ProcessBuilder("sudo", "systemctl", "restart", "postgresql.service")) //NON-NLS
				.throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.Restart_PostgreSQL_failed"));
		println(nls.get("ConfigurePostgresOp.println.PostgreSQL_restarted"));
		setProgress(40);
	}

	private void createUser(Supplier<ProcessBuilder> loginCommand) throws Exception {
		println(nls.get("ConfigurePostgresOp.println.Check_if_role_exists_{0}", user));
		progress(2);

		var checkUsersExists = loginCommand.get();
		checkUsersExists.command().add("-tA"); //NON-NLS
		checkUsersExists.command().add("-c"); //NON-NLS
		checkUsersExists.command().add("SELECT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '" + user + "');"); //NON-NLS

		var checkResult = execute(checkUsersExists);
		checkResult.throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.Check_role_exists_failed_for_{0}", user));
		if ("t".equals(checkResult.output().trim())) { //NON-NLS
			println(nls.get("ConfigurePostgresOp.println.Role_{0}_already_exists", user));
			progress(2);
			return;
		}
		println(nls.get("ConfigurePostgresOp.println.Create_role_{0}", user));
		progress(2);

		var createUser = loginCommand.get();
		createUser.command().add("-c"); //NON-NLS
		createUser.command().add("CREATE USER " + user + ";"); //NON-NLS

		execute(createUser).throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.Create_role_failed"));

		println(nls.get("ConfigurePostgresOp.println.Role_{0}_created_successfully", user));
		progress(2);
	}

	private void setUserPassword(Supplier<ProcessBuilder> loginCommand) throws Exception {
		if (userPassword.isBlank()) {
			println(nls.get("ConfigurePostgresOp.println.No_password_set_for_user_{0}", user));
			progress(2);
			return;
		}
		println(nls.get("ConfigurePostgresOp.println.Set_password_for_user_{0}", user));
		progress(2);
		var pb = loginCommand.get();
		pb.command().add("-c"); //NON-NLS
		pb.command().add("ALTER USER " + user + " WITH PASSWORD '" + userPassword + "';"); //NON-NLS
		execute(pb).throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.Set_password_failed"));

		println(nls.get("ConfigurePostgresOp.println.Password_set_successfully_for_{0}", user));
		progress(2);
	}

	private void createDatabase(Supplier<ProcessBuilder> loginCommand) throws Exception {
		println(nls.get("ConfigurePostgresOp.println.Create_database_and_tables"));
		progress(2);

		deleteAfterUse(getTmpFile(POSTGRES_INIT_SQL), script -> {
			println(nls.get("ConfigurePostgresOp.println.Execute"));
			println(Files.readString(script));
			var pb = loginCommand.get();
			pb.command().add("-f"); //NON-NLS
			pb.command().add(script.toString());
			execute(pb).throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.Create_database_failed"));

			println(nls.get("ConfigurePostgresOp.println.Database_and_tables_created_successfully"));
			progress(2);
		});
	}

	private void grantUserOwnershipOfDatabase(Supplier<ProcessBuilder> loginCommand) throws Exception {
		println(nls.get("ConfigurePostgresOp.println.Grant_ownership_of_database_{0}_to_{1}", dbName, user));
		progress(2);

		@NonNls String sql = format("""
				GRANT ALL PRIVILEGES ON DATABASE {0} TO {1};
				GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO {1};
				""", dbName, user);

		println(nls.get("ConfigurePostgresOp.println.Execute"));
		println(sql);

		var pb = loginCommand.get();
		pb.command().add("-d"); //NON-NLS
		pb.command().add(dbName);
		pb.command().add("-c"); //NON-NLS
		pb.command().add(sql);
		execute(pb).throwIfNonZeroExit(nls.get("ConfigurePostgresOp.println.Grant_ownership_failed"));

		println(nls.get("ConfigurePostgresOp.println.Ownership_granted_successfully"));
		progress(2);
	}

	private void testUserConnection() throws SQLException {
		String host = databasePageData.getPostgresHost();
		println(nls.get("ConfigurePostgresOp.println.Test_JDBC_connection_for_user_{0}", user));
		progress(2);

		@NonNls String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
		try (Connection _ = getConnection(url, user, userPassword)) {
			println(nls.get("ConfigurePostgresOp.println.JDBC_connection_successful_for_user_{0}", user));
			progress(2);
		}
	}
}
