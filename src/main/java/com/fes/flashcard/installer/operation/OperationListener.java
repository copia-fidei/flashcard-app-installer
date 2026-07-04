package com.fes.flashcard.installer.operation;

import java.util.List;

/**
 *
 * All methods are called in the event dispatch thread.
 */
public interface OperationListener {

	void statusChanged(OperationStatus status);

	void progressChanged(int progress);

	void intermediateResults(List<String> intermediateResults);
}
