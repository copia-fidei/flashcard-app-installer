package com.fes.flashcard.installer.validation.dialog;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;

class ValidationResultsTable extends JTable {

	public ValidationResultsTable(ValidationResultsTableModel model) {
		super(model);
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {

		Component component = super.prepareRenderer(renderer, row, column);

		if (component instanceof JTextArea textArea) {
			int width = getColumnModel().getColumn(column).getWidth();

			textArea.setSize(width, Short.MAX_VALUE);

			int preferredHeight = textArea.getPreferredSize().height + 2; // padding for descenders
			if (getRowHeight(row) != preferredHeight) {
				setRowHeight(row, preferredHeight);
			}
		}

		return component;
	}
}
