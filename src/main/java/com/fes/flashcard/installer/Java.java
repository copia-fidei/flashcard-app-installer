package com.fes.flashcard.installer;

import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.util.List;

import static java.lang.IO.println;
import static java.lang.ProcessBuilder.Redirect.INHERIT;
import static java.lang.ProcessBuilder.Redirect.PIPE;
import static java.lang.ProcessBuilder.startPipeline;
import static java.lang.String.valueOf;
import static java.util.List.of;

@NonNls
public interface Java {

	static String getCurrentExecutable() throws InterruptedException, IOException {
		var findPath = new ProcessBuilder("readlink", "-f", "/usr/bin/java").start(); //$NON-NLS
		String path;
		try (var out = findPath.inputReader()) {
			path = out.readLine();
		}
		findPath.waitFor();
		return path;
	}

	static String getCurrentHome() throws InterruptedException, IOException {
		return getCurrentExecutable().replace("/bin/java", "");
	}

	private static String getHome(int version) throws IOException {
		return getExecutable(version).replace("/bin/java", "");
	}

	static int getCurrentVersion() throws IOException, InterruptedException {
		var getVersion = new ProcessBuilder(getCurrentExecutable(), "--version").start(); //NON-NLS
		int version;
		try (var out = getVersion.inputReader()) {
			version = Integer.parseInt(out.readLine().replaceFirst("^[^0-9]*([0-9]+).*", "$1")); //NON-NLS
		}
		getVersion.waitFor();
		return version;
	}

	static String getExecutable(int version) throws IOException {
		@NonNls List<Process> processes = startPipeline(of(
				new ProcessBuilder("update-alternatives", "--list", "java").inheritIO().redirectOutput(PIPE),
				new ProcessBuilder("grep", valueOf(version)).redirectError(INHERIT)
		));
		String path;
		try (var out = processes.getLast().inputReader()) {
			path = out.readLine();
		}
		return path;
	}

	// For testing
	static void main() throws IOException, InterruptedException {
		println(getCurrentHome());
		println(getCurrentExecutable());
		println(getCurrentVersion());
		println(getHome(21));
		println(getExecutable(21));
	}
}
