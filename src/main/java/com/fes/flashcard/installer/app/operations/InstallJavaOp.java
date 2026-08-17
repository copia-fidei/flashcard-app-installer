package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.Java;
import com.fes.flashcard.installer.app.pages.JavaPageData;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.utilities.Nls;

import static java.lang.System.lineSeparator;

// TODO remove some NLS keys
public class InstallJavaOp extends Operation {

	private final static Nls nls = new Nls(InstallJavaOp.class);

	private final String debianPackage;

	public InstallJavaOp(JavaPageData javaPageData) {
		super(nls.get("InstallJavaOp.title"), nls.get("InstallJavaOp.description"));

		this.debianPackage = javaPageData.getJavaPackageName();
	}

	@Override
	public String getDescription() {
		return "sudo apt install " + debianPackage + lineSeparator(); //NON-NLS
	}

	@Override
	protected String doInBackground() throws Exception {
		setProgress(10);
		println("sudo apt install " + debianPackage); //NON-NLS
		setProgress(20);
		execute(new ProcessBuilder("sudo", "apt", "install", debianPackage).start()) //NON-NLS
				.throwIfNonZeroExit(nls.get("InstallJavaOp.println.Java_installation_failed"));
		setProgress(30);
		println(nls.get("InstallJavaOp.println.Find_out_where_Java_is_installed"));
		setProgress(40);
		String JAVA_HOME = Java.getLocation();
		println(nls.get("InstallJavaOp.println.Java_located_at_{0}", JAVA_HOME));
		setProgress(50);
		println(nls.get("InstallJavaOp.println.Update_JAVA_HOME_in_/etc/environment"));
		setProgress(60);
		var replace_JAVA_HOME = new ProcessBuilder("sudo", "sed", "-i", "s|^JAVA_HOME=\"[^\"]*\"|JAVA_HOME=\"" + JAVA_HOME + "\"|", "/etc/environment").start(); //NON-NLS
		replace_JAVA_HOME.waitFor();
		setProgress(70);
		var does_JAVA_HOME_exist = new ProcessBuilder("sudo", "grep", "-q", "^JAVA_HOME=", "/etc/environment").start(); //NON-NLS
		boolean JAVA_HOME_exists = (does_JAVA_HOME_exist.waitFor() == 0);
		setProgress(80);
		if (!JAVA_HOME_exists) {
			new ProcessBuilder("sudo", "bash", "-c", "echo 'JAVA_HOME=\"" + JAVA_HOME + "\"' >> /etc/environment").start().waitFor(); //NON-NLS
		}
		// For some reason, the new JAVA_HOME environment variable is only at effect when the system is restarted
		// A user could override JAVA_HOME through other means, but there is no way to prevent every possible scenario.
		setProgress(100);
		return nls.get("InstallJavaOp.println.Installation_completed");
	}
}