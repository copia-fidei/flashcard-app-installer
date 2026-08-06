package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.TextBuilder;
import com.fes.flashcard.installer.WriterAdapter;
import com.fes.flashcard.installer.app.pages.DatabasePageData;
import com.fes.flashcard.installer.app.pages.DatabasePageData.PostgresState;
import com.fes.flashcard.installer.app.pages.RootPasswordPageData;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.swing.DecisionDialog;
import com.fes.flashcard.installer.swing.DecisionDialog.Option;
import com.pty4j.PtyProcessBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstallPostgresOp extends Operation {

	private final DatabasePageData databasePageData;
	private final RootPasswordPageData rootPasswordPageData;

	private final String postgresVersion;
	private final String postgresPackageName;

	public InstallPostgresOp(DatabasePageData databasePageData, RootPasswordPageData rootPasswordPageData) {
		super("PostgreSQL", "Installiere PostgreSQL");

		this.databasePageData = databasePageData;
		this.rootPasswordPageData = rootPasswordPageData;
		this.postgresVersion = databasePageData.getSelectedPostgresVersion();
		this.postgresPackageName = "postgresql-" + postgresVersion;

	}

	public String getDescription() {
		var text = new TextBuilder();
		text.line("Prüfe, ob PostgreSQL " + postgresVersion + " bereits installiert ist");
		text.line("Falls bereits installiert: Frage Benutzer, ob neu installiert oder wiederverwendet werden soll");
		text.line("Bei Neuinstallation: Entferne vorhandene PostgreSQL Konfiguration");
		text.line("Installiere PostgreSQL " + postgresVersion + " über apt");
		return text.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		setProgress(0);
		publishLn("Prüfe ob PostgreSQL bereits installiert ist...");
		setProgress(2);
		switch (isPostgresInstalled()) {
			case FULLY -> {
				publishLn("PostgreSQL ist bereits installiert.");
				databasePageData.getPostgresInstallStates().put(postgresVersion, PostgresState.ALREADY_INSTALLED);
				handleExistingPostgres();
			}
			case NOT -> {
				publishLn("Installiere PostgreSQL...");
				installPostgres();
			}
		}
		setProgress(100);
		return "Installation durchgeführt";
	}

	private Installed isPostgresInstalled() throws IOException, InterruptedException {
		var process = new ProcessBuilder("dpkg-query", "-f=${db:Status-Abbrev}", "-W", postgresPackageName).start();
		process.waitFor();
		String status;
		try (var reader = process.inputReader()) {
			status = reader.readAllAsString();
		}
		return status.startsWith("ii") ? Installed.FULLY : Installed.NOT;
	}

	private void handleExistingPostgres() throws Exception {
		setProgress(3);
		Choice choice = (Choice) showConflictDialog().id();
		setProgress(4);
		if (choice == Choice.REINSTALL) {
			setProgress(5);
			publishLn("Installiere PostgreSQL neu...");
			purgePostgres();
			if (isCancelled()) {
				return;
			}
			installPostgres();
		} else {
			publishLn("Vorhandene PostgreSQL-Installation wird wiederverwendet.");
		}
		setProgress(99);
	}

	private DecisionDialog.Option showConflictDialog() throws Exception {
		var reuseOption     = new Option(Choice.REUSE, "PostgreSQL wiederverwenden", "Die vorhandene Installation wird verwendet");
		var reinstallOption = new Option(Choice.REINSTALL, "PostgreSQL neu installieren", "Die vorhandene Installation wird entfernt und neu installiert");

		return DecisionDialog.showDialog("PostgreSQL ist bereits installiert", getDlgDescription(), List.of(reuseOption, reinstallOption), reuseOption);
	}

	private String getDlgDescription() {
		return """
				%s ist bereits auf dem System installiert.
				
				Es kann entweder eine Neuinstallation durchgeführt oder die vorhandene Installation weiterverwendet werden (empfohlen).
				
				Achtung: Bei einer Neuinstallation wird die vorhandene PostgreSQL Konfiguration entfernt. Das Datenbank-Cluster hingegen bleibt intakt.
				""".formatted(postgresPackageName);
	}

	private void installPostgres() throws IOException, InterruptedException {
		publishLn("Installiere PostgreSQL " + postgresVersion);
		setProgress(20);
		List<String> command = List.of("sudo", "apt", "install", postgresPackageName);
		publishLn("Führe aus: " + String.join(" ", command));
		setProgress(30);
		var process = new ProcessBuilder(command).start();
		setProgress(40);
		int exitCode = redirectOutputs(process).exitCode();
		setProgress(50);
		if (exitCode != 0) {
			publishLn("Fehler beim Installieren von PostgreSQL");
			return;
		}
		setProgress(90);
		publishLn("PostgreSQL erfolgreich installiert.");

		databasePageData.getPostgresInstallStates().put(postgresVersion, PostgresState.INSTALLED_BY_INSTALLER);
	}

	private static final Pattern YES_NO_PROMPT = Pattern.compile("\\[\\D+/(\\D+)]");

	private void purgePostgres() throws IOException, InterruptedException {
		setProgress(10);
		publishLn("Entferne PostgreSQL Installation...");
		setProgress(15);

		var purgeB = new PtyProcessBuilder(new String[] {"sudo", "-S", "DEBIAN_FRONTEND=readline", "apt", "purge", "-y", postgresPackageName});
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
						publishLn("Cancelled process with id " + purge.pid()); // TODO localize
						return;
					}
					var str = new String(cbuf, off, len);

					if (str.toLowerCase().contains("[sudo]"))	{
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

		int purgeExitCode = purge.waitFor();
		setProgress(40);
		if (purgeExitCode != 0) {
			throw new IOException("apt purge fehlgeschlagen mit Exit-Code " + purgeExitCode);
		}
		setProgress(60);
		publishLn("PostgreSQL erfolgreich entfernt.");
		setProgress(70);
	}

	private enum Installed {
		NOT, FULLY
	}
}
