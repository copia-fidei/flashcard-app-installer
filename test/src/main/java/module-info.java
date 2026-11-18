import com.epau.app.flashcard.app.installer.test.AssertJIntegrationTest;

module com.epau.app.flashcard.app.installer.test {

	requires com.epau.app.flashcard.app.installer;
	requires com.epau.lib.swing.wizard;

	requires assertj.swing;
	requires com.epau.util.swing;
	requires java.desktop;

	provides com.epau.app.flashcard.app.installer.services.IntegrationTest with AssertJIntegrationTest;

}