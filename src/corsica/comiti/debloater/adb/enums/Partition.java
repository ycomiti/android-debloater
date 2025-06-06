package corsica.comiti.debloater.adb.enums;

public enum Partition {
	
	BOOTLOADER("bootloader"),
	RECOVERY("recovery"),
	SIDELOAD("sideload"),
	SIDELOAD_AUTO_REBOOT("sideload-auto-reboot")
	;

	private String partition;

	Partition(String partition) {
		this.partition = partition;
	}
	
	public String getPartition() {
		return this.partition;
	}
	
}
