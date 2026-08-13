package com.fes.flashcard.installer.validation.dialog;

import com.fes.flashcard.installer.utilities.Nls;
import com.fes.flashcard.installer.validation.ValidationResults;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;
import static javax.swing.BoxLayout.LINE_AXIS;

public class ValidationResultsDialog extends JDialog {

	private static final Nls nls = new Nls(ValidationResultsDialog.class);

	private final ValidationResults validationResults;

	public ValidationResultsDialog(Window parent, ValidationResults validationResults) {
		super(parent, nls.get("ValidationResultsDialog.title"));
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
		var table      = new ValidationResultsTable(tableModel);

		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(550);
		table.getTableHeader().setReorderingAllowed(false);
		table.setFillsViewportHeight(true);
		table.getColumnModel().getColumn(1).setCellRenderer(new MultilineTableCellRenderer());

		((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

		var scrollPane = new JScrollPane(table);

		var closeButton = new JButton(nls.get("ValidationResultsDialog.button.close"));
		closeButton.addActionListener(_ -> dispose());

		var buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, LINE_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(closeButton);

		add(scrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, LINE_START, BOTH, new Insets(0, 0, 0, 0), 0, 0));
		add(buttonPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
	}

}
