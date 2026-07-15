package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.app.pages.JavaPageData;
import com.fes.flashcard.installer.operation.Operation;

import java.io.IOException;

// TODO activate Java 21, after installation
public class InstallJavaOperation extends Operation {

	private final JavaPageData javaPageData;

	public InstallJavaOperation(JavaPageData javaPageData) {
		super("Java", "Install OpenJDK 21");

		this.javaPageData = javaPageData;
	}

	@Override
	protected String doInBackground() throws Exception {
		// TODO password might be asked in the commandline, how to cancel that?
		setProgress(1);
		publish("Installiere OpenJDK 21");
		setProgress(2);
		publish("Führe aus: sudo apt install " + javaPageData.getJavaPackageName());
		setProgress(3);
		var process = new ProcessBuilder(
				"sudo", "apt", "install", javaPageData.getJavaPackageName()
		).start();
		setProgress(4);

		var output = new CancellableWriter();
		try (var stdout = process.inputReader(); var stderr = process.errorReader()) {
			stdout.transferTo(output);
			stderr.transferTo(new CancellableWriter());
		}
		catch (IOException e) {
			if (!isCancelled()) {
				throw e;
			}
			long pid = process.pid();
			process.destroy();
			publish("Cancelled process with id " + pid);
		}
		setProgress(5);
		if (output.toString().isBlank()) {
			throw new IOException("apt command produced no output"); // TODO language
		}
		setProgress(6);

		setProgress(100);
		return "done";
	}
}
