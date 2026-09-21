package com.epau.app.flashcard.app.installer;

import com.epau.app.flashcard.app.installer.operations.ConfigureKarafOp;
import com.epau.app.flashcard.app.installer.operations.ConfigurePostgresOp;
import com.epau.app.flashcard.app.installer.operations.CreatePostgresDataSourceOp;
import com.epau.app.flashcard.app.installer.operations.InstallH2Operation;
import com.epau.app.flashcard.app.installer.operations.InstallJavaOp;
import com.epau.app.flashcard.app.installer.operations.InstallKarafOp;
import com.epau.app.flashcard.app.installer.operations.InstallPostgresOp;
import com.epau.app.flashcard.app.installer.pages.DatabasePage;
import com.epau.app.flashcard.app.installer.pages.DatabasePageData;
import com.epau.app.flashcard.app.installer.pages.JavaPage;
import com.epau.app.flashcard.app.installer.pages.JavaPageData;
import com.epau.app.flashcard.app.installer.pages.KarafPage;
import com.epau.app.flashcard.app.installer.pages.KarafPageData;
import com.epau.app.flashcard.app.installer.pages.RootPasswordPage;
import com.epau.app.flashcard.app.installer.pages.RootPasswordPageData;
import com.epau.lib.swing.wizard.page.PageFrame;
import com.epau.lib.swing.wizard.page.PagePool;
import com.epau.util.swing.operation.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AppPagePool extends PagePool {

	private KarafPageData        karafPageData;
	private DatabasePageData     databasePageData;
	private JavaPageData         javaPageData;
	private RootPasswordPageData rootPwPageData;

	public AppPagePool(PageFrame pageFrame) {
		super(pageFrame);
	}

	@Override
	protected void addPages() {
		karafPageData    = new KarafPageData(pageDataPool);
		databasePageData = new DatabasePageData(pageDataPool);
		javaPageData     = new JavaPageData(pageDataPool);
		rootPwPageData   = new RootPasswordPageData(pageDataPool);

		pageDataPool.add(rootPwPageData);
		pageDataPool.add(karafPageData);
		pageDataPool.add(databasePageData);
		pageDataPool.add(javaPageData);

		// order is important
		pages.add(new RootPasswordPage(rootPwPageData));
		pages.add(new KarafPage(karafPageData));
		pages.add(new DatabasePage(databasePageData));
		pages.add(new JavaPage(javaPageData));
	}

	@Override
	protected List<Supplier<Operation>> getOperations() {
		var operations = new ArrayList<Supplier<Operation>>();

		Database dbImp = databasePageData.getDatabaseImplementation();

		operations.add(() -> new InstallJavaOp(javaPageData));
		operations.add(() -> new InstallKarafOp(karafPageData));
		operations.add(() -> new ConfigureKarafOp(karafPageData));

		switch (dbImp) {
			case PostgreSQL -> {
				operations.add(() -> new InstallPostgresOp(databasePageData, rootPwPageData));
				operations.add(() -> new ConfigurePostgresOp(databasePageData));
				operations.add(() -> new CreatePostgresDataSourceOp(karafPageData, databasePageData));
			}
			case H2 -> operations.add(() -> new InstallH2Operation(karafPageData, databasePageData));
		}
		return operations;
	}
}