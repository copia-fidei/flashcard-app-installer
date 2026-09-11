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

	requires com.epau.installer;
	requires com.epau.utilities.nls;
	requires com.epau.stream.utilities;

	// TODO language
	opens com.epau.app.flashcard.app.installer to com.epau.utilities.nls;
	opens com.epau.app.flashcard.app.installer.operations to com.epau.utilities.nls;
	opens com.epau.app.flashcard.app.installer.pages to com.epau.utilities.nls;

}