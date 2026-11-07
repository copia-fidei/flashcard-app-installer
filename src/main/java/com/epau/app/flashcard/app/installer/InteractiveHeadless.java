package com.epau.app.flashcard.app.installer;

import com.epau.app.flashcard.app.installer.pages.DatabasePageData;
import com.epau.app.flashcard.app.installer.pages.JavaPageData;
import com.epau.app.flashcard.app.installer.pages.KarafPageData;
import com.epau.app.flashcard.app.installer.pages.RootPasswordPageData;
import com.epau.lib.swing.wizard.page.PageData;
import com.epau.lib.swing.wizard.page.PageDataPool;
import com.epau.lib.validation.Severity;
import org.jetbrains.annotations.NonNls;

import static java.lang.IO.println;

// INFO: not done, just an idea
@NonNls
public class InteractiveHeadless {

	private static final String HELP = """
			Usage:
			h, help							Display help information
			exit							Exit program
			next							Go to next wizard page, only if current the page has no validation errors
			back							Go to previous wizard page
			validate						Validate the page data and print the results to the console
			""";

	private static final String VALIDATION_RESULT_TEMPLATE = """
			%s: [
				description: %s
				severity: %s
			]
			""";

	static void run() {
		var console = System.console();

		var pageDataPool = new PageDataPool();
		// order is important
		var rootPwPageData = new RootPasswordPageData(pageDataPool);
		var _              = new KarafPageData(pageDataPool);
		var _              = new DatabasePageData(pageDataPool);
		var _              = new JavaPageData(pageDataPool);

		var listIterator = pageDataPool.listIterator();
		PageData currentPage  = rootPwPageData; // first page

		println(HELP);
		while (true) {
			@NonNls String line = console.readLine();
			switch (line) {
				case "exit" -> {
					return;
				}
				case "next" -> {
					if (!currentPage.validate().contains(Severity.ERROR)) {
						if (listIterator.hasNext()) {
							currentPage = listIterator.next();
						}
					} else {
						println("Cannot proceed to next page. Current page has validation errors."); //NON-NLS
					}
				}
				case "back" -> {
					if (listIterator.hasPrevious()) {
						currentPage = listIterator.previous();
					}
				}
				case "validate" -> {
					var validationResults = currentPage.validate();
					for (var validationResult : validationResults.list()) {
						println(VALIDATION_RESULT_TEMPLATE.formatted(validationResult.title(), validationResult.description(), validationResult.severity()));
					}

				}
				case "help", "h" -> {
					println(HELP);
				}
				default -> {
					println("Unrecognized options."); //NON-NLS
					println(HELP);
				}

			}

		}
	}
}