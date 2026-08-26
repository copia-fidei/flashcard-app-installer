package com.fes.flashcard.installer.apply;

import com.fes.flashcard.installer.operation.WaitingOperation;
import com.fes.flashcard.installer.swing.TestFrames;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

class DetailsDialogDemo {

	static void main() {
		TestFrames.showDialog("Details Dialog", frame -> { //NON-NLS
			var dialog = new DetailsDialog(frame, new WaitingOperation("Wait 10 seconds", 10));
			dialog.setModal(true);
			dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			return dialog;
		});
	}
}
