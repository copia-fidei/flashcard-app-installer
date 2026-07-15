package com.fes.flashcard.installer.app;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

/**
 * Lists contents of directories in a tree-like format.
 *
 * Needs the 'tar' and 'tree' commands to be installed.
 */
public interface Tree {

	Logger LOG = getLogger(Tree.class.getName());

	static void printTarGzFile(Path tarGzFile) throws InterruptedException, IOException {
		List<Process> processes = null;
		try {
			processes = ProcessBuilder.startPipeline(List.of(
					new ProcessBuilder("tar", "-tzf", tarGzFile.toAbsolutePath().toString()).inheritIO().redirectOutput(ProcessBuilder.Redirect.PIPE),
					new ProcessBuilder("tree", "--fromfile", ".").redirectOutput(ProcessBuilder.Redirect.INHERIT))
			);
		} catch (IOException e) {
			LOG.log(WARNING, "Failed to start process", e);
			throw e;
		}
		try {
			processes.getLast().waitFor();
		} catch (InterruptedException e) {
			LOG.log(WARNING, "Interrupted", e);
			throw e;
		}
	}

	static void printDirectory(Path dir) throws InterruptedException, IOException {
		ProcessBuilder builder = new ProcessBuilder("tree", dir.toAbsolutePath().toString()).redirectOutput(Redirect.INHERIT);
		Process        process = null;
		try {
			process = builder.start();
		} catch (IOException e) {
			LOG.log(WARNING, "Failed to execute " + builder.command(), e);
			throw e;
		}
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			LOG.log(WARNING, "Interrupted", e);
			throw e;
		}
	}


	static void main() throws InterruptedException, IOException, URISyntaxException {
		printTarGzFile(Path.of(Resources.KARAF.toURI()));
		//		printDirectory(new KarafPageData(null).getKarafInstallationDir());
	}
}
