package com.fes.flashcard.installer.apply;

import javax.swing.Icon;
import javax.swing.JProgressBar;
import java.awt.Graphics;

class ProgressBarWithIcon extends JProgressBar {

	private Icon icon;

	ProgressBarWithIcon(int min, int max) {
		super(min, max);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (icon == null) {
			return;
		}
		String text = getString();
		if (text == null) {
			return;
		}
		int textWidth   = g.getFontMetrics(getFont()).stringWidth(text);
		int iconWidth   = icon.getIconWidth();
		int gap        = 20;
		int totalWidth = iconWidth + gap + textWidth;
		int x          = (getWidth() - totalWidth) / 2;
		int y          = (getHeight() - icon.getIconHeight()) / 2;
		icon.paintIcon(this, g, x, y);
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
		repaint();
	}
}
