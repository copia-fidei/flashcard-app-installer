package com.fes.flashcard.installer;

import java.io.IOException;

public interface Java {

	static String getLocation() throws InterruptedException, IOException {
		var findPath = new ProcessBuilder(
				"readlink", "-f", "/usr/bin/java"
		).start();

		String path;
		try (var stdout = findPath.inputReader()) {
			path = stdout.readLine();
		}
		findPath.waitFor();
		return path.replace("/bin/java", "");
	}
}
