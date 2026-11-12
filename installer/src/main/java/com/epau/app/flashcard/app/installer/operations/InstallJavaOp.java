package com.epau.app.flashcard.app.installer.operations;

import com.epau.app.flashcard.app.installer.Java;
import com.epau.app.flashcard.app.installer.Runner;
import com.epau.app.flashcard.app.installer.pages.JavaPageData;
import com.epau.util.nls.Nls;
import com.epau.util.swing.operation.Operation;

import static java.lang.System.lineSeparator;

public class InstallJavaOp extends Operation {

	private final static Nls nls = new Nls(InstallJavaOp.class);

	private final String debianPackage;

	public InstallJavaOp(JavaPageData javaPageData) {
		super(nls.get("InstallJavaOp.title"), nls.get("InstallJavaOp.description"));

		this.debianPackage = javaPageData.getJavaPackageName();
	}

	@Override
	public String description() {
		return "sudo apt install " + debianPackage + lineSeparator(); //NON-NLS
	}

	@Override
	protected String doInBackground() throws Exception {
		setProgress(10);
		println("sudo apt install " + debianPackage); //NON-NLS
		setProgress(20);
		execute("sudo", "apt", "install", "-y", debianPackage) //NON-NLS
				.throwIfNonZeroExit(nls.get("InstallJavaOp.println.Java_installation_failed"));
		setProgress(30);
		println(nls.get("InstallJavaOp.println.Find_out_where_Java_is_installed"));
		setProgress(40);

		String JAVA_HOME = Java.getCurrentHome();
		println(nls.get("InstallJavaOp.println.Java_located_at_{0}", JAVA_HOME));
		setProgress(50);

		int currentJavaVersion = Java.getCurrentVersion();
		int targetJavaVersion = JavaPageData.TARGET_JAVA_VERSION;
		setProgress(60);

		if (currentJavaVersion != targetJavaVersion) {
			println(nls.get("InstallJavaOp.println.Change_Java_version_from_{0}_to_{1}", currentJavaVersion, targetJavaVersion));

			execute("sudo", "update-alternatives", "--set", "java", Java.getExecutable(targetJavaVersion)); //NON-NLS

			JAVA_HOME = Java.getCurrentHome();
			println(nls.get("InstallJavaOp.Updated_JAVA_HOME_{0}", JAVA_HOME));
		}
		println(nls.get("InstallJavaOp.println.Update_JAVA_HOME_in_/etc/environment"));
		setProgress(70);

		// replace JAVA_HOME in /etc/environment
		Runner.execute("sudo", "sed", "-i", "s|^JAVA_HOME=\"[^\"]*\"|JAVA_HOME=\"" + JAVA_HOME + "\"|", "/etc/environment"); //NON-NLS
		setProgress(80);
		boolean JAVA_HOME_exists = (Runner.execute("sudo", "grep", "-q", "^JAVA_HOME=", "/etc/environment") == 0); //NON-NLS
		setProgress(90);
		if (!JAVA_HOME_exists) {
			new ProcessBuilder("sudo", "bash", "-c", "echo 'JAVA_HOME=\"" + JAVA_HOME + "\"' >> /etc/environment").start().waitFor(); //NON-NLS
		}
		// For some reason, the new JAVA_HOME environment variable is only at effect when the system is restarted
		// A user could override JAVA_HOME through other means, but there is no way to prevent every possible scenario.
		setProgress(100);
		return nls.get("InstallJavaOp.println.Installation_completed");
	}
}