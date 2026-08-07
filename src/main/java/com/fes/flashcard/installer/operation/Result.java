package com.fes.flashcard.installer.operation;

/// the result of an operation
public class Result {

	private final CancellableWriter output;
	private final CancellableWriter error;
	private final int               exitCode;

	Result(CancellableWriter output, CancellableWriter error, int exitCode) {
		this.output = output;
		this.error = error;
		this.exitCode = exitCode;
	}

	public String output() {
		return output.written();
	}

	public String error() {
		return error.written();
	}

	public int exitCode() {
		return exitCode;
	}

	public void throwIfNonZeroExit(String error) throws ErrorCode {
		if (exitCode != 0) {
			throw new ErrorCode(exitCode, error);
		}
	}
}
