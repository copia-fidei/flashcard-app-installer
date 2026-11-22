package com.epau.app.flashcard.app.installer.main;

import com.epau.app.flashcard.app.installer.AppPagePool;
import com.epau.lib.swing.wizard.page.PageFrame;
import com.epau.lib.swing.wizard.page.PagePool;
import com.epau.util.nls.Nls;

import javax.swing.WindowConstants;

import static javax.swing.SwingUtilities.invokeLater;

/// this gui application
public class App {

	private AppPagePool pagePool;

	void run() {
		invokeLater(() -> {
			var frame = new PageFrame(nls.get("App.title.app"));
			pagePool = new AppPagePool(frame);

			pagePool.init();
			pagePool.showFirstPage();
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.setSize(700, 500);
			frame.setLocationRelativeTo(null);
			frame.pack();
			frame.setVisible(true);
		});
	}

	private static final Nls nls = new Nls(Main.class);

	public PagePool getPagePool() {
		return pagePool;
	}
}