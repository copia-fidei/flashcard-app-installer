package com.fes.flashcard.installer.app;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.walk;
import static java.util.stream.Collectors.toSet;

public interface Dir {

	/// Gets all files and directories below the specified directory.
	///
	/// **Notes:**
	/// - Directories have trailing slashes.
	/// - The starting directory itself is not included.
	/// - Returned paths are relative to the specified directory and do not start with a slash.
	///
	/// @param directory the parent directory, must exist
	/// @return all files and directories below the specified directory
	/// @throws IOException if an I/O error occurs while traversing the directory
	static Set<String> descendantsOf(Path directory) throws IOException {
		try (var descendants = walk(directory)) {
			return descendants.map(file -> {
				String path = directory.relativize(file).toString();
				if (isDirectory(file) && !path.isEmpty()) {
					path += "/";
				}
				return path;
			}).filter(path -> !path.isEmpty()).collect(toSet());
		}
	}
}
