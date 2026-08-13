package com.fes.flashcard.installer.page;

import com.fes.flashcard.installer.validation.ValidationResults;
import org.jetbrains.annotations.NonNls;

import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static java.util.logging.Logger.getLogger;
import static java.util.prefs.Preferences.userNodeForPackage;

public abstract class PageData {

	protected final @NonNls Logger log = getLogger(getClass().getName());

    protected Preferences preferences = userNodeForPackage(this.getClass());

    protected PageDataPool pageDataPool;

    public PageData(PageDataPool pageDataPool) {
        this.pageDataPool = pageDataPool;
	}

    public abstract void load();

	public abstract void save();

	public abstract ValidationResults validate();

    public abstract void loadDefaults();
}
