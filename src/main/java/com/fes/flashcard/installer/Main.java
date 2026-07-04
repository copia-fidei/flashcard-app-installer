import com.fes.flashcard.installer.PageFrame;
import com.fes.flashcard.installer.PagePool;

import static javax.swing.SwingUtilities.invokeLater;

void main() {
	invokeLater(() -> {
		var pageFrame = new PageFrame();
		var pagePool  = new PagePool(pageFrame);

		//		buttonsBar.getApplyButton().addActionListener(_ -> {
		//			new apply.ApplyDialog();
		//		});

		pagePool.init();
		//		pageFrame.build();
		pagePool.showFirstPage();
		pageFrame.setLocationRelativeTo(null);
		pageFrame.pack();
		pageFrame.setVisible(true);
	});
}
