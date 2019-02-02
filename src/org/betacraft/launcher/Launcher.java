package org.betacraft.launcher;

import java.applet.Applet;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class Launcher {
	public static String VERSION = "Preview 1";
	//public static String c_hash = "Process.Start(@\"java\", @\"-Xms1024m -Xmx1024m -cp \"\"\" + appData + @\"\\betacraft\\betacraft\\bin\\*\"\" -Djava.library.path=\"\"\" + appData + @\"\\betacraft\\betacraft\\bin\\natives\"\" net.minecraft.client.Minecraft \" + username);";
	//public static Applet applet;

	public void LaunchGame(String ram, String username) {
		try {
			File file = new File(getBetacraft() + "versions/", Window.chosen_version + ".jar");
			URL[] urls = new URL[1];

            urls[0] = file.toURI().toURL();
            URLClassLoader loader = new URLClassLoader(urls, getClass().getClassLoader());
            Class<Applet> jarClass = (Class<Applet>) loader.loadClass("net.minecraft.client.MinecraftApplet");
            
            //Class<? extends Applet> plugin = jarClass.asSubclass(Applet.class);

            //Constructor<? extends Applet> constructor = plugin.getConstructor();

            Applet result = jarClass.newInstance();

            result.init();
			/*final Class<Applet> appletClass = (Class<Applet>)new URLClassLoader(new URL[] {new URL(getBetacraft() + "versions/" + Window.chosen_version + ".jar")}).loadClass("net.minecraft.client.MinecraftApplet");
	        applet = appletClass.newInstance();
	        applet.setStub(Window.window);
	        applet.setSize(Window.window.getWidth(), Window.window.getHeight());
	        Window.window.setLayout(new BorderLayout());
	        Window.window.add(applet, "Center");
	        applet.init();
	        applet.start();
	        Window.window.validate();
			Logger.a("Włączam grę. Nick \"" + username + "\", RAM " + ram + ", Wersja \"" + Window.chosen_version + "\"");
			String start1 = OS.isWindows() ? "javaw" : "java";
			String start = start1 + " -Xmx" + ram + "M -cp \"" + getVerFolder() + "/" + Window.chosen_version + ".jar\" -Djava.library.path=\"" + getBetacraft() + "bin/natives\" net.minecraft.client.Minecraft " + username;
			String s = "java -Xmx1024m -cp \"" + getVerFolder() + "/" + Window.chosen_version + ".jar:" + getBetacraft() + "bin/jinput.jar:" + getBetacraft() + "bin/lwjgl.jar:" + getBetacraft() + "bin/lwjgl_util.jar\" -Djava.library.path=\"" + getBetacraft() + "bin/natives\" net.minecraft.client.Minecraft";
			System.out.println(Window.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			//Process process = Runtime.getRuntime().exec("java -jar Titan.jar");
			//Process process = new ProcessBuilder("java", "-jar", getBetacraft() + "bin/minecraft.jar", "-Djava.library.path=\"" + getBetacraft() + "bin/natives\"", "net.minecraft.client.Minecraft").start();
			//Process process = new ProcessBuilder("java", "\"-jar " + getBetacraft() + "versions/" + Window.chosen_version + ".jar -Djava.library.path=\"" + getBetacraft() + "bin/natives\" net.minecraft.client.Minecraft " + username).start();
			//Process process = new ProcessBuilder("java", "-jar", "/home/moresteck/Pulpit/Titan.jar").start();
			Process process = new ProcessBuilder("java", "-Xms1024m -Xmx1024m -cp", "\"/home/moresteck/.betacraft/bin/*\" -Djava.library.path=\"/home/moresteck/.betacraft/bin/natives\" net.minecraft.client.Minecraft Moresteck").start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			//System.out.printf("Output of running %s is:", Arrays.toString());
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}*/
			//InputStream err = process.getErrorStream();
		} catch (Exception ex) {
			Logger.a("KRYTYCZNY BŁĄD");
			Logger.a("podczas uruchamiania gry: ");
			//Logger.a(ex.getMessage());
			ex.printStackTrace();
		}
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
			Logger.a(ex.getMessage());
			ex.printStackTrace();
			return false;
		}
	}

	public static void Unrar(String filepath, String SRC) {
		
	}

	public static void downloadUpdate() {
		try {
			download("https://betacraft.ovh/versions/launcher.jar", new File(getBetacraft()), "betacraft.jar");
			final String pathToJar = Window.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch (Exception ex) {
			
		}
	}

	public static String getBetacraft() {
		String folder = null;
		if (OS.isLinux()) {
			folder = System.getProperty("user.home") + "/.betacraft/";
		} else if (OS.isiOS()) {
			folder = System.getProperty("user.home") + "/Library/Application Support/betacraft/";
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
			Logger.a(ex.getMessage());
			ex.printStackTrace();
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
			ex.printStackTrace();
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
			ex.printStackTrace();
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
			Logger.a(ex.getMessage());
			ex.printStackTrace();
		} finally {
			try {writer.close();} catch (Exception ex) {}
		}
		return null;
	}
}
