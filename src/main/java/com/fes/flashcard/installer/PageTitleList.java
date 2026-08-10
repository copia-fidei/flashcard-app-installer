package com.fes.flashcard.installer;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Component;

import static javax.swing.BorderFactory.createMatteBorder;

public class PageTitleList extends JList<String> {

	public PageTitleList() {
		super();

		setEnabled(false);
		setCellRenderer(new UnderlineSelected());
	}

	static class UnderlineSelected extends JLabel implements ListCellRenderer<Object> {

		public UnderlineSelected() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			setText(value.toString());
			setBorder(isSelected ? createMatteBorder(0, 0, 2, 0, list.getSelectionBackground()) : null);
			setBackground(list.getBackground());
			setForeground(list.getForeground());
			return this;
		}
	}


}
