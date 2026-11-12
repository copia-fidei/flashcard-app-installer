package com.epau.app.flashcard.app.installer.main;

import com.epau.app.flashcard.app.installer.services.IntegrationTest;
import com.epau.lib.swing.wizard.page.PageData;
import org.jetbrains.annotations.NonNls;

import java.util.ServiceLoader;
import java.util.logging.Logger;

import static java.lang.IO.println;
import static java.util.logging.Logger.getLogger;

public class Main {

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

	@SuppressWarnings({"unused", "UnnecessaryModifier"})
	public static void main(String[] args) {
		if (args.length != 0) {
			// @formatter:off
			@NonNls String arg = args[0];
			switch (arg) {
				case "-h", "--help" 			-> println(HELP_INFO);
				case "--cli" 					-> Headless.run();
				case "--install-default-config" -> Install.now(PageData::loadDefaults);
				case "--install-saved-config" 	-> Install.now(PageData::load);
				case "--test" 					-> test();
				default -> {
					println("Unrecognized options. Starting GUI...");
					start();
				}
			}
			// @formatter:on
		} else {
			start();
		}
	}

	private static void test() {
		ServiceLoader.load(IntegrationTest.class).findFirst().ifPresentOrElse(IntegrationTest::run, () -> println("No test provider in this build."));
	}

	private static App APP;

	/// Start the GUI app
	public static void start() {
		APP = new App();
		APP.run();
	}

	public static App getApp() {
		return APP;
	}



	private static final Logger LOG = getLogger(Main.class.getName());

}

