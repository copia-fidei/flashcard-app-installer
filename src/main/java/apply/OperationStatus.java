package apply;

public enum OperationStatus {
	NOT_STARTED("🔘", "Die apply.Operation wurde noch nicht gestartet"),
	RUNNING("⚙️", "Die apply.Operation läuft"),
	ERROR("⚠️", "Die apply.Operation wurde aufgrund eines Fehlers abgebrochen"),
	CANCELLED_BY_USER("🚫", "Die apply.Operation wurde vom Anwender abgebrochen"),
	COMPLETED("✅", "Die apply.Operation wurde erfolgreich beendet");

	private final String icon;
	private final String description;

	OperationStatus(String icon, String description) {
		this.icon = icon;
		this.description = description;
	}

	public String getIcon() {
		return icon;
	}

	public String getDescription() {
		return description;
	}

	public String getDisplayText() {
		return icon + " " + description;
	}
}
