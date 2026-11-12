module com.epau.app.flashcard.app.installer {
	requires java.desktop;
	requires java.prefs;
	requires java.sql;

	requires static org.jetbrains.annotations;
	requires com.formdev.flatlaf;
	requires com.formdev.flatlaf.extras;
	requires org.apache.commons.compress;
	requires pty4j;
	requires org.postgresql.jdbc;
	requires com.github.weisj.jsvg;

	requires com.epau.lib.swing.wizard;
	requires com.epau.util.nls;
	requires com.epau.util.stream;
	requires com.epau.util.swing;
	requires com.epau.util.io;
	requires com.epau.util.text;

	exports com.epau.app.flashcard.app.installer.services;
	exports com.epau.app.flashcard.app.installer.main;
	exports com.epau.app.flashcard.app.installer.pages;

	opens com.epau.app.flashcard.app.installer to com.epau.util.nls;
	opens com.epau.app.flashcard.app.installer.operations to com.epau.util.nls;
	opens com.epau.app.flashcard.app.installer.pages to com.epau.util.nls;
	opens com.epau.app.flashcard.app.installer.main to com.epau.util.nls;
	opens com.epau.app.flashcard.app.installer.services to com.epau.util.nls;

	uses com.epau.app.flashcard.app.installer.services.IntegrationTest;


}