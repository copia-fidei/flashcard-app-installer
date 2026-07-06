package com.fes.flashcard.installer.app.pages;

import com.fes.flashcard.installer.page.Page;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.*;
import java.awt.event.ActionListener;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;

public class KarafPage extends Page {

	private final KarafPageData karafPageData;

	private final JTextField karafDirField = new JTextField();
	private final JFileChooser fileChooser = new JFileChooser();

	private final JButton selectKarafDirBtn = new JButton("Auswahl");

	private final ActionListener selectButtonListener = _ -> {
		if (fileChooser.showOpenDialog(getContent()) == JFileChooser.APPROVE_OPTION) {
			pageChanged();
		}
	};

	public KarafPage(KarafPageData pageData, Runnable onValidationChanged) {
		super(pageData, onValidationChanged);
		this.karafPageData = pageData;

	}


	@Override
	public void build() {
		var karafDirLabel = new JLabel("Installationsverzeichnis");

		fileChooser.setFileSelectionMode(DIRECTORIES_ONLY);
		fileChooser.setFileHidingEnabled(false);
		karafDirField.setEditable(false);

		content.add(karafDirLabel, 		new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, LINE_START, NONE, 		 new Insets(10, 10, 0, 10), 0, 0));
		content.add(karafDirField, 		new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		content.add(selectKarafDirBtn,  new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, LINE_START, NONE, 		 new Insets(10, 10, 0, 10), 0, 0));
		// Filler
		content.add(new JPanel(),  		new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0, LINE_START, BOTH, new Insets(10, 10, 0, 10), 0, 0));
	}

	@Override
	protected void updatePageData() {
		karafPageData.setKarafInstallationDir(fileChooser.getSelectedFile().toPath());
	}

	@Override
	protected void addListeners() {
		selectKarafDirBtn.addActionListener(selectButtonListener);
	}

	@Override
	protected void removeListeners() {
		selectKarafDirBtn.removeActionListener(selectButtonListener);
	}

	@Override
	protected void fillGUI() {
		karafDirField.setText(karafPageData.getKarafInstallationDir().toString());
		fileChooser.setSelectedFile(karafPageData.getKarafInstallationDir().toFile());
	}

	@Override
	public void updateGUI() {
		karafDirField.setText(fileChooser.getSelectedFile().toString());
	}

	@Override
	public void updateDependantValues() {}

	@Override
	public String getTitle() {
		return "Karaf";
	}

	@Override
	public String getDescription() {
		return "Karaf Installation vorbereiten";
	}
}
