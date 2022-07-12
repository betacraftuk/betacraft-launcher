package org.betacraft.launcher;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.betacraft.Classic12aWrapper;
import org.betacraft.Classic15aWrapper;
import org.betacraft.FkWrapper;
import org.betacraft.PreClassicWrapper;
import org.betacraft.PreClassicWrapper2;
import org.betacraft.Wrapper;
import org.betacraft.WrapperDetector;
import org.betacraft.launcher.Release.VersionInfo;
import org.betacraft.launcher.Window.Tab;

import pl.betacraft.auth.Accounts;
import pl.betacraft.auth.Authenticator;
import pl.betacraft.auth.Credentials;
import pl.betacraft.auth.CustomRequest;
import pl.betacraft.auth.CustomResponse;
import pl.betacraft.auth.DownloadRequest;
import pl.betacraft.auth.DownloadResponse;
import pl.betacraft.auth.NoAuth;
import pl.betacraft.json.lib.LaunchMethods;
import pl.betacraft.json.lib.ModObject;
import pl.betacraft.json.lib.MouseFixMacOSJson;

/** Main class */
public class Launcher {
	public static String VERSION = "1.09_16-pre1"; // TODO Always update this

	public static Instance currentInstance;
	public static boolean forceUpdate = false;
	public static boolean disableWarnings = false;
	public static ArrayList<Thread> totalThreads = new ArrayList<Thread>();
	public static Authenticator auth;
	public static Accounts accounts = new Accounts();
	public static LaunchMethods launchMethods = new LaunchMethods();

	public static String JAVA_HOME = System.getProperty("java.home");
	public static File javaRuntime = new File(JAVA_HOME, "bin/java" + (OS.isWindows() ? ".exe" : ""));

	public static void main(String[] args) {

		String javaver = System.getProperty("java.runtime.version");
		String javadistro = System.getProperty("java.vendor");
		System.err.println("Java version: " + javadistro + ", " + System.getProperty("java.runtime.name") + ", " + javaver);
		System.err.println("System: " + OS.OS + ", " + OS.VER + ", " + OS.ARCH);
		long nano = System.nanoTime();
		boolean systemlookandfeel = Boolean.parseBoolean(System.getProperty("betacraft.systemLookAndFeel", "true"));

		if (systemlookandfeel) {
			try {
				// Fix for Java having a cross-platform look and feel
				if (OS.isWindows()) UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				else if (OS.isLinux()) UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			} catch (Exception ex) {
				// why
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception ex1) {
					ex1.printStackTrace();
					Logger.printException(ex1);
				}
			}
		}

		try {
			// Define a current path for the launcher
			String p = Window.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			if (OS.isWindows()) {
				p = p.substring(1, p.length());
			}
			BC.currentPath = new File(p);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Logger.clearLauncherLog();

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
				File dest = new File(pathToJar);
				Util.copy(BC.currentPath, dest);

				// Launch the updated launcher
				ArrayList<String> pa = new ArrayList<String>();
				pa.add("java");
				pa.add("-jar");
				pa.add(dest.getAbsolutePath());
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

		// Launch the game if wanted
		if (args.length > 0 && args[0].equals("wrap")) {
			BC.wrapped = true;
			BC.SETTINGS = new File(BC.get() + "launcher", "launcher.settings");
			Lang.refresh(false, false);
			String username = args[1];
			String sessionid = args[2];
			String server = args[3].equals("-") ? null : args[3];
			String mppass = args[4].equals("-") ? "0" : args[4];
			String uuid = args[5];

			// Convert arguments to work with Wrapper
			StringBuilder split = new StringBuilder();
			for (int i = 6; i < args.length; i++) {
				split.append(args[i] + " ");
			}
			String instanceName = split.toString();
			instanceName = instanceName.substring(0, instanceName.length() - 1);

			currentInstance = Instance.loadInstance(instanceName);
			ReleaseJson json = new ReleaseJson(currentInstance.version);
			String meth = json.getLaunchMethod();

			// Get addons as classes
			ArrayList<Class<Addon>> addons = new ArrayList<Class<Addon>>();
			if (!currentInstance.addons.isEmpty()) {
				try {
					System.out.println("Loading addons...");
					for (String s : currentInstance.addons) {
						try {
							String path = BC.get() + "launcher" + File.separator + "addons" + File.separator + s + ".jar";
							URLClassLoader loader = new URLClassLoader(new URL[] {
									new File(path).toURI().toURL()
							});

							System.out.println("- " + s);
							loadClasses(path, loader);
							Class<Addon> c = (Class<Addon>) loader.loadClass(s);
							addons.add(c);
						} catch (Exception ex) {
							System.err.println("An error occurred while loading an addon: " + s);
							ex.printStackTrace();
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			if (meth == null || meth.equalsIgnoreCase("")) { // assume the version
				meth = WrapperDetector.getLaunchMethod(BC.get() + "versions/" + currentInstance.version + ".jar");
				if (meth.equals("custom")) {
					String err = "Error code 7 (ERRCONFIG): Couldn't determine the launch method for your JAR. Configure your version configuration file: " + BC.get() + "versions/jsons/" + currentInstance.version + ".info";
					System.err.println(err);
					JOptionPane.showMessageDialog(Window.mainWindow, err, "Error", JOptionPane.INFORMATION_MESSAGE);
					System.exit(0);
				}
			}

			System.out.println("Loaded in: " + (System.nanoTime() - nano) + " ns");
			if (meth.equalsIgnoreCase("rd") || meth.equalsIgnoreCase("mc")) {
				new Launcher().extractFromJar("/PreClassic.jar", new File(BC.get() + "launcher/", "PreClassic.jar"));
				new PreClassicWrapper2(username, currentInstance.name, currentInstance.version, sessionid, currentInstance.gameDir, currentInstance.height, currentInstance.width, currentInstance.RPC, meth, server, mppass, Lang.WRAP_USER, Lang.WRAP_VERSION, currentInstance.getIcon(), addons);
			} else if (meth.equalsIgnoreCase("preclassic")) {
				new PreClassicWrapper(username, currentInstance.name, currentInstance.version, sessionid, currentInstance.gameDir, currentInstance.height, currentInstance.width, currentInstance.RPC, meth, server, mppass, uuid, Lang.WRAP_USER, Lang.WRAP_VERSION, currentInstance.getIcon(), addons);
			} else if (meth.equalsIgnoreCase("classic12a")) {
				new Classic12aWrapper(username, currentInstance.name, currentInstance.version, sessionid, currentInstance.gameDir, currentInstance.height, currentInstance.width, currentInstance.RPC, meth, server, mppass, uuid, Lang.WRAP_USER, Lang.WRAP_VERSION, currentInstance.getIcon(), addons);
			} else if (meth.equalsIgnoreCase("classic15a")) {
				new Classic15aWrapper(username, currentInstance.name, currentInstance.version, sessionid, currentInstance.gameDir, currentInstance.height, currentInstance.width, currentInstance.RPC, meth, server, mppass, uuid, Lang.WRAP_USER, Lang.WRAP_VERSION, currentInstance.getIcon(), addons);
			} else if (meth.equalsIgnoreCase("indev") || meth.equalsIgnoreCase("classicmp") || meth.equalsIgnoreCase("classic")) {
				new Wrapper(username, currentInstance.name, currentInstance.version, sessionid, currentInstance.gameDir, currentInstance.height, currentInstance.width, currentInstance.RPC, meth, server, mppass, uuid, Lang.WRAP_USER, Lang.WRAP_VERSION, currentInstance.getIcon(), addons);
			} else if (meth.equalsIgnoreCase("4k")) {
				new FkWrapper(username, currentInstance.name, currentInstance.version, sessionid, currentInstance.gameDir, currentInstance.height, currentInstance.width, currentInstance.RPC, meth, server, mppass, Lang.WRAP_USER, Lang.WRAP_VERSION, currentInstance.getIcon(), addons);
			} else {
				try {
					String path = BC.get() + "launcher" + File.separator + "launch-methods" + File.separator + meth + ".jar";
					URLClassLoader loader = new URLClassLoader(new URL[] {
							new File(path).toURI().toURL()
					});

					loadClasses(path, loader);
					System.out.println("Launch method: " + meth);
					Class c = loader.loadClass(meth);
					Constructor con = c.getConstructor(String.class, String.class, String.class, String.class, String.class, Integer.class, Integer.class, Boolean.class, String.class, String.class, String.class, String.class, String.class, String.class, Image.class, ArrayList.class);
					con.newInstance(username, currentInstance.name, currentInstance.version, sessionid, currentInstance.gameDir, currentInstance.height, currentInstance.width, currentInstance.RPC, json.getLaunchMethod(), server, mppass, uuid, Lang.WRAP_USER, Lang.WRAP_VERSION, currentInstance.getIcon(), addons);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return;
		}

		BC.SETTINGS = new File(BC.get() + "launcher", "launcher.settings");

		if (BC.SETTINGS.exists() && !"1".equals(Util.getProperty(BC.SETTINGS, "version"))) {
			removeRecursively(new File(BC.get() + "launcher"), true, false);
			writeDefault();
		}

		// Create required directories
		new File(BC.get() + "versions" + File.separator + "jsons").mkdirs();
		new File(BC.get() + "launcher" + File.separator + "lang").mkdirs();
		new File(BC.get() + "launcher" + File.separator + "addons").mkdirs();
		new File(BC.get() + "launcher" + File.separator + "instances").mkdirs();
		new File(BC.get() + "launcher" + File.separator + "launch-methods").mkdirs();
		new File(BC.get() + "bin" + File.separator + "natives").mkdirs();

		Logger.a("BetaCraft Launcher JE v" + VERSION + " loading...");
		Logger.a("Java version: " + System.getProperty("java.vendor") + ", " + System.getProperty("java.runtime.name") + ", " + System.getProperty("java.runtime.version"));
		Logger.a("Portable: " + BC.portable);
		Logger.a("EXE: " + BC.currentPath.getAbsolutePath().endsWith(".exe"));
		Logger.a("Prerelease: " + BC.prerelease);
		Logger.a("Nightly: " + BC.nightly);

		// Load language pack
		Lang.refresh(false, false);
		Util.readAccounts();

		// If the properties file doesn't exist, create it
		if (!BC.SETTINGS.exists() || Util.getProperty(BC.SETTINGS, "lastInstance").equals("")) {
			writeDefault();
			currentInstance = Instance.newInstance(Util.getProperty(BC.SETTINGS, "lastInstance"));
			currentInstance.saveInstance();
		} else {
			currentInstance = Instance.loadInstance(Util.getProperty(BC.SETTINGS, "lastInstance"));
			if (currentInstance == null) {
				currentInstance = Instance.newInstance(Util.getProperty(BC.SETTINGS, "lastInstance"));
				currentInstance.saveInstance();
			}
		}

		disableWarnings = "true".equals(Util.getProperty(BC.SETTINGS, "disableWarnings"));

		try {
			// initialize GUI
			StartThread t = new StartThread();
			totalThreads.add(t);
			t.start();
		} catch (Exception ex) {
			Logger.a("A critical error has occurred while trying to initialize the launcher!");
			ex.printStackTrace();
			Logger.printException(ex);
		}

		Logger.a("Loaded in: " + (System.nanoTime() - nano) + " ns");
	}

	public void extractFromJar(String filepath, File to) {
		Logger.a("Extracting \"" + filepath + "\" to \"" + to.getAbsolutePath() + "\"");
		Util.copy(getClass().getResourceAsStream(filepath), to);
	}

	public static void loadClasses(String pathtojar, URLClassLoader loader) {
		try {
			JarFile jarFile = new JarFile(pathtojar);
			Enumeration<JarEntry> e = jarFile.entries();

			// Load all classes from the addon jar
			while (e.hasMoreElements()) {
				JarEntry entry = e.nextElement();
				if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
					continue;
				}
				String className = entry.getName().substring(0, entry.getName().length() -6);
				className = className.replaceAll("/", ".");
				try {
					loader.loadClass(className);
				} catch (NoClassDefFoundError ex) {
					Logger.a("Couldn't find class " + className + ". Skipping!");
					ex.printStackTrace();
					Logger.printException(ex);
				}
			}
			jarFile.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void restart(String javapath) {
		try {
			ArrayList<String> params = new ArrayList<String>();
			params.add(javapath);
			params.add("-jar");
			params.add(BC.currentPath.getAbsolutePath());
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
		Util.setProperty(BC.SETTINGS, "lastInstance", Launcher.currentInstance.name);
	}
	
	public static void removeInstance(String instance) {
		Instance i = Instance.loadInstance((String) instance);

		if (i != null) {
			int res = JOptionPane.showConfirmDialog(null, Lang.INSTANCE_REMOVE_DIRECTORY + "\n" + i.gameDir);
			if (res == JOptionPane.YES_OPTION) {
				removeRecursively(new File(i.gameDir), true, false);
			}

			i.removeInstance();
		}

		if (Instance.getInstances().size() > 0) {
			Launcher.setInstance(Instance.loadInstance(Instance.getInstances().get(0)));
		} else {
			Instance in = Instance.newInstance("default instance");
			in.saveInstance();
			Launcher.setInstance(in);
		}
	}

	public static void initStartup() {
		File wrapper = new File(BC.get() + "launcher", "betacraft_wrapper.jar");
		if (BC.currentPath.length() != wrapper.length()) {
			try {
				Util.copy(BC.currentPath, wrapper);
			} catch (Exception ex) {
				ex.printStackTrace();
				Logger.printException(ex);
				JOptionPane.showMessageDialog(Window.mainWindow, "The file could not be copied! Try running with Administrator rights. If that won't help, contact: @Moresteck#1688", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		// Download Discord RPC if the checkbox is selected
		if (Launcher.currentInstance.RPC) {
			File rpc = new File(BC.get() + "launcher/", "discord_rpc.jar");
			if (rpc.exists()) {
				try {
					String sha1 = Util.getSHA1(rpc);
					String expected_hash = new CustomRequest("http://files.betacraft.uk/launcher/assets/discord_rpc.sha1").perform().response.replace("\n", "");
					if (!sha1.equals(expected_hash)) {
						Launcher.downloadWithButtonOutput("http://files.betacraft.uk/launcher/assets/discord_rpc.jar", rpc);
					}
				} catch (Throwable t) {}
			}
			if (!rpc.exists() || Launcher.forceUpdate) {
				Launcher.downloadWithButtonOutput("http://files.betacraft.uk/launcher/assets/discord_rpc.jar", rpc);
			}
		}

		Release rel = Release.getReleaseByName(Launcher.currentInstance.version);
		if (!rel.getInfo().isCustom()) {
			rel.getInfo().downloadJson();
		} else {
			// auto update mods !!
			ModObject mo = ModsRepository.getMod(rel.getInfo().getVersion());
			if (mo != null) {
				if (mo.checkUpdate || !rel.getInfo().getInfoFile().exists()) {
					DownloadResult res = download(mo.infoFileURL, rel.getInfo().getInfoFile());
					if (!res.isPositive()) {
						Logger.a("Failed to refresh mod: " + rel.getInfo().getVersion());
					}
				}
			}
		}

		ReleaseJson info = new ReleaseJson(rel.getName());
		rel.setInfo(info);

		// Download the game if not done already
		if (!Launcher.isVersionReady(info) || Launcher.forceUpdate) {
			if (!info.getDownloadURL().equals("") && !Launcher.downloadWithButtonOutput(info.getDownloadURL(), new File(Launcher.getVerFolder(), info.getVersion() + ".jar")).isPositive()) {
				JOptionPane.showMessageDialog(Window.mainWindow, Lang.ERR_NO_CONNECTION, Lang.ERR_DL_FAIL, JOptionPane.ERROR_MESSAGE);
			}
		}
		if (!Launcher.isLaunchMethodReady(Launcher.currentInstance.version) || Launcher.forceUpdate) {
			Launcher.downloadLaunchMethod(Launcher.currentInstance.version);
		}

		Launcher.readyAddons(Launcher.currentInstance, Launcher.forceUpdate);

		if (OS.isMac()) {
			if ("true".equalsIgnoreCase(info.getEntry("macos-mousefix"))) {
				String json = new CustomRequest("http://files.betacraft.uk/launcher/assets/macos-mousefix.json").perform().response;
				if (json != null) {
					MouseFixMacOSJson mousefix_json = Util.gsonPretty.fromJson(json, MouseFixMacOSJson.class);
					Util.installMacOSFix(mousefix_json, Launcher.forceUpdate);
				}
			}
		}

		// Download the latest libs and natives
		if (!Launcher.checkDepends() || Launcher.forceUpdate) {
			if (!Launcher.downloadDepends()) {
				JOptionPane.showMessageDialog(Window.mainWindow, Lang.ERR_NO_CONNECTION, Lang.ERR_DL_FAIL, JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void launchGame(Instance instance) {
		launchGame(instance, "-", "-");
	}

	public static void launchGame(Instance instance, String server, String mppass) {
		try {
			// Rarely there will be no chosen version, so we need to check that
			if (instance.version != null) {
				if (getNickname().equals("")) {
					JOptionPane.showMessageDialog(Window.mainWindow, Lang.WINDOW_USERNAME_FIELD_EMPTY, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				ArrayList<String> params = new ArrayList<String>();

				params.add(instance.javaPath);

				// The colon in the launch arguments is different for Windows
				String colon = ":";
				if (OS.isWindows()) {
					colon = ";";
					params.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
				}

				// Additional parameters:
				// - Discord RPC
				String add = "";
				if (instance.RPC) {
					// Add DRPC to the launch arguments
					add = colon + BC.get() + "launcher" + File.separator + "discord_rpc.jar";
				}

				// Let the user overwrite this argument - put it before the custom ones
				params.add("-Djava.util.Arrays.useLegacyMergeSort=true");

				VersionInfo info = Release.getReleaseByName(instance.version).getInfo();

				// Required to fix the following on MacOS: Classic, Indev after 0129-2, Infdev and Alpha to 1.0.1
				if (OS.isMac() && "true".equalsIgnoreCase(info.getEntry("macos-mousefix"))) {
					params.add("-javaagent:" + BC.get() + "launcher/macos-javaagent.jar=" + BC.get());
				}

				if (OS.isLinux() && "true".equalsIgnoreCase(info.getEntry("linux-mousefix-earlyclassic"))) {
					params.add("-Dbetacraft.linux_mousefix_earlyclassic=true");
				}

				// Classic and early Indev
				if ("true".equalsIgnoreCase(info.getEntry("resize-applet"))) {
					params.add("-Dbetacraft.resize_applet=true");
				}

				if ("true".equalsIgnoreCase(info.getEntry("do-not-get-mppass"))) {
					params.add("-Dbetacraft.obtainMPpass=false");
				}

				if (info.getProtocol() != null && "classicmp".equals(info.getLaunchMethod())) {
					params.add("-Dbetacraft.ask_for_server=true");
				}

				if (OS.isMac()) {
					params.add("-Xdock:name=" + instance.name);
					params.add("-Xdock:icon=" + instance.getIconLocation());
				}

				params.add("-Dhttp.nonProxyHosts=api.betacraft.uk|files.betacraft.uk");

				// Add custom parameters from options
				if (instance.launchArgs != null && !instance.launchArgs.equals("")) {
					params.addAll(getCustomParameters());
				}

				if (instance.proxy) {
					String[] args = info.getProxyArgs().split(" ");
					for (String s : args) {
						if (s.equals("")) continue;
						params.add(s);
					}
				}
				String token = getAuthToken();

				// Add the rest of params and launch the wrapper
				//params.add("-Duser.home=" + instance.gameDir);
				params.add("-cp");
				params.add(BC.get() + "launcher" + File.separator + "betacraft_wrapper.jar" + add);
				params.add("org.betacraft.launcher.Launcher");
				params.add("wrap");
				params.add(getNickname());
				params.add(token);
				params.add(server);
				params.add(mppass);
				if (!(Launcher.auth instanceof NoAuth)) {
					params.add(Launcher.auth.getCredentials().local_uuid);
				} else {
					params.add("-");
				}
				params.add(currentInstance.name);
				System.out.println(params.toString());
				Logger.a(!token.equals("-") ? params.toString().replaceAll(token, "[censored sessionid]") : params.toString());

				ProcessBuilder builder = new ProcessBuilder(params);
				builder.redirectErrorStream(true);
				new File(instance.gameDir).mkdirs();
				//builder.environment().put("APPDATA", instance.gameDir);
				builder.directory(new File(instance.gameDir));

				// Close the launcher if desired
				if (!instance.keepopen) {
					Window.quit(false);
				}
				
				// Console frame
				ConsoleLogFrame clf = new ConsoleLogFrame(instance.name, instance.console);

				// Clear previous logs
				Logger.clearClientLog();

				// For debugging
				Process process = builder.start();
				InputStream output = process.getInputStream();
				InputStreamReader isr_log = new InputStreamReader(output);
				BufferedReader br_log = new BufferedReader(isr_log);
				String line1;
				while ((line1 = br_log.readLine()) != null) {
					if (!token.equals("-")) line1 = line1.replaceAll(token, "[censored sessionid]");

					Logger.logClient(line1);
					clf.log(line1 + "\n");
				}
				Logger.logClient("End of client input");

				clf.log("\nClient closed.\n");
				Logger.a("Client closed.");

				if (!instance.keepopen) {
					Window.quit(!clf.isVisible());
				}
				return;
			}
		} catch (Exception ex) {
			Logger.a("A critical error has occurred while attempting to launch the game!");
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	public static void writeDefault() {
		Util.setProperty(BC.SETTINGS, "language", "English");
		Util.setProperty(BC.SETTINGS, "lastInstance", "default instance");
		Util.setProperty(BC.SETTINGS, "tab", Tab.CHANGELOG.name());
		Util.setProperty(BC.SETTINGS, "disableWarnings", "false");
		Util.setProperty(BC.SETTINGS, "version", "1");
	}

	public static void removeRecursively(File folder, boolean deleteFolderItself, boolean deleteOnlyFiles) {
		String[] entries = folder.list();
		for (String s: entries) {
			File currentFile = new File(folder.getPath(), s);
			if (currentFile.isDirectory() && !deleteOnlyFiles) {
				for (String s1 : currentFile.list()) {
					// Delete files inside this folder
					new File(currentFile.getPath(), s1).delete();
				}
				try {
					currentFile.delete();
				} catch (Exception ex) {}
			} else {
				currentFile.delete();
			}
		}
		if (deleteFolderItself) folder.delete();
	}

	public static File getVerFolder() {
		return new File(BC.get() + "versions" + File.separator);
	}

	public static void downloadLaunchMethod(String version) {
		VersionInfo json = Release.getReleaseByName(version).getInfo();
		if (!json.getLaunchMethodURL().equals("")) {
			if (!downloadWithButtonOutput(json.getLaunchMethodURL(), new File(BC.get() + "launcher" + File.separator + "launch-methods", json.getLaunchMethod() + ".jar")).isPositive()) {
				JOptionPane.showMessageDialog(Window.mainWindow, "Couldn't download the launch method for this version.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public static boolean isLaunchMethodReady(String version) {
		VersionInfo json = Release.getReleaseByName(version).getInfo();
		if (json.getLaunchMethodURL() != null) {
			String name = json.getLaunchMethod();

			File file = new File(BC.get() + "launcher" + File.separator + "launch-methods", name + ".jar");
			if (!file.exists()) {
				return false;
			}

			if (Launcher.launchMethods.nameToHash.containsKey(name)) {
				String hash = Launcher.launchMethods.nameToHash.get(name);
				if (!Util.getSHA1(file).equalsIgnoreCase(hash)) {
					return false;
				}
			}
		}
		return true;
	}

	public static void readyAddons(Instance instance, boolean force) {
		for (String s : instance.addons) {
			boolean download = false;

			Addon a = Addon.addons.get(s);
			File destination = new File(BC.get() + "launcher" + File.separator + "addons", s + ".jar");

			if (!destination.exists()) {
				download = true;
			} else if (a.online) {
				String filehash = Util.getSHA1(destination);
				if (!filehash.equalsIgnoreCase(a.onlinehash)) {
					download = true;
				}
			}

			if (download && !downloadWithButtonOutput("http://files.betacraft.uk/launcher/assets/addons/" + Addon.addonVer + "/" + s + ".jar", destination).isPositive()) {
				JOptionPane.showMessageDialog(Window.mainWindow, "Couldn't download addon: " + s, "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public static boolean isVersionReady(ReleaseJson version) {
		if (version.sha1 == null) {
			return version.hasJar();
		} else if (version.hasJar()) {
			String file_sha1 = Util.getSHA1(version.getJar());
			if (file_sha1.equalsIgnoreCase(version.sha1)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
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
			URL url = new URL("http://files.betacraft.uk/launcher/assets/depends-version.txt");
			Scanner s = new Scanner(url.openStream(), "UTF-8");
			String libs = s.nextLine().split(":")[1];
			String natives = s.nextLine().split(":")[1];
			s.close();
			boolean lastLibsMatch = libs.equals(Util.getProperty(BC.SETTINGS, "libs-version"));
			boolean lastNativesMatch = natives.equals(Util.getProperty(BC.SETTINGS, "natives-version"));

			if (!lastLibsMatch || !lastNativesMatch) {
				return false;
			}
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			Logger.printException(t);

			// Let's assume it's all good...
			return true;
		}
	}

	public static boolean downloadDepends() {
		File destLibs = new File(BC.get() + "bin" + File.separator);
		File destNatives = new File(BC.get() + "bin" + File.separator + "natives" + File.separator);

		String link1 = "http://files.betacraft.uk/launcher/assets/natives-windows.zip";
		String link2 = "http://files.betacraft.uk/launcher/assets/libs-windows.zip";
		if (OS.isLinux()) {
			link2 = "http://files.betacraft.uk/launcher/assets/libs-linux.zip";
			link1 = "http://files.betacraft.uk/launcher/assets/natives-linux.zip";
		}
		if (OS.isMac()) {
			link2 = "http://files.betacraft.uk/launcher/assets/libs-osx.zip";
			link1 = "http://files.betacraft.uk/launcher/assets/natives-osx.zip";
		}

		File dest1 = new File(BC.get() + "launcher/", "natives.zip");
		if (!downloadWithButtonOutput(link1, dest1).isPositive()) {
			return false;
		}

		File dest2 = new File(BC.get() + "launcher/", "libs.zip");
		if (!downloadWithButtonOutput(link2, dest2).isPositive()) {
			return false;
		}

		// Update the local memory with depends' version
		CustomResponse res = new CustomRequest("http://files.betacraft.uk/launcher/assets/depends-version.txt").perform();
		Scanner s = new Scanner(res.response);
		String libs = s.nextLine().split(":")[1];
		String natives = s.nextLine().split(":")[1];
		s.close();
		Util.setProperty(BC.SETTINGS, "libs-version", libs);
		Util.setProperty(BC.SETTINGS, "natives-version", natives);

		// If everything went ok, delete the 'bin' folder contents
		removeRecursively(destNatives, true, false);
		removeRecursively(destLibs, false, false);
		destNatives.mkdirs();

		// Lastly, schedule zips for extraction
		totalThreads.add(Util.unzip(dest1, destNatives, true));
		totalThreads.add(Util.unzip(dest2, destLibs, true));
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

	public static DownloadResult downloadWithButtonOutput(String link, final File folder) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Window.setStatus(Window.playButton, "Downloading: " + BC.trimBetaCraftDir(folder.getAbsolutePath()));
			}
		});
		return download(link, folder);
	}

	public static DownloadResult download(String link, File folder) {
		Logger.a("Download started from: " + link);

		DownloadResponse response = new DownloadRequest(link, folder.getAbsolutePath(), null, true).perform();
		return response.result;
	}

	public static void downloadUpdate(boolean release) {
		// Get the update name
		String update = getUpdate(release);
		try {
			boolean yes = false;

			// Format the message
			String update_name = update.startsWith("!") ? update.substring(1) : update; 
			String rr = Lang.UPDATE_FOUND.replaceAll("%s", update_name);

			if (!update.startsWith("!")) {
				// Ask if the user wants this update or not
				int result = JOptionPane.showConfirmDialog(Window.mainWindow, rr, Lang.OPTIONS_UPDATE_HEADER, JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					Logger.a("The user wants to update to: " + update_name);
					yes = true;
				} else {
					Logger.a("The user doesn't want to update. The launcher stays at version: " + VERSION);
				}
			} else {
				Logger.a("Forced update to: " + update_name);
				yes = true;
			}
			// If the user accepted the update, or it is a mandatory update, download it
			if (yes) {
				String ending = ".jar";
				if (BC.currentPath.getAbsolutePath().endsWith(".exe")) {
					ending = ".exe";
				}
				if (BC.portable) {
					ending = "-portable" + ending;
				}

				String url = "http://files.betacraft.uk/launcher/launcher-" + update_name + ending;
				if (!release) url = "http://files.betacraft.uk/launcher/launcher-" + update_name + ending;

				// Download the update
				download(url, new File(BC.get(), "betacraft.jar$tmp"));

				// Launch the new version to finish updating
				String[] args = new String[] {"java", "-jar", BC.get() + "betacraft.jar$tmp", "update", BC.currentPath.getAbsolutePath()};
				Runtime.getRuntime().exec(args);

				// Close this process
				Window.quit(true);
			}
		} catch (Exception ex) {
			Logger.a("An error has occurred while updating the launcher!");
			ex.printStackTrace();
			Logger.printException(ex);
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
			String Url = "http://files.betacraft.uk/launcher/rel.txt";
			if (!release) Url = "http://files.betacraft.uk/launcher/pre.txt";
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
		Credentials c = auth.getCredentials();
		if (c != null) {
			String name = c.username;
			if (name != null && name.length() > 0) {
				return name;
			}
		}
		return "";
	}

	public static String getAuthToken() {
		Credentials c = auth.getCredentials();
		if (c != null) {
			String token = c.access_token;
			if (token != null && token.length() > 0) {
				return token;
			}
		}
		return "-";
	}
}
