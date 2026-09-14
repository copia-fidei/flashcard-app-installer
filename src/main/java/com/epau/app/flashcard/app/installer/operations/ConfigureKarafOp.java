package com.epau.app.flashcard.app.installer.operations;

import com.epau.app.flashcard.app.installer.Karaf;
import com.epau.app.flashcard.app.installer.pages.KarafPageData;
import com.epau.util.nls.Nls;
import com.epau.util.swing.operation.ErrorCode;
import com.epau.util.swing.operation.Operation;
import com.epau.util.text.MultilineText;
import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.epau.app.flashcard.app.installer.Resources.FLASHCARDS_APP_BUNDLE;
import static com.epau.app.flashcard.app.installer.Resources.FLASHCARD_APP_BUNDLE_NAME;
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
		var text = new MultilineText();
		text.add(nls.get("ConfigureKarafOp.description.Install_Karaf_Features"));
		text.add(nls.get("ConfigureKarafOp.description.Activate_password_prompt"));
		text.add(nls.get("ConfigureKarafOp.description.Start_Karaf_in_{0}", karafLocation));
		text.add(nls.get("ConfigureKarafOp.description.Execute"));
		text.add("feature:repo-add mvn:no.priv.bang.karaf/jersey/LATEST/xml/features"); //NON-NLS
		text.add("feature:install jersey-karaf-feature"); //NON-NLS
		text.add("feature:install war"); //NON-NLS
		text.add("feature:install http"); //NON-NLS
		text.add("feature:install jndi"); //NON-NLS
		text.add("feature:install pax-jdbc-postgresql"); //NON-NLS
		text.add("feature:install pax-jdbc-sqlite"); //NON-NLS
		text.add("bundle:install mvn:org.glassfish.jersey.media/jersey-media-multipart/2.47"); //NON-NLS
		text.add("bundle:install mvn:org.glassfish.jersey.ext/jersey-mvc/2.47"); //NON-NLS
		text.add("bundle:install mvn:org.glassfish.jersey.ext/jersey-mvc-mustache/2.47"); //NON-NLS
		text.add("bundle:install wrap:mvn:com.github.spullara.mustache.java/compiler/0.9.14"); //NON-NLS
		text.add("bundle:install mvn:org.jvnet.mimepull/mimepull/1.9.15"); //NON-NLS
		text.add("feature:install jdbc"); //NON-NLS
		text.add(nls.get("ConfigureKarafOp.println.Copy_Flashcards_JAR_to_Karaf_deploy_folder"));
		text.add(nls.get("ConfigureKarafOp.println.Stop_Karaf"));
		return text.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		setProgress(1);

		progress(3);
		println(nls.get("ConfigureKarafOp.println.Activate_password_login_for_Karaf_user"));
		activatePasswordLogin();
		progress(3);

		var karaf = new Karaf(karafLocation, this);
		try {
			println(nls.get("ConfigureKarafOp.println.Start_Karaf"));
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

			println(nls.get("ConfigureKarafOp.println.Copy_Flashcards_JAR_to_Karaf_deploy_folder"));
			progress(3);
			deployBundle();
			progress(3);
		} finally {
			println(nls.get("ConfigureKarafOp.println.Stop_Karaf"));
			karaf.stop();
		}
		setProgress(100);

		return nls.get("ConfigureKarafOp.println.Installation_completed");
	}

	private void execute(Karaf karaf, @NonNls String line) throws IOException, InterruptedException, ErrorCode {
		println(line);
		progress(3);
		karaf.execute(line);
		progress(3);
	}

	// Deploy flashcards.jar in Karaf
	private void deployBundle() throws IOException {
		Path destination = karafLocation.resolve("deploy", FLASHCARD_APP_BUNDLE_NAME);  //NON-NLS
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
