package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.app.Karaf;
import com.fes.flashcard.installer.app.pages.DatabasePageData;
import com.fes.flashcard.installer.app.pages.KarafPageData;
import com.fes.flashcard.installer.operation.ErrorCode;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.utilities.Nls;

import java.io.IOException;

import static com.fes.flashcard.installer.app.Resources.H2_INIT_SQL;
import static com.fes.flashcard.installer.app.Resources.getTmpFile;
import static com.fes.flashcard.installer.utilities.Temporary.use;

public class InstallH2Operation extends Operation {

	private static final Nls nls = new Nls(InstallH2Operation.class);

	private final KarafPageData    karafPageData;
	private final DatabasePageData databasePageData;

	private final String h2Version;
	private final String dataSourceName;

	public InstallH2Operation(KarafPageData karafPageData, DatabasePageData databasePageData) {
		super(nls.get("InstallH2Operation.title"), nls.get("InstallH2Operation.description"));

		this.karafPageData = karafPageData;
		this.databasePageData = databasePageData;
		this.h2Version = databasePageData.getSelectedH2Version();

		dataSourceName = databasePageData.getDataSourceName();
	}

	public String getDescription() {
		return nls.get("InstallH2Operation.description.installKarafJDBCFeature") + "\n" +
		       nls.get("InstallH2Operation.description.installPaxJDBCH2DriverForVersion", h2Version) + "\n" +
		       nls.get("InstallH2Operation.description.createH2Database") + "\n" +
		       nls.get("InstallH2Operation.description.createDataSourceForH2") + "\n" +
		       nls.get("InstallH2Operation.description.createDatabaseTables") + "\n" +
		       nls.get("InstallH2Operation.description.testDataSource");
	}

	@Override
	protected String doInBackground() throws Exception {
		var karaf = new Karaf(karafPageData.getKarafInstallationDir(), this);
		try {
			setProgress(1);
			karaf.start();
			setProgress(20);

			println(nls.get("InstallH2Operation.println.installPaxJDBCH2Feature"));
			setProgress(50);
			karaf.execute("feature:install pax-jdbc-h2");
			println(nls.get("InstallH2Operation.println.paxJDBCH2DriverInstalled"));
			setProgress(60);

			println(nls.get("InstallH2Operation.println.createDataSource"));
			setProgress(70);
			createDataSource(karaf);
			setProgress(80);

			println(nls.get("InstallH2Operation.println.createDatabaseTables"));
			setProgress(85);
			createTables(karaf);
			setProgress(90);

			println(nls.get("InstallH2Operation.println.testDataSource"));
			setProgress(95);
			karaf.execute("jdbc:query " + dataSourceName + " SELECT VERSION();");
			println(nls.get("InstallH2Operation.println.dataSourceTestSuccessful"));
		} finally {
			println(nls.get("InstallH2Operation.println.stopKaraf"));
			karaf.stop();
		}
		setProgress(100);

		return nls.get("InstallH2Operation.println.h2DatabaseAndDataSourceCreatedSuccessfully");
	}

	private void createDataSource(Karaf karaf) throws IOException, InterruptedException, ErrorCode {
		println(nls.get("InstallH2Operation.println.deleteDataSource", dataSourceName));
		karaf.execute("jdbc:ds-delete " + dataSourceName);
		String url = "\"jdbc:h2:${karaf.data}/database/h2/" + databasePageData.getDbName() + ";MODE=PostgreSQL\""; //NON-NLS
		karaf.execute("jdbc:ds-create " + "-dc org.h2.Driver " + "-url " + url + " " + "-u " + databasePageData.getH2User() + " " + dataSourceName);
		println(nls.get("InstallH2Operation.println.dataSourceCreated", dataSourceName));
	}

	// H2 database will be created on first connection
	private void createTables(Karaf karaf) throws Exception {
		use(getTmpFile(H2_INIT_SQL), file -> {
			println(nls.get("InstallH2Operation.println.databaseTablesBeingCreated"));
			println(nls.get("InstallH2Operation.println.scriptBeingExecuted", file));
			karaf.executeSuccessfully("jdbc:execute " + dataSourceName + " RUNSCRIPT FROM '" + file + "'");
			println(nls.get("InstallH2Operation.println.databaseTablesCreated"));
		});
	}
}