package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.Java;
import com.fes.flashcard.installer.app.pages.JavaPageData;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.utilities.Nls;

public class InstallJavaOp extends Operation {

	private final static Nls nls = new Nls(InstallJavaOp.class);

	private final String debianPackage;

	public InstallJavaOp(JavaPageData javaPageData) {
		super(nls.get("InstallJavaOp.title"), nls.get("InstallJavaOp.description"));

		this.debianPackage = javaPageData.getJavaPackageName();
	}

	@Override
	public String getDescription() {
		return nls.get("InstallJavaOp.description.installOpenJDK21") + "\n" +
		       nls.get("InstallJavaOp.description.sudoAptInstall", debianPackage);
	}

	@Override
	protected String doInBackground() throws Exception {
		println(nls.get("InstallJavaOp.println.installOpenJDK21"));
		setProgress(10);
		println(nls.get("InstallJavaOp.println.execute"));
		println(nls.get("InstallJavaOp.println.sudoAptInstall", debianPackage));
		setProgress(20);
		execute(new ProcessBuilder("sudo", "apt", "install", debianPackage).start()) //NON-NLS
				.throwIfNonZeroExit(nls.get("InstallJavaOp.println.javaInstallationFailed"));
		setProgress(30);
		println(nls.get("InstallJavaOp.println.determineJavaInstallationPath"));
		setProgress(40);
		String JAVA_HOME = Java.getLocation();
		println(nls.get("InstallJavaOp.println.javaInstallationPath", JAVA_HOME));
		setProgress(50);
		println(nls.get("InstallJavaOp.println.updateEtcEnvironmentWithJavaHome"));
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
		return nls.get("InstallJavaOp.println.installationCompleted");
	}
}