package com.fes.flashcard.installer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.*;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;

public class DatabasePage extends Page {

	private final DatabasePageData databasePageData;

	private final JComboBox<Database> dbImplementationComboBox = new JComboBox<>();
	private final JComboBox<String>   versionsComboBox         = new JComboBox<>();
	private final JTextField          usernameField            = new JTextField(20);
	private final JPasswordField      passwordField            = new JPasswordField(20);
	private final JTextField          dbNameField              = new JTextField(20);
	private final JTextField          portField                = new JTextField(20);
	private final JTextField          hostField                = new JTextField(20);

	public DatabasePage(DatabasePageData pageData, ButtonBar buttonBar) {
		super(pageData, buttonBar);
		this.databasePageData = pageData;
	}


	@Override
	public String getTitle() {
		return "Datenbankauswahl";
	}

	@Override
	public String getDescription() {
		return "Datenbank und Version auswählen.";
	}

	static void main() {
		EventQueue.invokeLater(() -> {
			var pageDataPool = new PageDataPool();
			var pageData     = new DatabasePageData(pageDataPool);
			var page         = new DatabasePage(pageData, new ButtonBar());
			page.build();
			page.willBecomeVisible();
			TestFrames.showComponent("Database Configuration", page.content);
		});
	}

	@Override
	public void build() {
		JLabel databaseLabel = new JLabel("Implementation:");
		JLabel versionLabel  = new JLabel("Version:");
		JLabel usernameLabel = new JLabel("Nutzer:");
		JLabel passwordLabel = new JLabel("Passwort:");
		JLabel portLabel     = new JLabel("Port:");
		JLabel hostLabel     = new JLabel("Host:");
		JLabel dbNameLabel   = new JLabel("Datenbank:");

		dbImplementationComboBox.setModel(new DefaultComboBoxModel<>(Database.values()));
		dbNameField.setEditable(false);
		hostField.setEditable(false);

		content.add(databaseLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(5, 5, 5, 5), 0, 0));
		content.add(dbImplementationComboBox, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		content.add(versionLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(5, 5, 5, 5), 0, 0));
		content.add(versionsComboBox, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		content.add(usernameLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(5, 5, 5, 5), 0, 0));
		content.add(usernameField, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		content.add(passwordLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(5, 5, 5, 5), 0, 0));
		content.add(passwordField, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		content.add(hostLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(5, 5, 5, 5), 0, 0));
		content.add(hostField, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		content.add(portLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(5, 5, 5, 5), 0, 0));
		content.add(portField, new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		content.add(dbNameLabel, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, LINE_START, NONE, new Insets(5, 5, 5, 5), 0, 0));
		content.add(dbNameField, new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		// Filler
		content.add(new JPanel(), new GridBagConstraints(0, 7, 2, 1, 1.0, 1.0, LINE_START, BOTH, new Insets(5, 5, 5, 5), 0, 0));
	}

	protected void addListeners() {
		dbImplementationComboBox.addItemListener(_ -> pageChanged());
		versionsComboBox.addItemListener(_ -> pageChanged());
		usernameField.getDocument().addDocumentListener(new DocumentAdapter(this::pageChanged));
		passwordField.getDocument().addDocumentListener(new DocumentAdapter(this::pageChanged));
		portField.getDocument().addDocumentListener(new DocumentAdapter(this::pageChanged));
	}

	// TODO simpler way
	protected void updatePageData() {
		Database previousSelectedDbImp = databasePageData.getDatabaseImplementation();
		Database dbImplementation      = (Database) dbImplementationComboBox.getSelectedItem();
		databasePageData.setDatabaseImplementation(dbImplementation);

		switch (previousSelectedDbImp) {
			case H2 -> {
				databasePageData.setSelectedH2Version(versionsComboBox.getSelectedItem().toString());

				databasePageData.setH2Username(usernameField.getText());
				databasePageData.setH2Password(new String(passwordField.getPassword())); // TODO store as char[]
			}
			case PostgreSQL -> {
				databasePageData.setSelectedPostgresqlVersion(versionsComboBox.getSelectedItem().toString());

				databasePageData.setPostgresqlUsername(usernameField.getText());
				databasePageData.setPostgresqlPassword(new String(passwordField.getPassword()));

				databasePageData.setPostgresqlPort(portField.getText());
				databasePageData.setPostgresqlHost(hostField.getText());
			}
		}
	}

	@Override
	protected void fillGUI() {
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

}
