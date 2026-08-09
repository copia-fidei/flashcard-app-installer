package com.fes.flashcard.installer.app.operations;

import com.fes.flashcard.installer.TextBuilder;
import com.fes.flashcard.installer.app.Karaf;
import com.fes.flashcard.installer.app.pages.DatabasePageData;
import com.fes.flashcard.installer.app.pages.KarafPageData;
import com.fes.flashcard.installer.operation.ErrorCode;
import com.fes.flashcard.installer.operation.Operation;

import java.io.IOException;

import static com.fes.flashcard.installer.Temporary.use;
import static com.fes.flashcard.installer.app.Resources.H2_INIT_SQL;
import static com.fes.flashcard.installer.app.Resources.getTmpFile;

// TODO messages
public class InstallH2Operation extends Operation {

	private final KarafPageData    karafPageData;
	private final DatabasePageData databasePageData;

	private final String h2Version;
	private final String dataSourceName;

	public InstallH2Operation(KarafPageData karafPageData, DatabasePageData databasePageData) {
		super("H2 Database", "Installiere H2 Datenbank und DataSource");

		this.karafPageData = karafPageData;
		this.databasePageData = databasePageData;
		this.h2Version = databasePageData.getSelectedH2Version();

		dataSourceName = databasePageData.getDataSourceName();
	}

	public String getDescription() {
		var text = new TextBuilder();
		text.line("Installiere das Karaf JDBC Feature");
		text.line("Installiere den Pax JDBC H2 Driver für Version " + h2Version);
		text.line("Erstelle die H2 Datenbank");
		text.line("Erstelle die DataSource für die H2 Datenbank");
		text.line("Erstelle die Datenbanktabellen");
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

			println("Installiere das Pax JDBC H2 Feature");
			setProgress(50);
			karaf.execute("feature:install pax-jdbc-h2");
			println("Pax JDBC H2 Driver erfolgreich installiert");
			setProgress(60);

			println("Erstelle die DataSource");
			setProgress(70);
			createDataSource(karaf);
			setProgress(80);

			println("Erstelle die Datenbanktabellen");
			setProgress(85);
			createTables(karaf);
			setProgress(90);

			println("Teste DataSource (SELECT version();)");
			setProgress(95);
			karaf.execute("jdbc:query " + dataSourceName + " SELECT VERSION();");
			println("DataSource Test erfolgreich");
		} finally {
			println("Stoppe Karaf");
			karaf.stop();
		}
		setProgress(100);

		return "H2 Datenbank, DataSource und Tabellen erfolgreich erstellt";
	}

	private void createDataSource(Karaf karaf) throws IOException, InterruptedException, ErrorCode {
		println("Lösche die DataSource " + dataSourceName);
		karaf.execute("jdbc:ds-delete " + dataSourceName);

		String dbName = databasePageData.getDbName();
		String user   = databasePageData.getH2User();

		String url = "\"jdbc:h2:${karaf.data}/database/h2/" + dbName + ";MODE=PostgreSQL\"";
		karaf.execute("jdbc:ds-create " + "-dc org.h2.Driver " + "-url " + url + " " + "-u " + user + " " + dataSourceName);
		println("DataSource '" + dataSourceName + "' erfolgreich erstellt");
	}

	// H2 database will be created on first connection
	private void createTables(Karaf karaf) throws Exception {
		use(getTmpFile(H2_INIT_SQL), file -> {
			println("Datenbanktabellen werden erstellt");
			println("Skript wird ausgeführt: " + file);
			karaf.executeSuccessfully("jdbc:execute " + dataSourceName + " RUNSCRIPT FROM '" + file + "'");
			println("Datenbanktabellen erfolgreich erstellt");
		});
	}
}
