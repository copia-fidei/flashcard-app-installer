import static javax.swing.SwingUtilities.invokeLater;

void main() {
	invokeLater(() -> {
		var pageTitleList = new PageTitleList();
		var buttonsBar    = new ButtonsBar();
		var pageFrame     = new PageFrame(pageTitleList, buttonsBar);
		var pagePool      = new PagePool(pageTitleList, pageFrame);

//		buttonsBar.getApplyButton().addActionListener(_ -> {
//			new apply.ApplyDialog();
//		});

		pagePool.init();
		pageFrame.build();
		pagePool.showFirstPage();
		pageFrame.setLocationRelativeTo(null);
		pageFrame.pack();
		pageFrame.setVisible(true);
	});
}
