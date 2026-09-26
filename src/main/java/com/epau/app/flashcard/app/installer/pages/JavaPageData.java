package com.epau.app.flashcard.app.installer.pages;

import com.epau.lib.swing.wizard.page.NoOpPageData;
import com.epau.lib.swing.wizard.page.PageDataPool;
import org.jetbrains.annotations.NonNls;

@NonNls
public class JavaPageData extends NoOpPageData {

	/// The Java version that must be installed and activated.
	public static final int TARGET_JAVA_VERSION = 21;

	// Values
	private static final String JAVA_PACKAGE_NAME = "openjdk-" + TARGET_JAVA_VERSION + "-jdk";

	public JavaPageData(PageDataPool pageDataPool) {
		super(pageDataPool);
	}

	public String getJavaPackageName() {
		return JAVA_PACKAGE_NAME;
	}
}
