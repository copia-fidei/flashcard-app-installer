package com.epau.app.flashcard.app.installer.pages;

import com.epau.app.flashcard.app.installer.Database;
import com.epau.lib.swing.wizard.page.Page;
import com.epau.lib.swing.wizard.page.PageDataPool;
import com.epau.util.nls.Nls;
import com.epau.util.swing.frame.TestFrames;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.Component;

import static com.epau.app.flashcard.app.installer.Database.PostgreSQL;
import static java.awt.EventQueue.invokeLater;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;

public class DatabasePage extends Page {

	private final Nls nls = new Nls(this);

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

	public DatabasePage(DatabasePageData pageData) {
		super(pageData);
		this.databasePageData = pageData;
	}


	@Override
	public String getTitle() {
		return nls.get("DatabasePage.title.Database");
	}

	@Override
	public String getDescription() {
		return nls.get("DatabasePage.description.Select_database_and_version");
	}

	@Override
	public void build() {
		var databaseLabel      = new JLabel(nls.get("DatabasePage.label.Implementation"));
		var versionLabel       = new JLabel(nls.get("DatabasePage.label.Version"));
		var adminLabel         = new JLabel(nls.get("DatabasePage.label.Administrator"));
		var adminPasswordLabel = new JLabel(nls.get("DatabasePage.label.Administrator_password"));
		var portLabel          = new JLabel(nls.get("DatabasePage.label.Port"));
		var hostLabel          = new JLabel(nls.get("DatabasePage.label.Host"));
		var dbNameLabel        = new JLabel(nls.get("DatabasePage.label.Database_name"));
		var userLabel          = new JLabel(nls.get("DatabasePage.label.User"));
		var userPasswordLabel  = new JLabel(nls.get("DatabasePage.label.User_password"));

		versionsComboBox.setRenderer(new VersionListCellRenderer());

		adminField.setEditable(false);
		dbNameField.setEditable(false);
		hostField.setEditable(false);
		userField.setEditable(false);

		add(databaseLabel, 			  0, 0, 1, 1, 0.0, 0.0, LINE_START, NONE, 		10, 10, 10, 10, 0, 0);
		add(dbImplementationComboBox, 1, 0, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, 10, 10, 10, 10, 0, 0);
		add(versionLabel, 			  0, 1, 1, 1, 0.0, 0.0, LINE_START, NONE, 		10, 10, 10, 10, 0, 0);
		add(versionsComboBox, 		  1, 1, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL,	10, 10, 10, 10, 0, 0);
		add(adminLabel, 			  0, 2, 1, 1, 0.0, 0.0, LINE_START, NONE,	 	10, 10, 10, 10, 0, 0);
		add(adminField, 			  1, 2, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL,	10, 10, 10, 10, 0, 0);
		add(adminPasswordLabel, 	  0, 3, 1, 1, 0.0, 0.0, LINE_START, NONE,	 	10, 10, 10, 10, 0, 0);
		add(adminPasswordField, 	  1, 3, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL,	10, 10, 10, 10, 0, 0);
		add(hostLabel,				  0, 4, 1, 1, 0.0, 0.0, LINE_START, NONE,	 	10, 10, 10, 10, 0, 0);
		add(hostField,				  1, 4, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL,	10, 10, 10, 10, 0, 0);
		add(portLabel,				  0, 5, 1, 1, 0.0, 0.0, LINE_START, NONE, 		10, 10, 10, 10, 0, 0);
		add(portField,				  1, 5, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, 10, 10, 10, 10, 0, 0);
		add(dbNameLabel, 			  0, 6, 1, 1, 0.0, 0.0, LINE_START, NONE, 		10, 10, 10, 10, 0, 0);
		add(dbNameField, 			  1, 6, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL,	10, 10, 10, 10, 0, 0);
		add(userLabel, 				  0, 7, 1, 1, 0.0, 0.0, LINE_START, NONE, 		10, 10, 10, 10, 0, 0);
		add(userField, 				  1, 7, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL,	10, 10, 10, 10, 0, 0);
		add(userPasswordLabel, 		  0, 8, 1, 1, 0.0, 0.0, LINE_START, NONE, 		10, 10, 10, 10, 0, 0);
		add(userPasswordField, 		  1, 8, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, 10, 10, 10, 10, 0, 0);
		// Filler
		add(new JPanel(), 			  0, 9, 2, 1, 1.0, 1.0, LINE_START, BOTH, 		10, 10, 10, 10, 0, 0);

		new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() {
				PostgresVersions.findOutInstallStates(databasePageData.getPostgresInstallStates(), databasePageData.getPostgresVersions());
				return null;
			}

			@Override
			protected void done() {
				versionsComboBox.repaint();
			}
		}.execute();
	}

	protected void addListeners() {
		dbImplementationComboBox.addItemListener(itemListener);
		versionsComboBox.addItemListener(itemListener);
		adminField.getDocument().addDocumentListener(documentListener);
		adminPasswordField.getDocument().addDocumentListener(documentListener);
		userField.getDocument().addDocumentListener(documentListener);
		userPasswordField.getDocument().addDocumentListener(documentListener);
		portField.getDocument().addDocumentListener(documentListener);
	}

	@Override
	protected void removeListeners() {
		dbImplementationComboBox.removeItemListener(itemListener);
		versionsComboBox.removeItemListener(itemListener);
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
				String status;
				if (installStates.get(version).isInstalled()) {
					status = nls.get("DatabasePage.label.{0}_(already_installed)", version);
				} else {
					status = nls.get("DatabasePage.label.{0}_(not_installed)", version);
				}
				label.setText(status);
			}
			return label;
		}
	}

	// For testing
	static void main() {
		invokeLater(() -> {
			var pageDataPool = new PageDataPool();
			var pageData     = new DatabasePageData(pageDataPool);
			var page         = new DatabasePage(pageData);
			page.build();
			page.willBecomeVisible();
			TestFrames.showComponent("Database Configuration", page.content); //NON-NLS
		});
	}
}
