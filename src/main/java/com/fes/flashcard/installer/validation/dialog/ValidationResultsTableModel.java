package com.fes.flashcard.installer.validation.dialog;

import com.fes.flashcard.installer.validation.ValidationResult;
import com.fes.flashcard.installer.validation.ValidationResults;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

class ValidationResultsTableModel extends AbstractTableModel {

	private final List<ValidationResult> sortedResults = new ArrayList<>();

	public ValidationResultsTableModel(ValidationResults validationResults) {
		this.sortedResults.addAll(validationResults.list());
		this.sortedResults.sort(new ValidationResultComparator());
	}

	@Override
	public int getRowCount() {
		return sortedResults.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int column) {
		return switch (column) {
			case 0 -> "Level";
			case 1 -> "Beschreibung";
			default -> "";
		};
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < sortedResults.size()) {
			ValidationResult result = sortedResults.get(rowIndex);
			return switch (columnIndex) {
				case 0 -> result.severity().getDisplayName();
				case 1 -> result.description();
				default -> "";
			};
		}
		return "";
	}
}
