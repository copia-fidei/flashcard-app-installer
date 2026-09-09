package com.epau.app.flashcard.app.installer.operations;

import com.epau.app.flashcard.app.installer.pages.DatabasePageData;
import com.epau.app.flashcard.app.installer.pages.PostgresState;
import com.epau.app.flashcard.app.installer.pages.RootPasswordPageData;
import com.epau.installer.swing.DecisionDialog;
import com.epau.installer.swing.Option;
import com.epau.installer.utilities.WriterAdapter;
import com.epau.utilities.nls.Nls;
import com.epau.utilities.swing.operation.Cancelled;
import com.epau.utilities.swing.operation.ErrorCode;
import com.epau.utilities.swing.operation.Operation;
import com.pty4j.PtyProcessBuilder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstallPostgresOp extends Operation {

	private final static Nls nls = new Nls(InstallPostgresOp.class);

	private final DatabasePageData     databasePageData;
	private final RootPasswordPageData rootPasswordPageData;

	private final         String postgresVersion;
	private final @NonNls String postgresPackageName;

	public InstallPostgresOp(DatabasePageData databasePageData, RootPasswordPageData rootPasswordPageData) {
		super(nls.get("InstallPostgresOp.title"), nls.get("InstallPostgresOp.description"));

		this.databasePageData = databasePageData;
		this.rootPasswordPageData = rootPasswordPageData;
		this.postgresVersion = databasePageData.getSelectedPostgresVersion();
		this.postgresPackageName = "postgresql-" + postgresVersion;

	}

	public String getDescription() {
		return nls.get("InstallPostgresOp.description.Install_PostgreSQL");
	}

	@Override
	protected String doInBackground() throws Exception {
		setProgress(0);
		println(nls.get("InstallPostgresOp.println.Check_if_PostgreSQL_installed"));
		setProgress(2);
		switch (isPostgresInstalled()) {
			case FULLY -> {
				println(nls.get("InstallPostgresOp.println.PostgreSQL_is_already_installed"));
				databasePageData.getPostgresInstallStates().put(postgresVersion, PostgresState.ALREADY_INSTALLED);
				handleExistingPostgres();
			}
			case NOT -> installPostgres();
		}
		setProgress(100);
		return nls.get("InstallPostgresOp.println.Installation_completed");
	}

	private Installed isPostgresInstalled() throws IOException, InterruptedException {
		var process = new ProcessBuilder("dpkg-query", "-f=${db:Status-Abbrev}", "-W", postgresPackageName).start(); //$NON-NLS
		process.waitFor();
		String status;
		try (var reader = process.inputReader()) {
			status = reader.readAllAsString();
		}
		return status.startsWith("ii") ? Installed.FULLY : Installed.NOT; //$NON-NLS
	}

	private void handleExistingPostgres() throws Exception {
		setProgress(3);
		Choice choice = (Choice) showConflictDialog().id();
		setProgress(4);
		if (choice == Choice.REINSTALL) {
			setProgress(5);
			println(nls.get("InstallPostgresOp.println.Reinstall_PostgreSQL"));
			purgePostgres();
			if (isCancelled()) {
				return;
			}
			installPostgres();
		} else {
			println(nls.get("InstallPostgresOp.println.Existing_PostgreSQL_installation_will_be_reused"));
		}
		setProgress(99);
	}

	private Option showConflictDialog() throws Exception {
		var reuseOption     = new Option(Choice.REUSE, nls.get("InstallPostgresOp.button.Reuse_PostgreSQL"), nls.get("InstallPostgresOp.tooltip.The_existing_installation_will_be_used"));
		var reinstallOption = new Option(Choice.REINSTALL, nls.get("InstallPostgresOp.button.Reinstall_PostgreSQL"), nls.get("InstallPostgresOp.tooltip.The_existing_installation_will_be_removed_and_reinstalled"));

		return DecisionDialog.showDialog(nls.get("InstallPostgresOp.title.PostgreSQL_is_already_installed"), getDlgDescription(), List.of(reuseOption, reinstallOption), reuseOption);
	}

	private String getDlgDescription() {
		return nls.get("InstallPostgresOp.description.{0}_is_already_installed_on_the_system", postgresPackageName);
	}

	private void installPostgres() throws IOException, InterruptedException {
		println(nls.get("InstallPostgresOp.println.Install_PostgreSQL_{0}", postgresVersion));
		setProgress(20);
		List<String> command = List.of("sudo", "apt", "install", postgresPackageName); //$NON-NLS
		println(nls.get("InstallPostgresOp.println.Execute_{0}", String.join(" ", command)));
		setProgress(40);
		int exitCode = execute(new ProcessBuilder(command).start()).exitCode();
		setProgress(60);
		if (exitCode != 0) {
			println(nls.get("InstallPostgresOp.println.Error_installing_PostgreSQL"));
			return;
		}
		setProgress(80);
		println(nls.get("InstallPostgresOp.println.PostgreSQL_installed_successfully"));

		databasePageData.getPostgresInstallStates().put(postgresVersion, PostgresState.INSTALLED_BY_INSTALLER);
	}

	private static final Pattern YES_NO_PROMPT = Pattern.compile("\\[\\D+/(\\D+)]");

	private void purgePostgres() throws IOException, InterruptedException, ErrorCode {
		setProgress(10);
		println(nls.get("InstallPostgresOp.println.Remove_PostgreSQL_installation"));
		setProgress(15);

		var purgeB = new PtyProcessBuilder(new String[] {"sudo", "-S", "DEBIAN_FRONTEND=readline", "apt", "purge", "-y", postgresPackageName}); //NON-NLS
		purgeB.setRedirectErrorStream(true);
		var purge = purgeB.start();
		setProgress(20);

		try (var reader = purge.inputReader();
		     var writer = purge.outputWriter()) {

			reader.transferTo(new WriterAdapter() {
				@Override
				public void write(char @NotNull [] cbuf, int off, int len) throws IOException {
					if (isCancelled()) {
						purge.destroy();
						throw Cancelled.process(purge);
					}
					var str = new String(cbuf, off, len);

					if (str.toLowerCase().contains("[sudo]")) { // NON-NLS
						writer.write(rootPasswordPageData.getRootPassword());
						writer.write(System.lineSeparator());
						writer.flush();
					}
					Matcher matcher = YES_NO_PROMPT.matcher(str);
					if (matcher.find()) {
						var no = matcher.group(1); // e.g. "nein", "no", "non", ...
						writer.write(no);
						writer.write(System.lineSeparator());
						writer.flush();
					}

					publish(str);
				}
			});
		}
		int exitCode = purge.waitFor();
		setProgress(40);
		if (exitCode != 0) {
			throw new ErrorCode(exitCode, "apt purge " + postgresPackageName + " fehlgeschlagen");
		}
		setProgress(60);
		println(nls.get("InstallPostgresOp.println.PostgreSQL_removed_successfully"));
		setProgress(70);
	}

	private enum Installed {
		NOT, FULLY
	}
}
