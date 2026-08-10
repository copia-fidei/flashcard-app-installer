package com.fes.flashcard.installer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

public interface Temporary {

	interface ThrowingConsumer<T> {
		void accept(T t) throws Exception;
	}

	Logger LOG = getLogger(Temporary.class.getName());

	static void use(Path file, ThrowingConsumer<Path> logic) throws Exception {
		try {
			logic.accept(file);
		} finally {
			try {
				Files.deleteIfExists(file);
			} catch (IOException e) {
				LOG.log(Level.WARNING, "Failed to delete temporary file: " + file, e);
			}
		}
	}
}
