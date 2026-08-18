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
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.page.PageFrame;
import com.fes.flashcard.installer.page.PagePool;

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
		pages.add(new RootPasswordPage(rootPwPageData, this::updateButtons));
		pages.add(new KarafPage(karafPageData, this::updateButtons));
		pages.add(new DatabasePage(databasePageData, this::updateButtons));
		pages.add(new JavaPage(javaPageData, this::updateButtons));
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