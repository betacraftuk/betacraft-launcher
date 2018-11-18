package me.kazu.betacraftlaunucher;

import java.io.File;

public class Launcher {
	public static String VERSION = "Preview 1";

	public static void LaunchGame(String username) {
		
	}

	public static void Download(String ver) {
		if (ver.startsWith("_")) {
			System.out.println("Pobieram client dla gracza...");

			String version = ver.substring(1);
			// tutaj bêdzie kod pobierania
			

			
		} else if (ver.startsWith("admin_")) {
			System.out.println("Pobieram client dla admina...");
		}
	}

	private static void download(String version) {
		File appdata = new File(System.getenv("APPDATA") + "\\.betacraft\\bin\\versions\\" + version);
		appdata.mkdirs();
		
		
	}

	public static void Fix() {
		
	}

	public static void Unrar(String filepath, String SRC) {
		
	}

	public static void LauncherUpdate() {
		
	}
}
