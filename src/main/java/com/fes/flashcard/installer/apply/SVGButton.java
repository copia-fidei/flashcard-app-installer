package com.fes.flashcard.installer.apply;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.jetbrains.annotations.NonNls;

import javax.swing.JButton;
import java.awt.Insets;

class SVGButton extends JButton {

	@NonNls
	public SVGButton(String svgPath, int size) {
		super(new FlatSVGIcon(svgPath, size, size));

		setMargin(new Insets(0, 0, 0, 0));
	}
}
