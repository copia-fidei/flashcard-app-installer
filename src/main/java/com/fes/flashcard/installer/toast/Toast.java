package com.fes.flashcard.installer.toast;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.*;

import static java.awt.BorderLayout.*;
import static java.awt.BorderLayout.WEST;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static javax.swing.BorderFactory.createEmptyBorder;

public class Toast extends JPanel {

	private static final int ROUNDEDNESS = 20;

	private final JLabel iconLabel = new JLabel();
	private final JLabel textLabel = new JLabel();
	private       Color  color;

	public Toast() {
		setBorder(createEmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(5, 0));
		add(iconLabel, WEST);
		add(textLabel, CENTER);
		setOpaque(false);
		iconLabel.setOpaque(false);
		textLabel.setOpaque(false);
	}


	public void display(Icon icon, String message, Color color) {
		this.color = color;
		message = message + " ";
		iconLabel.setIcon(icon);
		textLabel.setText(message);
		textLabel.setForeground(color);
		repaint();
		setVisible(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		var g2d = (Graphics2D) g.create();
		g2d.setRenderingHints(new RenderingHints(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON));
		g2d.setColor(UIManager.getColor("TextArea.background"));
		g2d.fillRoundRect(0, 0, getWidth(), getHeight(), ROUNDEDNESS, ROUNDEDNESS);
		g2d.setColor(color);
		g2d.setStroke(new BasicStroke(1f));
		g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ROUNDEDNESS, ROUNDEDNESS);
		g2d.dispose();
	}
}
