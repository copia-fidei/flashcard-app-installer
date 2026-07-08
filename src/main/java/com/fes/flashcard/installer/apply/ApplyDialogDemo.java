package com.fes.flashcard.installer.apply;

import com.fes.flashcard.installer.TestFrames;
import com.fes.flashcard.installer.operation.BlockingOperation;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.operation.WaitingOperation;

import java.util.List;

public interface ApplyDialogDemo {

	List<Operation> TEST_OPERATIONS = List.of(
			new WaitingOperation("Test 1"),
			new WaitingOperation("Test 2", 5, true),
			new WaitingOperation("Test 3", 4, false),
			new BlockingOperation("Block", "Block forever"),
			new BlockingOperation("Block", "Block forever")
	);

	static void main() {
		TestFrames.showDialog("Apply Dialog", frame -> new ApplyDialog(frame, TEST_OPERATIONS));
	}
}
