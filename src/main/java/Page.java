import javax.swing.AbstractAction;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;

public abstract class Page extends JPanel {
    protected final PageData pageData;

    public Page(PageData pageData) {
        this.pageData = pageData;
    }

    public abstract void build();

    public void willBecomeVisible() {
        pageData.load();
        ValidationResults results = pageData.validate();
        new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };

        // TODO show status bar
    }

    abstract void fillGuiWithData();

    public void willBecomeInvisible() {
        pageData.save();
    }

    public void updateGUI() {
        // Default implementation - can be overridden by subclasses
    }

    public void updateDependantValues() {
        // Default implementation - can be overridden by subclasses
    }

    public abstract String getTitle();
    public abstract String getDescription();
}
