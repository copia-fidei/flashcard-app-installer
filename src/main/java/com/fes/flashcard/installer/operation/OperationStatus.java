package com.fes.flashcard.installer.operation;

public enum OperationStatus {

	// TODO remove descriptions
	NOT_STARTED("🔘", "Die Operation wurde noch nicht gestartet"),
	RUNNING("⚙️", "Die Operation läuft"),
	ERROR("⚠️", "Die Operation wurde aufgrund eines Fehlers abgebrochen"),
	CANCELLED_BY_USER("🚫", "Die Operation wurde vom Anwender abgebrochen"),
	COMPLETED("✅", "Die Operation wurde erfolgreich beendet");

	private final String icon;
	private final String description;

	OperationStatus(String icon, String description) {
		this.icon = icon;
		this.description = description;
	}
	// TODO separate icon from enum
	public String getIcon() {
		return icon;
	}
	// TODO separate description message from enum
	public String getDescription() {
		return description;
	}
	// TODO fix icon not returned
	public String getDisplayText() {
		return icon + " " + description;
	}
}
