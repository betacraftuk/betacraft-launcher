package org.betacraft.launcher;

public class OS {

	public static boolean isWindows() {
		return (Launcher.OS.indexOf("win") >= 0);
	}

	public static boolean isiOS() {
		return (Launcher.OS.indexOf("mac") >= 0);
	}

	public static boolean isLinux() {
		return (Launcher.OS.indexOf("nix") >= 0 || Launcher.OS.indexOf("nux") >= 0 || Launcher.OS.indexOf("aix") > 0);
	}
}
