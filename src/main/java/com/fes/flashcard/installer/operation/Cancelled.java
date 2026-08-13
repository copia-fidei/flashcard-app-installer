package com.fes.flashcard.installer.operation;

import com.fes.flashcard.installer.utilities.Nls;

import java.io.IOException;

public class Cancelled extends IOException {

	private final static Nls nls = new Nls(Cancelled.class);

	public static Cancelled process(Process process, Throwable cause) {
		return new Cancelled(nls.get("Cancelled.exception.Cancelled_process_id", process.pid()), cause);
	}

	public static Cancelled process(Process process) {
		return new Cancelled(nls.get("Cancelled.exception.Cancelled_process_id", process.pid()));
	}

	public Cancelled(String message) {
		super(message);
	}

	public Cancelled(String message, Throwable cause) {
		super(message, cause);
	}

	public Cancelled() {
		super("Abgebrochen");
	}
}
