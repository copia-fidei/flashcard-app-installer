package com.fes.flashcard.installer;

import java.nio.file.Path;

import static java.lang.IO.println;
import static java.lang.System.getProperty;

/**
 * the home directory
 */
public interface Home {

	Path PATH = Path.of(getProperty("user.home"));

	static void main() {
		println(PATH);
	}
}
