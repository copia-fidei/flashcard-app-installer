package com.fes.flashcard.installer.app;

import com.epau.installer.page.PageFrame;
import com.epau.utilities.nls.Nls;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Main {

	static {
		// system locale will be used, use below to override
//		Locale.setDefault(Locale.ENGLISH);
//		Locale.setDefault(Locale.GERMAN);
	}

	private static final Nls nls = new Nls(Main.class);

	@SuppressWarnings("unused")
	static void main(String[] args) {
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

