package com.epau.app.flashcard.app.installer.app.operations;

import com.epau.app.flashcard.app.installer.app.Karaf;
import com.epau.app.flashcard.app.installer.app.pages.DatabasePageData;
import com.epau.app.flashcard.app.installer.app.pages.KarafPageData;
import com.epau.installer.utilities.TextBuilder;
import com.epau.utilities.nls.Nls;
import com.epau.utilities.swing.operation.ErrorCode;
import com.epau.utilities.swing.operation.Operation;

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
		text.line("feature:install pax-jdbc-postgresql"); //$NON-NLS
		text.line(nls.get("CreatePostgresDataSourceOp.description.Create_DataSource_for_PostgreSQL_database"));
		text.line(nls.get("CreatePostgresDataSourceOp.description.Test_DataSource"));
		return text.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		var karaf = new Karaf(karafPageData.getKarafInstallationDir(), this);
        try {
			setProgress(1);
			karaf.start();
			setProgress(30);
			println("feature:install pax-jdbc-postgresql"); //$NON-NLS
			karaf.execute("feature:install pax-jdbc-postgresql");
			setProgress(50);
			println(nls.get("CreatePostgresDataSourceOp.println.Pax_JDBC_PostgreSQL_Driver_installed_successfully"));
			setProgress(60);
			println(nls.get("CreatePostgresDataSourceOp.println.Create_DataSource_for_PostgreSQL"));
			createDataSource(karaf);
			setProgress(80);
			println(nls.get("CreatePostgresDataSourceOp.println.Test_DataSource"));
			karaf.execute("jdbc:query " + dataSourceName + " SELECT version();");
			setProgress(90);
			println(nls.get("CreatePostgresDataSourceOp.println.Stop_Karaf"));
		}
		finally {
			karaf.stop();
		}
		setProgress(100);

		return nls.get("CreatePostgresDataSourceOp.println.DataSource_created_successfully");
	}

	private void createDataSource(Karaf karaf) throws IOException, InterruptedException, ErrorCode {
		println(nls.get("CreatePostgresDataSourceOp.println.Delete_DataSource_{0}", dataSourceName));
		karaf.execute("jdbc:ds-delete " + dataSourceName);

		String dbName   = databasePageData.getDbName();
		String user     = databasePageData.getPostgresUser();
		String password = databasePageData.getPostgresUserPassword();
		String port     = databasePageData.getPostgresPort();
		String host     = databasePageData.getPostgresHost();

		String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName; //NON-NLS
		String command = "jdbc:ds-create -dc org.postgresql.Driver -url " + url + " --username " + user + " --password " + password + " " + dataSourceName;
		karaf.execute(command);
		println(nls.get("CreatePostgresDataSourceOp.println.DataSource_created_successfully_{0}", dataSourceName));
	}
}
