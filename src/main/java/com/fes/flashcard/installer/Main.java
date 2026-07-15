package com.fes.flashcard.installer;

import com.fes.flashcard.installer.app.operations.InstallJavaOperation;
import com.fes.flashcard.installer.app.operations.InstallKarafOperation;
import com.fes.flashcard.installer.app.pages.DatabasePage;
import com.fes.flashcard.installer.app.pages.DatabasePageData;
import com.fes.flashcard.installer.app.pages.JavaPage;
import com.fes.flashcard.installer.app.pages.JavaPageData;
import com.fes.flashcard.installer.app.pages.KarafPage;
import com.fes.flashcard.installer.app.pages.KarafPageData;
import com.fes.flashcard.installer.app.pages.RootPasswordPage;
import com.fes.flashcard.installer.app.pages.RootPasswordPageData;
import com.fes.flashcard.installer.apply.ApplyDialog;

import java.util.List;

import static javax.swing.SwingUtilities.invokeLater;

public class Main {

	static void main(String[] args) {
		var karafPageData    = new KarafPageData(null);
		var karafPage        = new KarafPage(karafPageData, () -> {});
		var databasePageData = new DatabasePageData(null);
		var databasePage     = new DatabasePage(databasePageData, () -> {});
		var javaPageData     = new JavaPageData(null);
		var javaPage         = new JavaPage(javaPageData, () -> {});
		var rootPwPageData   = new RootPasswordPageData(null);
		var rootPwPage       = new RootPasswordPage(rootPwPageData, () -> {});



		invokeLater(() -> {
			var pageFrame = new PageFrame();
			var pagePool  = new PagePool(pageFrame);

			pagePool.init();
			pagePool.showFirstPage();
			pageFrame.setLocationRelativeTo(null);
			pageFrame.pack();
			//		pageFrame.setVisible(true);

			ApplyDialog applyDialog =
					new ApplyDialog(pageFrame, List.of(
							new InstallJavaOperation(javaPageData),
							new InstallKarafOperation(karafPageData)));
			applyDialog.setVisible(true);
		});
	}
}

