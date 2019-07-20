package org.betacraft.launcher;

import java.io.File;

import javax.swing.JOptionPane;

public class BC {

	public static String get() {
		if (OS.isWindows()) {
			return windowsPath();
		} else {
			return path();
		}
	}

	public static String windowsPath() {
		return "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Roaming\\.betacraft\\";
	}

	public static String path() {
		String folder = null;
		if (OS.isLinux() || OS.isSolaris()) {
			folder = System.getProperty("user.home") + "/.betacraft/";
		} else if (OS.isMac()) {
			folder = System.getProperty("user.home") + "/Library/Application Support/betacraft/";
		} else {
			Logger.a("Your operating system is not supported.");
			JOptionPane.showMessageDialog(null, "Your operating system is not supported ;(", "I'm sorry, but", JOptionPane.WARNING_MESSAGE);
			Window.quit();
			return null;
		}

		File betacraft = new File(folder);
		betacraft.mkdirs();
		return folder;
	}
}
