package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.app.Karaf;
import com.fes.flashcard.installer.app.pages.DatabasePageData;
import com.fes.flashcard.installer.app.pages.KarafPageData;
import com.fes.flashcard.installer.operation.ErrorCode;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.utilities.Nls;
import com.fes.flashcard.installer.utilities.TextBuilder;

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
		var text = new TextBuilder();
		text.line(nls.get("InstallH2Operation.description.Install_H2_in_Karaf"));
		text.line("feature:install pax-jdbc-h2"); //NON-NLS
		text.line(nls.get("InstallH2Operation.description.Create_DataSource"));
		text.line(nls.get("InstallH2Operation.description.Create_database_tables"));
		text.line(nls.get("InstallH2Operation.description.Test_DataSource"));
		return text.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		var karaf = new Karaf(karafPageData.getKarafInstallationDir(), this);
		try {
			setProgress(1);
			karaf.start();
			setProgress(20);

			println("feature:install pax-jdbc-h2"); //NON-NLS
			setProgress(50);
			karaf.execute("feature:install pax-jdbc-h2");
			setProgress(60);

			println(nls.get("InstallH2Operation.description.Create_DataSource"));
			setProgress(70);
			createDataSource(karaf);
			setProgress(80);

			println(nls.get("InstallH2Operation.println.Create_database_tables"));
			setProgress(85);
			createTables(karaf);
			setProgress(90);

			println(nls.get("InstallH2Operation.println.Test_DataSource_(SELECT_version())"));
			setProgress(95);
			karaf.execute("jdbc:query " + dataSourceName + " SELECT VERSION();");
		} finally {
			println(nls.get("InstallH2Operation.println.Stop_Karaf"));
			karaf.stop();
		}
		setProgress(100);

		return nls.get("InstallH2Operation.println.H2_successfully_installed");
	}

	private void createDataSource(Karaf karaf) throws IOException, InterruptedException, ErrorCode {
		println(nls.get("InstallH2Operation.println.Delete_old_DataSource_{0}", dataSourceName));
		karaf.execute("jdbc:ds-delete " + dataSourceName);
		String url = "\"jdbc:h2:${karaf.data}/database/h2/" + databasePageData.getDbName() + ";MODE=PostgreSQL\""; //NON-NLS
		karaf.execute("jdbc:ds-create " + "-dc org.h2.Driver " + "-url " + url + " " + "-u " + databasePageData.getH2User() + " " + dataSourceName);
		println(nls.get("InstallH2Operation.println.DataSource_created_{0}", dataSourceName));
	}

	// H2 database will be created on first connection
	private void createTables(Karaf karaf) throws Exception {
		use(getTmpFile(H2_INIT_SQL), file -> {
			println(nls.get("InstallH2Operation.println.Execute_script_{0}", file));
			karaf.executeSuccessfully("jdbc:execute " + dataSourceName + " RUNSCRIPT FROM '" + file + "'");
			println(nls.get("InstallH2Operation.println.Tables_created"));
		});
	}
}