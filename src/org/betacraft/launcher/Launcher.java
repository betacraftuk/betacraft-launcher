package org.betacraft.launcher;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.betacraft.Wrapper;

// Main class
public class Launcher {
	// Location of the launcher executable
	public static File currentPath;
	public static File SETTINGS = new File(BC.get() + "launcher/", "launcher.settings");

	// Chosen version on init is equivalent to the BetaCraft's server version
	public static String chosen_version = "b1.7.3";
	public static String VERSION = "1.08"; // TODO Always update this

	// This is done incorrectly, but seems to not cause bugs,
	// so it's here to stay this way, at least for now
	public static Integer sessions = 0;

	public static String update = "There is a new version of the launcher available (%s). Would you like to update?";
	public static String lang_version = "Version";

	public static void main(String[] args) {
		// Create required directories
		new File(BC.get() + "versions/").mkdirs();
		new File(BC.get() + "launcher/lang/").mkdirs();
		new File(BC.get() + "bin/natives/").mkdirs();

		Logger.a("BetaCraft Launcher v" + VERSION + " loaded.");

		// If the properties file doesn't exist, create it
		if (!SETTINGS.exists() || Lang.get("version").equals("")) {
			writeDefault();
		}

		// Unload natives from previous sessions if they're not active
		unloadNatives();

		// Launch the game if wanted
		if (args.length > 0 && args[0].equals("wrap")) {
			// Convert arguments to work with Wrapper
			String[] split = new String[args.length-1];
			for (int i = 1; i < args.length; i++) {
				split[i-1] = args[i];
			}

			// Try to start the game
			Wrapper.main(split);
			return;
		}

		// Finish updating of the launcher if wanted
		if (args.length >= 2 && (args[0].equals("update") || (args[1].equals("update")))) {
			try {
				// Backwards compatibility with older versions of the launcher
				int e = 1;
				if (args[1].equals("update")) {
					e++;
				}

				// Define a path for the update destination
				String pathToJar = "";
				for (int i = e; i < args.length; i++) {
					if (pathToJar.equals("")) {
						pathToJar = args[i];
					} else {
						pathToJar = pathToJar + " " + args[i];
					}
				}

				// Fix the path for different operating systems
				if ((pathToJar.startsWith("//") && !OS.isWindows()) || (pathToJar.startsWith("/") && OS.isWindows())) pathToJar = pathToJar.substring(1, pathToJar.length());

				// Move the updated version to the destination
				File version = new File(BC.get(), "betacraft.jar$tmp");
				File dest = new File(pathToJar);
				Files.copy(version.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

				// Launch the updated launcher
				ArrayList<String> pa = new ArrayList<String>();
				pa.add("java");
				pa.add("-jar");
				pa.add(dest.toPath().toString());
				new ProcessBuilder(pa).start();

				// Exit this process, its job is done
				System.exit(0);
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}
			return;
		}

		// Define last chosen version
		String ver = Launcher.getProperty(SETTINGS, "version");
		if (!ver.equals("")) {
			chosen_version = ver;
		}

		try {
			// Define a current path for the launcher
			String p = Window.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			if (OS.isWindows()) {
				p = p.substring(1, p.length());
			}
			currentPath = new File(p);

			// Create UI
			new Window();
			Release.initVersions();
		} catch (Exception ex) {
			Logger.a("A critical error has occurred while trying to initialize the launcher!");
			ex.printStackTrace();
		}

		// Finish UI
		Window.mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Window.mainWindow.setVisible(true);

		// Load language pack
		String lang = Launcher.getProperty(Launcher.SETTINGS, "language");
		if (!lang.equals("")) {
			// Refresh the language pack to match the latest version
			Lang.download(lang);
		}
		Lang.apply();

		// Check for updates
		if (Launcher.checkForUpdate()) Launcher.downloadUpdate();
	}

	public void launchGame(ArrayList<String> customparams, final String username) {
		try {
			// Rarely there will be no chosen version, so we need to check that
			if (chosen_version != null) {
				ArrayList<String> params = new ArrayList<String>();

				// The colon in the launch arguments is different for Windows
				String colon = ":";
				if (OS.isWindows()) {
					// Does it even show the console with "java"?
					params.add("javaw");
					colon = ";";
				} else {
					params.add("java");
				}

				// Additional parameters:
				// - Discord RPC
				String add = "";
				if (Launcher.getProperty(SETTINGS, "RPC").equalsIgnoreCase("true")) {
					// Add DRPC to the launch arguments
					add = colon + BC.get() + "launcher/discord_rpc.jar";
				}

				// Add custom parameters from options
				if (customparams != null && !customparams.isEmpty()) {
					params.addAll(customparams);
				}

				boolean proxy = Launcher.getProperty(Launcher.SETTINGS, "proxy").equals("true");

				// Add the rest of params and launch the wrapper
				params.add("-cp");
				params.add(BC.get() + "launcher/betacraft_wrapper.jar" + add);
				params.add("org.betacraft.Wrapper");
				params.add(username + ":" + Integer.toString(sessions) + ":" + chosen_version + ":" + proxy);
				params.add(BC.get());
				ProcessBuilder builder = new ProcessBuilder(params);
				builder.start();

				// For debugging
				/*Process process = builder.start();
				InputStream err = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(err);
				BufferedReader br = new BufferedReader(isr);
				String line1;
				while ((line1 = br.readLine()) != null) {
					Logger.a(line1);
				}*/

				// Close the launcher if desired
				if (!Launcher.getProperty(SETTINGS, "keepopen").equals("true")) {
					Window.quit();
				}
				return;
			}
		} catch (Exception ex) {
			Logger.a("A critical error has occurred while attempting to launch the game!");
			ex.printStackTrace();
		}
	}

	public static void writeDefault() {
		setProperty(SETTINGS, "launch", "-Xmx1G");
		setProperty(SETTINGS, "language", "English");
		setProperty(SETTINGS, "version", "b1.7.3");
		setProperty(SETTINGS, "RPC", "true");
		setProperty(SETTINGS, "keepopen", "false");
		setProperty(SETTINGS, "proxy", "true");
	}

	public static String getVerLink(String version) {
		return "https://betacraft.ovh/versions/" + version + ".jar";
	}

	public static File getVerFolder() {
		return new File(BC.get() + "versions/");
	}

	public static boolean isReadyToPlay(String version) {
		File file = new File(getVerFolder(), version + ".jar");
		if (file.exists() && !file.isDirectory()) {
			return true;
		}
		return false;
	}

	// This method has two uses:
	// one use is to download the natives - true
	// the other one is to check if natives are downloaded - false
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
		if (OS.isMac()) {
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

	public static ArrayList<String> getCustomParameters() {
		// Get the parameters in form of string
		String params = getProperty(SETTINGS, "launch");

		// If the parameters are empty, return an empty list
		if (params.length() == 0) return new ArrayList<String>();

		// Split them between spaces
		String[] split = params.split(" ");

		// Add the parameters to the list
		ArrayList<String> parameters = new ArrayList<String>();
		for (String s : split) {
			parameters.add(s);
		}

		// Return the list
		return parameters;
	}

	public static void unloadNatives() {
		// Get the version list
		File file = new File(BC.get() + "versions/");
		String[] entries = file.list();
		for (String s: entries) {
			File currentFile = new File(file.getPath(), s);
			// If it's a directory, it's likely a natives folder that we have to delete
			if (currentFile.isDirectory()) {
				for (String s1 : currentFile.list()) {
					// Delete files inside this folder
					new File(currentFile.getPath(), s1).delete();
				}
				try {
					// Delete the folder itself; we cannot delete the directory
					// without at first removing the files inside it, because of
					// java dumbness.
					Files.delete(currentFile.toPath());
				} catch (Exception ex) {}
			}
		}
	}

	public static DownloadResult download(String link, File folder) {
		Logger.a("Download started from: " + link);

		// Get a backup file that we will use to restore the file if the upload fails
		File backupfile = new File(BC.get() + "launcher/backup.tmp");
		try {
			// If the file already exists, make a copy of it
			if (!folder.createNewFile()) {
				backupfile.createNewFile();
				Files.copy(folder.toPath(), backupfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}

			// Start download
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
			return DownloadResult.OK;
		} catch (Exception ex) {
			Logger.a("A critical error has occurred while attempting to download a file from: " + link);
			ex.printStackTrace();

			// Delete the failed download
			folder.delete();

			// Restore the copy if existed
			if (backupfile.exists()) {
				try {
					Files.copy(backupfile.toPath(), folder.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {}

				// Remove the backup file
				backupfile.delete();

				// We had a backup, so we're returning this value
				return DownloadResult.FAILED_WITH_BACKUP;
			}
			return DownloadResult.FAILED_WITHOUT_BACKUP;
		}
	}

	public static void Unrar(String filepath, String SRC) {

	}

	public static void downloadUpdate() {
		// Get the update name
		String update = getUpdate();
		try {
			boolean yes = false;

			// Format the message
			String update_name = update.startsWith("!") ? update.replace("!", "") : update; 
			String rr = Launcher.update.replaceAll("%s", update_name);

			// Ask if the user wants this update or not
			int result = JOptionPane.showConfirmDialog(null, rr, Options.update, JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				Logger.a("The user wants to update to: " + update_name);
				yes = true;
			} else {
				Logger.a("The user doesn't want to update. The launcher stays at version: " + VERSION);
			}
			// If the user accepted the update, or it is a mandatory update, download it
			if (yes || update.startsWith("!")) {
				// Display downloading dialog
				new Pobieranie(update_name);
				// Download the update
				download("https://betacraft.ovh/versions/launcher.jar", new File(BC.get(), "betacraft.jar$tmp"));

				// Launch the new version to finish updating
				final String pathToJar = Window.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				Runtime.getRuntime().exec("java -jar " + BC.get() + "betacraft.jar$tmp" + " update " + pathToJar);

				// Close this process
				Window.quit();
			}
		} catch (Exception ex) {
			Logger.a("An error has occurred while updating the launcher!");
			ex.printStackTrace();
		}
	}

	public static void write(File file, String[] lines, boolean append) {
		try {
			// Create new file, if it doesn't already exist
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedWriter writer = null;
		try {
			// Write in UTF-8
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, append), "utf-8"));
			for (int i = 0; i < lines.length; i++) {
				// Skip empty lines
				if (lines[i] != null) {
					writer.write(lines[i]);
					writer.newLine();
				}
			}
		} catch (Exception ex) {
			Logger.a("A critical error occurred while attempting to write to file: " + file);
			ex.printStackTrace();
		} finally {
			// Close the file
			try {writer.close();} catch (Exception ex) {}
		}
	}

	public static boolean checkForUpdate() {
		// Get the latest version
		String update = getUpdate();
		if (update == null) {
			// No internet connection scenario
			return false;
		}
		String update_name = update.startsWith("!") ? update.replace("!", "") : update; 
		if (!VERSION.equalsIgnoreCase(update_name)) {
			// The latest version doesn't match the local version
			Logger.a("Found a new version of the launcher (" + update + ").");
			return true;
		} else {
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
			Logger.a(null);
		} catch (SocketTimeoutException ex) {
			Logger.a(null);
		} catch (SocketException ex) {
			Logger.a(null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
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
			// Create new, if doesn't exist
			if (file.createNewFile()) {
				Logger.a("Created a new file: " + file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedReader reader = null;
		String[] lines = new String[4096]; // We likely wouldn't have to deal with more lines than this
		try {
			// Read in UTF-8
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
			Logger.a("A critical error occurred while reading from file: " + file);
			ex.printStackTrace();
		} finally {
			// Close the file
			try {reader.close();} catch (Exception ex) {}
		}
		return null;
	}

	public static void setProperty(File file, String property, String value) {
		// Read the lines
		String[] lines = read(file);
		String[] newlines = new String[lines.length + 1];

		// Try to find the property wanted to be set
		boolean found = false;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] == null) continue;
			if (lines[i].startsWith(property + ":")) {
				// The wanted property has been found, so we're going to replace its value
				newlines[i] = property + ":" + value;
				found = true;
				continue;
			}
			// The property didn't match, just take this line further
			newlines[i] = lines[i];
		}

		if (!found) {
			// There was no wanted property in the file, so we're going to append it to the file 
			write(file, new String[] {property + ":" + value}, true);
			return;
		}

		// Write to file, without appending
		write(file, newlines, false);
	}

	public static String getProperty(File file, String property) {
		String[] lines = read(file);
		String value = "";
		for (int i = 0; i < lines.length; i++) {
			// If the array is empty, ignore it
			if (lines[i] == null) continue;

			// Check if the property matches
			if (lines[i].startsWith(property + ":")) {
				value = lines[i].split(":", 2)[1];
				break;
			}
		}
		return value;
	}

	private static final Pattern patternControlCode = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");

	public static String ticksToElapsedTime(int ticks)
	{
		int i = ticks / 20;
		int j = i / 60;
		i = i % 60;
		return i < 10 ? j + ":0" + i : j + ":" + i;
	}

	public static String stripControlCodes(String p_76338_0_)
	{
		return patternControlCode.matcher(p_76338_0_).replaceAll("");
	}

	protected static String[] splitObjectName(String toSplit)
	{
		String[] astring = new String[] {null, toSplit};
		int i = toSplit.indexOf(58);

		if (i >= 0)
		{
			astring[1] = toSplit.substring(i + 1, toSplit.length());

			if (i > 1)
			{
				astring[0] = toSplit.substring(0, i);
			}
		}

		return astring;
	}
}
