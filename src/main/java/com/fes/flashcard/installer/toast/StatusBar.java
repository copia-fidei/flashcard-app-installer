package com.fes.flashcard.installer.toast;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.*;

import static java.awt.EventQueue.invokeLater;
import static javax.swing.Box.createHorizontalStrut;
import static javax.swing.BoxLayout.LINE_AXIS;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public record StatusBar(Toast toast) {

	private static final Color ERROR_COLOR   = Color.RED;
	private static final Color WARNING_COLOR = new Color(212, 172, 13);
	private static final Color INFO_COLOR    = new Color(76, 175, 80);

	private static final FlatSVGIcon ERROR_ICON   = newIcon("icons/svgrepo/error-svgrepo-com.svg", ERROR_COLOR);
	private static final FlatSVGIcon WARNING_ICON = newIcon("icons/svgrepo/warning-filled-svgrepo-com.svg", WARNING_COLOR);
	private static final FlatSVGIcon INFO_ICON    = newIcon("icons/svgrepo/info-svgrepo-com.svg", INFO_COLOR);

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


	static void main() {
		invokeLater(() -> {
			var frame = new JFrame("Toast");
			frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
			var statusBar = new StatusBar();
			statusBar.toast().display(INFO_ICON, "Click one off the buttons", Color.WHITE); // make space

			JPanel panel = new JPanel(new GridBagLayout());
			panel.add(statusBar.toast(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));
			var buttons = new JPanel();
			buttons.setLayout(new BoxLayout(buttons, LINE_AXIS));
			var errorButton = new JButton("Error");
			errorButton.addActionListener(_ -> statusBar.displayError("Something went terribly wrong."));
			var warningButton = new JButton("Warning");
			warningButton.addActionListener(_ -> statusBar.displayWarning("This is a warning."));
			var infoButton = new JButton("Info");
			infoButton.addActionListener(_ -> statusBar.displayInfo("Everything is working correctly."));

			buttons.add(errorButton);
			buttons.add(createHorizontalStrut(10));
			buttons.add(warningButton);
			buttons.add(createHorizontalStrut(10));
			buttons.add(infoButton);

			panel.add(buttons, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));

			frame.setContentPane(panel);
			frame.setLocationRelativeTo(null);
			frame.pack();
			frame.setVisible(true);
		});
	}

	private static FlatSVGIcon newIcon(String iconPath, Color color) {
		var colorFilter = new ColorFilter();
		colorFilter.setMapper(_ -> color);
		var icon = new FlatSVGIcon(iconPath, 20, 20);
		icon.setColorFilter(colorFilter);
		return icon;
	}
}
