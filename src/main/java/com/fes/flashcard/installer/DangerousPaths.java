package com.fes.flashcard.installer;

import java.nio.file.Path;

/**
 * Checks whether the given path is dangerous to delete, e.g. the user's home directory.
 */
// TODO localize
public interface DangerousPaths {

	static void check(Path path) throws Exception {
		if (!path.isAbsolute()) {
			throw new Exception("Only absolute paths are allowed.");
		}
		if (path.equals(Home.PATH)) {
			throw new Exception(Home.PATH + " must not be used");
		}
		if (path.equals(Home.PATH.getParent())) {
			throw new Exception(Home.PATH.getParent() + " must not be used");
		}
	}
}
