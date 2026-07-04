package com.fes.flashcard.installer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.*;
import java.nio.file.Path;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;

// TODO open dialog to select karaf installation directory
public class KarafPage extends Page {

	private final KarafPageData karafPageData;

	private final JTextField karafDirField = new JTextField();

	public KarafPage(KarafPageData pageData, ButtonBar buttonBar) {
		super(pageData, buttonBar);
		this.karafPageData = pageData;
	}


	@Override
	public void build() {
		var karafDirLabel = new JLabel("Karaf Installation Directory:");

		content.add(karafDirLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 0, 10), 0, 0));
		content.add(karafDirField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		content.add(new JPanel(),  new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, LINE_START, HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
	}

	@Override
	protected void updatePageData() {
		// TODO make sure it is a path
		karafPageData.setKarafInstallationDir(Path.of(karafDirField.getText()));
	}

	@Override
	protected void addListeners() {
		karafDirField.getDocument().addDocumentListener(new DocumentAdapter(this::pageChanged));
	}

	@Override
	protected void fillGUI() {
		karafDirField.setText(karafPageData.getKarafInstallationDir().toString());
	}

	@Override
	public void updateGUI() {}

	@Override
	public void updateDependantValues() {}

	@Override
	public String getTitle() {
		return "Karaf";
	}

	@Override
	public String getDescription() {
		return "Karaf Installation vorbereiten.";
	}
}
