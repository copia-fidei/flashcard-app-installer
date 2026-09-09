package com.epau.app.flashcard.app.installer;

import org.jetbrains.annotations.NonNls;

import java.text.MessageFormat;

public interface PgHba {

	/// Entries that are added to pg_hba.conf, so that the user can connect to the database
	@NonNls
	static String getEntries(String database, String user) {
		return MessageFormat.format("""
				host    {0}     {1}      127.0.0.1/32            scram-sha-256
				host    {0}     {1}      ::1/128                 scram-sha-256
				""", database, user);
	}
}
