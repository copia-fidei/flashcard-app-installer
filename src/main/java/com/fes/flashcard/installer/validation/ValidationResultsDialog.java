package com.fes.flashcard.installer.validation;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;
import static javax.swing.BoxLayout.LINE_AXIS;

// TODO resize behaviour
// FIXME text cannot be viewed if it too long
// TODO open message, or format cell like text area
public class ValidationResultsDialog extends JDialog {

	private final ValidationResults validationResults;

	public ValidationResultsDialog(Window parent, ValidationResults validationResults) {
		super(parent, "Validierungsergebnisse");
		this.validationResults = validationResults;

		buildGUI();
		pack();
		setSize(700, 500);
		setLocationRelativeTo(parent);
		setModal(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void buildGUI() {
		setLayout(new GridBagLayout());

		var tableModel = new ValidationResultsTableModel(validationResults);
		var table      = new JTable(tableModel);
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(550);
		table.setRowHeight(25);
		table.setShowHorizontalLines(false);
		table.getTableHeader().setReorderingAllowed(false);
		table.setFillsViewportHeight(true);

		((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

		var scrollPane = new JScrollPane(table);

		var closeButton = new JButton("Schließen");
		closeButton.addActionListener(_ -> dispose());

		var buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, LINE_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(closeButton);

		add(scrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, LINE_START, BOTH, new Insets(0, 0, 0, 0), 0, 0));
		add(buttonPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
	}

	private static class ValidationResultsTableModel extends AbstractTableModel {

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

	private static class ValidationResultComparator implements Comparator<ValidationResult> {

		@Override
		public int compare(ValidationResult result1, ValidationResult result2) {
			int severityComparison = Integer.compare(result2.severity().ordinal(), result1.severity().ordinal());
			if (severityComparison != 0) {
				return severityComparison;
			}
			return Integer.compare(result2.priority(), result1.priority());
		}
	}
}
