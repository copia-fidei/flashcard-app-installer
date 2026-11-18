package com.epau.app.flashcard.app.installer.main;

import com.epau.app.flashcard.app.installer.Karaf;
import com.epau.app.flashcard.app.installer.pages.KarafPageData;
import com.epau.app.flashcard.app.installer.services.IntegrationTest;
import com.epau.lib.swing.wizard.page.PageData;
import com.epau.util.swing.operation.ErrorCode;
import com.epau.util.swing.operation.imp.NoOperation;
import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import static java.lang.IO.println;
import static java.util.logging.Logger.getLogger;

public class Main {

	private static final @NonNls String HELP_INFO = """
			Options:
			-h, --help						Display help information
			--install-default-config		Start installation with default configuration options
			--install-saved-config			Start installation with configuration options found on disk
			--start-karaf					Start the Karaf application server inside the directory configured by this installer.
											The directory might not exist, e.g. if the installation has not happened yet.
			--test							Run integration test. Intended for Vagrant. Works only if this is a special build.
			""";

	static {
		// system locale will be used, use below to override
		//		Locale.setDefault(Locale.ENGLISH);
		//		Locale.setDefault(Locale.GERMAN);
	}

	@SuppressWarnings({"unused", "UnnecessaryModifier"})
	public static void main(String[] args) throws IOException, ErrorCode, InterruptedException {
		if (args.length != 0) {
			// @formatter:off
			@NonNls String arg = args[0];
			switch (arg) {
				case "-h", "--help" 			-> println(HELP_INFO);
//				case "--cli" 					-> Headless.run(); // Not supported, do not uncomment
				case "--install-default-config" -> Install.now(PageData::loadDefaults);
				case "--install-saved-config" 	-> Install.now(PageData::load);
				case "--test" 					-> test();
				case "--start-karaf" 			-> startKaraf();
				default -> {
					println("Unrecognized options. Starting GUI..."); //NON-NLS
					start();
				}
			}
			// @formatter:on
		} else {
			start();
		}
	}

	private static void test() {
		ServiceLoader.load(IntegrationTest.class).findFirst().ifPresentOrElse(IntegrationTest::run, () -> println("Integration test code not included.")); //NON-NLS
	}

	private static void startKaraf() throws IOException, InterruptedException, ErrorCode {
		var data = new KarafPageData();
		data.load();
		var karaf = new Karaf(data.getKarafInstallationDir(), new NoOperation());
		karaf.start();
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

