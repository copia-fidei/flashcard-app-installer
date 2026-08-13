package com.fes.flashcard.installer.app;

import com.fes.flashcard.installer.page.PageFrame;
import com.fes.flashcard.installer.utilities.Nls;

import java.util.Locale;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Main {

	private static final Nls nls = new Nls(Main.class);

	@SuppressWarnings("unused")
	static void main(String[] args) {
		Locale.setDefault(Locale.ENGLISH);
		invokeLater(() -> {
			var pageFrame = new PageFrame(nls.get("Main.title.app"));
			var pagePool  = new AppPagePool(pageFrame);

			pagePool.init();
			pagePool.showFirstPage();
			pageFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
			pageFrame.setSize(700, 500);
			pageFrame.setLocationRelativeTo(null);
			pageFrame.pack();
			pageFrame.setVisible(true);
		});
	}
}

