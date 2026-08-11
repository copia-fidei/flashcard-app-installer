package com.fes.flashcard.installer.operation;

import com.fes.flashcard.installer.Icons;
import com.formdev.flatlaf.extras.FlatSVGIcon;

public class OperationStatusPresentation {

	private final static int SIZE = 16;

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
			case NOT_STARTED -> "Noch nicht gestartet";
			case RUNNING -> "Wird ausgeführt…";
			case ERROR -> "Fehlgeschlagen";
			case CANCELLED_BY_USER -> "Vom Benutzer abgebrochen";
			case COMPLETED -> "Erfolgreich abgeschlossen";
		};
	}
}
