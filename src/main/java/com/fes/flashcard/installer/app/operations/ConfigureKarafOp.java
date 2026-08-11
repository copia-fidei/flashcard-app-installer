package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.TextBuilder;
import com.fes.flashcard.installer.app.Karaf;
import com.fes.flashcard.installer.app.pages.KarafPageData;
import com.fes.flashcard.installer.operation.ErrorCode;
import com.fes.flashcard.installer.operation.Operation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.fes.flashcard.installer.app.Resources.FLASHCARDS_APP_BUNDLE;
import static com.fes.flashcard.installer.app.Resources.FLASHCARDS_APP_BUNDLE_NAME;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

// TODO localize
public class ConfigureKarafOp extends Operation {

	private final Path karafLocation;

	public ConfigureKarafOp(KarafPageData karafPageData) {
		super("Karaf Konfiguration", "Installiere Karaf Features");

		this.karafLocation = karafPageData.getKarafInstallationDir();
	}

	public String getDescription() {
		var text = new TextBuilder();
		text.line("Installiere Karaf Features");
		text.line("Aktiviere die Passwortanmeldung");
		text.line("Starte Karaf in " + karafLocation);
		text.line("Führe aus: ");
		text.line("feature:repo-add mvn:no.priv.bang.karaf/jersey/LATEST/xml/features");
		text.line("feature:install jersey-karaf-feature");
		text.line("feature:install war");
		text.line("feature:install http");
		text.line("feature:install jndi");
		text.line("feature:install pax-jdbc-postgresql");
		text.line("feature:install pax-jdbc-sqlite");
		text.line("bundle:install mvn:org.glassfish.jersey.media/jersey-media-multipart/2.47");
		text.line("bundle:install mvn:org.glassfish.jersey.ext/jersey-mvc/2.47");
		text.line("bundle:install mvn:org.glassfish.jersey.ext/jersey-mvc-mustache/2.47");
		text.line("bundle:install wrap:mvn:com.github.spullara.mustache.java/compiler/0.9.14");
		text.line("bundle:install mvn:org.jvnet.mimepull/mimepull/1.9.15");
		text.line("feature:install jdbc");
		text.line("Kopiere das flashcards.jar in Karafs deploy Ordner");
		text.line("Stoppe Karaf");
		return text.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		setProgress(1);

		progress(3);
		println("Aktiviere Passwortlogin für den Benutzer Karaf");
		activatePasswordLogin();
		progress(3);

		var karaf = new Karaf(karafLocation, this);
		try {
			println("Starte Karaf");
			progress(3);
			karaf.start();
			progress(3);

			execute(karaf, "feature:repo-add mvn:no.priv.bang.karaf/jersey/LATEST/xml/features");
			execute(karaf, "feature:install jersey-karaf-feature");
			execute(karaf, "feature:install war");
			execute(karaf, "feature:install http");
			execute(karaf, "feature:install jndi");
			execute(karaf, "feature:install pax-jdbc-postgresql");
			execute(karaf, "feature:install pax-jdbc-sqlite");
			execute(karaf, "bundle:install mvn:org.glassfish.jersey.media/jersey-media-multipart/2.47");
			execute(karaf, "bundle:install mvn:org.glassfish.jersey.ext/jersey-mvc/2.47");
			execute(karaf, "bundle:install mvn:org.glassfish.jersey.ext/jersey-mvc-mustache/2.47");
			execute(karaf, "bundle:install wrap:mvn:com.github.spullara.mustache.java/compiler/0.9.14");
			execute(karaf, "bundle:install mvn:org.jvnet.mimepull/mimepull/1.9.15");
			execute(karaf, "feature:install jdbc");

			println("Kopiere flashcards.jar in Karafs deploy Ordner");
			progress(3);
			deployBundle();
			progress(3);
		} finally {
			println("Stoppe Karaf");
			karaf.stop();
		}
		setProgress(100);

		return "Installation abgeschlossen";
	}

	private void execute(Karaf karaf, String line) throws IOException, InterruptedException, ErrorCode {
		println(line);
		progress(3);
		karaf.execute(line);
		progress(3);
	}

	// Deploy flashcards.jar in Karaf
	private void deployBundle() throws IOException {
		Path destination = karafLocation.resolve("deploy", FLASHCARDS_APP_BUNDLE_NAME);
		try (var bundle = FLASHCARDS_APP_BUNDLE.openStream()) {
			Files.copy(bundle, destination, REPLACE_EXISTING);
		}
	}

	private void activatePasswordLogin() throws IOException {
		Path usersFile = karafLocation.resolve("etc/users.properties");
		var  lines     = readAllLines(usersFile);
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("#karaf =") || line.startsWith("#_g_\\:admingroup")) {
				lines.set(i, line.substring(1));
			}
		}
		Files.write(usersFile, lines);
	}
}
