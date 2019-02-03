package org.betacraft.launcher;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

public class Launcher {
	public static String VERSION = "Preview 1";
	int sessions = 0;

	public void LaunchGame(String ram, String username) {
		File launchwrapper = new File(getBetacraft() + "bin", "launchwrapper-1.5.jar");
		if (!launchwrapper.exists()) {
			download("https://libraries.minecraft.net/net/minecraft/launchwrapper/1.5/launchwrapper-1.5.jar", new File(getBetacraft() + "bin/"), "launchwrapper-1.5.jar");
		}
		try {
			//String file = getBetacraft() + "versions/" +  Window.chosen_version + ".jar";
			String retrocraft = " -Dhttp.proxyHost=classic.retrocraft.net -Dhttp.proxyPort=80 -Djava.util.Arrays.useLegacyMergeSort=true";
			String libpath = "-Djava.library.path=\"" + getBetacraft() + "bin/natives\"";
			if (Window.chosen_version.startsWith("c") || Window.chosen_version.startsWith("in")) {
				libpath = libpath + retrocraft;
			}
			applyVersion();
			String jars = getBetacraft() + "bin/minecraft.jar:" + getBetacraft() + "bin/lwjgl.jar:" + getBetacraft() + "bin/lwjgl_util.jar:" + getBetacraft() + "bin/jinput.jar";
			String line = "java -Xms1024m -Xmx1024m -cp " + jars + " " + libpath + " net.minecraft.client.Minecraft " + username + " " + sessions;

			if (Window.chosen_version.startsWith("a1.0.5") || Window.chosen_version.startsWith("a1.0.2") || Window.chosen_version.startsWith("a1.0.1") || 
					Window.chosen_version.startsWith("a1.0.4") || Window.chosen_version.startsWith("a1.0.3")) {
				line = "java -Xms1024m -Xmx1024m -cp " + getBetacraft() + "bin/lwjgl.jar:" + getBetacraft() + "bin/lwjgl_util.jar:" + getBetacraft() + "bin/jinput.jar";

				File file = new File(getBetacraft() + "bin/minecraft.jar");
				File file1 = new File(getBetacraft() + "bin/lwjgl.jar");
				File file2 = new File(getBetacraft() + "bin/lwjgl_util.jar");
				File file3 = new File(getBetacraft() + "bin/jinput.jar");
				URL[] urls = new URL[4];

				urls[0] = file.toURI().toURL();
				urls[1] = file1.toURI().toURL();
				urls[2] = file2.toURI().toURL();
				urls[3] = file3.toURI().toURL();
				URLClassLoader loader = new URLClassLoader(urls);
				// TODO zaladowac wszystkie classy

				Class<Applet> jarClass = (Class<Applet>) loader.loadClass("net.minecraft.client.MinecraftApplet");
				//loader.loadClass("org.lwjgl.LWJGLUtil");

				Class<? extends Applet> plugin = jarClass.asSubclass(Applet.class);

				Constructor<? extends Applet> constructor = plugin.getConstructor();

				final Applet result = jarClass.newInstance();

				for (final Field field : jarClass.getDeclaredFields()) {
		            final String name = field.getType().getName();
		            if (!name.contains("awt") && !name.contains("java")) {
		                Field fileField = null;
		                final Class<?> clazz = loader.loadClass(name);
		                for (final Field field1 : clazz.getDeclaredFields()) {
		                    if (Modifier.isStatic(field1.getModifiers()) && field1.getType().getName().equals("java.io.File")) {
		                        fileField = field1;
		                    }
		                }
		                if (fileField != null) {
		                    fileField.setAccessible(true);
		                    fileField.set(null, new File(getBetacraft()));
		                    break;
		                }
		            }
		        }

				final Frame launcherFrameFake = new Frame();
		        launcherFrameFake.setTitle("Minecraft");
		        launcherFrameFake.setBackground(Color.BLACK);
		        final JPanel panel = new JPanel();
		        launcherFrameFake.setLayout(new BorderLayout());
		        panel.setPreferredSize(new Dimension(854, 480));
		        launcherFrameFake.add(panel, "Center");
		        launcherFrameFake.pack();
		        launcherFrameFake.setLocationRelativeTo(null);
		        launcherFrameFake.setVisible(true);
				AlphaStub stub = new AlphaStub(username, Integer.toString(sessions));
				result.setStub(stub);
				stub.setLayout(new BorderLayout());
				stub.add(result, "Center");
				stub.validate();
				launcherFrameFake.removeAll();
		        launcherFrameFake.setLayout(new BorderLayout());
		        launcherFrameFake.add(stub, "Center");
		        launcherFrameFake.validate();
				result.init();
				result.start();
		        loader.close();
				return;
			}
			CommandLine commandLine = CommandLine.parse(line);
			DefaultExecutor executor = new DefaultExecutor();
			executor.setExitValue(1);
			executor.execute(commandLine);
			sessions++;
			//Window.window.setVisible(false);
			//Minecraft.main(new String[] {"Moresteck", "1"});
		} catch (Exception ex) {
			Logger.a("KRYTYCZNY BLAD");
			Logger.a("podczas uruchamiania gry: ");
			//Logger.a(ex.getMessage());
			ex.printStackTrace();
			//Window.window.setVisible(true);
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

	public static void applyVersion() {
		File version = new File(getVerFolder(), Window.chosen_version + ".jar");
		File dest = new File(getBetacraft() + "bin/minecraft.jar");
		try {
			Files.copy(version.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Logger.a("Nie mozna przeniesc " + version.getPath() + " do " + dest.getPath());
			e.printStackTrace();
		}
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
			Logger.a("KRYTYCZNY BLAD");
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
			Logger.a("Twoj system nie jest wspierany.");
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
			Logger.a("KRYTYCZNY BLAD!");
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
			Logger.a("Znaleziono aktualizacje (" + update + ").");
			int result = JOptionPane.showConfirmDialog(null, "Wydano nowa wersje launchera (" + update + "). Czy chcesz pobrac aktualizacje?", "Aktualizacja", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				downloadUpdate();
			} else {
				Logger.a("Odmowiono pobrania aktualizacji. Launcher dziala w wersji " + VERSION);
			}
		} else {
			Logger.a("Launcher dziala w wersji " + VERSION + "");
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
			Logger.a("Brak polaczenia z internetem! (albo serwer padl) ");
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
			Logger.a("KRYTYCZNY BLAD!");
			Logger.a("podczas zapisywania do pliku \"" + file + "\" ");
			Logger.a(ex.getMessage());
			ex.printStackTrace();
		} finally {
			try {writer.close();} catch (Exception ex) {}
		}
		return null;
	}
}
