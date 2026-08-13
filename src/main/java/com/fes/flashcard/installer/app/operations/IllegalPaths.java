package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.Home;
import com.fes.flashcard.installer.utilities.Nls;

import java.nio.file.Path;

/**
 * Checks whether the given path is dangerous to delete, e.g. the user's home directory.
 */
interface IllegalPaths {

	Nls nls = new Nls(IllegalPaths.class);

	static void check(Path path) throws Exception {
		if (!path.isAbsolute()) {
			throw new Exception(nls.get("IllegalPaths.exception.pathMustBeAbsolute", path.toAbsolutePath()));
		}
		if (path.equals(Home.PATH) || path.equals(Home.PATH.getParent())) {
			throw new Exception(nls.get("IllegalPaths.exception.pathIsNotAllowed", path));
		}
	}

	// For testing
	static void main() throws Exception {
//		check(Home.PATH);
		check(Path.of(""));
	}
}
