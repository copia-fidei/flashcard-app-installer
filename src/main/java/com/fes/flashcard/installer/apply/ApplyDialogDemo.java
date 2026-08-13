package com.fes.flashcard.installer.apply;

import com.fes.flashcard.installer.operation.BlockingOperation;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.operation.WaitingOperation;
import com.fes.flashcard.installer.swing.TestFrames;
import org.jetbrains.annotations.NonNls;

import java.util.List;

@NonNls
interface ApplyDialogDemo {

	List<Operation> TEST_OPERATIONS = List.of(
			new WaitingOperation("Operation 1"),
			new WaitingOperation("Operation 2", 5, true),
			new WaitingOperation("Operation 3", 4, false),
			new BlockingOperation("Block 1", "Block forever"),
			new BlockingOperation("Block 2", "Block forever")
	);

	static void main() {
		TestFrames.showDialog("Apply Dialog", frame -> new ApplyDialog(frame, TEST_OPERATIONS)); //NON-NLS
	}
}
