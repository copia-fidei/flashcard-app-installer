package com.fes.flashcard.installer.swing;

import org.jetbrains.annotations.NonNls;

import java.util.List;

public class DecisionDialogDemo {

	static void main() {
		@NonNls var options = List.of(
				new Option(
						"install",
						"Install the application",
						"Install the application on this computer"
				),
				new Option(
						"repair",
						"Repair the installation",
						"Repair the existing application installation"
				),
				new Option(
						"uninstall",
						"Uninstall the application",
						"Remove the application from this computer"
				)
		);
		TestFrames.showDialog(
				"DecisionDialog Demo", //NON-NLS
				parent -> new DecisionDialog(
						parent,
						"Choose an action", //NON-NLS
						"Please select what you would like to do:", //NON-NLS
						options,
						options.getFirst()
				)
		);
	}
}