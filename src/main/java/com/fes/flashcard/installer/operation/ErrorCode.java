package com.fes.flashcard.installer.operation;

import com.fes.flashcard.installer.utilities.Nls;

/// represents a non-zero exit code of a process
public class ErrorCode extends Exception {

	private static final Nls nls = new Nls(ErrorCode.class);

	private int exitCode;

	public ErrorCode(int exitCode, String error) {
		super(nls.get("ErrorCode.exception.Exit_code_{0}_{1}", exitCode, error));
	}

	public int exitCode() {
		return exitCode;
	}
}
