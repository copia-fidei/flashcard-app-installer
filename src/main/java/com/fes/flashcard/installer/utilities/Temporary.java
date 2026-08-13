package com.fes.flashcard.installer.utilities;

import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import static java.nio.file.Files.deleteIfExists;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

@NonNls
public interface Temporary {

	Logger LOG = getLogger(Temporary.class.getName());

	static void use(Path file, ThrowingConsumer<Path> logic) throws Exception {
		try {
			logic.accept(file);
		} finally {
			try {
				deleteIfExists(file);
			} catch (IOException e) {
				LOG.log(WARNING, "Failed to delete temporary file: " + file, e);
			}
		}
	}
}
