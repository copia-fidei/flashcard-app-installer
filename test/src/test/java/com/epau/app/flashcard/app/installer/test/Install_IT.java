package com.epau.app.flashcard.app.installer.test;

import org.jetbrains.annotations.NonNls;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class Install_IT {

	@Test
	void install_default_config() throws IOException, InterruptedException {
		execute("vagrant", "provision", "--provision-with", "install-default-config");
		execute("vagrant", "provision", "--provision-with", "start-karaf");

		TestRequest.sendAndAssert();
	}

	@Test
	void install_saved_config() throws IOException, InterruptedException {
		execute("vagrant", "provision", "--provision-with", "copy-configuration");
		execute("vagrant", "provision", "--provision-with", "install-saved-config");
		execute("vagrant", "provision", "--provision-with", "start-karaf");

		TestRequest.sendAndAssert();
	}

	@BeforeEach
	void startup() throws IOException, InterruptedException {
		execute("vagrant", "destroy", "-f");
		execute("vagrant", "up");
	}


	private static void execute(@NonNls String... commands) throws IOException, InterruptedException {
		new ProcessBuilder(commands).inheritIO().start().waitFor();
	}
}
