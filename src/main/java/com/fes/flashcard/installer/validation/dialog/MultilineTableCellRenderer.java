package com.fes.flashcard.installer.validation.dialog;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;

class MultilineTableCellRenderer extends JTextArea implements TableCellRenderer {

	public MultilineTableCellRenderer() {
		setLineWrap(true);
		setWrapStyleWord(true);
		setOpaque(true);
		setBorder(null);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		setText(value == null ? "" : value.toString());
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}

		return this;
	}
}
