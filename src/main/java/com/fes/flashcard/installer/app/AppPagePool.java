package com.fes.flashcard.installer.app;

import com.fes.flashcard.installer.app.operations.ConfigureKarafOp;
import com.fes.flashcard.installer.app.operations.ConfigurePostgresOp;
import com.fes.flashcard.installer.app.operations.CreatePostgresDataSourceOp;
import com.fes.flashcard.installer.app.operations.InstallH2Operation;
import com.fes.flashcard.installer.app.operations.InstallJavaOp;
import com.fes.flashcard.installer.app.operations.InstallKarafOp;
import com.fes.flashcard.installer.app.operations.InstallPostgresOp;
import com.fes.flashcard.installer.app.pages.DatabasePage;
import com.fes.flashcard.installer.app.pages.DatabasePageData;
import com.fes.flashcard.installer.app.pages.JavaPage;
import com.fes.flashcard.installer.app.pages.JavaPageData;
import com.fes.flashcard.installer.app.pages.KarafPage;
import com.fes.flashcard.installer.app.pages.KarafPageData;
import com.fes.flashcard.installer.app.pages.RootPasswordPage;
import com.fes.flashcard.installer.app.pages.RootPasswordPageData;
import com.fes.flashcard.installer.page.PageFrame;
import com.fes.flashcard.installer.page.PagePool;

public class AppPagePool extends PagePool {

	public AppPagePool(PageFrame pageFrame) {
		super(pageFrame);
	}

	@Override
	protected void initOverride() {
		var karafPageData    = new KarafPageData(pageDataPool);
		var databasePageData = new DatabasePageData(pageDataPool);
		var javaPageData     = new JavaPageData(pageDataPool);
		var rootPwPageData   = new RootPasswordPageData(pageDataPool);

		pageDataPool.add(rootPwPageData);
		pageDataPool.add(karafPageData);
		pageDataPool.add(databasePageData);
		pageDataPool.add(javaPageData);

		// order important
		var rootPwPage   = new RootPasswordPage(rootPwPageData, this::updateButtons);
		var karafPage    = new KarafPage(karafPageData, this::updateButtons);
		var databasePage = new DatabasePage(databasePageData, this::updateButtons);
		var javaPage     = new JavaPage(javaPageData, this::updateButtons);

		pages.add(rootPwPage);
		pages.add(karafPage);
		pages.add(databasePage);
		pages.add(javaPage);

		operations.add(() -> new InstallJavaOp(javaPageData));
		operations.add(() -> new InstallKarafOp(karafPageData));
		operations.add(() -> new ConfigureKarafOp(karafPageData));
		operations.add(() -> new InstallPostgresOp(databasePageData, rootPwPageData));
		operations.add(() -> new ConfigurePostgresOp(databasePageData));
		operations.add(() -> new CreatePostgresDataSourceOp(karafPageData, databasePageData));
		operations.add(() -> new InstallH2Operation(karafPageData, databasePageData));
	}
}
