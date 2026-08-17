package com.fes.flashcard.installer.app.pages;

import com.fes.flashcard.installer.page.Page;
import com.fes.flashcard.installer.swing.DocumentAdapter;
import com.fes.flashcard.installer.utilities.Nls;
import org.jetbrains.annotations.NonNls;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.FIRST_LINE_START;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;

public class KarafPage extends Page {

	private final Nls nls = new Nls(this);

	private final KarafPageData karafPageData;

	private final JTextField   parentDirField  = new JTextField(50);
	private final JTextField   folderNameField = new JTextField(50);
	private final JTextArea    fullPathText    = new JTextArea(2, 50);
	private final JFileChooser fileChooser     = new JFileChooser();

	private final JButton selectParentDirBtn = new JButton(nls.get("KarafPage.button.Browse"));

	private final ActionListener  selectButtonListener = _ -> {
		if (fileChooser.showOpenDialog(getContent()) == APPROVE_OPTION) {
			pageChanged();
		}
	};
	private final DocumentAdapter textfieldListener    = new DocumentAdapter(this::pageChanged);

	public KarafPage(KarafPageData pageData, Runnable onValidationChanged) {
		super(pageData, onValidationChanged);
		this.karafPageData = pageData;
	}


	@Override
	public void build() {
		var parentDirLabel  = new JLabel(nls.get("KarafPage.label.Parent_Directory"));
		var folderNameLabel = new JLabel(nls.get("KarafPage.label.Folder_Name"));


		fullPathText.setEditable(false);
		fullPathText.setLineWrap(true);
		fullPathText.setWrapStyleWord(true);
		fullPathText.setOpaque(false);
		fullPathText.setFocusable(false);
		fileChooser.setFileSelectionMode(DIRECTORIES_ONLY);
		fileChooser.setFileHidingEnabled(false);

		parentDirField.setEditable(false);
		parentDirField.setFocusable(false);

		content.add(parentDirLabel, 	new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 0, 10), 0, 0));
		content.add(parentDirField, 	new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		content.add(selectParentDirBtn, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, LINE_START, NONE, new Insets(10, 10, 0, 10), 0, 0));
		content.add(folderNameLabel, 	new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0, LINE_START, NONE, new Insets(10, 10, 0, 10), 0, 0));
		content.add(folderNameField, 	new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		content.add(fullPathText, 		new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0, FIRST_LINE_START, BOTH, new Insets(10, 10, 0, 10), 0, 0));

		content.add(new JPanel(), new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0, FIRST_LINE_START, BOTH, new Insets(10, 10, 0, 10), 0, 0));
	}

	@Override
	protected void updatePageData() {
		karafPageData.setParentDir(fileChooser.getSelectedFile().toPath());
		karafPageData.setFolderName(folderNameField.getText());
	}

	@Override
	protected void addListeners() {
		selectParentDirBtn.addActionListener(selectButtonListener);
		folderNameField.getDocument().addDocumentListener(textfieldListener);
	}

	@Override
	protected void removeListeners() {
		selectParentDirBtn.removeActionListener(selectButtonListener);
		folderNameField.getDocument().removeDocumentListener(textfieldListener);

	}

	@Override
	protected void fillGUI() {
		parentDirField.setText(karafPageData.getParentDir().toString());
		folderNameField.setText(karafPageData.getFolderName());
		fileChooser.setSelectedFile(karafPageData.getParentDir().toFile());
		updateDependantValues();
	}

	@Override
	public void updateGUI() {
		parentDirField.setText(fileChooser.getSelectedFile().toString());
	}

	@Override
	public void updateDependantValues() {
		fullPathText.setText(nls.get("KarafPage.description.Karaf_will_be_installed_to_{0}_{1}", fileChooser.getSelectedFile().toString(), karafPageData.getFolderName()));
	}

	@Override
	public @NonNls String getTitle() {
		return "Karaf";
	}

	@Override
	public String getDescription() {
		return nls.get("KarafPage.description.Karaf_Installation_Location");
	}
}
