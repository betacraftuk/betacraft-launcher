package org.betacraft.launcher;

import java.io.File;

/*
 * This is needed because Windows' file system is dumb
 */
public class BC {

	public static String get() {
		if (OS.isWindows()) {
			return windowsshit();
		} else {
			return path();
		}
	}

	public static String windowsshit() {
		return "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Roaming\\.betacraft\\";
	}

	public static String path() {
		String folder = null;
		if (OS.isLinux()) {
			folder = System.getProperty("user.home") + "/.betacraft/";
		} else if (OS.isiOS()) {
			folder = System.getProperty("user.home") + "/Library/Application Support/betacraft/";
		} else {
			Logger.a("Your operating system is not supported.");
			Window.quit();
			return null;
		}

		File betacraft = new File(folder);
		betacraft.mkdirs();
		return folder;
	}
}
