package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.TextBuilder;
import com.fes.flashcard.installer.app.Karaf;
import com.fes.flashcard.installer.app.pages.KarafPageData;
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
		super("Karaf Features", "Installiere Karaf Features");

		this.karafLocation = karafPageData.getKarafInstallationDir();
	}

	public String getDescription() {
		var text = new TextBuilder();
		text.line("Installiere Karaf Features");
		text.line("Aktiviere die Passwortanmeldung");
		text.line("Starte Karaf in " + karafLocation);
		text.line("Warte bis Karaf vollständig gestartet ist");
		text.line("Füge das Jersey Feature Repository hinzu");
		text.line("Installiere das jersey-karaf-feature");
		text.line("Installiere das war Feature");
		text.line("Installiere das http Feature");
		text.line("Installiere das jndi Feature");
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
			println("Füge Jersey Feature Repository hinzu");
			progress(3);
			karaf.execute("feature:repo-add mvn:no.priv.bang.karaf/jersey/LATEST/xml/features");
			progress(3);
			println("Installiere jersey-karaf-feature");
			progress(3);
			karaf.execute("feature:install jersey-karaf-feature");
			progress(3);
			println("Installiere war Feature");
			progress(3);
			karaf.execute("feature:install war");
			progress(3);
			println("Installiere http Feature");
			progress(3);
			karaf.execute("feature:install http");
			progress(3);
			println("Installiere jndi Feature");
			progress(3);
			karaf.execute("feature:install jndi");
			progress(3);
			println("Kopiere flashcards.jar in Karafs deploy Ordner");
			progress(3);
			deployBundle();
			//bundle:install -s mvn:org.glassfish.jersey.media/jersey-media-multipart/2.47.0
			//bundle:install -s mvn:org.glassfish.jersey.ext/jersey-mvc/2.47.0
			//bundle:install -s mvn:org.glassfish.jersey.ext/jersey-mvc-mustache/2.47.0

			// TODO
			karaf.execute("feature:install pax-jdbc-sqlite");
			karaf.execute("bundle:install mvn:org.glassfish.jersey.media/jersey-media-multipart/2.47");
			karaf.execute("bundle:install mvn:org.glassfish.jersey.ext/jersey-mvc/2.47");
			karaf.execute("bundle:install mvn:org.glassfish.jersey.ext/jersey-mvc-mustache/2.47");
			karaf.execute("bundle:install wrap:mvn:com.github.spullara.mustache.java/compiler/0.9.14");
			karaf.execute("bundle:install mvn:org.glassfish.jersey.media/jersey-media-multipart/2.47");
			karaf.execute("bundle:install mvn:org.jvnet.mimepull/mimepull/1.9.15");
		} finally {
			println("Stoppe Karaf");
			karaf.stop();
		}
		setProgress(100);

		return "Installation abgeschlossen";
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
