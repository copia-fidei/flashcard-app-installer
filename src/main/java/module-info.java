module com.fes.flashcard.installer {
	requires java.desktop;
	requires java.prefs;
	requires java.sql;

	requires static org.jetbrains.annotations;
	requires com.formdev.flatlaf;
	requires com.formdev.flatlaf.extras;
	requires org.apache.commons.compress;
	requires pty4j;

	opens icons.svgrepo;

	requires org.postgresql.jdbc;
	requires com.github.weisj.jsvg;
}