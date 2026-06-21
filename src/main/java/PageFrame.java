import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.*;

public class PageFrame extends JFrame {

    private final PageTitleList pageTitleList;
    private final ButtonsBar    buttonsBar;
    private final JPanel        pageArea;

    public PageFrame(PageTitleList pageTitleList, ButtonsBar buttonsBar) {
        super("Karteikarten Anwendung Installer");

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.pageTitleList = pageTitleList;
        this.buttonsBar = buttonsBar;
        pageArea = new JPanel();
    }

    public void build() {
        setLayout(new BorderLayout());

        // Left side: PageTitleList
        JPanel leftPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(pageTitleList);
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);
        scrollPane.getViewport().getView().setEnabled(false);

        // Center: Page area
        add(pageArea, BorderLayout.CENTER);

        // Bottom: ButtonsBar
        add(buttonsBar, BorderLayout.SOUTH);
    }

    public void showPage(Page page) {
        pageArea.removeAll();
        pageArea.add(page);
        pageArea.revalidate();
        pageArea.repaint();
    }

    public ButtonsBar getButtonsBar() {
        return buttonsBar;
    }
}
