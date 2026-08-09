package com.fes.flashcard.installer.app;

import com.fes.flashcard.installer.Java;
import com.fes.flashcard.installer.operation.ErrorCode;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.operation.Result;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.List.of;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Karaf {

	private final Operation operation;

	private final String JAVA_HOME;

	/**
	 * the path to the Karaf client program where you can send commands to the running Karaf server
	 */
	private final String clientProgram;
	private final String startProgram;
	private final String stopProgram;


	public Karaf(Path location, Operation operation) throws IOException, InterruptedException {
		this.operation = operation;

		// for some reason the updated JAVA_HOME variable is not at effect until a system restart,
		// that is why we have to find out its location manually
		operation.println("Finde Javas Installationsort heraus");
		JAVA_HOME = Java.getLocation();
		operation.println("Java ist installiert in " + JAVA_HOME);

		clientProgram = location.resolve("bin/client").toString();
		startProgram = location.resolve("bin/start").toString();
		stopProgram = location.resolve("bin/stop").toString();

	}

	public void start() throws IOException, InterruptedException, ErrorCode {
		// Check if Karaf is already running
		if (isRunning()) {
			operation.println("Karaf läuft bereits.");
			return;
		}
		runSuccessfully(startProgram);

		operation.println("Warte bis Karaf gestartet ist...");
		long timeout = System.currentTimeMillis() + 30_000; // 30 seconds
		while (System.currentTimeMillis() < timeout) {
			if (operation.isCancelled()) {
				throw new InterruptedException("Operation beim Warten auf den Start von Karaf abgebrochen.");
			}
			if (isRunning()) {
				operation.println("Karaf ist gestartet.");
				return;
			}
			SECONDS.sleep(1);
		}
		throw new IOException("Timeout beim Warten auf den Start von Karaf.");
	}

	private boolean isRunning() throws IOException, InterruptedException {
		try {
			var process = getBuilder(onKaraf("version")).start();
			return process.waitFor() == 0;
		} catch (IOException _) {
			// Karaf isn't accepting connections yet.
			return false;
		}
	}

	public void stop() throws IOException, InterruptedException, ErrorCode {
		runSuccessfully(stopProgram);

		SECONDS.sleep(1); // stopping actually takes time
	}

	/// throws if the process returns a non-zero exit code
	public void executeSuccessfully(String command) throws IOException, InterruptedException, ErrorCode {
		runSuccessfully(onKaraf(command));
	}

	public Result execute(String command) throws IOException, InterruptedException, ErrorCode {
		return run(onKaraf(command));
	}

	private String[] onKaraf(String... command) {
		List<String> args = new ArrayList<>();
		args.add(clientProgram);
		args.add("-u");
		args.add("karaf");
		args.add("-p");
		args.add("karaf");
		args.addAll(of(command));
		return args.toArray(String[]::new);
	}

	private void runSuccessfully(String... commands) throws IOException, InterruptedException, ErrorCode {
		operation.execute(getBuilder(commands).start())
		         .throwIfNonZeroExit("Befehl fehlgeschlagen: " + String.join(" ", commands));
	}

	private Result run(String... commands) throws IOException, InterruptedException {
		return operation.execute(getBuilder(commands).start());
	}

	private ProcessBuilder getBuilder(String... commands) {
		var pb = new ProcessBuilder();
		pb.command(commands);
		pb.environment().put("JAVA_HOME", JAVA_HOME);
		return pb;
	}
}
