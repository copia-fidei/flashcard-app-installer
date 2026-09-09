package com.epau.app.flashcard.app.installer;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter;
import org.jetbrains.annotations.NonNls;

import java.awt.Color;

// TODO
@NonNls
public interface Icons {

	String ERROR_ICON_PATH     = "icons/svgrepo/error-svgrepo-com.svg";
	String WARNING_ICON_PATH   = "icons/svgrepo/warning-filled-svgrepo-com.svg";
	String INFO_ICON_PATH      = "icons/svgrepo/info-svgrepo-com.svg";

	static FlatSVGIcon error(int size) {
		return newIcon(ERROR_ICON_PATH, Colors.ERROR, size);
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
