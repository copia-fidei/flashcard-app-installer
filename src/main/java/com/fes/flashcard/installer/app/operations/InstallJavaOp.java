package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.Java;
import com.fes.flashcard.installer.app.pages.JavaPageData;
import com.fes.flashcard.installer.operation.Operation;

import java.io.IOException;

// TODO activate Java 21, after installation
public class InstallJavaOp extends Operation {

	private final JavaPageData javaPageData;

	public InstallJavaOp(JavaPageData javaPageData) {
		super("Java", "Installiere OpenJDK 21");

		this.javaPageData = javaPageData;
	}

	@Override
	protected String doInBackground() throws Exception {
		// TODO password might be asked in the commandline, how to cancel that?
		setProgress(1);
		publishLn("Installiere OpenJDK 21");
		setProgress(2);
		publishLn("Führe aus: sudo apt install " + javaPageData.getJavaPackageName());
		setProgress(3);
		var installJava = new ProcessBuilder("sudo", "apt", "install", javaPageData.getJavaPackageName()).start();
		setProgress(4);

		String output = redirectOutputs(installJava).output();
		if (output.isBlank()) {
			throw new IOException("apt command produced no output"); // TODO language
		}

		setProgress(5);

		setProgress(6);

		// Find the actual Java installation path and export JAVA_HOME
		publishLn("Ermittle Java Installationspfad");
		setProgress(7);
		String JAVA_HOME = Java.getLocation();
		publishLn("Java Installationspfad: " + JAVA_HOME);

		publishLn("Aktualisiere /etc/environment mit JAVA_HOME");
		setProgress(8);
		var replace_JAVA_HOME = new ProcessBuilder("sudo", "sed", "-i", "s|^JAVA_HOME=\"[^\"]*\"|JAVA_HOME=\"" + JAVA_HOME + "\"|", "/etc/environment").start();
		replace_JAVA_HOME.waitFor();

		setProgress(9);

		var does_JAVA_HOME_exist = new ProcessBuilder("sudo", "grep", "-q", "^JAVA_HOME=", "/etc/environment").start();
		boolean JAVA_HOME_exists = (does_JAVA_HOME_exist.waitFor() == 0);

		setProgress(10);

		if (!JAVA_HOME_exists) {
			var addJavaHome = new ProcessBuilder("sudo", "bash", "-c", "echo 'JAVA_HOME=\"" + JAVA_HOME + "\"' >> /etc/environment").start();
			addJavaHome.waitFor();
		}
		// TODO investigate, who overrides JAVA_HOME?


		setProgress(100);
		return "Installation abgeschlossen";
	}
}
