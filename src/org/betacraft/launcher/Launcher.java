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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Launcher {
	public static File SETTINGS = new File(BC.get() + "launcher/", "launcher.settings");
	public static File LOGIN = new File(BC.get(), "lastlogin");

	public static String chosen_version = "b1.6.6";
	public static String VERSION = "Preview 3 build 3";
	private static URLClassLoader classLoader;
	int sessions = 0;

	public static String update = "There is a new version of the launcher (%s). Would you like to update?";

	public static void main(String[] args) {
		new File(BC.get() + "versions/").mkdirs();
		new File(BC.get() + "launcher/lang").mkdirs();
		new File(BC.get() + "bin/natives/").mkdirs();
		if (args.length == 2 && args[0].equals("update")) {
			try {
				final String pathToJar = args[1];
				File version = new File(BC.get(), "betacraft.jar$tmp");
				File dest = new File(pathToJar);
				Files.copy(version.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
				new Runnable() {
					public void run() {
						try {
							Runtime.getRuntime().exec("java -jar " + pathToJar);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}.run();
				System.exit(0);
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}
		}
		new Window();
		try {
			Release.initVersions();
		} catch (Exception ex) {
			Logger.a("FATALNY ERROR: ");
			Logger.a(ex.getMessage());
			ex.printStackTrace();
		}
		Window.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Window.window.setVisible(true);
		Lang.apply();
		if (Launcher.checkForUpdate()) {
			Launcher.downloadUpdate();
		}
		String ver = Launcher.getProperty(SETTINGS, "version");
		if (ver.equals("")) {
			chosen_version = "c0.0.13a_03";
		} else {
			chosen_version = ver;
		}
	}

	public void LaunchGame(String params, final String username) {
		try {
			// This is a permanent solution for a1.0.6-b1.3_01 versions crashing (at least on Linux)
			// java.nio.BufferOverflowException, why?
			String retrocraft = " -Dhttp.proxyHost=classic.retrocraft.net -Dhttp.proxyPort=80 -Djava.util.Arrays.useLegacyMergeSort=true";
			String libpath = "-Djava.library.path=" + BC.get() + "bin/natives";
			if (getProperty(SETTINGS, "retrocraft").equals("true")) {
				libpath = libpath + retrocraft;
			}
			applyVersion();
			String jars = BC.get() + "bin/minecraft.jar:" + BC.get() + "bin/lwjgl.jar:" + BC.get() + "bin/lwjgl_util.jar:" + BC.get() + "bin/jinput.jar";
			if (OS.isWindows()) { // windows ty chuju pierdolon, niszczysz mi zycie
				jars = BC.get() + "bin\\*";
			}
			String line = " " + params + " -cp " + jars + " " + libpath + " net.minecraft.client.Minecraft " + username + " " + sessions;
			sessions++;

			line = OS.isWindows() ? (line = "javaw" + line) : (line = "java" + line);

			// Classic-Indev 20100130 works on this, but the newer versions come up with java.nio.BufferOverflowException
			if (chosen_version.equals("a1.0.1") || chosen_version.equals("a1.0.1_01") || chosen_version.startsWith("a1.0.2") || chosen_version.startsWith("a1.0.3") ||
					chosen_version.startsWith("a1.0.4") || chosen_version.startsWith("a1.0.5") || chosen_version.startsWith("in") || chosen_version.startsWith("c")) {
			//if (chosen_version != null) {
				String file = BC.get() + "versions/" + chosen_version + ".jar";
				String file1 = BC.get() + "bin/lwjgl.jar";
				String file2 = BC.get() + "bin/lwjgl_util.jar";
				String file3 = BC.get() + "bin/jinput.jar";

				final URL[] url = new URL[4];
				url[0] = new File(file).toURI().toURL();
				url[1] = new File(file1).toURI().toURL();
				url[2] = new File(file2).toURI().toURL();
				url[3] = new File(file3).toURI().toURL();

		        if (classLoader == null) {
		        	classLoader = new URLClassLoader(url);
		        }

		        final Class<Applet> appletClass;
				if (chosen_version.startsWith("c")) {
					appletClass = (Class<Applet>)classLoader.loadClass("com.mojang.minecraft.MinecraftApplet");
				} else {
					appletClass = (Class<Applet>)classLoader.loadClass("net.minecraft.client.MinecraftApplet");
				}
				final Applet applet = appletClass.newInstance();

				/*for (final Field field : appletClass.getDeclaredFields()) {
					final String name = field.getType().getName();
					if (!name.contains("awt") && !name.contains("java")) {
						Field fileField = null;
						final Class<?> clazz = classLoader.loadClass(name);
						for (final Field field1 : clazz.getDeclaredFields()) {
							if (Modifier.isStatic(field1.getModifiers()) && field1.getType().getName().equals("java.io.File")) {
								fileField = field1;
							}
						}
						if (fileField != null) {
							fileField.setAccessible(true);
							fileField.set(null, new File(BC.get()));
							break;
						}
					}
				}*/

				//AlphaStub stub = new AlphaStub(username, Integer.toString(sessions));
				//sessions++;

				//stub.doo(applet);
				System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	            System.setProperty("http.proxyPort", "80");
	            System.setProperty("http.proxyHost", "classic.retrocraft.net");
	            System.setProperty("java.library.path", BC.get() + "bin/natives");
	            System.setProperty("org.lwjgl.librarypath", BC.get() + "bin/natives");
		        System.setProperty("net.java.games.input.librarypath", BC.get() + "bin/natives");

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
				launcherFrameFake.addWindowListener(new WindowAdapter() {
		            @Override
		            public void windowClosing(final WindowEvent e) {
		            	applet.stop();
		                applet.destroy();
		                launcherFrameFake.setVisible(false);
		                launcherFrameFake.dispose();
		            }
		        });
				AlphaStub stub = new AlphaStub(username, Integer.toString(sessions));
				sessions++;
				applet.setStub(stub);
				stub.setLayout(new BorderLayout());
				stub.add(applet, "Center");
				stub.validate();
				launcherFrameFake.removeAll();
				launcherFrameFake.setLayout(new BorderLayout());
				launcherFrameFake.add(stub, "Center");
				launcherFrameFake.validate();
				applet.init();
				applet.start();
				//classLoader.close();
				return;
			}
			System.out.println(line);
			if (!getProperty(SETTINGS, "keepon").equals("true")) {
				Window.window.setVisible(false);
			}

			Process process = Runtime.getRuntime().exec(line);
			InputStream err = process.getErrorStream();
			InputStreamReader isr = new InputStreamReader(err);
			BufferedReader br = new BufferedReader(isr);
			String line1;
			while ((line1 = br.readLine()) != null) {
				Logger.a(line1);
			}
			Window.window.setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.a("KRYTYCZNY BLAD");
			Logger.a("podczas uruchamiania gry: ");
			Logger.a(ex.getMessage());
			Window.window.setVisible(true);
		}
	}

	public static String getVerLink(String version) {
		return "https://betacraft.ovh/versions/" + version + ".jar";
	}

	public static File getVerFolder() {
		return new File(BC.get() + "versions/");
	}

	public static boolean getVerDownloaded(String version) {
		File file = new File(getVerFolder(), version + ".jar");
		if (file.exists() && !file.isDirectory()) {
			return true;
		}
		return false;
	}

	public static boolean nativesDownloaded(boolean download) {
		File lwjgl = new File(BC.get() + "bin/", "lwjgl.jar");
		File lwjglutil = new File(BC.get() + "bin/", "lwjgl_util.jar");
		File jinput = new File(BC.get() + "bin/", "jinput.jar");

		File jinputDll = null;
		File jinputDll64 = null;
		File lwjgl32 = null;
		File lwjgl64 = null;
		File openal = null;
		File openal64 = null;

		File jinput_dx8 = null;
		File jinput_dx8_64 = null;

		if (OS.isLinux()) {
			jinputDll = new File(BC.get() + "bin/natives/", "libjinput-linux.so");
			jinputDll64 = new File(BC.get() + "bin/natives/", "libjinput-linux64.so");
			lwjgl32 = new File(BC.get() + "bin/natives/", "liblwjgl.so");
			lwjgl64 = new File(BC.get() + "bin/natives/", "liblwjgl64.so");
			openal = new File(BC.get() + "bin/natives/", "libopenal.so");
			openal64 = new File(BC.get() + "bin/natives/", "libopenal64.so");

			try {
				if (!download && (!jinputDll.exists() || !jinputDll64.exists() || !lwjgl64.exists() || !lwjgl32.exists() || !openal.exists() ||
						!openal64.exists())) {
					return false;
				}
			} catch (Exception ex) {
				Logger.a("Could not check for natives!");
				ex.printStackTrace();
			}

			if (download) {
				download("https://betacraft.ovh/versions/libjinput-linux.so", jinputDll);
				download("https://betacraft.ovh/versions/libjinput-linux64.so", jinputDll64);
				download("https://betacraft.ovh/versions/liblwjgl.so", lwjgl32);
				download("https://betacraft.ovh/versions/liblwjgl64.so", lwjgl64);
				download("https://betacraft.ovh/versions/libopenal.so", openal);
				download("https://betacraft.ovh/versions/libopenal64.so", openal64);
			}
		}
		if (OS.isWindows()) {
			jinputDll = new File(BC.get() + "bin/natives/", "jinput-raw.dll");
			jinputDll64 = new File(BC.get() + "bin/natives/", "jinput-raw_64.dll");
			jinput_dx8 = new File(BC.get() + "bin/natives/", "jinput-dx8.dll");
			jinput_dx8_64 = new File(BC.get() + "bin/natives/", "jinput-dx8_64.dll");
			lwjgl32 = new File(BC.get() + "bin/natives/", "lwjgl.dll");
			lwjgl64 = new File(BC.get() + "bin/natives/", "lwjgl64.dll");
			openal = new File(BC.get() + "bin/natives/", "OpenAL32.dll");
			openal64 = new File(BC.get() + "bin/natives/", "OpenAL64.dll");

			try {
				if (!download && (!jinputDll.exists() || !jinputDll64.exists() || !lwjgl64.exists() || !lwjgl32.exists() || !openal.exists() ||
						!openal64.exists() || !jinput_dx8.exists() || !jinput_dx8_64.exists())) {
					return false;
				}
			} catch (Exception ex) {
				Logger.a("Could not check for natives!");
				ex.printStackTrace();
			}

			if (download) {
				download("https://betacraft.ovh/versions/jinput-raw.dll", jinputDll);
				download("https://betacraft.ovh/versions/jinput-raw_64.dll", jinputDll64);
				download("https://betacraft.ovh/versions/jinput-dx8.dll", jinput_dx8);
				download("https://betacraft.ovh/versions/jinput-dx8_64.dll", jinput_dx8_64);
				download("https://betacraft.ovh/versions/lwjgl-windows.dll", lwjgl32);
				download("https://betacraft.ovh/versions/lwjgl64.dll", lwjgl64);
				download("https://betacraft.ovh/versions/OpenAL32.dll", openal);
				download("https://betacraft.ovh/versions/OpenAL64.dll", openal64);
			}
		}
		if (OS.isiOS()) {
			jinputDll = new File(BC.get() + "bin/natives/", "libjinput-osx.jnilib");
			lwjgl32 = new File(BC.get() + "bin/natives/", "liblwjgl.jnilib");
			openal = new File(BC.get() + "bin/natives/", "openal.dylib");
			openal64 = new File(BC.get() + "bin/natives/", "libopenal.dylib");

			try {
				if (!download && (!jinputDll.exists() || !lwjgl32.exists() || !openal.exists() ||
						!openal64.exists())) {
					return false;
				}
			} catch (Exception ex) {
				Logger.a("Could not check for natives!");
				ex.printStackTrace();
			}

			if (download) {
				download("https://betacraft.ovh/versions/libjinput-osx.jnilib", jinputDll);
				download("https://betacraft.ovh/versions/liblwjgl.jnilib", lwjgl32);
				download("https://betacraft.ovh/versions/openal.dylib", openal);
				download("https://betacraft.ovh/versions/libopenal.dylib", openal64);
			}
		}
		if (OS.isSolaris()) { // we are going to add support for this too
			lwjgl32 = new File(BC.get() + "bin/natives/", "liblwjgl.so");
			lwjgl64 = new File(BC.get() + "bin/natives/", "liblwjgl64.so");
			openal = new File(BC.get() + "bin/natives/", "libopenal.so");

			try {
				if (!download && (!lwjgl64.exists() || !lwjgl32.exists() || !openal.exists())) {
					return false;
				}
			} catch (Exception ex) {
				Logger.a("Could not check for natives!");
				ex.printStackTrace();
			}

			if (download) {
				download("https://betacraft.ovh/versions/liblwjgl.so", lwjgl32);
				download("https://betacraft.ovh/versions/liblwjgl64.so", lwjgl64);
				download("https://betacraft.ovh/versions/libopenal.so", openal);
			}
		}

		try {
			if (!download && (!jinput.exists() || !lwjgl.exists() || !lwjglutil.exists())) {
				return false;
			}
		} catch (Exception ex) {
			Logger.a("Could not check for libraries!");
			ex.printStackTrace();
		}

		if (download) {
			if (OS.isWindows()) {
				download("https://betacraft.ovh/versions/lwjgl-windows.jar", lwjgl);
			} else {
				download("https://betacraft.ovh/versions/lwjgl.jar", lwjgl);
			}
			download("http://s3.amazonaws.com/MinecraftDownload/lwjgl_util.jar", lwjglutil);
			download("http://s3.amazonaws.com/MinecraftDownload/jinput.jar", jinput);
		}
		return true;
	}

	public static String getCustomParameters() {
		String params = getProperty(SETTINGS, "launch");
		
		return (params.length() >= 2) ? params.substring(1, params.length() - 1) : "";
	}

	public static void applyVersion() {
		File version = new File(getVerFolder(), chosen_version + ".jar");
		File dest = new File(BC.get() + "bin", "minecraft.jar");
		try {
			dest.createNewFile();
			Files.copy(version.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Logger.a("Nie mozna przeniesc " + version.getPath() + " do " + dest.getPath());
			e.printStackTrace();
		}
	}

	public static boolean download(String link, File folder) {
		Logger.a("Zainicjowano pobieranie z " + link);
		try {
			folder.createNewFile();

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
		String update = getUpdate();
		try {
			boolean yes = false;
			String rr = Launcher.update.replaceAll("%s", update);
			int result = JOptionPane.showConfirmDialog(null, rr, Opcje.update, JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				Logger.a("Zaakceptowano pobranie aktualizacji " + update);
				yes = true;
			} else {
				Logger.a("Odmowiono pobrania aktualizacji. Launcher dziala w wersji " + VERSION);
			}
			if (yes || update.startsWith("!")) { // jezeli jest jakas wazna aktualizacja, to pobierz ja bez zgody :P
				new Pobieranie(update);
				download("https://betacraft.ovh/versions/launcher.jar", new File(BC.get(), "betacraft.jar$tmp"));
				final String pathToJar = Window.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				//File version = new File(BC.get(), "betacraft.jar$tmp");
				//File dest = new File(pathToJar);
				Runtime.getRuntime().exec("java -jar " + BC.get() + "betacraft.jar$tmp" + " org.betacraft.launcher.Launcher update " + pathToJar);
				Window.quit();
			}
		} catch (Exception ex) {
			Logger.a("Nie udalo sie pobrac aktualizacji!");
			ex.printStackTrace();
		}
	}

	public static void write(File file, String[] lines, boolean append) {
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	public static boolean checkForUpdate() {
		String update = getUpdate();
		if (update == null) {
			return false;
		}
		if (!VERSION.equalsIgnoreCase(update)) {
			Logger.a("Znaleziono aktualizacje (" + update + ").");
			return true;
		} else {
			Logger.a("Launcher dziala w wersji " + VERSION + "");
			return false;
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
		File file = new File(BC.get(), "lastlogin");
		if (!file.exists()) {
			return "";
		}
		return read(file)[0];
	}

	public static String[] read(File file) {
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedReader reader = null;
		String[] lines = new String[4096];
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "utf-8"));
			int i = 0;
			String line = null;
			while ((line = reader.readLine()) != null) {
				lines[i] = line;
				i++;
			}
			return lines;
		} catch (Exception ex) {
			Logger.a("KRYTYCZNY BLAD!");
			Logger.a("podczas czytania z pliku \"" + file + "\" ");
			Logger.a(ex.getMessage());
			ex.printStackTrace();
		} finally {
			try {reader.close();} catch (Exception ex) {}
		}
		return null;
	}

	public static void setProperty(File file, String property, String value) {
		String[] lines = read(file);
		String[] newlines = new String[lines.length + 1];
		boolean found = false;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] == null) continue;
			if (lines[i].startsWith(property + ":")) {
				newlines[i] = property + ":" + value;
				found = true;
				continue;
			}
			newlines[i] = lines[i];
		}
		
		if (!found) {
			newlines[lines.length] = property + ":" + value;
		}
		write(file, newlines, false);
	}

	public static String getProperty(File file, String property) {
		String[] lines = read(file);
		String value = "";
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] == null) continue;
			if (lines[i].startsWith(property + ":")) {
				value = lines[i].split(":", 2)[1];
				continue;
			}
		}
		return value;
	}
}
