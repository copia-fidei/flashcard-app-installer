package com.fes.flashcard.installer;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter;
import org.jetbrains.annotations.NonNls;

import java.awt.Color;

// TODO
@NonNls
public interface Icons {

	String HOURGLASS_ICON_PATH = "icons/svgrepo/hourglass-done-svgrepo-com.svg";
	String COGWHEEL_ICON_PATH  = "icons/svgrepo/cogwheel-configuration-gear-svgrepo-com.svg";
	String ERROR_ICON_PATH     = "icons/svgrepo/error-svgrepo-com.svg";
	String CANCEL_ICON_PATH    = "icons/svgrepo/cancel-svgrepo-com.svg";
	String CHECK_ICON_PATH     = "icons/svgrepo/check-circle-svgrepo-com.svg";
	String WARNING_ICON_PATH   = "icons/svgrepo/warning-filled-svgrepo-com.svg";
	String INFO_ICON_PATH      = "icons/svgrepo/info-svgrepo-com.svg";

	static FlatSVGIcon hourglass(int size) {
		return new FlatSVGIcon(HOURGLASS_ICON_PATH, size, size);
	}

	static FlatSVGIcon cogwheel(int size) {
		return newIcon(COGWHEEL_ICON_PATH, Color.GRAY, size);
	}

	static FlatSVGIcon error(int size) {
		return newIcon(ERROR_ICON_PATH, Colors.ERROR, size);
	}

	static FlatSVGIcon cancel(int size) {
		return newIcon(CANCEL_ICON_PATH, Color.RED, size);
	}

	static FlatSVGIcon check(int size) {
		return newIcon(CHECK_ICON_PATH, Colors.INFO, size);
	}

	static FlatSVGIcon warning(int size) {
		return newIcon(WARNING_ICON_PATH, Colors.WARNING, size);
	}

	static FlatSVGIcon info(int size) {
		return newIcon(INFO_ICON_PATH, Colors.INFO, size);
	}

	private static FlatSVGIcon newIcon(String location, Color color, int size) {
		return new FlatSVGIcon(location, size, size).setColorFilter(new ColorFilter(_ -> color));
	}
}
