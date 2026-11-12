package com.epau.app.flashcard.app.installer;

import com.epau.app.flashcard.app.installer.operations.ConfigureKarafOp;
import com.epau.app.flashcard.app.installer.operations.ConfigurePostgresOp;
import com.epau.app.flashcard.app.installer.operations.CreatePostgresDataSourceOp;
import com.epau.app.flashcard.app.installer.operations.InstallH2Operation;
import com.epau.app.flashcard.app.installer.operations.InstallJavaOp;
import com.epau.app.flashcard.app.installer.operations.InstallKarafOp;
import com.epau.app.flashcard.app.installer.operations.InstallPostgresOp;
import com.epau.app.flashcard.app.installer.pages.DatabasePageData;
import com.epau.app.flashcard.app.installer.pages.JavaPageData;
import com.epau.app.flashcard.app.installer.pages.KarafPageData;
import com.epau.app.flashcard.app.installer.pages.RootPasswordPageData;
import com.epau.util.swing.operation.Operation;

import java.util.ArrayList;
import java.util.function.Supplier;

public class Operations {

	public static ArrayList<Supplier<Operation>> from(DatabasePageData databasePageData, JavaPageData javaPageData, KarafPageData karafPageData, RootPasswordPageData rootPwPageData) {
		var operations = new ArrayList<Supplier<Operation>>();

		operations.add(() -> new InstallJavaOp(javaPageData));
		operations.add(() -> new InstallKarafOp(karafPageData));
		operations.add(() -> new ConfigureKarafOp(karafPageData));

		switch (databasePageData.getDatabaseImplementation()) {
			case PostgreSQL -> {
				operations.add(() -> new InstallPostgresOp(databasePageData, rootPwPageData));
				operations.add(() -> new ConfigurePostgresOp(databasePageData));
				operations.add(() -> new CreatePostgresDataSourceOp(karafPageData, databasePageData));
			}
			case H2 -> {
				operations.add(() -> new InstallH2Operation(karafPageData, databasePageData));
			}
		}
		return operations;
	}
}
