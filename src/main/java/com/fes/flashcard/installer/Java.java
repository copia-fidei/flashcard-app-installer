package com.fes.flashcard.installer;

import org.jetbrains.annotations.NonNls;

import java.io.IOException;

@NonNls
public interface Java {

	static String getLocation() throws InterruptedException, IOException {
		var findPath = new ProcessBuilder("readlink", "-f", "/usr/bin/java").start(); //$NON-NLS
		String path;
		try (var stdout = findPath.inputReader()) {
			path = stdout.readLine();
		}
		findPath.waitFor();
		return path.replace("/bin/java", "");
	}
}
