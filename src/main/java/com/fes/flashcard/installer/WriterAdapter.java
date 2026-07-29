package com.fes.flashcard.installer;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;

public class WriterAdapter extends Writer {

	@Override
	public void write(char @NotNull [] cbuf, int off, int len) throws IOException {}

	@Override
	public void flush() throws IOException {}

	@Override
	public void close() throws IOException {}
}
