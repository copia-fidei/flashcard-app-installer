package com.epau.app.flashcard.app.installer;

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
		return Operations.from(databasePageData, javaPageData, karafPageData, rootPwPageData);
	}
}