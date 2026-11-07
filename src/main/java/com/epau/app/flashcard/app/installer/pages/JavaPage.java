package com.epau.app.flashcard.app.installer.pages;

import com.epau.lib.swing.wizard.page.NoOpPage;
import com.epau.util.nls.Nls;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;

public class JavaPage extends NoOpPage {

	private final Nls nls = new Nls(this);

	private final JavaPageData javaPageData;

	private final JTextField javaPackageField = new JTextField();

	public JavaPage(JavaPageData pageData) {
		super(pageData);

		this.javaPageData = pageData;
	}

	@Override
	public void build() {
		var javaPackageLabel = new JLabel(nls.get("JavaPage.label.Package_name"));

		javaPackageField.setEditable(false);

		add(javaPackageLabel, 0, 0, 1, 1, 0.0, 0.0, LINE_START, NONE, 	   10, 10, 0, 10, 0, 0);
		add(javaPackageField, 1, 0, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, 10, 10, 0, 10, 0, 0);
		add(new JPanel(),  	  0, 1, 1, 1, 0.0, 1.0, LINE_START, HORIZONTAL, 10, 10, 0, 10, 0, 0);
	}

	@Override
	protected void fillGUI() {
		javaPackageField.setText(javaPageData.getJavaPackageName());
	}

	@Override
	public String getTitle() {
		return nls.get("JavaPage.title.Java");
	}

	@Override
	public String getDescription() {
		return nls.get("JavaPage.description.Installed_Java");
	}
}
