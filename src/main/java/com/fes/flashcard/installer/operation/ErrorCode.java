package com.fes.flashcard.installer.operation;

/// represents a non-zero exit code of a process
public class ErrorCode extends Exception {

	private int exitCode;

	ErrorCode(int exitCode, String error) {
		super("Exit-Code " + exitCode + ": " + error);
	}

	public int exitCode() {
		return exitCode;
	}
}
