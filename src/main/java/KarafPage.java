import javax.swing.*;
import java.awt.*;

public class KarafPage extends Page {

    private final KarafPageData karafPageData;

    private JTextField karafDirField = new JTextField();

    public KarafPage(KarafPageData pageData) {
        super(pageData);
        this.karafPageData = pageData;
    }

    @Override
    public void build() {
        setLayout(new GridLayout(2, 2));

        add(new JLabel("Karaf Installation Directory:"));
        karafDirField = new JTextField();
        add(karafDirField);
    }

    @Override
    void fillGuiWithData() {
        karafDirField.setText(karafPageData.getKarafInstallationDir().toString());
    }

    @Override
    public String getTitle() {
        return "Karaf";
    }

    @Override
    public String getDescription() {
        return "Karaf Installation vorbereiten.";
    }
}
