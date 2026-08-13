package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.app.Karaf;
import com.fes.flashcard.installer.app.pages.DatabasePageData;
import com.fes.flashcard.installer.app.pages.KarafPageData;
import com.fes.flashcard.installer.operation.ErrorCode;
import com.fes.flashcard.installer.operation.Operation;
import com.fes.flashcard.installer.utilities.Nls;
import com.fes.flashcard.installer.utilities.TextBuilder;

import java.io.IOException;

public class CreatePostgresDataSourceOp extends Operation {

	private static final Nls nls = new Nls(CreatePostgresDataSourceOp.class);

	private final KarafPageData    karafPageData;
	private final DatabasePageData databasePageData;

	private final String dataSourceName;

	public CreatePostgresDataSourceOp(KarafPageData karafPageData, DatabasePageData databasePageData) {
		super(nls.get("CreatePostgresDataSourceOp.title"), nls.get("CreatePostgresDataSourceOp.description"));

		this.karafPageData = karafPageData;
		this.databasePageData = databasePageData;

		dataSourceName = databasePageData.getDataSourceName();
	}

	public String getDescription() {
		var text = new TextBuilder();
		text.line(nls.get("CreatePostgresDataSourceOp.description.installKarafJDBCFeature"));
		text.line(nls.get("CreatePostgresDataSourceOp.description.installPaxJDBCPostgreSQLDriver"));
		text.line(nls.get("CreatePostgresDataSourceOp.description.createDataSourceForPostgreSQL"));
		text.line(nls.get("CreatePostgresDataSourceOp.description.testDataSource"));
		return text.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		var karaf = new Karaf(karafPageData.getKarafInstallationDir(), this);
		try {
			setProgress(1);
			karaf.start();
			setProgress(20);
			println(nls.get("CreatePostgresDataSourceOp.println.installKarafJDBCFeature"));
			setProgress(30);
			karaf.execute("feature:install jdbc");
			println(nls.get("CreatePostgresDataSourceOp.println.jdbcFeatureInstalledSuccessfully"));
			setProgress(40);

			println(nls.get("CreatePostgresDataSourceOp.println.installPaxJDBCPostgreSQLDriver"));
			setProgress(50);
			karaf.execute("feature:install pax-jdbc-postgresql");
			println(nls.get("CreatePostgresDataSourceOp.println.paxJDBCPostgreSQLDriverInstalledSuccessfully"));
			setProgress(60);

			println(nls.get("CreatePostgresDataSourceOp.println.createDataSourceForPostgreSQL"));
			setProgress(70);
			createDataSource(karaf);
			setProgress(80);

			println(nls.get("CreatePostgresDataSourceOp.println.testDataSource"));
			setProgress(90);
			karaf.execute("jdbc:query " + dataSourceName + " SELECT version();");
			println(nls.get("CreatePostgresDataSourceOp.println.dataSourceTestSuccessful"));

		} finally {
			println(nls.get("CreatePostgresDataSourceOp.println.stopKaraf"));
			karaf.stop();
		}
		setProgress(100);

		return nls.get("CreatePostgresDataSourceOp.println.dataSourceCreatedSuccessfullyFinal");
	}

	private void createDataSource(Karaf karaf) throws IOException, InterruptedException, ErrorCode {
		println(nls.get("CreatePostgresDataSourceOp.println.deleteDataSource", dataSourceName));
		karaf.execute("jdbc:ds-delete " + dataSourceName);

		String dbName   = databasePageData.getDbName();
		String user     = databasePageData.getPostgresUser();
		String password = databasePageData.getPostgresUserPassword();
		String port     = databasePageData.getPostgresPort();
		String host     = databasePageData.getPostgresHost();

		String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
		String command = "jdbc:ds-create -dc org.postgresql.Driver -url " + url + " --username " + user + " --password " + password + " " + dataSourceName;
		karaf.execute(command);
		println(nls.get("CreatePostgresDataSourceOp.println.dataSourceCreatedSuccessfully", dataSourceName));
	}
}
