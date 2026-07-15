package com.fes.flashcard.installer.app.operations;

import static java.lang.System.lineSeparator;

/**
 * Similar to a string builder, but each appended string is in a new line.
 */
record TextBuilder(StringBuilder builder) {

	TextBuilder() {
		this(new StringBuilder());
	}

	TextBuilder line(String s) {
		builder.append(s);
		builder.append(lineSeparator());
		return this;
	}

	@Override
	public String toString() {
		return builder.toString();
	}
}
