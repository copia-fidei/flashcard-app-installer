package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.TextBuilder;
import com.fes.flashcard.installer.app.Karaf;
import com.fes.flashcard.installer.app.pages.DatabasePageData;
import com.fes.flashcard.installer.app.pages.KarafPageData;
import com.fes.flashcard.installer.operation.ErrorCode;
import com.fes.flashcard.installer.operation.Operation;

import java.io.IOException;

public class CreatePostgresDataSourceOp extends Operation {

	private final KarafPageData    karafPageData;
	private final DatabasePageData databasePageData;

	private final String dataSourceName;

	public CreatePostgresDataSourceOp(KarafPageData karafPageData, DatabasePageData databasePageData) {
		super("PostgreSQL DataSource erstellen", "Erstelle Karaf JDBC DataSource");

		this.karafPageData = karafPageData;
		this.databasePageData = databasePageData;

		dataSourceName = databasePageData.getDataSourceName();
	}

	public String getDescription() {
		var text = new TextBuilder();
		text.line("Installiere das Karaf JDBC Feature");
		text.line("Installiere den Pax JDBC PostgreSQL Driver");
		text.line("Erstelle die DataSource für die PostgreSQL Datenbank");
		text.line("Teste die DataSource");
		return text.toString();
	}

	@Override
	protected String doInBackground() throws Exception {
		var karaf = new Karaf(karafPageData.getKarafInstallationDir(), this);
		try {
			setProgress(1);
			karaf.start();
			setProgress(20);
			println("Installiere das Karaf JDBC Feature");
			setProgress(30);
			karaf.execute("feature:install jdbc");
			println("JDBC Feature erfolgreich installiert");
			setProgress(40);

			println("Installiere den Pax JDBC PostgreSQL Driver");
			setProgress(50);
			karaf.execute("feature:install pax-jdbc-postgresql");
			println("Pax JDBC PostgreSQL Driver erfolgreich installiert");
			setProgress(60);

			println("Erstelle die DataSource für die PostgreSQL Datenbank");
			setProgress(70);
			createDataSource(karaf);
			setProgress(80);

			println("Teste DataSource (SELECT version();)");
			setProgress(90);
			karaf.execute("jdbc:query " + dataSourceName + " SELECT version();");
			println("DataSource Test erfolgreich");

		} finally {
			println("Stoppe Karaf");
			karaf.stop();
		}
		setProgress(100);

		return "DataSource erfolgreich erstellt";
	}

	private void createDataSource(Karaf karaf) throws IOException, InterruptedException, ErrorCode {
		println("Lösche die DataSource " + dataSourceName);
		karaf.execute("jdbc:ds-delete " + dataSourceName);

		String dbName   = databasePageData.getDbName();
		String user     = databasePageData.getPostgresUser();
		String password = databasePageData.getPostgresUserPassword();
		String port     = databasePageData.getPostgresPort();
		String host     = databasePageData.getPostgresHost();

		String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
		String command = "jdbc:ds-create -dc org.postgresql.Driver -url " + url + " --username " + user + " --password " + password + " " + dataSourceName;
		karaf.execute(command);
		println("DataSource '" + dataSourceName + "' erfolgreich erstellt");
	}
}
