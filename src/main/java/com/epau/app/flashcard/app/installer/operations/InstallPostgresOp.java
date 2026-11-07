package com.epau.app.flashcard.app.installer.operations;

import com.epau.app.flashcard.app.installer.pages.DatabasePageData;
import com.epau.app.flashcard.app.installer.pages.PostgresState;
import com.epau.app.flashcard.app.installer.pages.RootPasswordPageData;
import com.epau.util.io.WriterAdapter;
import com.epau.util.nls.Nls;
import com.epau.util.swing.operation.Cancelled;
import com.epau.util.swing.operation.ErrorCode;
import com.epau.util.swing.operation.Operation;
import com.epau.util.swing.option_dialog.Option;
import com.pty4j.PtyProcessBuilder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.epau.app.flashcard.app.installer.Runner.getOutputOf;
import static com.epau.app.flashcard.app.installer.operations.Choice.KEEP;
import static com.epau.app.flashcard.app.installer.operations.Choice.REINSTALL;
import static com.epau.app.flashcard.app.installer.pages.PostgresState.ALREADY_INSTALLED;
import static com.epau.app.flashcard.app.installer.pages.PostgresState.INSTALLED_BY_INSTALLER;
import static com.epau.util.swing.option_dialog.OptionDialog.showDialog;
import static java.lang.String.join;

public class InstallPostgresOp extends Operation {

	private final static Nls nls = new Nls(InstallPostgresOp.class);

	private final static String LINE = System.lineSeparator();

	private final RootPasswordPageData rootPasswordPageData;

	private final         String postgresVersion;
	private final @NonNls String postgresPackageName;

	private final Map<String, PostgresState> installStates;

	public InstallPostgresOp(DatabasePageData databasePageData, RootPasswordPageData rootPasswordPageData) {
		super(nls.get("InstallPostgresOp.title"), nls.get("InstallPostgresOp.description"));

		this.rootPasswordPageData = rootPasswordPageData;
		this.postgresVersion = databasePageData.getSelectedPostgresVersion();
		this.postgresPackageName = "postgresql-" + postgresVersion;
		this.installStates = databasePageData.getPostgresInstallStates();
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
			case COMPLETELY -> {
				println(nls.get("InstallPostgresOp.println.PostgreSQL_is_already_installed"));
				installStates.put(postgresVersion, ALREADY_INSTALLED);
				handleExistingPostgres();
			}
			case NOT -> installPostgres();
		}
		setProgress(100);
		return nls.get("InstallPostgresOp.println.Installation_completed");
	}

	private Installed isPostgresInstalled() throws IOException, InterruptedException {
		String output = getOutputOf("dpkg-query", "-f=${db:Status-Abbrev}", "-W", postgresPackageName); //$NON-NLS
		return output.startsWith("ii") ? Installed.COMPLETELY : Installed.NOT; //$NON-NLS
	}

	private void handleExistingPostgres() throws Exception {
		setProgress(3);
		var choice = getChoice();
		setProgress(4);
		if (choice == REINSTALL) {
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

	private Choice getChoice() throws Exception {
		if (GraphicsEnvironment.isHeadless()) {
			println("PostgreSQL is already installed. Decide");
			println("1) Keep installation (default)");
			println("2) Reinstall");
			var console = System.console();
			for (int attempt = 1; attempt <= 3; attempt++) {
				switch (console.readLine("Enter your choice: ")) {
					case "1", "1)" -> {
						return KEEP;
					}
					case "2", "2)" -> {
						return REINSTALL;
					}
					default -> console.printf("Invalid choice. Failed attempts (%d/3).%n", attempt);
				}
			}
			IO.println("Maximum number of attempts exceeded. Defaulting to option 1.");
			return KEEP;
		} else {
			return (Choice) showConflictDialog().id();
		}
	}

	private Option showConflictDialog() throws Exception {
		var keepOption      = new Option(KEEP, 	    nls.get("InstallPostgresOp.button.Reuse_PostgreSQL"), 	  nls.get("InstallPostgresOp.tooltip.The_existing_installation_will_be_used"));
		var reinstallOption = new Option(REINSTALL, nls.get("InstallPostgresOp.button.Reinstall_PostgreSQL"), nls.get("InstallPostgresOp.tooltip.The_existing_installation_will_be_removed_and_reinstalled"));

		return showDialog(dialogTitle(), dialogDescription(), List.of(keepOption, reinstallOption), keepOption);
	}

	private static String dialogTitle() {
		return nls.get("InstallPostgresOp.title.PostgreSQL_is_already_installed");
	}

	private String dialogDescription() {
		return nls.get("InstallPostgresOp.description.{0}_is_already_installed_on_the_system", postgresPackageName);
	}

	private void installPostgres() throws IOException, InterruptedException {
		println(nls.get("InstallPostgresOp.println.Install_PostgreSQL_{0}", postgresVersion));
		setProgress(20);
		String[] command = {"sudo", "apt", "install", "-y", postgresPackageName}; //$NON-NLS
		println(nls.get("InstallPostgresOp.println.Execute_{0}", join(" ", command)));
		setProgress(40);
		int exitCode = execute(command).exitCode();
		setProgress(60);
		if (exitCode != 0) {
			println(nls.get("InstallPostgresOp.println.Error_installing_PostgreSQL"));
			return;
		}
		setProgress(80);
		println(nls.get("InstallPostgresOp.println.PostgreSQL_installed_successfully"));

		installStates.put(postgresVersion, INSTALLED_BY_INSTALLER);
	}

	private static final Pattern YES_NO_PROMPT = Pattern.compile("\\[\\D+/(\\D+)]");

	/// Implementation note:
	/// During purging of the Postgres package, an interactive prompt appears in the terminal
	/// asking whether the PostgreSQL database data should be removed as well.
	///
	/// An attempt was made to answer this prompt non-interactively using debconf-set-selections:
	/// ```bash
	/// sudo debconf-set-selections <<EOF
	/// postgresql-14 postgresql-14/postrm_purge_data boolean false
	/// postgresql-14 postgresql-14/postrm_purge_data seen true
	/// EOF
	/// ```
	/// However, the PostgreSQL package's postrm script explicitly resets
	/// postrm_purge_data to true immediately before the prompt is displayed.
	/// I suspect this to be a bug in the postrm script, as debconf is intended to provide configuration values for package scripts.
	///
	/// Therefore, an explicit "no" is written to the input stream instead.
	/// A localized "no" is extracted from the prompt via pattern matching.
	/// If, because of a different locale, the pattern does not match, the result might be the loss of the database data.
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
						writer.write(LINE);
						writer.flush();
					}
					var matcher = YES_NO_PROMPT.matcher(str);
					if (matcher.find()) {
						var no = matcher.group(1); // e.g. "nein", "no", "non", ...
						writer.write(no);
						writer.write(LINE);
						writer.flush();
					}

					print(str);
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
		NOT, COMPLETELY
	}
}
