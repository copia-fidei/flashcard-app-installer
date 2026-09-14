package com.epau.app.flashcard.app.installer;

import com.epau.lib.swing.installer.page.PageFrame;
import com.epau.util.nls.Nls;

import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Main {

	private static final Logger LOG = getLogger(Main.class.getName());

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

