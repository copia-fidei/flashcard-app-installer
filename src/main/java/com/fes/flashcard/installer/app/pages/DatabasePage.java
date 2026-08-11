package com.fes.flashcard.installer.app.pages;

import com.fes.flashcard.installer.PageDataPool;
import com.fes.flashcard.installer.TestFrames;
import com.fes.flashcard.installer.app.Database;
import com.fes.flashcard.installer.page.Page;
import com.fes.flashcard.installer.swing.DocumentAdapter;

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

import static com.fes.flashcard.installer.app.Database.PostgreSQL;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;
import static java.util.logging.Level.WARNING;

public class DatabasePage extends Page {

	private final DatabasePageData databasePageData;

	private final JComboBox<Database> dbImplementationComboBox = new JComboBox<>();

	private final JComboBox<String> versionsComboBox   = new JComboBox<>();
	private final JTextField        adminField         = new JTextField(20);
	private final JTextField        adminPasswordField = new JTextField(20);
	private final JTextField        userField          = new JTextField(20);
	private final JTextField        userPasswordField  = new JTextField(20);
	private final JTextField        dbNameField        = new JTextField(20);
	private final JTextField        portField          = new JTextField(20);
	private final JTextField        hostField          = new JTextField(20);

	private final DocumentAdapter documentListener = new DocumentAdapter(this::pageChanged);
	private final ItemListener    comboBoxListener = _ -> pageChanged();


	public DatabasePage(DatabasePageData pageData, Runnable onValidationChanged) {
		super(pageData, onValidationChanged);
		this.databasePageData = pageData;
	}


	@Override
	public String getTitle() {
		return "Datenbank";
	}

	@Override
	public String getDescription() {
		return "Datenbank und Version auswählen";
	}

	// For testing
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

	@Override
	public void build() {
		var databaseLabel      = new JLabel("Implementation");
		var versionLabel       = new JLabel("Version");
		var adminLabel         = new JLabel("Administrator");
		var adminPasswordLabel = new JLabel("Administratorpasswort");
		var portLabel          = new JLabel("Port");
		var hostLabel          = new JLabel("Host");
		var dbNameLabel        = new JLabel("Datenbankname");
		var userLabel          = new JLabel("Benutzer");
		var userPasswordLabel  = new JLabel("Benutzerpasswort");

		versionsComboBox.setRenderer(new VersionListCellRenderer());

		adminField.setEditable(false);
		dbNameField.setEditable(false);
		hostField.setEditable(false);
		userField.setEditable(false);

		content.add(databaseLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(dbImplementationComboBox, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		content.add(versionLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(versionsComboBox, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		content.add(adminLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(adminField, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		content.add(adminPasswordLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(adminPasswordField, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		content.add(hostLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(hostField, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		content.add(portLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(portField, new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		content.add(dbNameLabel, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(dbNameField, new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		content.add(userLabel, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(userField, new GridBagConstraints(1, 7, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		content.add(userPasswordLabel, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(10, 10, 10, 10), 0, 0));
		content.add(userPasswordField, new GridBagConstraints(1, 8, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		// Filler
		content.add(new JPanel(), new GridBagConstraints(0, 9, 2, 1, 1.0, 1.0, LINE_START, BOTH, new Insets(10, 10, 10, 10), 0, 0));

		new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() {
				findOutIsPostgresInstalled();
				return null;
			}

			@Override
			protected void done() {
				versionsComboBox.repaint();
			}
		}.execute();
	}

	private void findOutIsPostgresInstalled() {
		for (String version : databasePageData.getPostgresVersions()) {
			try {
				var process = new ProcessBuilder("dpkg-query", "-f=${db:Status-Abbrev}", "-W", "postgresql" + "-" + version).start();
				process.waitFor();

				String status;
				try (var reader = process.inputReader()) {
					status = reader.readAllAsString();
				}
				if (status.startsWith("ii")) {
					databasePageData.getPostgresInstallStates().put(version, PostgresState.ALREADY_INSTALLED);
				} else {
					databasePageData.getPostgresInstallStates().put(version, PostgresState.NOT_INSTALLED);
				}
			} catch (IOException | InterruptedException e) {
				log.log(WARNING, "Failed to find out if Postgres packages are installed", e);
			}

		}
	}

	protected void addListeners() {
		dbImplementationComboBox.addItemListener(comboBoxListener);
		versionsComboBox.addItemListener(comboBoxListener);
		adminField.getDocument().addDocumentListener(documentListener);
		adminPasswordField.getDocument().addDocumentListener(documentListener);
		userField.getDocument().addDocumentListener(documentListener);
		userPasswordField.getDocument().addDocumentListener(documentListener);
		portField.getDocument().addDocumentListener(documentListener);
	}

	@Override
	protected void removeListeners() {
		dbImplementationComboBox.removeItemListener(comboBoxListener);
		versionsComboBox.removeItemListener(comboBoxListener);
		adminField.getDocument().removeDocumentListener(documentListener);
		adminPasswordField.getDocument().removeDocumentListener(documentListener);
		userField.getDocument().removeDocumentListener(documentListener);
		userPasswordField.getDocument().removeDocumentListener(documentListener);
		portField.getDocument().removeDocumentListener(documentListener);
	}

	protected void updatePageData() {
		Database previousSelectedDbImp = databasePageData.getDatabaseImplementation();
		Database dbImplementation      = (Database) dbImplementationComboBox.getSelectedItem();
		databasePageData.setDatabaseImplementation(dbImplementation);

		switch (previousSelectedDbImp) {
			case H2 -> {
				databasePageData.setSelectedH2Version((String) versionsComboBox.getSelectedItem());
			}
			case PostgreSQL -> {
				databasePageData.setSelectedPostgresVersion((String) versionsComboBox.getSelectedItem());

				databasePageData.setPostgresAdmin(adminField.getText());
				databasePageData.setPostgresAdminPassword(adminPasswordField.getText());

				databasePageData.setPostgresPort(portField.getText());
				databasePageData.setPostgresUserPassword(userPasswordField.getText());
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
			adminField.setText("");
			adminPasswordField.setText("");
			dbNameField.setText(databasePageData.getDbName());
			portField.setText("");
			hostField.setText("");
			userField.setText(databasePageData.getH2User());
			userPasswordField.setText("");
		} else {
			adminField.setText(databasePageData.getPostgresAdmin());
			adminPasswordField.setText(databasePageData.getPostgresAdminPassword());
			dbNameField.setText(databasePageData.getDbName());
			portField.setText(databasePageData.getPostgresPort());
			hostField.setText(databasePageData.getPostgresHost());
			userField.setText(databasePageData.getPostgresUser());
			userPasswordField.setText(databasePageData.getPostgresUserPassword());
		}
	}

	@Override
	public void updateGUI() {
		Database dbImpl   = databasePageData.getDatabaseImplementation();
		String[] versions = getVersions(dbImpl);
		versionsComboBox.setModel(new DefaultComboBoxModel<>(versions));

		boolean isPostgres = dbImpl == PostgreSQL;
		portField.setEnabled(isPostgres);
		adminPasswordField.setEnabled(isPostgres);
		hostField.setEnabled(isPostgres);
		userField.setEnabled(isPostgres);
		userPasswordField.setEnabled(isPostgres);
	}

	@Override
	public void updateDependantValues() {
		updateFields(databasePageData.getDatabaseImplementation());
	}


	private String[] getVersions(Database currentDbImp) {
		return switch (currentDbImp) {
			case H2 -> databasePageData.getH2Versions();
			case PostgreSQL -> databasePageData.getPostgresVersions();
		};
	}

	private String getSelectedVersion(Database currentDbImp) {
		return switch (currentDbImp) {
			case H2 -> databasePageData.getSelectedH2Version();
			case PostgreSQL -> databasePageData.getSelectedPostgresVersion();
		};
	}

	private class VersionListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label   = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			String version = (String) value;

			var installStates = databasePageData.getPostgresInstallStates();
			if (databasePageData.getDatabaseImplementation() == PostgreSQL && installStates.containsKey(version)) {
				PostgresState isInstalled = installStates.get(version);
				String        statusText  = isInstalled == PostgresState.ALREADY_INSTALLED || isInstalled == PostgresState.INSTALLED_BY_INSTALLER ? "(bereits installiert)" : "(nicht installiert)";
				label.setText(version + " " + statusText);
			}

			return label;
		}
	}

}
