package com.epau.app.flashcard.app.installer;

import com.epau.installer.Java;
import com.epau.utilities.nls.Nls;
import com.epau.utilities.swing.operation.ErrorCode;
import com.epau.utilities.swing.operation.Operation;
import com.epau.utilities.swing.operation.Result;
import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

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

	public Karaf(Path toKaraf, Operation operation) throws IOException, InterruptedException {
		this.operation = operation;

		// for some reason the updated JAVA_HOME variable is not at effect until a system restart,
		// that is why we have to find out its toKaraf manually
		operation.println(nls.get("Karaf.Find_Javas_install_path"));
		JAVA_HOME = Java.getCurrentHome();
		operation.println(nls.get("Karaf.Java_is_installed_in_{0}", JAVA_HOME));

		clientProgram = toKaraf.resolve("bin/client").toString();
		startProgram  = toKaraf.resolve("bin/start").toString();
		stopProgram   = toKaraf.resolve("bin/stop").toString();
	}

	public void start() throws IOException, InterruptedException, ErrorCode {
		if (isRunning()) {
			operation.println(nls.get("Karaf.Karaf_is_already_running"));
			return;
		}
		__executeSuccessfully(startProgram);

		operation.println(nls.get("Karaf.Waiting_for_Karaf_to_start"));
		long timeout = System.currentTimeMillis() + 30_000; // 30 seconds
		while (System.currentTimeMillis() < timeout) {
			if (operation.isCancelled()) {
				throw new InterruptedException(nls.get("Karaf.Operation_cancelled_while_waiting_for_Karaf_to_start"));
			}
			if (isRunning()) {
				operation.println(nls.get("Karaf.Karaf_is_started"));
				return;
			}
			SECONDS.sleep(1);
		}
		throw new IOException(nls.get("Karaf.Timeout_while_waiting_for_Karaf_to_start"));
	}

	private boolean isRunning() throws InterruptedException {
		try {
			return operation.execute(getBuilder(new KarafCommand("version").arguments())).exitCode() == 0;
		} catch (IOException e) {

			// Karaf isn't accepting connections yet.
			return false;
		}
	}

	public void stop() throws IOException, InterruptedException, ErrorCode {
		__executeSuccessfully(stopProgram);

		// stopping actually takes time, the 5 seconds are just arbitrary and in no way robust
		SECONDS.sleep(5);
	}

	/// throws if the process returns a non-zero exit code
	public void executeSuccessfully(@NonNls String command) throws IOException, InterruptedException, ErrorCode {
		_executeSuccessfully(new KarafCommand(command));
	}

	public Result execute(@NonNls String command) throws IOException, InterruptedException, ErrorCode {
		return _execute(new KarafCommand(command));
	}

	private void _executeSuccessfully(KarafCommand command) throws IOException, InterruptedException, ErrorCode {
		__executeSuccessfully(command.arguments());
	}

	private void __executeSuccessfully(String... args) throws IOException, InterruptedException, ErrorCode {
		operation.execute(getBuilder(args)).throwIfNonZeroExit(nls.get("Karaf.Command_failed_{0}", String.join(" ", args)));
	}

	private Result _execute(KarafCommand command) throws IOException, InterruptedException {
		return operation.execute(getBuilder(command.arguments()));
	}

	private ProcessBuilder getBuilder(String... commands) {
		@NonNls var pb = new ProcessBuilder();
		pb.command(commands);
		pb.environment().put("JAVA_HOME", JAVA_HOME);
		return pb;
	}

	/// A command executed on Karaf, with user and password.
	@NonNls
	private class KarafCommand {

		public static final String USER     = "karaf";
		public static final String PASSWORD = "karaf";

		private final String[] command;

		private KarafCommand(String... command) {
			this.command = command;
		}

		private String[] arguments() {
			@NonNls var args = new ArrayList<String>();
			args.add(clientProgram);
			args.add("-u");
			args.add(USER);
			args.add("-p");
			args.add(PASSWORD);
			args.addAll(of(command));
			return args.toArray(String[]::new);
		}
	}
}
