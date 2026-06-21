import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.lang.IO.println;
import static java.util.Objects.requireNonNull;

public class KarafPageData extends PageData {

	// constants
	private static final Path HOME_PATH = Path.of(System.getProperty("user.home"));

	// Preferences keys
	private static final String KARAF_INSTALLATION_DIR = "karaf.installation.dir";

	// Default values
	private final Path defaultKarafInstallationDir = HOME_PATH.resolve(".local/bin/apache-karaf-4.4.8");

	// values
	private Path karafInstallationDir = defaultKarafInstallationDir;

	public KarafPageData(PageDataPool pageDataPool) {
		super(pageDataPool);
	}

	// Test
	void main() {
		println(defaultKarafInstallationDir);
	}

	@Override
	public void load() {
		String karafInstallationDir = preferences.get(KARAF_INSTALLATION_DIR, defaultKarafInstallationDir.toString());
		this.karafInstallationDir = Path.of(karafInstallationDir);
	}

	@Override
	public void save() {
		preferences.put(KARAF_INSTALLATION_DIR, karafInstallationDir.toString());
	}

	@Override
	void loadDefaults() {
		karafInstallationDir = defaultKarafInstallationDir;
		preferences.remove(KARAF_INSTALLATION_DIR);
	}

	@Override
	public ValidationResults validate() {
		List<ValidationResult> validationResults = new ArrayList<>();

		if (!karafInstallationDir.startsWith(HOME_PATH)) {
			validationResults.add(new ValidationResult("Karaf Installation Directory", "Karaf installation directory must be inside home directory", ValidationResult.Severity.ERROR));
		}

		return new ValidationResults(validationResults);
	}

	public Path getKarafInstallationDir() {
		return karafInstallationDir;
	}

	public void setKarafInstallationDir(Path karafInstallationDir) {
		requireNonNull(karafInstallationDir);
		this.karafInstallationDir = karafInstallationDir;
	}
}
