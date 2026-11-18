package com.epau.app.flashcard.app.installer.main;

import com.epau.app.flashcard.app.installer.Operations;
import com.epau.app.flashcard.app.installer.pages.DatabasePageData;
import com.epau.app.flashcard.app.installer.pages.JavaPageData;
import com.epau.app.flashcard.app.installer.pages.KarafPageData;
import com.epau.app.flashcard.app.installer.pages.RootPasswordPageData;
import com.epau.lib.swing.wizard.page.PageData;
import com.epau.lib.swing.wizard.page.PageDataPool;
import org.jetbrains.annotations.NonNls;

import java.util.function.Consumer;
import java.util.logging.Level;

import static java.lang.IO.print;
import static java.lang.IO.println;
import static java.util.logging.Logger.getLogger;

@NonNls
class Install {

	static void now(Consumer<PageData> actionForAllPageData) {
		var pageDataPool     = new PageDataPool();

		var rootPwPageData   = new RootPasswordPageData();
		var karafPageData    = new KarafPageData();
		var databasePageData = new DatabasePageData();
		var javaPageData     = new JavaPageData();

		pageDataPool.add(rootPwPageData);
		pageDataPool.add(karafPageData);
		pageDataPool.add(databasePageData);
		pageDataPool.add(javaPageData);

		pageDataPool.forEach(actionForAllPageData);

		print("Need sudo password: "); //NON-NLS
		var    console = System.console();
		char[] pw      = console.readPassword();
		rootPwPageData.setRootPassword(pw);
		var validationResults = rootPwPageData.validate();
		if (validationResults.exist()) {
			println("Root password is incorrect. Exiting..."); //NON-NLS
		}
		println("Starting installation"); //NON-NLS

		for (var newOperation : Operations.from(databasePageData, javaPageData, karafPageData, rootPwPageData)) {
			var operation = newOperation.get();
			try {
				println("Executing: " + operation.title()); //NON-NLS
				operation.run();
			} catch (Exception e) {
				getLogger(Install.class.getName()).log(Level.SEVERE, "Error occurred while running operation " + operation.title(), e); //NON-NLS
			}
		}
	}
}