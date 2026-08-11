package com.fes.flashcard.installer.operation;

import java.io.IOException;

// TODO localize
public class Cancelled extends IOException {

	public static Cancelled process(Process process, Throwable cause) {
		return new Cancelled("Cancelled process with id " + process.pid(), cause);
	}

	public static Cancelled process(Process purge) {
		return new Cancelled("Cancelled process with id " + purge.pid());
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
