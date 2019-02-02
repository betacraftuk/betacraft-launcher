package org.betacraft.launcher;

import java.io.BufferedInputStream;
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
import java.util.stream.Stream;

import javax.swing.JOptionPane;

public class Launcher {
	public static String VERSION = "Preview 1";

	public static void LaunchGame(String username) {
		
	}

	public static String getVerLink(String version) {
		return "https://betacraft.ovh/versions/" + version + ".jar";
	}

	public static File getVerFolder() {
		return new File(getBetacraft() + "versions/");
	}

	public static boolean getVerDownloaded(String version) {
		File file = new File(getVerFolder(), version + ".jar");
		if (file.exists() && !file.isDirectory()) {
			return true;
		}
		return false;
	}

	public static boolean download(String link, File folder, String file) {
		Logger.a("Zainicjowano pobieranie z " + link);
		folder.mkdirs();
		folder = new File(folder, file);
		try {
			URL url = new URL(link);
			BufferedInputStream inputst = new BufferedInputStream(url.openStream());
			FileOutputStream outputst = new FileOutputStream(folder);
			byte[] buffer = new byte[1024];
			int count = 0;
			while((count = inputst.read(buffer, 0, 1024)) != -1) {
				outputst.write(buffer, 0, count);
			}
			outputst.close();
			inputst.close();
			return true;
		} catch (Exception ex) {
			Logger.a("KRYTYCZNY BŁĄD");
			Logger.a("podczas pobierania pliku z " + link + " ");
			Logger.a(ex.toString());
			return false;
		}
	}

	public static void Unrar(String filepath, String SRC) {
		
	}

	public static void downloadUpdate() {
		try {
			download("https://betacraft.ovh/versions/launcher.jar", new File(getBetacraft()), "betacraft.jar");
			String path = new File(".").getCanonicalPath();
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
			Logger.a("Twój system nie jest wspierany.");
			Window.quit();
			return null;
		}

		File betacraft = new File(folder);
		betacraft.mkdirs();
		return folder;
	}

	public static void write(String file, String[] lines, boolean append) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, append), "utf-8"));
			for (int i = 0; i < lines.length; i++) {
				if (lines[i] != null) {
					writer.write(lines[i]);
					writer.newLine();
				}
			}
		} catch (Exception ex) {
			Logger.a("KRYTYCZNY BŁĄD!");
			Logger.a("podczas zapisywania do pliku \"" + file + "\" ");
			Logger.a(ex.toString());
		} finally {
			try {writer.close();} catch (Exception ex) {}
		}
	}

	public static void checkForUpdate() {
		String update = getUpdate();
		if (update == null) {
			return;
		}
		if (!VERSION.equalsIgnoreCase(update)) {
			Logger.a("Znaleziono aktualizację (" + update + ").");
			int result = JOptionPane.showConfirmDialog(null, "Wydano nową wersję launchera (" + update + "). Czy chcesz pobrać aktualizację?", "Aktualizacja", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				downloadUpdate();
			} else {
				Logger.a("Odmówiono pobrania aktualizacji. Launcher działa w wersji " + VERSION);
			}
		} else {
			Logger.a("Launcher działa w wersji " + VERSION + "");
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
			Logger.a("Brak połączenia z internetem! (albo serwer padł) ");
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

	public static String[] read(String file) {
		new File(file);
		BufferedReader writer = null;
		String[] lines = new String[4096];
		try {
			writer = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "utf-8"));
			int i = 0;
			String line = null;
			while ((line = writer.readLine()) != null) {
				lines[i] = line;
				i++;
			}
			return lines;
		} catch (Exception ex) {
			Logger.a("KRYTYCZNY BŁĄD!");
			Logger.a("podczas zapisywania do pliku \"" + file + "\" ");
			Logger.a(ex.toString());
		} finally {
			try {writer.close();} catch (Exception ex) {}
		}
		return null;
	}
}
