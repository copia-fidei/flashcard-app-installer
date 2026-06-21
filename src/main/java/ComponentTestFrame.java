import javax.swing.*;
import java.awt.*;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public interface ComponentTestFrame {

	static void show(String title, Component comp) {
		invokeLater(() -> {
			var frame = new JFrame(title);
			frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
			frame.add(comp);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
}
