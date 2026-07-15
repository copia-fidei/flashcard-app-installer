package com.fes.flashcard.installer.toast;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter;

import java.awt.Color;

public record StatusBar(Toast toast) {

	private static final Color       ERROR_COLOR   = Color.RED;
	private static final Color       WARNING_COLOR = new Color(212, 172, 13);
	private static final Color       INFO_COLOR    = new Color(76, 175, 80);

	private static final FlatSVGIcon ERROR_ICON    = newIcon("icons/svgrepo/error-svgrepo-com.svg", ERROR_COLOR);
	private static final FlatSVGIcon WARNING_ICON  = newIcon("icons/svgrepo/warning-filled-svgrepo-com.svg", WARNING_COLOR);
	private static final FlatSVGIcon INFO_ICON     = newIcon("icons/svgrepo/info-svgrepo-com.svg", INFO_COLOR);

	public StatusBar() {
		this(new Toast());
	}

	public void displayError(String message) {
		toast.display(ERROR_ICON, message, ERROR_COLOR);
	}

	public void displayWarning(String message) {
		toast.display(WARNING_ICON, message, WARNING_COLOR);
	}

	public void displayInfo(String message) {
		toast.display(INFO_ICON, message, INFO_COLOR);
	}

	private static FlatSVGIcon newIcon(String iconPath, Color color) {
		var colorFilter = new ColorFilter();
		colorFilter.setMapper(_ -> color);
		var icon = new FlatSVGIcon(iconPath, 20, 20);
		icon.setColorFilter(colorFilter);
		return icon;
	}
}
