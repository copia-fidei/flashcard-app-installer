package com.fes.flashcard.installer.swing;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Function;

import static java.awt.Color.GRAY;
import static java.awt.Font.PLAIN;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/// Utility methods to view simple components within a JFrame.
public interface TestFrames {

	static void showComponent(String title, Component comp) {
		invokeLater(() -> {
			var frame = new JFrame(title);
			frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
			frame.add(comp);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

	static void showDialog(String title, Function<JFrame, JDialog> newDialog) {
		invokeLater(() -> {
			var frame = new JFrame(title);
			frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
			frame.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					newDialog.apply(frame).setVisible(true);
				}
			});
			var label = new JLabel("Click here to open dialog", CENTER); //$NON-NLS
			label.setFont(label.getFont().deriveFont(PLAIN, 18f));
			frame.add(label, BorderLayout.CENTER);
			label.setForeground(GRAY);
			frame.pack();
			frame.setSize(new Dimension(300, 300));
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			newDialog.apply(frame).setVisible(true);
		});
	}
}
