package com.epau.app.flashcard.app.installer.pages;

import com.epau.lib.swing.wizard.page.Page;
import com.epau.util.nls.Nls;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;

public class JavaPage extends Page {

	private final Nls nls = new Nls(this);

	private final JavaPageData javaPageData;

	private final JTextField javaPackageField = new JTextField();

	public JavaPage(JavaPageData pageData, Runnable onValidationChanged) {
		super(pageData, onValidationChanged);

		this.javaPageData = pageData;
	}


	@Override
	public void build() {
		var javaPackageLabel = new JLabel(nls.get("JavaPage.label.Package_name"));

		javaPackageField.setEditable(false);

		content.add(javaPackageLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, LINE_START, NONE, 	   new Insets(10, 10, 0, 10), 0, 0));
		content.add(javaPackageField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		content.add(new JPanel(),  	  new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, LINE_START, HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
	}

	@Override
	protected void updatePageData() {}

	@Override
	protected void addListeners() {}

	@Override
	protected void removeListeners() {}

	@Override
	protected void fillGUI() {
		javaPackageField.setText(javaPageData.getJavaPackageName());
	}

	@Override
	public void updateGUI() {}

	@Override
	public void updateDependantValues() {}

	@Override
	public String getTitle() {
		return nls.get("JavaPage.title.Java");
	}

	@Override
	public String getDescription() {
		return nls.get("JavaPage.description.Installed_Java");
	}
}
