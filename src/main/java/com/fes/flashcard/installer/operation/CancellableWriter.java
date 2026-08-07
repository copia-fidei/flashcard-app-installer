package com.fes.flashcard.installer.operation;

import java.io.IOException;
import java.io.Writer;

public class CancellableWriter extends Writer {

	private final Operation    operation;
	private final StringBuffer written = new StringBuffer();

	public CancellableWriter(Operation operation) {
		this.operation = operation;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		if (operation.isCancelled()) {
			throw new Cancelled();
		}
		var str = new String(cbuf, off, len);
		written.append(str);
		operation.print(str);
	}

	public String written() {
		return written.toString();
	}

	@Override
	public void flush() {}

	@Override
	public void close() {}

}
