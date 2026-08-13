package com.fes.flashcard.installer.operation;

import com.fes.flashcard.installer.Icons;
import com.fes.flashcard.installer.utilities.Nls;
import com.formdev.flatlaf.extras.FlatSVGIcon;

public class OperationStatusPresentation {

	private final static int SIZE = 16;
	private final static Nls nls = new Nls(OperationStatusPresentation.class);

	public static FlatSVGIcon getIcon(OperationStatus status) {
		return switch (status) {
			case NOT_STARTED       -> Icons.hourglass(SIZE);
			case RUNNING           -> Icons.cogwheel(SIZE);
			case ERROR             -> Icons.warning(SIZE);
			case CANCELLED_BY_USER -> Icons.cancel(SIZE);
			case COMPLETED         -> Icons.check(SIZE);
		 };
	}

	public static String getDescription(OperationStatus status) {
		return switch (status) {
			case NOT_STARTED  	   -> nls.get("OperationStatusPresentation.notStarted");
			case RUNNING 		   -> nls.get("OperationStatusPresentation.running");
			case ERROR 			   -> nls.get("OperationStatusPresentation.error");
			case CANCELLED_BY_USER -> nls.get("OperationStatusPresentation.cancelledByUser");
			case COMPLETED 		   -> nls.get("OperationStatusPresentation.completed");
		};
	}
}
