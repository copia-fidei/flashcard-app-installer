package com.epau.app.flashcard.app.installer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Utility methods for java.lang.Process.
public interface Runner {

	static int execute(String... command) throws InterruptedException, IOException {
		return new ProcessBuilder(command).start().waitFor();
	}

	static String getOutputOf(ProcessBuilder pb) throws InterruptedException, IOException {
		Process process = pb.start();
		process.waitFor();
		String output;
		try (var reader = process.inputReader()) {
			output = reader.readAllAsString();
		}
		return output;
	}

	static String getOutputOf(String... command) throws InterruptedException, IOException {
		return getOutputOf(new ProcessBuilder(command));
	}

	static String getFirstLineOf(String... command) throws InterruptedException, IOException {
		var process = new ProcessBuilder(command).start(); //$NON-NLS
		process.waitFor();
		try (var out = process.inputReader()) {
			return out.readLine();
		}
	}

	static List<String> getLines(String... command) throws IOException, InterruptedException {
		var process = new ProcessBuilder(command).start();
		process.waitFor();
		try (var stdout = process.inputReader()) {
			return new ArrayList<>(stdout.readAllLines());
		}
	}
}