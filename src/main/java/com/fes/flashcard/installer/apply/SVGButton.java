package com.fes.flashcard.installer.apply;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.JButton;
import java.awt.*;

class SVGButton extends JButton {

	public SVGButton(String svgPath, int size) {
		super(new FlatSVGIcon(svgPath, size, size));

		setMargin(new Insets(0, 0, 0, 0));
	}
}
