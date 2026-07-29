package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.Java;
import com.fes.flashcard.installer.TextBuilder;
import com.fes.flashcard.installer.app.Resources;
import com.fes.flashcard.installer.app.pages.KarafPageData;
import com.fes.flashcard.installer.operation.Operation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Stream.concat;

public class InstallKarafFeaturesOp extends Operation {

	private final KarafPageData karafPageData;
	private final Path          karafDirectory;
	// the path to the karaf client script
	private final String        karafClient;
	private       String        JAVA_HOME;


	public InstallKarafFeaturesOp(KarafPageData karafPageData) {
		super("Karaf Features", "Installiere Karaf Features");

		this.karafPageData = karafPageData;
		this.karafDirectory = karafPageData.getKarafInstallationDir();
		this.karafClient = karafDirectory.resolve("bin/client").toString();
	}

	public String getDescription() {
		var text = new TextBuilder();
		text.line("Installiere Karaf Features");
		text.line("Ermittle Java Installationspfad");
		text.line("Konfiguriere Karaf Benutzer für Passwort-Login");
		text.line("Starte Karaf in " + karafDirectory);
		text.line("Warte bis Karaf vollständig gestartet ist");
		text.line("Füge Jersey Feature Repository hinzu");
		text.line("Installiere jersey-karaf-feature");
		text.line("Installiere war Feature");
		text.line("Installiere http Feature");
		text.line("Installiere jndi Feature");
		text.line("Kopiere flashcards.jar in Karafs deploy Ordner");
		text.line("Stoppe Karaf");
		return text.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		setProgress(1);
		publishLn("Ermittle Java Installationspfad");
		JAVA_HOME = Java.getLocation();
		publishLn("Java ist installiert in " + JAVA_HOME);
		setProgress(2);
		activatePasswordLogin();
		setProgress(3);
		try {
			publishLn("Starte Karaf");
			setProgress(4);
			startKaraf();
			setProgress(5);
			waitForKaraf();
			setProgress(6);
			publishLn("Füge Jersey Feature Repository hinzu");
			setProgress(7);
			executeKarafCommand("feature:repo-add mvn:no.priv.bang.karaf/jersey/LATEST/xml/features");
			setProgress(8);
			publishLn("Installiere jersey-karaf-feature");
			setProgress(9);
			executeKarafCommand("feature:install jersey-karaf-feature");
			setProgress(10);
			publishLn("Installiere war Feature");
			setProgress(11);
			executeKarafCommand("feature:install war");
			setProgress(12);
			publishLn("Installiere http Feature");
			setProgress(13);
			executeKarafCommand("feature:install http");
			setProgress(14);
			publishLn("Installiere jndi Feature");
			setProgress(15);
			executeKarafCommand("feature:install jndi");
			setProgress(16);
			publishLn("Kopiere flashcards.jar in Karafs deploy Ordner");
			setProgress(17);
			deployFlashcardsBundle();
			setProgress(100);
		} finally {
			publishLn("Stoppe Karaf");
			stopKaraf();
		}
		return "Installation abgeschlossen";
	}

	private void stopKaraf() throws IOException, InterruptedException {
		executeCommand(karafDirectory.resolve("bin/stop").toString());
	}

	private void startKaraf() throws IOException, InterruptedException {
		executeCommand(karafDirectory.resolve("bin/start").toString());
	}

	// Deploy flashcards.jar in Karaf
	private void deployFlashcardsBundle() throws IOException {
		Path destination = karafDirectory.resolve("deploy", Resources.FLASHCARDS_APP_BUNDLE_NAME);
		try (var stream = Resources.FLASHCARDS_APP_BUNDLE.openStream()) {
			Files.copy(stream, destination, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private void activatePasswordLogin() throws IOException {
		Path usersFile = karafDirectory.resolve("etc/users.properties");
		publishLn("Konfiguriere Karaf Benutzer");
		List<String> lines = Files.readAllLines(usersFile);
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("#karaf =") || line.startsWith("#_g_\\:admingroup")) {
				lines.set(i, line.substring(1));
			}
		}
		Files.write(usersFile, lines);
	}

	private void executeKarafCommand(String command) throws IOException, InterruptedException {
		executeCommand(karafClientCommand(command));
	}

	private String[] karafClientCommand(String... command) {
		return concat(Stream.of(karafClient, "-u", "karaf", "-p", "karaf"), Arrays.stream(command)).toArray(String[]::new);
	}

	private void executeCommand(String... commands) throws IOException, InterruptedException {
		var process  = startProcess(commands);
		int exitCode = redirectOutputs(process).exitCode();
		if (exitCode != 0) {
			throw new IOException("Karaf command failed with exit code " + exitCode);
		}
	}

	private void waitForKaraf() throws Exception {
		publishLn("Warte bis Karaf gestartet ist...");

		long timeout = System.currentTimeMillis() + 60_000; // 1 minute
		while (System.currentTimeMillis() < timeout) {
			try {
				var process = startProcess(karafClientCommand("version"));
				if (process.waitFor() == 0) {
					publishLn("Karaf ist gestartet.");
					return;
				}
			} catch (IOException _) {
				// Karaf isn't accepting connections yet.
			}
			SECONDS.sleep(1);
		}
		throw new IOException("Timeout beim Warten auf den Start von Karaf.");
	}

	private Process startProcess(String... commands) throws IOException {
		var pb = new ProcessBuilder(commands);
		pb.environment().put("JAVA_HOME", JAVA_HOME);
		return pb.start();
	}
}
