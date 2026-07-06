package com.fes.flashcard.installer.app.pages;

import com.fes.flashcard.installer.PageDataPool;
import com.fes.flashcard.installer.page.PageData;
import com.fes.flashcard.installer.validation.ValidationResults;

import java.util.List;

public class JavaPageData extends PageData {

	// Values
	private final String javaPackageName = "openjdk-21-jdk";

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
		return javaPackageName;
	}
}
