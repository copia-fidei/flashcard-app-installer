package com.epau.app.flashcard.app.installer;

import com.epau.app.flashcard.app.installer.pages.DatabasePageData;
import com.epau.app.flashcard.app.installer.pages.JavaPageData;
import com.epau.app.flashcard.app.installer.pages.KarafPageData;
import com.epau.app.flashcard.app.installer.pages.RootPasswordPageData;
import com.epau.lib.swing.wizard.page.PageData;
import com.epau.lib.swing.wizard.page.PageDataPool;
import com.epau.lib.swing.wizard.page.PageFrame;
import com.epau.util.nls.Nls;
import org.jetbrains.annotations.NonNls;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.epau.app.flashcard.app.installer.Operations.from;
import static java.lang.IO.print;
import static java.lang.IO.println;
import static java.util.logging.Logger.getLogger;
import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Main {

	private static final Logger LOG = getLogger(Main.class.getName());

	private static final @NonNls String HELP_INFO = """
			
			Options:
			-h, --help						Display help information
			--cli							Start in headless mode
			--install-default-config		Start installation with default configuration options
			--install-saved-config			Start installation with configuration options found on disk
			""";

	static {
		// system locale will be used, use below to override
		//		Locale.setDefault(Locale.ENGLISH);
		//		Locale.setDefault(Locale.GERMAN);
	}

	private static final Nls nls = new Nls(Main.class);

	@SuppressWarnings({"unused", "UnnecessaryModifier"})
	public static void main(String[] args) {

		if (args.length != 0) {
			// @formatter:off
			@NonNls String arg = args[0];
			switch (arg) {
				case "-h", "--help" 			-> println(HELP_INFO);
				case "--cli" 					-> InteractiveHeadless.run();
				case "--install-default-config" -> install(PageData::loadDefaults);
				case "--install-saved-config" 	-> install(PageData::load);
				default -> {
					println("Unrecognized options. Starting GUI...");
					launch();
				}
			}
			// @formatter:on
		} else {
			launch();
		}
	}

	// TODO localize
	private static void install(Consumer<PageData> actionForAllPageData) {
		var pageDataPool     = new PageDataPool();
		var karafPageData    = new KarafPageData(pageDataPool);
		var databasePageData = new DatabasePageData(pageDataPool);
		var javaPageData     = new JavaPageData(pageDataPool);
		var rootPwPageData   = new RootPasswordPageData(pageDataPool);

		pageDataPool.forEach(actionForAllPageData);

		print("Need sudo password: ");
		var console = System.console();
		char[] pw = console.readPassword();
		rootPwPageData.setRootPassword(pw);
		var validationResults = rootPwPageData.validate();
		if (validationResults.exist()) {
			println("Root password is incorrect. Exiting...");
		}
		println("Starting installation");

		for (var newOperation : from(databasePageData, javaPageData, karafPageData, rootPwPageData)) {
			var operation = newOperation.get();
			try {
				println("Executing: " + operation.title());
				operation.run();
			} catch (Exception e) {
				LOG.log(Level.SEVERE, "Error occurred while running operation " + operation.title(), e);
			}
		}
	}

	private static void launch() {
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

