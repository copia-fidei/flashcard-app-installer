package com.fes.flashcard.installer.app;

import org.jetbrains.annotations.NonNls;

/// see https://www.postgresql.org/docs/current/auth-methods.html
/// not all authentication methods are supported by this installer
@NonNls
public interface AuthMethod {

	static boolean isSupportedForAdmin(String authMethod) {
		return authMethod.equals("peer") || authMethod.equals("trust") || authMethod.equals("scram-sha-256") || authMethod.equals("md5") || authMethod.equals("password");
	}

	/// for regular users, use isSupportedForAdmin to check for the role postgres
	static boolean isSupported(String authMethod) {
		return authMethod.equals("trust") || authMethod.equals("scram-sha-256") || authMethod.equals("md5") || authMethod.equals("password");
	}

	static boolean isPasswordBased(String authMethod) {
		return authMethod.equals("scram-sha-256") || authMethod.equals("md5") || authMethod.equals("password");
	}
}
