package com.fes.flashcard.installer.operation;

import com.fes.flashcard.installer.utilities.Nls;

/**
 * A counter that maps the processing of a fixed number of items to a progress range between 0 and 100.
 */
public class Counter {

	private static final Nls nls = new Nls(Counter.class);

	private final int start; // inclusive
	private final int end;
	private final int maxProcessed;

	int processed = 0;

	public Counter(int start, int end, int maxProcessed) {
		if (start < 0 || start > 100) {
			throw new IllegalArgumentException(nls.get("Counter.exception.Counter_start_must_be_between_0_and_100"));
		}
		if (end < 0 || end > 100) {
			throw new IllegalArgumentException(nls.get("Counter.exception.Counter_end_must_be_between_0_and_100"));
		}
		if (start >= end) {
			throw new IllegalArgumentException(nls.get("Counter.exception.Counter_start_must_be_smaller_than_end"));
		}

		this.start = start;
		this.end = end;
		this.maxProcessed = maxProcessed;
	}

	public int up() {
		if (maxProcessed == 0) {
			return end;
		}

		int progress = start + processed * (end - start) / maxProcessed;
		processed++;
		return progress;
	}

	@Override
	public String toString() {
		return String.valueOf(processed);
	}
}
