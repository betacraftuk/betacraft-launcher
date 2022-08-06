package org.betacraft.launcher;

public enum DownloadResult {
	OK,
	FAILED_WITH_BACKUP,
	FAILED_WITHOUT_BACKUP;

	public boolean isPositive() {
		return this == OK || this == FAILED_WITH_BACKUP;
	}

	public boolean isOK() {
		return this == OK;
	}
}
