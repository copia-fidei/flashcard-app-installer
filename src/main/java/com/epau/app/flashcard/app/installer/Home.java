package com.epau.app.flashcard.app.installer;

import java.nio.file.Path;

import static java.lang.IO.println;
import static java.lang.System.getProperty;

/// represents /home
public interface Home {

	Path PATH = Path.of(getProperty("user.home"));

	// Print the home directory
	static void main() {
		println(PATH);
	}
}
