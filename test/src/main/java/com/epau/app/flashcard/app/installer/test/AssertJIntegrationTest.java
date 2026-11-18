package com.epau.app.flashcard.app.installer.test;

import com.epau.app.flashcard.app.installer.main.Main;
import com.epau.app.flashcard.app.installer.pages.RootPasswordPage;
import com.epau.app.flashcard.app.installer.services.IntegrationTest;
import com.epau.lib.swing.wizard.apply.ApplyDialog;
import com.epau.lib.swing.wizard.page.ButtonBar;
import com.epau.lib.swing.wizard.page.PageData;
import com.epau.lib.swing.wizard.page.PageFrame;
import com.epau.util.swing.option_dialog.OptionDialog;
import com.epau.util.swing.option_dialog.OptionDialog.Name;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.exception.WaitTimedOutError;
import org.assertj.swing.timing.Condition;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.swing.finder.WindowFinder.findDialog;
import static org.assertj.swing.finder.WindowFinder.findFrame;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;

public class AssertJIntegrationTest implements IntegrationTest {

	/// vagrant sudo password
	private static final String PW = "vagrant"; //NON-NLS

	@Override
	public void run() {
		FailOnThreadViolationRepaintManager.install();
		var robot = BasicRobot.robotWithCurrentAwtHierarchy();

		Main.start();

		var frame        = findFrame(PageFrame.class).using(robot);
		var pagePool     = Main.getApp().getPagePool();
		var pageDataPool = pagePool.getPageDataPool();
		pageDataPool.forEach(PageData::loadDefaults);

		var panel         = frame.panel(PageFrame.Name.BUTTON_BAR);
		var nextBtn       = panel.button(ButtonBar.Name.NEXT);
		var passwordField = frame.textBox(RootPasswordPage.Name.PASSWORD_FIELD);

		passwordField.enterText(PW);

		pause(new Condition("root password is valid") { //NON-NLS
			@Override
			public boolean test() {
				return GuiActionRunner.execute(nextBtn::isEnabled);
			}
		}, timeout(30, SECONDS));

		for (int i = 0; i < pageDataPool.size(); i++) {
			nextBtn.click();
		}
		var applyBtn = panel.button(ButtonBar.Name.APPLY);
		applyBtn.click();

		var applyDialog = findDialog(ApplyDialog.class).using(robot);
		var startBtn    = applyDialog.button(ApplyDialog.Name.START_BUTTON);
		var closeBtn    = applyDialog.button(ApplyDialog.Name.CLOSE_BUTTON);

		try (var executor = newSingleThreadScheduledExecutor()) {
			var scheduled = executor.scheduleAtFixedRate(() -> {
				try {
					var dialog = findDialog(OptionDialog.class).withTimeout(5, SECONDS).using(robot);
					dialog.button(Name.OK_BUTTON).click();
				} catch (WaitTimedOutError _) {
					// no dialog this interval — normal
				}
			}, 0, 10, SECONDS);
			startBtn.click();
			pause(new Condition("operations finished") { //NON-NLS
				@Override
				public boolean test() {
					return GuiActionRunner.execute(closeBtn::isEnabled);
				}
			}, timeout(300, SECONDS));
			scheduled.cancel(false);
		}

		pause(10, SECONDS); // keep open at end
		robot.cleanUp();
	}
}