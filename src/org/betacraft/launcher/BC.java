package org.betacraft.launcher;

import java.io.File;

import javax.swing.JOptionPane;

public class BC {
	public static boolean portable = true;
	public static boolean wrapped = false;

	public static String get() {
		if (OS.isWindows()) {
			return windowsPath();
		} else {
			return path();
		}
	}

	public static String windowsPath() {
		if (portable) return prefBC() + "\\.betacraft\\";
		return System.getenv("APPDATA") + "\\.betacraft\\";
	}

	public static String path() {
		String folder = null;
		if (OS.isLinux()) {
			folder = System.getProperty("user.home") + "/.betacraft/";
			if (portable) folder = prefBC() + "/.betacraft/";
		} else if (OS.isMac()) {
			folder = System.getProperty("user.home") + "/Library/Application Support/betacraft/";
			if (portable) folder = prefBC() + "/betacraft/";
		} else {
			Logger.a("Your operating system is not supported.");
			JOptionPane.showMessageDialog(null, "Your operating system is not supported ;(", "I'm sorry, but", JOptionPane.WARNING_MESSAGE);
			Window.quit(true);
			return null;
		}

		File betacraft = new File(folder);
		betacraft.mkdirs();
		return folder;
	}

	public static String prefBC() {
		if (wrapped) return Launcher.currentPath.getAbsoluteFile().getParentFile().getParentFile().getParent();
		return Launcher.currentPath.getAbsoluteFile().getParent();
	}
}
