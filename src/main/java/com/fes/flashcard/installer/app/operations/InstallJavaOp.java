package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.Java;
import com.fes.flashcard.installer.app.pages.JavaPageData;
import com.fes.flashcard.installer.operation.Operation;

public class InstallJavaOp extends Operation {

	private final String debianPackage;

	public InstallJavaOp(JavaPageData javaPageData) {
		super("Java Installation", "Installiere OpenJDK 21");

		this.debianPackage = javaPageData.getJavaPackageName();
	}

	@Override
	public String getDescription() {
		return """
			Installiere OpenJDK 21
			sudo apt install %s
			""".formatted(debianPackage);
	}

	@Override
	protected String doInBackground() throws Exception {
		println("Installiere OpenJDK 21");
		setProgress(10);
		println("Führe aus:");
		println("sudo apt install " + debianPackage);
		setProgress(20);
		execute(new ProcessBuilder("sudo", "apt", "install", debianPackage).start()).throwIfNonZeroExit("Java-Installation fehlgeschlagen");
		setProgress(30);
		println("Ermittle Javas Installationspfad");
		setProgress(40);
		String JAVA_HOME = Java.getLocation();
		println("Java Installationspfad: " + JAVA_HOME);
		setProgress(50);
		println("Aktualisiere /etc/environment mit JAVA_HOME");
		setProgress(60);
		var replace_JAVA_HOME = new ProcessBuilder("sudo", "sed", "-i", "s|^JAVA_HOME=\"[^\"]*\"|JAVA_HOME=\"" + JAVA_HOME + "\"|", "/etc/environment").start();
		replace_JAVA_HOME.waitFor();
		setProgress(70);
		var does_JAVA_HOME_exist = new ProcessBuilder("sudo", "grep", "-q", "^JAVA_HOME=", "/etc/environment").start();
		boolean JAVA_HOME_exists = (does_JAVA_HOME_exist.waitFor() == 0);
		setProgress(80);
		if (!JAVA_HOME_exists) {
			new ProcessBuilder("sudo", "bash", "-c", "echo 'JAVA_HOME=\"" + JAVA_HOME + "\"' >> /etc/environment").start().waitFor();
		}
		// For some reason, the new JAVA_HOME environment variable is only at effect when the system is restarted
		// A user could override JAVA_HOME through other means, but there is no way to prevent every possible scenario.
		setProgress(100);
		return "Installation abgeschlossen";
	}
}
