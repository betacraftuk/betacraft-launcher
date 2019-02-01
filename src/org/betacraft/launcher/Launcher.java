package org.betacraft.launcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class Launcher {
	public static String VERSION = "Preview 1";

	public static void LaunchGame(String username) {
		
	}

	public static void download(String version) {
		File betacraft = new File(getBetacraft() + "versions/" + version);
		// TODO pobieranie
	}

	public static void Unrar(String filepath, String SRC) {
		
	}

	public static void downloadUpdate() {
		try {
			URL link = new URL("https://betacraft.ovh/versions/launcher.jar");
		} catch (Exception ex) {
			
		}
	}

	public static String getBetacraft() {
		String folder = null;
		if (OS.isLinux()) {
			folder = System.getProperty("user.home") + "/.betacraft/";
		} else if (OS.isiOS()) {
			folder = System.getProperty("user.home") + "/Application Support/betacraft/";
		} else if (OS.isWindows()) {
			folder = System.getenv("APPDATA") + "/.betacraft/";
		} else {
			System.out.println("Your system is not supported. Quitting.");
			Window.quit();
			return null;
		}

		File betacraft = new File(folder);
		betacraft.mkdirs();
		return folder;
	}

	public static void write(String file, String[] lines) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "utf-8"));
			for (int i = 0; i < lines.length; i++) {
				if (lines[i] != null) {
					writer.write(lines[i]);
					writer.newLine();
				}
			}
		} catch (Exception ex) {
			System.out.println("KRYTYCZNY BŁĄD!");
			System.out.println("podczas zapisywania do pliku \"" + file + "\" ");
			ex.printStackTrace();
		} finally {
			try {writer.close();} catch (Exception ex) {}
		}
	}

	public static void checkForUpdate() {
		String update = getUpdate();
		if (!VERSION.equalsIgnoreCase(update)) {
			int result = JOptionPane.showConfirmDialog(null, "Wydano nową wersję launchera (" + update + "). Czy chcesz pobrać aktualizację?", "Aktualizacja", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				
			} else {
				System.out.println("Odrzucono pobranie aktualizacji.");
			}
		} else {
			System.out.println("Launcher działa w najnowszej wersji.");
		}
	}

	public static String getUpdate() {
		try {
			URL url = new URL("https://betacraft.ovh/version_index");
			Scanner s = new Scanner(url.openStream());
			String update = s.nextLine().split(":")[1];
			s.close();
			return update;
		} catch (UnknownHostException ex) {
			System.out.println("Brak połączenia z internetem! (albo serwer padł) ");
			return null;
		} catch (Exception ex) {
			return null;
		}
	}

	public static String getLastlogin() {
		String file = getBetacraft() + "lastlogin";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "utf-8"));
			return reader.readLine();
		} catch (Exception ex) {
			return "";
		} finally {
			try {reader.close();} catch (Exception ex) {}
		}
	}
}
