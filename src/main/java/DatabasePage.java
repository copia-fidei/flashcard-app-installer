import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.*;
import java.awt.event.ItemEvent;

public class DatabasePage extends Page {

	private final DatabasePageData databasePageData;

	private JComboBox<DatabasePageData.Database> databaseTypeComboBox;
	private JComboBox<String>                    versionComboBox;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JTextField dbNameField;
	private JTextField portField;
	private JTextField hostField;

	public DatabasePage(DatabasePageData pageData) {
		super(pageData);
		this.databasePageData = pageData;
	}

	static void main() {
		PageDataPool pageDataPool = new PageDataPool();
		DatabasePageData pageData = new DatabasePageData(pageDataPool);
		DatabasePage page = new DatabasePage(pageData);
		page.build();
		// TODO: text fields are empty before the database type is selected

		ComponentTestFrame.show("Database Configuration", page);
	}

	@Override
	public void build() {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Title
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		JLabel titleLabel = new JLabel("Datenbankauswahl");
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
		add(titleLabel, gbc);

		// Description
		gbc.gridy = 1;
		JLabel descriptionLabel = new JLabel("Datenbank und Version auswählen.");
		add(descriptionLabel, gbc);

		gbc.gridwidth = 1;

		// Database Type
		gbc.gridy = 2;
		add(new JLabel("Datenbanktyp:"), gbc);
		gbc.gridx = 1;
		databaseTypeComboBox = new JComboBox<>(DatabasePageData.Database.values());
		add(databaseTypeComboBox, gbc);

		// Version
		gbc.gridx = 0;
		gbc.gridy = 3;
		add(new JLabel("Version:"), gbc);
		gbc.gridx = 1;
		versionComboBox = new JComboBox<>();
		add(versionComboBox, gbc);

		// Username
		gbc.gridx = 0;
		gbc.gridy = 4;
		add(new JLabel("Datenbank Nutzer:"), gbc);
		gbc.gridx = 1;
		usernameField = new JTextField(20);
		add(usernameField, gbc);

		// Password
		gbc.gridx = 0;
		gbc.gridy = 5;
		add(new JLabel("Datenbank Passwort:"), gbc);
		gbc.gridx = 1;
		passwordField = new JPasswordField(20);
		add(passwordField, gbc);

		// Database Name (not editable)
		gbc.gridx = 0;
		gbc.gridy = 6;
		add(new JLabel("Datenbank Name:"), gbc);
		gbc.gridx = 1;
		dbNameField = new JTextField(20);
		dbNameField.setEditable(false);
		add(dbNameField, gbc);

		// Port (not required for H2)
		gbc.gridx = 0;
		gbc.gridy = 7;
		add(new JLabel("Datenbank Port:"), gbc);
		gbc.gridx = 1;
		portField = new JTextField(20);
		add(portField, gbc);

		// Host (not editable, not required for H2)
		gbc.gridx = 0;
		gbc.gridy = 8;
		add(new JLabel("Datenbank Host:"), gbc);
		gbc.gridx = 1;
		hostField = new JTextField(20);
		hostField.setEditable(false);
		add(hostField, gbc);

		// Add listeners
		databaseTypeComboBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				DatabasePageData.Database selectedType = (DatabasePageData.Database) e.getItem();
				databasePageData.setDatabaseType(selectedType);
				updateGUI();
				updateDependantValues();
			}
		});

		versionComboBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				String selectedVersion = (String) e.getItem();
				databasePageData.setCurrentVersion(selectedVersion);
			}
		});

		// Add listeners for text fields to update data
		usernameField.addActionListener(e -> {
			DatabasePageData.Database currentType = databasePageData.getDatabaseType();
			if (currentType == DatabasePageData.Database.H2) {
				databasePageData.setH2Username(usernameField.getText());
			} else {
				databasePageData.setPostgresqlUsername(usernameField.getText());
			}
		});
		passwordField.addActionListener(e -> {
			DatabasePageData.Database currentType = databasePageData.getDatabaseType();
			if (currentType == DatabasePageData.Database.H2) {
				databasePageData.setH2Password(new String(passwordField.getPassword()));
			} else {
				databasePageData.setPostgresqlPassword(new String(passwordField.getPassword()));
			}
		});
		portField.addActionListener(e -> databasePageData.setPostgresqlPort(portField.getText()));
	}

	@Override
	void fillGuiWithData() {
		databaseTypeComboBox.setSelectedItem(databasePageData.getDatabaseType());
		updateGUI();
		updateDependantValues();
	}

	@Override
	public void updateGUI() {
		DatabasePageData.Database currentType = databasePageData.getDatabaseType();

		// Update version combobox based on database type
		versionComboBox.setModel(new DefaultComboBoxModel<>(databasePageData.getVersionsForType(currentType)));
		versionComboBox.setSelectedItem(databasePageData.getCurrentVersion());

		// Enable/disable Port and Host fields based on database type
		boolean isPostgreSQL = currentType == DatabasePageData.Database.PostgreSQL;
		portField.setEnabled(isPostgreSQL);
		hostField.setEnabled(isPostgreSQL);
	}

	@Override
	public void updateDependantValues() {
		DatabasePageData.Database currentType = databasePageData.getDatabaseType();

		// Update version selection
		String currentVersion = databasePageData.getCurrentVersion();
		versionComboBox.setSelectedItem(currentVersion);

		// Update field values based on database type
		if (currentType == DatabasePageData.Database.H2) {
			// For H2, use H2-specific values or defaults
			usernameField.setText(databasePageData.getH2Username());
			passwordField.setText(databasePageData.getH2Password());
			dbNameField.setText(databasePageData.getDbName());
			portField.setText("");
			hostField.setText("");
		} else {
			// For PostgreSQL, use PostgreSQL-specific values or defaults
			usernameField.setText(databasePageData.getPostgresqlUsername());
			passwordField.setText(databasePageData.getPostgresqlPassword());
			dbNameField.setText(databasePageData.getDbName());
			portField.setText(databasePageData.getPostgresqlPort());
			hostField.setText(databasePageData.getPostgresqlHost());
		}
	}

	@Override
	public String getTitle() {
		return "Datenbank";
	}

	@Override
	public String getDescription() {
		return "Datenbank und Version auswählen.";
	}
}
