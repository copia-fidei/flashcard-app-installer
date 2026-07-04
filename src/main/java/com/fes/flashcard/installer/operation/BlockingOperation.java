package com.fes.flashcard.installer.operation;

import static java.util.concurrent.TimeUnit.SECONDS;

public class BlockingOperation extends Operation {

	public BlockingOperation(String title, String description) {
		super(title, description);
	}

	@Override
	protected String doInBackground() throws Exception {
		//noinspection InfiniteLoopStatement
		while(true) {
			SECONDS.sleep(1);
		}
	}
}
