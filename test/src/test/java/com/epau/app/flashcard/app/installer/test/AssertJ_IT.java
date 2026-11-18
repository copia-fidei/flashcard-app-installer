package com.epau.app.flashcard.app.installer.test;

import org.jetbrains.annotations.NonNls;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class AssertJ_IT {

	@Test
	void assertJ() throws IOException, InterruptedException {
		execute("vagrant", "destroy", "-f");
		execute("vagrant", "up");
		execute("vagrant", "provision", "--provision-with", "test-assertj");
		execute("vagrant", "provision", "--provision-with", "start-karaf");

		TestRequest.sendAndAssert();
	}

	private static void execute(@NonNls String... commands) throws IOException, InterruptedException {
		new ProcessBuilder(commands).inheritIO().start().waitFor();
	}
}
