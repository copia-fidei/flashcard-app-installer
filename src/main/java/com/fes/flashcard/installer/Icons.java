package com.fes.flashcard.installer;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter;
import org.jetbrains.annotations.NonNls;

import java.awt.Color;

@NonNls
public final class Icons {

	private Icons() {}

	public static FlatSVGIcon hourglass(int size) {
		return new FlatSVGIcon("icons/svgrepo/hourglass-done-svgrepo-com.svg", size, size);
	}

	public static FlatSVGIcon cogwheel(int size) {
		return newIcon("icons/svgrepo/cogwheel-configuration-gear-svgrepo-com.svg", Color.GRAY, size);
	}

	public static FlatSVGIcon error(int size) {
		return newIcon("icons/svgrepo/error-svgrepo-com.svg", Colors.ERROR, size);
	}

	public static FlatSVGIcon cancel(int size) {
		return newIcon("icons/svgrepo/cancel-svgrepo-com.svg", Color.RED, size);
	}

	public static FlatSVGIcon check(int size) {
		return newIcon("icons/svgrepo/check-circle-svgrepo-com.svg", Colors.INFO, size);
	}

	public static FlatSVGIcon warning(int size) {
		return newIcon("icons/svgrepo/warning-filled-svgrepo-com.svg", Colors.WARNING, size);
	}

	public static FlatSVGIcon info(int size) {
		return newIcon("icons/svgrepo/info-svgrepo-com.svg", Colors.INFO, size);
	}

	private static FlatSVGIcon newIcon(String location, Color color, int size) {
		return new FlatSVGIcon(location, size, size).setColorFilter(new ColorFilter(_ -> color));
	}
}
