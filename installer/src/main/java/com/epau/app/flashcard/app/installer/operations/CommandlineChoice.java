package com.epau.app.flashcard.app.installer.operations;

import com.epau.util.swing.operation.Operation;
import org.jetbrains.annotations.NonNls;

import static com.epau.app.flashcard.app.installer.operations.Choice.KEEP;
import static com.epau.app.flashcard.app.installer.operations.Choice.REINSTALL;
import static java.lang.IO.println;

@NonNls
interface CommandlineChoice {

	static Choice get(Operation op, String application) {
		op.println(application + " is already installed. Decide");
		op.println("1) Keep installation (default)");
		op.println("2) Reinstall");
		var console = System.console();
		for (int attempt = 1; attempt <= 3; attempt++) {
			switch (console.readLine("Enter your choice: ")) { //NON-NLS
				case "1", "1)" -> {
					return KEEP;
				}
				case "2", "2)" -> {
					return REINSTALL;
				}
				default -> console.printf("Invalid choice. Failed attempts (%d/3).%n", attempt); //NON-NLS
			}
		}
		println("Maximum number of attempts exceeded. Defaulting to option 1."); //NON-NLS
		return KEEP;
	}
}