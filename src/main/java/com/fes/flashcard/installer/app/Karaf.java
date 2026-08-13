package com.fes.flashcard.installer.app;

import com.fes.flashcard.installer.Java;
import com.fes.flashcard.installer.operation.ErrorCode;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.operation.Result;
import com.fes.flashcard.installer.utilities.Nls;
import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.List.of;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Karaf {

	private static final Nls nls = new Nls(Karaf.class);

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
		operation.println(nls.get("Karaf.findJavaLocation"));
		JAVA_HOME = Java.getLocation();
		operation.println(nls.get("Karaf.javaInstalledIn", JAVA_HOME));

		clientProgram = location.resolve("bin/client").toString();
		startProgram = location.resolve("bin/start").toString();
		stopProgram = location.resolve("bin/stop").toString();

	}

	public void start() throws IOException, InterruptedException, ErrorCode {
		// Check if Karaf is already running
		if (isRunning()) {
			operation.println(nls.get("Karaf.alreadyRunning"));
			return;
		}
		runSuccessfully(startProgram);

		operation.println(nls.get("Karaf.waitingForStart"));
		long timeout = System.currentTimeMillis() + 30_000; // 30 seconds
		while (System.currentTimeMillis() < timeout) {
			if (operation.isCancelled()) {
				throw new InterruptedException(nls.get("Karaf.operationCancelled"));
			}
			if (isRunning()) {
				operation.println(nls.get("Karaf.started"));
				return;
			}
			SECONDS.sleep(1);
		}
		throw new IOException(nls.get("Karaf.timeoutWaitingForStart"));
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

		// stopping actually takes time, the 3 seconds is just arbitrary and in no way robust
		SECONDS.sleep(3);
	}

	/// throws if the process returns a non-zero exit code
	public void executeSuccessfully(@NonNls String command) throws IOException, InterruptedException, ErrorCode {
		runSuccessfully(onKaraf(command));
	}

	public Result execute(@NonNls String command) throws IOException, InterruptedException, ErrorCode {
		return run(onKaraf(command));
	}


	private String[] onKaraf(String... command) {
		@NonNls List<String> args = new ArrayList<>();
		args.add(clientProgram);
		args.add("-u");
		args.add("karaf");
		args.add("-p");
		args.add("karaf");
		args.addAll(of(command));
		return args.toArray(String[]::new);
	}

	private void runSuccessfully(String... commands) throws IOException, InterruptedException, ErrorCode {
		operation.execute(getBuilder(commands).start()).throwIfNonZeroExit(nls.get("Karaf.commandFailed", String.join(" ", commands)));
	}

	private Result run(String... commands) throws IOException, InterruptedException {
		return operation.execute(getBuilder(commands).start());
	}

	private ProcessBuilder getBuilder(String... commands) {
		@NonNls var pb = new ProcessBuilder();
		pb.command(commands);
		pb.environment().put("JAVA_HOME", JAVA_HOME);
		return pb;
	}
}
