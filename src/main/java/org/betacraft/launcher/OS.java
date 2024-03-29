package org.betacraft.launcher;

public class OS {
	public static String OS = System.getProperty("os.name").toLowerCase();
	public static String ARCH = System.getProperty("os.arch");
	public static String VER = System.getProperty("os.version").toLowerCase();

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}

	public static boolean isLinux() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
	}

	public static boolean isWindows10_11() {
		return isWindows() && (VER.indexOf("10") >= 0 || VER.indexOf("11") >= 0);
	}

	public static boolean isArchOdd() {
		return ARCH.equalsIgnoreCase("x86") || ARCH.equalsIgnoreCase("aarch64") || ARCH.contains("arm");
	}
}
