package com.fes.flashcard.installer.toast;

import org.jetbrains.annotations.NonNls;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import static java.awt.EventQueue.invokeLater;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.NONE;
import static javax.swing.Box.createHorizontalStrut;
import static javax.swing.BoxLayout.LINE_AXIS;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

@NonNls
interface StatusBarDemo {


	static void main() {
		invokeLater(() -> {
			var frame         = new JFrame("Toast"); //NON-NLS
			var statusBar     = new StatusBar();
			var errorButton   = new JButton("Error"); //NON-NLS
			var warningButton = new JButton("Warning"); //NON-NLS
			var infoButton    = new JButton("Info"); //NON-NLS
			var panel         = new JPanel(new GridBagLayout());
			var buttons       = new JPanel();

			errorButton.addActionListener(_   -> statusBar.displayError("Something went wrong.")); //NON-NLS
			warningButton.addActionListener(_ -> statusBar.displayWarning("This is a warning.")); //NON-NLS
			infoButton.addActionListener(_ 	 -> statusBar.displayInfo("Everything is working correctly.")); //NON-NLS

			buttons.setLayout(new BoxLayout(buttons, LINE_AXIS));
			buttons.add(errorButton);
			buttons.add(createHorizontalStrut(10));
			buttons.add(warningButton);
			buttons.add(createHorizontalStrut(10));
			buttons.add(infoButton);

			panel.add(statusBar.toast(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, BOTH, new Insets(10, 10, 10, 10), 0, 0));
			panel.add(buttons, 			 new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, CENTER, NONE, new Insets(10, 10, 10, 10), 0, 0));

			statusBar.displayInfo("Click one off the buttons."); //NON-NLS

			frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
			frame.setContentPane(panel);
			frame.setLocationRelativeTo(null);
			frame.pack();
			frame.setVisible(true);
		});
	}
}
