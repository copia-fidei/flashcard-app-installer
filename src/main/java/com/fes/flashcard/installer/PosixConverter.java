package com.fes.flashcard.installer;

import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public interface PosixConverter {

	static Set<PosixFilePermission> posixPermissionsFromDecimal(int decimal) {
		return PosixFilePermissions.fromString(posixStringFromOctal(decimal));
	}

	static String posixStringFromOctal(int decimal) {
		return posixStringFromOctal(Integer.toOctalString(decimal));
	}

	static String posixStringFromOctal(String octal) {
		// Keep only the last 3 octal digits (owner/group/other permissions).
		// This filters out number like 100644 that include the file type bits.
		if (octal.length() > 3) {
			octal = octal.substring(octal.length() - 3);
		}
		var sb = new StringBuilder();
		for (char character : octal.toCharArray()) {
			int num = Character.digit(character, 8);
			sb.append((num & 4) == 0 ? '-' : 'r');
			sb.append((num & 2) == 0 ? '-' : 'w');
			sb.append((num & 1) == 0 ? '-' : 'x');
		}
		return sb.toString();
	}
}
