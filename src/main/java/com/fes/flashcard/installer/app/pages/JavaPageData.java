package com.fes.flashcard.installer.app.pages;

import com.fes.flashcard.installer.page.PageData;
import com.fes.flashcard.installer.page.PageDataPool;
import com.fes.flashcard.installer.validation.ValidationResults;
import org.jetbrains.annotations.NonNls;

import java.util.List;

@NonNls
public class JavaPageData extends PageData {

	/// The Java version that must be installed and activated.
	public static final int TARGET_JAVA_VERSION = 21;

	// Values
//	private static final String JAVA_PACKAGE_NAME = "openjdk-" + TARGET_JAVA_VERSION + "-jdk";

	private static final String JAVA_PACKAGE_NAME = "openjdk-21-jdk";

	public JavaPageData(PageDataPool pageDataPool) {
		super(pageDataPool);
	}

	@Override
	public void load() {}

	@Override
	public void save() {}

	@Override
	public void loadDefaults() {}

	@Override
	public ValidationResults validate() {
		return new ValidationResults(List.of());
	}

	public String getJavaPackageName() {
		return JAVA_PACKAGE_NAME;
	}
}
