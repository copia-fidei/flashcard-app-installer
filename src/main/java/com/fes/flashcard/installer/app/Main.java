package com.fes.flashcard.installer.app;

import com.fes.flashcard.installer.PageFrame;

import static javax.swing.SwingUtilities.invokeLater;

public class Main {

	static void main(String[] args) {
//		var karafPageData    = new KarafPageData(null);
//		var karafPage        = new KarafPage(karafPageData, () -> {});
//		var databasePageData = new DatabasePageData(null);
//		var databasePage     = new DatabasePage(databasePageData, () -> {});
//		var javaPageData     = new JavaPageData(null);
//		var javaPage         = new JavaPage(javaPageData, () -> {});
//		var rootPwPageData   = new RootPasswordPageData(null);
//		var rootPwPage       = new RootPasswordPage(rootPwPageData, () -> {});

		invokeLater(() -> {
			var pageFrame = new PageFrame();
			var pagePool  = new AppPagePool(pageFrame);

			pagePool.init();
			pagePool.showFirstPage();
			pageFrame.setLocationRelativeTo(null);
			pageFrame.pack();
					pageFrame.setVisible(true);

//			ApplyDialog applyDialog =
//					new ApplyDialog(pageFrame, List.of(
//							new InstallJavaOp(javaPageData),
//							new InstallKarafOp(karafPageData),
//							new InstallPostgresOp(databasePageData),
//							new InstallKarafFeaturesOp(karafPageData),
//							new ConfigurePostgresOp(databasePageData)
//						));
//			applyDialog.setVisible(true);
		});
	}
}

