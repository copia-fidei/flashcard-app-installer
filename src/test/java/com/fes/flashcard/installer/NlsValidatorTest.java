package com.fes.flashcard.installer;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

// TODO make reusable
public class NlsValidatorTest {
	@Test
	void allNlsKeysExist() throws Exception {
		NlsValidator.validate(
				Path.of("src/main/java")
		);
	}
}
