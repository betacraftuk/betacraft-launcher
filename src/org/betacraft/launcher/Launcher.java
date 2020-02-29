package org.betacraft.launcher;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.betacraft.ClassicMPWrapper;
import org.betacraft.ClassicWrapper;
import org.betacraft.FkWrapper;
import org.betacraft.PreClassicWrapper;
import org.betacraft.Wrapper;
import org.betacraft.launcher.Window.Tab;

// Main class
public class Launcher {
	// Location of the launcher executable
	public static File currentPath;
	public static File SETTINGS;

	// Chosen version on init is equivalent to the BetaCraft's server version
	public static String VERSION = "1.09"; // TODO Always update this

	// This is done incorrectly, but seems to not cause bugs,
	// so it's here to stay this way, at least for now
	public static Integer sessions = 0;

	public static Instance currentInstance;

	public static void main(String[] args) {
		try {
			if (OS.isLinux()) UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			if (OS.isWindows()) UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception ex) {
			// why
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex1) {
				ex1.printStackTrace();
				Logger.printException(ex1);
			}
		}

		try {
			// Define a current path for the launcher
			String p = Window.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			if (OS.isWindows()) {
				p = p.substring(1, p.length());
			}
			currentPath = new File(p);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Launch the game if wanted
		if (args.length > 0 && args[0].equals("wrap")) {
			BC.wrapped = true;
			SETTINGS = new File(BC.get() + "launcher", "launcher.settings");
			Lang.refresh();
			String username = args[1];
			String sessionid = args[2];
			String server = args[3].equals("-") ? null : args[3];
			String mppass = args[4].equals("-") ? "0" : args[4];
			// Convert arguments to work with Wrapper
			StringBuilder split = new StringBuilder();
			for (int i = 5; i < args.length; i++) {
				split.append(args[i] + " ");
			}
			String instanceName = split.toString();
			instanceName = instanceName.substring(0, instanceName.length() - 1);

			currentInstance = Instance.loadInstance(instanceName);
			ReleaseJson json = new ReleaseJson(currentInstance.version, false);
			String meth = json.getLaunchMethod();

			// Get addons as classes
			ArrayList<Class> addons = new ArrayList<Class>();
			if (!currentInstance.addons.equals("")) {
				try {
					URLClassLoader loader = new URLClassLoader(new URL[] {
				            new URL("file://" + BC.get() + "launcher" + File.separator + "addons" + File.separator)
				    });
					System.out.println("Loading addons...");
					for (String s : currentInstance.addons) {
						//File classFile = new File(BC.get() + "launcher" + File.separator + "addons", s + ".class");
						System.out.println("- " + s);
						addons.add(loader.loadClass(s));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					Logger.printException(ex);
				}
			}
			
			if (meth.equalsIgnoreCase("rd") || meth.equalsIgnoreCase("mc")) {
				new PreClassicWrapper(username, currentInstance.name, currentInstance.version, sessionid, currentInstance.gameDir, currentInstance.height, currentInstance.width, currentInstance.RPC, json.getLaunchMethod(), server, mppass, Lang.WINDOW_USER, Lang.WRAP_VERSION, currentInstance.getIcon(), addons);
			} else if (meth.equalsIgnoreCase("classic")) {
				new ClassicWrapper(username, currentInstance.name, currentInstance.version, sessionid, currentInstance.gameDir, currentInstance.height, currentInstance.width, currentInstance.RPC, json.getLaunchMethod(), server, mppass, Lang.WINDOW_USER, Lang.WRAP_VERSION, currentInstance.getIcon(), addons);
			} else if (meth.equalsIgnoreCase("classicmp")) {
				new ClassicMPWrapper(username, currentInstance.name, currentInstance.version, sessionid, currentInstance.gameDir, currentInstance.height, currentInstance.width, currentInstance.RPC, json.getLaunchMethod(), server, mppass, Lang.WINDOW_USER, Lang.WRAP_VERSION, currentInstance.getIcon(), addons);
			} else if (meth.equalsIgnoreCase("indev")) {
				new Wrapper(username, currentInstance.name, currentInstance.version, sessionid, currentInstance.gameDir, currentInstance.height, currentInstance.width, currentInstance.RPC, json.getLaunchMethod(), server, mppass, Lang.WINDOW_USER, Lang.WRAP_VERSION, currentInstance.getIcon(), addons);
			} else if (meth.equalsIgnoreCase("4k")) {
				new FkWrapper(username, currentInstance.name, currentInstance.version, sessionid, currentInstance.gameDir, currentInstance.height, currentInstance.width, currentInstance.RPC, json.getLaunchMethod(), server, mppass, Lang.WINDOW_USER, Lang.WRAP_VERSION, currentInstance.getIcon(), addons);
			} else {
				try {
					URLClassLoader loader = new URLClassLoader(new URL[] {
				            new URL("file://" + BC.get() + "launcher" + File.separator + "launch-methods" + File.separator)
				    });
					System.out.print(meth);
					Class c = loader.loadClass(meth);
					Constructor con = c.getConstructor(String.class, String.class, String.class, String.class, String.class, Integer.class, Integer.class, Boolean.class, String.class, String.class, String.class, String.class, String.class, Image.class, ArrayList.class);
					con.newInstance(username, currentInstance.name, currentInstance.version, sessionid, currentInstance.gameDir, currentInstance.height, currentInstance.width, currentInstance.RPC, json.getLaunchMethod(), server, mppass, Lang.WINDOW_USER, Lang.WRAP_VERSION, currentInstance.getIcon(), addons);
				} catch (Exception ex) {
					ex.printStackTrace();
					Logger.printException(ex);
				}
			}

			// Try to start the game
			//Wrapper.main(split);
			return;
		}

		SETTINGS = new File(BC.get() + "launcher", "launcher.settings");

		if (SETTINGS.exists() && !getProperty(SETTINGS, "version").equals("1")) {
			removeRecursively(new File(BC.get() + "launcher"));
			writeDefault();
		}

		// Create required directories
		new File(BC.get() + "versions" + File.separator + "jsons").mkdirs();
		new File(BC.get() + "launcher" + File.separator + "lang").mkdirs();
		new File(BC.get() + "launcher" + File.separator + "addons").mkdirs();
		new File(BC.get() + "launcher" + File.separator + "instances").mkdirs();
		new File(BC.get() + "launcher" + File.separator + "launch-methods").mkdirs();
		new File(BC.get() + "bin" + File.separator + "natives").mkdirs();

		Logger.a("BetaCraft Launcher v" + VERSION + " loaded.");

		// Load language pack
		Lang.refresh();
		readLastLogin();

		// If the properties file doesn't exist, create it
		if (!SETTINGS.exists() || Launcher.getProperty(SETTINGS, "lastInstance").equals("")) {
			writeDefault();
			currentInstance = Instance.newInstance(Launcher.getProperty(Launcher.SETTINGS, "lastInstance"));
			currentInstance.saveInstance();
		} else {
			currentInstance = Instance.loadInstance(Launcher.getProperty(Launcher.SETTINGS, "lastInstance"));
			if (currentInstance == null) {
				currentInstance = Instance.newInstance(Launcher.getProperty(Launcher.SETTINGS, "lastInstance"));
				currentInstance.saveInstance();
			}
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
				Logger.printException(ex);
				System.exit(0);
			}
			return;
		}

		try {
			// Create UI

			// TODO Check for updates
			if (Launcher.checkForUpdate(true)) Launcher.downloadUpdate(true);
			new Window();
			if (!MojangLogging.password.equals("")) {
				new MojangLogging().authenticate(MojangLogging.email, MojangLogging.password);
			} else {
				if (MojangLogging.email.equals("")) {
					Window.nicknameButton.setText(Lang.LOGIN_CLICK_HERE_TO_LOG_IN);
				} else {
					Window.nicknameButton.setText(String.format(Lang.WINDOW_USER, MojangLogging.email));
				}
			}
			Release.initVersions();
			Addon.loadAddons();
			ModsRepository.loadMods();
		} catch (Exception ex) {
			Logger.a("A critical error has occurred while trying to initialize the launcher!");
			ex.printStackTrace();
			Logger.printException(ex);
		}

		// Finish UI
		Window.mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Window.mainWindow.setVisible(true);
	}

	public static void restart() {
		try {
			ArrayList<String> params = new ArrayList<String>();
			params.add("java");
			params.add("-jar");
			params.add(currentPath.toPath().toString());
			ProcessBuilder builder = new ProcessBuilder(params);
			builder.start();
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
		Window.quit(true);
	}

	public static void setInstance(Instance instance) {
		Launcher.currentInstance = instance;
		Window.selectedInstanceDisplay.setText(Launcher.currentInstance.name + " [" + Launcher.currentInstance.version + "]");
		setProperty(SETTINGS, "lastInstance", Launcher.currentInstance.name);
	}

	public void launchGame(Instance instance) {
		this.launchGame(instance, "-", "-");
	}

	public void launchGame(Instance instance, String server, String mppass) {
		try {
			// Rarely there will be no chosen version, so we need to check that
			if (instance.version != null) {
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
				if (instance.RPC) {
					// Add DRPC to the launch arguments
					add = colon + BC.get() + "launcher" + File.separator + "discord_rpc.jar";
				}

				// Add custom parameters from options
				if (instance.launchArgs != null && !instance.launchArgs.isEmpty()) {
					params.addAll(getCustomParameters());
				}

				if (instance.proxy) {
					String[] args = Release.getReleaseByName(instance.version).getJson().getProxyArgs().split(" ");
					for (String s : args) {
						params.add(s);
					}
				}

				// Add the rest of params and launch the wrapper
				params.add("-cp");
				params.add(BC.get() + "launcher" + File.separator + "betacraft_wrapper.jar" + add);
				params.add("org.betacraft.launcher.Launcher");
				params.add("wrap");
				params.add(getNickname());
				if (MojangLogging.userProfile != null) {
					params.add(getAuthToken(true));
				} else {
					params.add("0");
				}
				params.add(server);
				params.add(mppass);
				params.add(currentInstance.name);
				System.out.println(params.toString());
				ProcessBuilder builder = new ProcessBuilder(params);
				builder.start();

				// Close the launcher if desired
				if (!instance.keepopen) {
					Window.quit(true);
				}

				// For debugging
				/*Process process = builder.start();
				InputStream err = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(err);
				BufferedReader br = new BufferedReader(isr);
				String line1;
				while ((line1 = br.readLine()) != null) {
					Logger.a(line1);
				}*/
				return;
			}
		} catch (Exception ex) {
			Logger.a("A critical error has occurred while attempting to launch the game!");
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	public static void writeDefault() {
		setProperty(SETTINGS, "language", "English");
		setProperty(SETTINGS, "lastInstance", "(default instance)");
		setProperty(SETTINGS, "tab", Tab.CHANGELOG.name());
		setProperty(SETTINGS, "version", "1");
	}

	public static void removeRecursively(File folder) {
		String[] entries = folder.list();
		for (String s: entries) {
			File currentFile = new File(folder.getPath(), s);
			if (currentFile.isDirectory()) {
				for (String s1 : currentFile.list()) {
					// Delete files inside this folder
					new File(currentFile.getPath(), s1).delete();
				}
				try {
					Files.delete(currentFile.toPath());
				} catch (Exception ex) {}
			}
		}
	}

	public static File getVerFolder() {
		return new File(BC.get() + "versions" + File.separator);
	}

	public static boolean isReadyToPlay(String version) {
		File file = new File(getVerFolder(), version + ".jar");
		boolean bol = false;
		if (file.exists() && !file.isDirectory()) {
			bol = true;
		}
		boolean bol1 = false;
		ReleaseJson json = Release.getReleaseByName(version).getJson();
		if (!json.getLaunchMethodLink().equals("")) {
			if (download(json.getLaunchMethodLink(), new File(BC.get() + "launcher" + File.separator + "launch-methods", json.getLaunchMethod() + ".class")) != DownloadResult.FAILED_WITHOUT_BACKUP) {
				bol1 = true;
			} else {
				JOptionPane.showMessageDialog(null, "Couldn't download the launch method.", "Error", JOptionPane.ERROR_MESSAGE);
				bol1 = false;
			}
		}
		boolean bol2 = false;
		for (String s : Launcher.currentInstance.addons) {
			if (download("https://betacraft.pl/launcher/assets/addons/" + s + ".class", new File(BC.get() + "launcher" + File.separator + "addons", s + ".class")) != DownloadResult.FAILED_WITHOUT_BACKUP) {
				bol2 = true;
			} else {
				JOptionPane.showMessageDialog(null, "Couldn't download addon: " + s, "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		return bol && bol1 && bol2;
	}

	public static boolean checkDepends() {
		File bin = new File(BC.get() + "bin");
		if (bin.listFiles().length <= 1) {
			return false;
		}
		if (new File(bin, "natives").listFiles().length <= 1) {
			return false;
		}
		try {
			URL url = new URL("https://betacraft.pl/launcher/assets/depends-version.txt");
			Scanner s = new Scanner(url.openStream(), "UTF-8");
			String libs = s.nextLine().split(":")[1];
			String natives = s.nextLine().split(":")[1];
			s.close();

			if (!libs.equals(Launcher.getProperty(SETTINGS, "libs-version"))) {
				Launcher.setProperty(SETTINGS, "libs-version", libs);
				Launcher.setProperty(SETTINGS, "natives-version", natives);
				return false;
			}
			if (!natives.equals(Launcher.getProperty(SETTINGS, "natives-version"))) {
				Launcher.setProperty(SETTINGS, "libs-version", libs);
				Launcher.setProperty(SETTINGS, "natives-version", natives);
				return false;
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);

			return false;
		}
	}

	public static boolean downloadDepends() {
		File destLibs = new File(BC.get() + "bin" + File.separator);
		File destNatives = new File(BC.get() + "bin" + File.separator + "natives" + File.separator);

		destNatives.mkdirs();

		String link1 = "https://betacraft.pl/launcher/assets/natives-windows.zip";
		String link2 = "https://betacraft.pl/launcher/assets/libs-windows.zip";
		if (OS.isLinux()) {
			link2 = "https://betacraft.pl/launcher/assets/libs-linux.zip";
			link1 = "https://betacraft.pl/launcher/assets/natives-linux.zip";
		}
		if (OS.isMac()) {
			link2 = "https://betacraft.pl/launcher/assets/libs-osx.zip";
			link1 = "https://betacraft.pl/launcher/assets/natives-osx.zip";
		}

		File dest1 = new File(destNatives, "natives.zip");
		switch (download(link1, dest1)) {
		case FAILED_WITH_BACKUP: return true;
		case FAILED_WITHOUT_BACKUP: return true;
		case OK:
		}

		Unrar(dest1.toPath().toString(), destNatives.toPath().toString());
		//dest1.delete();

		File dest2 = new File(destLibs, "libs.zip");
		switch (download(link2, dest2)) {
		case FAILED_WITH_BACKUP: return true;
		case FAILED_WITHOUT_BACKUP: return true;
		case OK:
		}

		Unrar(dest2.toPath().toString(), destLibs.toPath().toString());
		//dest2.delete();
		return true;
	}

	public static ArrayList<String> getCustomParameters() {
		// Get the parameters in form of string
		String params = currentInstance.launchArgs;

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

	public static DownloadResult download(String link, File folder) {
		Logger.a("Download started from: " + link);

		// Get a backup file that we will use to restore the file if the upload fails
		File backupfile = new File(BC.get() + "launcher" + File.separator + "backup.tmp");
		try {
			System.out.println(folder.toPath().toString());
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
			Logger.printException(ex);

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
		FileInputStream fis;
		byte[] buffer = new byte[1024];
		try {
			fis = new FileInputStream(filepath);
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry entry = zis.getNextEntry();
			while (entry != null) {
				String fileName = entry.getName();
				File newFile = new File(SRC + File.separator + fileName);

				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);
				int length;
				while ((length = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, length);
				}

				fos.close();
				zis.closeEntry();
				entry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
			fis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	public static void downloadUpdate(boolean release) {
		// Get the update name
		String update = getUpdate(release);
		try {
			boolean yes = false;

			// Format the message
			String update_name = update.startsWith("!") ? update.replace("!", "") : update; 
			String rr = Lang.UPDATE_FOUND.replaceAll("%s", update_name);

			// Ask if the user wants this update or not
			int result = JOptionPane.showConfirmDialog(null, rr, Lang.OPTIONS_UPDATE_HEADER, JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				Logger.a("The user wants to update to: " + update_name);
				yes = true;
			} else {
				Logger.a("The user doesn't want to update. The launcher stays at version: " + VERSION);
			}
			// If the user accepted the update, or it is a mandatory update, download it
			if (yes || update.startsWith("!")) {
				// Display downloading dialogue
				DownloadFrame dl = new DownloadFrame(update_name);

				String url = "https://betacraft.pl/launcher/launcher.jar";
				if (!release) url = "https://betacraft.pl/launcher/pre.jar";

				// Download the update
				dl.download(url, new File(BC.get(), "betacraft.jar$tmp"));

				// Launch the new version to finish updating
				final String pathToJar = Window.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				Runtime.getRuntime().exec("java -jar " + BC.get() + "betacraft.jar$tmp" + " update " + pathToJar);

				// Close this process
				Window.quit(true);
			}
		} catch (Exception ex) {
			Logger.a("An error has occurred while updating the launcher!");
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	public static void write(File file, String[] lines, boolean append) {
		try {
			// Create new file, if it doesn't already exist
			file.createNewFile();
		} catch (IOException e) {
			System.out.println(file.toPath().toString());
			e.printStackTrace();
			Logger.printException(e);
		}
		OutputStreamWriter writer = null;
		try {
			// Write in UTF-8
			writer = new OutputStreamWriter(
					new FileOutputStream(file, append), "UTF-8");
			for (int i = 0; i < lines.length; i++) {
				// Skip empty lines
				if (lines[i] != null) {
					writer.write(lines[i] + "\n");
				}
			}
		} catch (Exception ex) {
			Logger.a("A critical error occurred while attempting to write to file: " + file);
			ex.printStackTrace();
			Logger.printException(ex);
		} finally {
			// Close the file
			try {writer.close();} catch (Exception ex) {}
		}
	}

	public static boolean checkForUpdate(boolean release) {
		// Get the latest version
		String update = getUpdate(release);
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

	public static String getUpdate(boolean release) {
		try {
			String Url = "https://betacraft.pl/launcher/rel.txt";
			if (!release) Url = "https://betacraft.pl/launcher/pre.txt";
			URL url = new URL(Url);
			Scanner s = new Scanner(url.openStream(), "UTF-8");
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
			Logger.printException(ex);
		}
		return null;
	}

	public static String getNickname() {
		if (MojangLogging.userProfile != null)
			return MojangLogging.userProfile.get("selectedProfile.name");

		return MojangLogging.email;
	}

	public static void readLastLogin() {
        try {
            final File lastLogin = new File(BC.get(), "lastlogin");
            final Cipher cipher = getCipher(2, "bcpasswordfile");
            DataInputStream dis;
            if (cipher != null) {
                dis = new DataInputStream(new CipherInputStream(new FileInputStream(lastLogin), cipher));
            }
            else {
                dis = new DataInputStream(new FileInputStream(lastLogin));
            }
            MojangLogging.email = dis.readUTF();
            MojangLogging.password = dis.readUTF();
            dis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.printException(ex);
        }
    }

	public static void saveLastLogin() {
        try {
            final File lastLogin = new File(BC.get(), "lastlogin");
            final Cipher cipher = getCipher(1, "bcpasswordfile");
            DataOutputStream dos;
            if (cipher != null) {
                dos = new DataOutputStream(new CipherOutputStream(new FileOutputStream(lastLogin), cipher));
            }
            else {
                dos = new DataOutputStream(new FileOutputStream(lastLogin));
            }
            dos.writeUTF(MojangLogging.email);
            if (Launcher.getProperty(Launcher.SETTINGS, "remember-password").equals("true")) {
                dos.writeUTF(MojangLogging.password);
            } else {
            	dos.writeUTF("");
            }
            dos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.printException(ex);
        }
    }
    
    private static Cipher getCipher(final int mode, final String password) throws Exception {
        final Random random = new Random(37635689L);
        final byte[] salt = new byte[8];
        random.nextBytes(salt);
        final PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);
        final SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
        final Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        cipher.init(mode, pbeKey, pbeParamSpec);
        return cipher;
    }

	public static String getAuthToken(boolean fulltoken) {
		if (MojangLogging.userProfile != null) {
			if (fulltoken) return "token:" + MojangLogging.userProfile.get("accessToken") + ":" + MojangLogging.userProfile.get("selectedProfile.id");  
			return MojangLogging.userProfile.get("accessToken");
		}
		return "0";
	}

	public static String[] read(File file) {
		try {
			// Create new, if doesn't exist
			if (file.createNewFile()) {
				Logger.a("Created a new file: " + file);
			}
		} catch (IOException e) {
			System.out.println(file.toPath().toString());
			e.printStackTrace();
			Logger.printException(e);
		}
		InputStreamReader reader = null;
		try {
			// Read in UTF-8
			reader = new InputStreamReader(
					new FileInputStream(file), "UTF-8");
			StringBuilder inputB = new StringBuilder();
			char[] buffer = new char[1024];
			while (true) {
				int readcount = reader.read(buffer);
				if (readcount < 0) break;
				inputB.append(buffer, 0, readcount);
			}
			return inputB.toString().split("\n");
		} catch (Exception ex) {
			Logger.a("A critical error occurred while reading from file: " + file);
			ex.printStackTrace();
			Logger.printException(ex);
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
				value = lines[i].substring(property.length()+1, lines[i].length());
				break;
			}
		}
		return value;
	}

	public static String[] excludeExistant(File file, String[] properties) {
		String[] lines = read(file);
		for (int i = 0; i < lines.length; i++) {
			// If the array is empty, ignore it
			if (lines[i] == null) continue;

			for (int i1 = 0; i1 < properties.length; i1++) {
				// If the property matches, remove it from array
				if (lines[i].startsWith(properties[i1] + ":")) {
					properties[i1] = null;
				}
			}
		}
		return properties;
	}
}
