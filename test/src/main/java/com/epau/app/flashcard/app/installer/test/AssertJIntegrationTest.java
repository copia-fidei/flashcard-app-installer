package com.epau.app.flashcard.app.installer.test;

import com.epau.app.flashcard.app.installer.main.Main;
import com.epau.app.flashcard.app.installer.pages.RootPasswordPageData;
import com.epau.app.flashcard.app.installer.services.IntegrationTest;
import com.epau.lib.swing.wizard.page.ButtonBar;
import com.epau.lib.swing.wizard.page.PageData;
import com.epau.lib.swing.wizard.page.PageFrame;
import com.epau.lib.swing.wizard.page.PageFrame.Name;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.timing.Condition;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.swing.finder.WindowFinder.findFrame;
import static org.assertj.swing.timing.Pause.pause;

public final class AssertJIntegrationTest implements IntegrationTest {

    @Override
    public void run() {
        FailOnThreadViolationRepaintManager.install();  
        var robot = BasicRobot.robotWithCurrentAwtHierarchy();  

        Main.start();

        var frame        = findFrame(PageFrame.class).withTimeout(30, SECONDS).using(robot);
        var pagePool     = Main.getApp().getPagePool();
        var pageDataPool = pagePool.getPageDataPool();
        pageDataPool.forEach(PageData::loadDefaults);

        pause(new Condition("correct root password to be entered manually") { //NON-NLS
            @Override
            public boolean test() {
                var rootPasswordPageData = pageDataPool.get(RootPasswordPageData.class).orElseThrow();
                return !rootPasswordPageData.validate().isValid();
            }
        });
        var panel   = frame.panel(Name.BUTTON_BAR);
        var nextBtn = panel.button(ButtonBar.Name.NEXT);

        pause(1, SECONDS);

        for (int i = 0; i < pageDataPool.size(); i++) {
            nextBtn.click();
        }
        var applyBtn = panel.button(ButtonBar.Name.APPLY);
        applyBtn.click();

        // TODO click start
        // ...

        pause(10, SECONDS); // keep open at end

        robot.cleanUp();
    }  
}