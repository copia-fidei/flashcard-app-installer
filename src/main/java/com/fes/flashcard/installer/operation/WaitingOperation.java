package com.fes.flashcard.installer.operation;

import static java.util.concurrent.TimeUnit.SECONDS;

public class WaitingOperation extends Operation {

	private final int secondsToWait;
	private final boolean throwExceptionAtHalftime;

	public WaitingOperation(String title) {
		this(title, 3);
	}

	public WaitingOperation(String title, int secondsToWait) {
		this(title, secondsToWait, false);
	}

	public WaitingOperation(String title, int secondsToWait, boolean throwExceptionAtHalftime) {
		super(title, "Wait for " + secondsToWait + " seconds");

		this.secondsToWait = secondsToWait;
		this.throwExceptionAtHalftime = throwExceptionAtHalftime;
	}

	@Override
	protected String doInBackground() throws Exception {
		for (int i = 1; i <= secondsToWait; i++) {
			if (isCancelled()) {
				return "Cancelled";
			}
			SECONDS.sleep(1);
			println("Waited for " + i + " seconds");
			int progress = i * 100 / secondsToWait;
			setProgress(progress);
			if (i > secondsToWait / 2 && throwExceptionAtHalftime) {
				throw new Exception("Error during execution");
			}

		}
		return "Done";
	}
}
