package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.Java;
import com.fes.flashcard.installer.app.pages.JavaPageData;
import com.fes.flashcard.installer.operation.Operation;

// TODO progress
public class InstallJavaOp extends Operation {

	private final JavaPageData javaPageData;

	public InstallJavaOp(JavaPageData javaPageData) {
		super("Java", "Installiere OpenJDK 21");

		this.javaPageData = javaPageData;
	}

	@Override
	protected String doInBackground() throws Exception {
		setProgress(1);
		println("Installiere OpenJDK 21");
		setProgress(2);
		println("Führe aus: sudo apt install " + javaPageData.getJavaPackageName());
		setProgress(3);
		var installJava = new ProcessBuilder("sudo", "apt", "install", javaPageData.getJavaPackageName()).start();
		setProgress(4);
		execute(installJava).throwIfNonZeroExit("Java-Installation fehlgeschlagen");
		setProgress(5);
		// Find the actual Java installation path and export JAVA_HOME
		println("Ermittle Javas Installationspfad");
		setProgress(6);
		String JAVA_HOME = Java.getLocation();
		println("Java Installationspfad: " + JAVA_HOME);

		println("Aktualisiere /etc/environment mit JAVA_HOME");
		setProgress(7);
		var replace_JAVA_HOME = new ProcessBuilder("sudo", "sed", "-i", "s|^JAVA_HOME=\"[^\"]*\"|JAVA_HOME=\"" + JAVA_HOME + "\"|", "/etc/environment").start();
		replace_JAVA_HOME.waitFor();

		setProgress(8);

		var does_JAVA_HOME_exist = new ProcessBuilder("sudo", "grep", "-q", "^JAVA_HOME=", "/etc/environment").start();
		boolean JAVA_HOME_exists = (does_JAVA_HOME_exist.waitFor() == 0);

		setProgress(9);

		if (!JAVA_HOME_exists) {
			var addJavaHome = new ProcessBuilder("sudo", "bash", "-c", "echo 'JAVA_HOME=\"" + JAVA_HOME + "\"' >> /etc/environment").start();
			addJavaHome.waitFor();
		}
		// TODO investigate, who overrides JAVA_HOME?


		setProgress(100);
		return "Installation abgeschlossen";
	}
}
