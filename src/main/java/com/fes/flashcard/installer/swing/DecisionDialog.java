package com.fes.flashcard.installer.swing;

import com.fes.flashcard.installer.utilities.Nls;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.FIRST_LINE_START;
import static java.awt.GridBagConstraints.LINE_END;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager;
import static javax.swing.BoxLayout.LINE_AXIS;
import static javax.swing.SwingUtilities.invokeAndWait;

public class DecisionDialog extends JDialog {

	private final Nls nls = new Nls(this);

	private final String       description;
	private final ButtonGroup  buttonGroup = new ButtonGroup();
	private final JButton      okButton    = new JButton("OK");
	private final List<Option> options     = new ArrayList<>();
	private final Option       defaultChoice;

	public DecisionDialog(Window parent, String title, String description, List<Option> options, Option defaultChoice) {
		super(parent, title, ModalityType.APPLICATION_MODAL);

		this.description = description;
		assert !options.isEmpty();
		this.options.addAll(options);
		this.defaultChoice = defaultChoice;

		buildGUI();
		addListeners();
	}

	public static Option showDialog(String title, String description, List<Option> options, Option defaultChoice) throws InterruptedException, InvocationTargetException {
		Option[] choice = new Option[1];
		invokeAndWait(() -> {
			var dialog = new DecisionDialog(getCurrentKeyboardFocusManager().getFocusedWindow(), title, description, options, defaultChoice);
			dialog.setVisible(true);
			choice[0] = dialog.getSelected();
		});
		return choice[0];
	}

	private void buildGUI() {
		setLayout(new GridBagLayout());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		int lineCount           = (int) description.lines().count();
		var descriptionTextArea = new JTextArea(lineCount, 55);
		descriptionTextArea.setText(description);
		descriptionTextArea.setEditable(false);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setBackground(getBackground());

		for (var option : options) {
			var radioButton = new JRadioButton(option.description());
			radioButton.setToolTipText(option.tooltip());
			radioButton.putClientProperty("option", option);
			buttonGroup.add(radioButton);
			if (option == defaultChoice) {
				radioButton.setSelected(true);
			}
		}
		okButton.setToolTipText(nls.get("DecisionDialog.tooltip.Apply_selection"));

		var buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, LINE_AXIS));
		buttonPanel.add(okButton);

		add(descriptionTextArea, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, FIRST_LINE_START, BOTH, new Insets(10, 10, 10, 10), 0, 0));
		for (var buttons = buttonGroup.getElements(); buttons.hasMoreElements(); ) {
			var button = buttons.nextElement();
			add(button, new GridBagConstraints(0, -1, 1, 1, 0.0, 0.0, FIRST_LINE_START, NONE, new Insets(10, 10, 0, 10), 0, 0));
		}
		add(new JPanel(), new GridBagConstraints(0, -1, 1, 1, 1.0, 1.0, FIRST_LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		add(buttonPanel, new GridBagConstraints(0, -1, 1, 1, 0.0, 0.0, LINE_END, NONE, new Insets(10, 10, 10, 10), 0, 0));

		pack();
		setLocationRelativeTo(getParent());
	}


	public Option getSelected() {
		var selected = buttonGroup.getSelection();
		for (var buttons = buttonGroup.getElements(); buttons.hasMoreElements(); ) {
			var button = buttons.nextElement();
			if (button.getModel() == selected) {
				return (Option) button.getClientProperty("option");
			}
		}
		throw new RuntimeException("no option selected");
	}

	private void addListeners() {
		okButton.addActionListener(_ -> {
			dispose();
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
	}
}
