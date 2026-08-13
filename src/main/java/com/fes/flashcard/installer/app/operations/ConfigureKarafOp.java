package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.app.Karaf;
import com.fes.flashcard.installer.app.pages.KarafPageData;
import com.fes.flashcard.installer.operation.ErrorCode;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.utilities.Nls;
import com.fes.flashcard.installer.utilities.TextBuilder;
import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.fes.flashcard.installer.app.Resources.FLASHCARDS_APP_BUNDLE;
import static com.fes.flashcard.installer.app.Resources.FLASHCARDS_APP_BUNDLE_NAME;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ConfigureKarafOp extends Operation {

	private static final Nls nls = new Nls(ConfigureKarafOp.class);

	private final Path karafLocation;

	public ConfigureKarafOp(KarafPageData karafPageData) {
		super(nls.get("ConfigureKarafOp.title"), nls.get("ConfigureKarafOp.description"));

		this.karafLocation = karafPageData.getKarafInstallationDir();
	}

	public String getDescription() {
		var text = new TextBuilder();
		text.line(nls.get("ConfigureKarafOp.description.installKarafFeatures"));
		text.line(nls.get("ConfigureKarafOp.description.activatePasswordPrompt"));
		text.line(nls.get("ConfigureKarafOp.description.startKarafIn", karafLocation));
		text.line(nls.get("ConfigureKarafOp.description.execute"));
		text.line("feature:repo-add mvn:no.priv.bang.karaf/jersey/LATEST/xml/features"); //NON-NLS
		text.line("feature:install jersey-karaf-feature"); //NON-NLS
		text.line("feature:install war"); //NON-NLS
		text.line("feature:install http"); //NON-NLS
		text.line("feature:install jndi"); //NON-NLS
		text.line("feature:install pax-jdbc-postgresql"); //NON-NLS
		text.line("feature:install pax-jdbc-sqlite"); //NON-NLS
		text.line("bundle:install mvn:org.glassfish.jersey.media/jersey-media-multipart/2.47"); //NON-NLS
		text.line("bundle:install mvn:org.glassfish.jersey.ext/jersey-mvc/2.47"); //NON-NLS
		text.line("bundle:install mvn:org.glassfish.jersey.ext/jersey-mvc-mustache/2.47"); //NON-NLS
		text.line("bundle:install wrap:mvn:com.github.spullara.mustache.java/compiler/0.9.14"); //NON-NLS
		text.line("bundle:install mvn:org.jvnet.mimepull/mimepull/1.9.15"); //NON-NLS
		text.line("feature:install jdbc"); //NON-NLS
		text.line("Kopiere das flashcards.jar in Karafs deploy Ordner");
		text.line("Stoppe Karaf");
		return text.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		setProgress(1);

		progress(3);
		println(nls.get("ConfigureKarafOp.println.activatePasswordLoginForKarafUser"));
		activatePasswordLogin();
		progress(3);

		var karaf = new Karaf(karafLocation, this);
		try {
			println(nls.get("ConfigureKarafOp.println.startKaraf"));
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

			println(nls.get("ConfigureKarafOp.println.copyFlashcardsJarToKarafDeployFolder"));
			progress(3);
			deployBundle();
			progress(3);
		} finally {
			println(nls.get("ConfigureKarafOp.println.stopKaraf"));
			karaf.stop();
		}
		setProgress(100);

		return nls.get("ConfigureKarafOp.println.installationCompleted");
	}

	private void execute(Karaf karaf, @NonNls String line) throws IOException, InterruptedException, ErrorCode {
		println(line);
		progress(3);
		karaf.execute(line);
		progress(3);
	}

	// Deploy flashcards.jar in Karaf
	private void deployBundle() throws IOException {
		Path destination = karafLocation.resolve("deploy", FLASHCARDS_APP_BUNDLE_NAME);  //NON-NLS
		try (var bundle = FLASHCARDS_APP_BUNDLE.openStream()) {
			Files.copy(bundle, destination, REPLACE_EXISTING);
		}
	}

	private void activatePasswordLogin() throws IOException {
		Path usersFile = karafLocation.resolve("etc/users.properties");
		var  lines     = readAllLines(usersFile);
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("#karaf =") || line.startsWith("#_g_\\:admingroup")) {  //NON-NLS
				lines.set(i, line.substring(1));
			}
		}
		Files.write(usersFile, lines);
	}
}
