package com.fes.flashcard.installer.operation;

import java.util.List;

public class OperationAdapter implements OperationListener {

	@Override
	public void statusChanged(OperationStatus status) {}

	@Override
	public void progressChanged(int progress) {}

	@Override
	public void intermediateResults(List<String> intermediateResults) {}
}
