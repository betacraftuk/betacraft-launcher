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
	
	//be aware that windows arm will release soon.
	public static boolean isARMLinux(){
		//aarch64 = 64bit arm, arm = 32bit
		return (ARCH.indexOf("aarch64") >= 0 || (ARCH.indexOf("arm"))
	}
}
