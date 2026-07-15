package com.fes.flashcard.installer.app.operations;

/**
 * A counter that maps the processing of a fixed number of items to a progress range.
 */
class Counter {

	private final int start; // inclusive
	private final int end;
	private final int maxProcessed;

	int processed = 0;

	Counter(int start, int end, int maxProcessed) {
		if (start < 0 || start > 100) {
			throw new IllegalArgumentException("start must be between 0 and 100");
		}
		if (end < 0 || end > 100) {
			throw new IllegalArgumentException("end must be between 0 and 100");
		}
		if (start >= end) {
			throw new IllegalArgumentException("start must be smaller than end");
		}

		this.start = start;
		this.end = end;
		this.maxProcessed = maxProcessed;
	}

	int up() {
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
