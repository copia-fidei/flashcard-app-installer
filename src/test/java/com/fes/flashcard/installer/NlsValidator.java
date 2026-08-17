package com.fes.flashcard.installer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

// TODO
public final class NlsValidator {

	private NlsValidator() {
	}

	public static void validate(Path sourceRoot) throws IOException {
		List<String> errors = new ArrayList<>();

		try (Stream<Path> files = Files.walk(sourceRoot)) {
			files.filter(path -> path.toString().endsWith(".java")).forEach(path -> validateFile(path, errors));
		}

		if (!errors.isEmpty()) {
			throw new AssertionError("Missing NLS resources:\n\n" + String.join("\n", errors));
		}
	}

	private static void validateFile(Path sourceFile, List<String> errors) {
		try {
			CompilationUnit unit = StaticJavaParser.parse(sourceFile);

			unit.findAll(MethodCallExpr.class).stream().filter(NlsValidator::isNlsGet)
				.forEach(call -> validateCall(sourceFile, call, errors));

		} catch (Exception e) {
			errors.add(sourceFile + ": Could not parse source file: " + e.getMessage());
		}
	}

	private static boolean isNlsGet(MethodCallExpr call) {
		return call.getNameAsString().equals("get") && call.getScope().map(scope -> scope.toString().equals("nls"))
														   .orElse(false);
	}

	private static void validateCall(Path sourceFile, MethodCallExpr call, List<String> errors) {
		if (call.getArguments().isEmpty()) {
			return;
		}

		if (!(call.getArgument(0) instanceof StringLiteralExpr literal)) {
			// Dynamic key. We cannot verify it statically.
			return;
		}

		String key = literal.asString();

		if (!hasResource(sourceFile, key)) {
			errors.add(sourceFile + ":" + call.getBegin().map(p -> p.line).orElse(-1) + " - missing NLS key: " + key);
		}
	}

	private static boolean hasResource(Path sourceFile, String key) {
		Path packageDirectory = sourceFile.getParent();
		Path properties       = packageDirectory.resolve("messages.properties");

		if (!Files.exists(properties)) {
			return false;
		}

		try {
			Set<String> keys = loadKeys(properties);
			return keys.contains(key);
		} catch (IOException e) {
			throw new RuntimeException("Could not read " + properties, e);
		}
	}

	private static Set<String> loadKeys(Path properties) throws IOException {
		Set<String> keys = new HashSet<>();

		for (String line : Files.readAllLines(properties)) {
			line = line.trim();

			if (line.isEmpty() || line.startsWith("#") || line.startsWith("!")) {
				continue;
			}

			int separator = findSeparator(line);

			if (separator > 0) {
				keys.add(line.substring(0, separator).trim());
			}
		}

		return keys;
	}

	private static int findSeparator(String line) {
		boolean escaped = false;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);

			if (escaped) {
				escaped = false;
				continue;
			}

			if (c == '\\') {
				escaped = true;
				continue;
			}

			if (c == '=' || c == ':' || Character.isWhitespace(c)) {
				return i;
			}
		}

		return -1;
	}
}