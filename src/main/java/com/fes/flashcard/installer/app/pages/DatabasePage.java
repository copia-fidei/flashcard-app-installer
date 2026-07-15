package com.fes.flashcard.installer.app.pages;

import com.fes.flashcard.installer.PageDataPool;
import com.fes.flashcard.installer.TestFrames;
import com.fes.flashcard.installer.app.Database;
import com.fes.flashcard.installer.page.Page;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;
import static java.util.logging.Logger.getLogger;

public class DatabasePage extends Page {

	private final DatabasePageData databasePageData;

	private final JComboBox<Database> dbImplementationComboBox = new JComboBox<>();

	private final JComboBox<String> versionsComboBox = new JComboBox<>();
	private final JTextField        usernameField    = new JTextField(20);
	private final JTextField        passwordField    = new JTextField(20);
	private final JTextField        dbNameField      = new JTextField(20);
	private final JTextField        portField        = new JTextField(20);
	private final JTextField        hostField        = new JTextField(20);

	private final DocumentAdapter documentListener = new DocumentAdapter(this::pageChanged);
	private final ItemListener    comboBoxListener = _ -> pageChanged();


	public DatabasePage(DatabasePageData pageData, Runnable onValidationChanged) {
		super(pageData, onValidationChanged);
		this.databasePageData = pageData;
	}


	@Override
	public String getTitle() {
		return "Datenbankauswahl";
	}

	@Override
	public String getDescription() {
		return "Datenbank und Version auswählen";
	}

	static void main() {
		EventQueue.invokeLater(() -> {
			var pageDataPool = new PageDataPool();
			var pageData     = new DatabasePageData(pageDataPool);
			var page         = new DatabasePage(pageData, () -> {});
			page.build();
			page.willBecomeVisible();
			TestFrames.showComponent("Database Configuration", page.content);
		});
	}

	// TODO clear, when page after Apply was pressed
	// TODO implement for H2 too
	private final Map<String, Boolean> postgresVersionsInstalled = new ConcurrentHashMap<>();

	private void findOutIsPostgresInstalled(String[] versions) {
		for (String version : versions) {
			try {
				Process process = new ProcessBuilder("dpkg-query", "-f=${db:Status-Abbrev}", "-W", "postgresql" + "-" + version).start();
				process.waitFor();

				String status;
				try (var reader = process.inputReader()) {
					status = reader.readAllAsString();
				}
				if (status.startsWith("ii")) {
					postgresVersionsInstalled.put(version, true);
				} else {
					postgresVersionsInstalled.put(version, false);
				}
			} catch (IOException | InterruptedException e) {
				getLogger(getClass().getName()).log(Level.WARNING, "Failed to find out if Postgres packages are installed", e);
			}

		}
	}

	@Override
	public void build() {
		var databaseLabel = new JLabel("Implementation");
		var versionLabel  = new JLabel("Version");
		var usernameLabel = new JLabel("Nutzer");
		var passwordLabel = new JLabel("Passwort");
		var portLabel     = new JLabel("Port");
		var hostLabel     = new JLabel("Host");
		var dbNameLabel   = new JLabel("Datenbank");

		versionsComboBox.setRenderer(new VersionListCellRenderer());

		dbNameField.setEditable(false);
		hostField.setEditable(false);

		content.add(databaseLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(dbImplementationComboBox, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		content.add(versionLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(versionsComboBox, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		content.add(usernameLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(usernameField, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		content.add(passwordLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(passwordField, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		content.add(hostLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(hostField, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		content.add(portLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(portField, new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		content.add(dbNameLabel, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(dbNameField, new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		// Filler
		content.add(new JPanel(), new GridBagConstraints(0, 7, 2, 1, 1.0, 1.0, LINE_START, BOTH, new Insets(10, 10, 10, 10), 0, 0));

		new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() {
				findOutIsPostgresInstalled(getVersions(Database.PostgreSQL));
				return null;
			}

			@Override
			protected void done() {
				versionsComboBox.repaint();
			}
		}.execute();
	}

	protected void addListeners() {
		dbImplementationComboBox.addItemListener(comboBoxListener);
		versionsComboBox.addItemListener(comboBoxListener);
		usernameField.getDocument().addDocumentListener(documentListener);
		passwordField.getDocument().addDocumentListener(documentListener);
		portField.getDocument().addDocumentListener(documentListener);
	}

	@Override
	protected void removeListeners() {
		dbImplementationComboBox.removeItemListener(comboBoxListener);
		versionsComboBox.removeItemListener(comboBoxListener);
		usernameField.getDocument().removeDocumentListener(documentListener);
		passwordField.getDocument().removeDocumentListener(documentListener);
		portField.getDocument().removeDocumentListener(documentListener);
	}

	protected void updatePageData() {
		Database previousSelectedDbImp = databasePageData.getDatabaseImplementation();
		Database dbImplementation      = (Database) dbImplementationComboBox.getSelectedItem();
		databasePageData.setDatabaseImplementation(dbImplementation);

		switch (previousSelectedDbImp) {
			case H2 -> {
				databasePageData.setSelectedH2Version((String) versionsComboBox.getSelectedItem());
				databasePageData.setH2Username(usernameField.getText());
				databasePageData.setH2Password(passwordField.getText());
			}
			case PostgreSQL -> {
				databasePageData.setSelectedPostgresqlVersion((String) versionsComboBox.getSelectedItem());

				databasePageData.setPostgresqlUsername(usernameField.getText());
				databasePageData.setPostgresqlPassword(passwordField.getText());

				databasePageData.setPostgresqlPort(portField.getText());
				databasePageData.setPostgresqlHost(hostField.getText());
			}
		}
	}

	@Override
	protected void fillGUI() {
		dbImplementationComboBox.setModel(new DefaultComboBoxModel<>(Database.values()));
		Database dbImpl = databasePageData.getDatabaseImplementation();
		dbImplementationComboBox.setSelectedItem(dbImpl);
		updateGUI();
		updateFields(dbImpl);
	}

	private void updateFields(Database dbImp) {
		versionsComboBox.setSelectedItem(getSelectedVersion(dbImp));

		if (dbImp == Database.H2) {
			usernameField.setText(databasePageData.getH2Username());
			passwordField.setText(databasePageData.getH2Password());
			dbNameField.setText(databasePageData.getDbName());
			portField.setText("");
			hostField.setText("");
		} else {
			usernameField.setText(databasePageData.getPostgresqlUsername());
			passwordField.setText(databasePageData.getPostgresqlPassword());
			dbNameField.setText(databasePageData.getDbName());
			portField.setText(databasePageData.getPostgresqlPort());
			hostField.setText(databasePageData.getPostgresqlHost());
		}
	}

	@Override
	public void updateGUI() {
		Database dbImpl   = databasePageData.getDatabaseImplementation();
		String[] versions = getVersions(dbImpl);
		versionsComboBox.setModel(new DefaultComboBoxModel<>(versions));

		boolean isPostgreSQL = dbImpl == Database.PostgreSQL;
		portField.setEnabled(isPostgreSQL);
		hostField.setEnabled(isPostgreSQL);
	}

	@Override
	public void updateDependantValues() {
		updateFields(databasePageData.getDatabaseImplementation());
	}


	private String[] getVersions(Database currentDbImp) {
		return switch (currentDbImp) {
			case H2 -> databasePageData.getH2Versions();
			case PostgreSQL -> databasePageData.getPostgresqlVersions();
		};
	}

	private String getSelectedVersion(Database currentDbImp) {
		return switch (currentDbImp) {
			case H2 -> databasePageData.getSelectedH2Version();
			case PostgreSQL -> databasePageData.getSelectedPostgresqlVersion();
		};
	}

	private class VersionListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label   = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			String version = (String) value;

			Database dbImpl = databasePageData.getDatabaseImplementation();

			// TODO for H2 too.
			// In that case make Resources the first page.
			if (dbImpl == Database.PostgreSQL && postgresVersionsInstalled.containsKey(version)) {
				boolean isInstalled = postgresVersionsInstalled.get(version);
				String  statusText  = isInstalled ? "(bereits installiert)" : "(nicht installiert)";
				label.setText(version + " " + statusText);
			}

			return label;
		}
	}

}
