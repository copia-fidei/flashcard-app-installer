package com.fes.flashcard.installer.utilities;

import org.jetbrains.annotations.NonNls;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static java.util.ResourceBundle.getBundle;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

// TODO into generic package
public final class Nls {

	private static final @NonNls Logger LOG = getLogger(Nls.class.getName());

	private final ResourceBundle bundle;

	public Nls(Class<?> cls) {
		this.bundle = getBundle(cls.getPackageName() + ".messages");
	}

	public Nls(Object cls) {
		this(cls.getClass());
	}

	public String get(@NonNls String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			LOG.log(WARNING, "Missing resource: " + key, e);
			return key;
		}
	}

	public String get(String key, Object... args) {
		return MessageFormat.format(get(key), args);
	}
}

