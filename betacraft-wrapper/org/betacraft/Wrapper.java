package org.betacraft;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.betacraft.launcher.Lang;
import org.betacraft.launcher.Launcher;
import org.betacraft.launcher.OS;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;

public class Wrapper extends Applet implements AppletStub {

	final static Map<String, String> params = new HashMap<String, String>(); // Custom parameters
	private static int session; // Session id to create natives folder for the game
	public static String mainFolder; // .betacraft folder
	public static String version; // Version to be launched
	private static URLClassLoader classLoader; // Class loader for linking the game and natives
	private static boolean discord = false; // Discord RPC

	private static Applet applet = null; // Game's applet
	private static int context = 0; // Return value for isActive
	private static boolean active = false; // If the game has started
	public static String ver_prefix = ""; // Version's official name, eg. Infdev, Alpha, etc.
	public static List<String> nonOnlineClassic = new ArrayList<String>(); // A list of Classic versions which cannot play online

	public static void main(String[] args) {
		System.out.println("Starting BetaCraftWrapper v" + Launcher.VERSION);
		try {
			if (args.length < 2) {
				JOptionPane.showMessageDialog(null, "Error code 1: Could not initialize wrapper (arguments too short)", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			// Our wrapper arguments are differenciated by colons
			String[] info = args[0].split(":");
			System.out.println("Wrapper arguments: " + args[0]);

			// Get a session number
			try {
				session = Integer.parseInt(info[1]);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Error code 2: Could not initialize wrapper (sessionid is not a valid number)", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}


			version = info[2];

			// Initialize game's main path (.betacraft)
			String path = "";
			for (int i = 1; i < args.length; i++) {
				path = path + " " + args[i];
			}
			if (path.equals("")) {
				JOptionPane.showMessageDialog(null, "Error code 3: Could not initialize wrapper (mainFolder argument is empty)", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			path = path.substring(1, path.length());
			mainFolder = path;

			// Add parameters for the client
			params.put("username", info[0]);
			params.put("sessionid", info[1]);
			params.put("haspaid", "true");

			// Fix crashing when teleporting with Java 8+
			System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

			// Turn on proxy if wanted
			if (info[3].equals("true")) {
				if (version.startsWith("c0.")) {
					// We gonna use Retrocraft for Classic for now :(
					System.setProperty("http.proxyHost", "classic.retrocraft.net");
				} else {
					System.setProperty("http.proxyHost", "betacraft.ovh");
				}
				System.setProperty("http.proxyPort", "80");
			}

			// Make a prefix for the version
			if (version.startsWith("b1.")) {
				ver_prefix = "Beta " + version.substring(1, version.length());
			} else if (version.startsWith("a1.")) {
				ver_prefix = "Alpha v" + version.substring(1, version.length());
			} else if (version.startsWith("inf-20100")) {
				ver_prefix = "Infdev " + version.substring(4, version.length());
			} else if (version.startsWith("in-20")) {
				ver_prefix = "Indev " + version.substring(3, version.length());
			} else if (version.startsWith("c0.")) {
				ver_prefix = "Classic " + version.substring(1, version.length());
			} else if (version.startsWith("mc-") || version.startsWith("rd-")) {
				ver_prefix = "Pre-Classic " + version.substring(3, version.length());
			} else {
				ver_prefix = version;
			}

			// Allow joining servers for Classic MP versions
			if (ver_prefix.startsWith("Classic") && !nonOnlineClassic.contains(version)) {
				String server = JOptionPane.showInputDialog(null, Lang.get("server"), "");
				String port = "25565";
				String IP = server;
				if (IP.contains(":")) {
					String[] params1 = server.split(":");
					IP = params1[0];
					port = params1[1];
				}
				if (!server.equals("")) {
					System.out.println("Accepted server parameters: " + server);
					params.put("server", IP);
					params.put("port", port);
				}
			}

			// Start the game
			new Wrapper().play();

			// Enable Discord RPC if wanted
			if (discord = Launcher.getProperty(Launcher.SETTINGS, "RPC").equalsIgnoreCase("true")) {
				DiscordRPC lib = DiscordRPC.INSTANCE;
				String applicationId = "567450523603566617";
				DiscordEventHandlers handlers = new DiscordEventHandlers();
				lib.Discord_Initialize(applicationId, handlers, true, "");
				DiscordRichPresence presence = new DiscordRichPresence();
				presence.startTimestamp = System.currentTimeMillis() / 1000;
				presence.state = Lang.get("version") + ": " + version;
				presence.details = "Nick: " + info[0];
				lib.Discord_UpdatePresence(presence);
				new DiscordThread(lib).start();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static class DiscordThread extends Thread {
		DiscordRPC rpc;

		DiscordThread(DiscordRPC lib) {
			super("RPC-Callback-Handler");
			rpc = lib;
		}

		// Update the RPC
		public void start() {
			while (!Thread.currentThread().isInterrupted()) {
				rpc.Discord_RunCallbacks();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ignored) {}
			}
		}
	}

	public static void applyNatives() {
		// Natives folder in mainFolder/versions/
		String natives = version + "-" + session;

		File lwjgl;
		File lwjgl64;
		File jinput;
		File jinput64;
		File openal;
		File openal64;

		File jinputdx8 = null;
		File jinputdx864 = null;

		// Assume the OS is Windows
		// If not, we'll correct that in a while
		String lwj = "lwjgl.dll";
		String lwj64 = "lwjgl64.dll";
		String jin = "jinput-raw.dll";
		String jin64 = "jinput-raw_64.dll";
		String ope = "OpenAL32.dll";
		String ope64 = "OpenAL64.dll";

		String jindx = null;
		String jindx64 = null;

		// Init file names for specific operating systems
		if (OS.isLinux() || OS.isSolaris()) {
			lwj = "liblwjgl.so";
			lwj64 = "liblwjgl64.so";
			jin = "libjinput-linux.so";
			jin64 = "libjinput-linux64.so";
			ope = "libopenal.so";
			ope64 = "libopenal64.so";
		} else if (OS.isWindows()) {
			jindx = "jinput-dx8.dll";
			jindx64 = "jinput-dx8_64.dll";
		} else {
			System.out.println("A critical error has occurred: Your operating system is not supported.");
			System.exit(0);
			return;
		}
		lwjgl = new File(mainFolder + "versions/" + natives, lwj);
		lwjgl64 = new File(mainFolder + "versions/" + natives, lwj64);
		jinput = new File(mainFolder + "versions/" + natives, jin);
		jinput64 = new File(mainFolder + "versions/" + natives, jin64);
		openal = new File(mainFolder + "versions/" + natives, ope);
		openal64 = new File(mainFolder + "versions/" + natives, ope64);
		if (jindx != null) {
			jinputdx8 = new File(mainFolder + "versions/" + natives, jindx);
			jinputdx864 = new File(mainFolder + "versions/" + natives, jindx64);
		}

		// Copy natives to session's folder
		try {
			Files.copy(new File(mainFolder + "bin/natives", lwj).toPath(), lwjgl.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(new File(mainFolder + "bin/natives", lwj64).toPath(), lwjgl64.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(new File(mainFolder + "bin/natives", jin).toPath(), jinput.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(new File(mainFolder + "bin/natives", jin64).toPath(), jinput64.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(new File(mainFolder + "bin/natives", ope).toPath(), openal.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(new File(mainFolder + "bin/natives", ope64).toPath(), openal64.toPath(), StandardCopyOption.REPLACE_EXISTING);
			if (jindx != null) {
				Files.copy(new File(mainFolder + "bin/natives", jindx).toPath(), jinputdx8.toPath(), StandardCopyOption.REPLACE_EXISTING);
				Files.copy(new File(mainFolder + "bin/natives", jindx64).toPath(), jinputdx864.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			System.out.println("An error has occurred: Could not copy natives (is there another instance of the game running?)");
			e.printStackTrace();
		}
	}

	public void play() {
		try {
			// Tell the website to update the skin for the proxy
			new Thread() {
				public void run() {
					try {
						new URL("https://betacraft.ovh/update_skin.php?name=" + params.get("username")).openStream().close();
					} catch (Exception ex) {
						System.out.println("Error: Could not connect to the website to update player's skin!");
					}
				}
			}.start();

			// Copy natives
			String nativesPath = mainFolder + "versions/" + version + "-" + session;
			final File natives = new File(nativesPath);
			natives.mkdirs();
			applyNatives();

			// Glue everything Minecraft needs for running
			String file = mainFolder + "versions/" + version + ".jar";
			String file1 = mainFolder + "bin/lwjgl.jar";
			String file2 = mainFolder + "bin/lwjgl_util.jar";
			String file3 = mainFolder + "bin/jinput.jar";

			System.setProperty("org.lwjgl.librarypath", nativesPath);
			System.setProperty("net.java.games.input.librarypath", nativesPath);

			final URL[] url = new URL[4];
			url[0] = new File(file).toURI().toURL();
			url[1] = new File(file1).toURI().toURL();
			url[2] = new File(file2).toURI().toURL();
			url[3] = new File(file3).toURI().toURL();

			classLoader = null;
			classLoader = new URLClassLoader(url);

			// Find game's applet or main class
			final Class appletClass;
			if (version.startsWith("c")) {
				appletClass = classLoader.loadClass("com.mojang.minecraft.MinecraftApplet");
			} else if (version.startsWith("rd")) {
				appletClass = classLoader.loadClass("com.mojang.rubydung.RubyDung");

				// This isn't an applet, we're going to run it like a Runnable
				Runnable run = (Runnable) appletClass.newInstance();
				run.run();
				return;
			} else if (version.startsWith("mc")) {
				appletClass = classLoader.loadClass("com.mojang.minecraft.RubyDung");

				// This isn't an applet, we're going to run it like a Runnable
				Runnable run = (Runnable) appletClass.newInstance();
				run.run();
				return;
			} else {
				// For the rest of the versions (Indev+)
				appletClass = classLoader.loadClass("net.minecraft.client.MinecraftApplet");
			}
			applet = (Applet) appletClass.newInstance();

			// Replace the main game folder to .betacraft
			if (version.startsWith("in") || version.startsWith("a1.") || version.startsWith("b1.")) {
				for (final Field field : appletClass.getDeclaredFields()) {
					final String name = field.getType().getName();
					if (!name.contains("awt") && !name.contains("java")) {
						Field fileField = null;
						final Class clazz = classLoader.loadClass(name);
						for (final Field field1 : clazz.getDeclaredFields()) {
							if (Modifier.isStatic(field1.getModifiers()) && field1.getType().getName().equals("java.io.File")) {
								fileField = field1;
							}
						}
						if (fileField != null) {
							fileField.setAccessible(true);
							fileField.set(null, new File(mainFolder));
							break;
						}
					}
				}
			}

			// Make a frame for the game
			final Frame gameFrame = new Frame();
			gameFrame.setLocationRelativeTo(null);
			gameFrame.setTitle("Minecraft " + ver_prefix);
			BufferedImage img = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("icons/favicon.png"));
			gameFrame.setIconImage(img);
			gameFrame.setBackground(Color.BLACK);

			// This is needed for the window size
			final JPanel panel = new JPanel();
			gameFrame.setLayout(new BorderLayout());
			if (version.equals("c0.0.12a-dev")) {
				panel.setPreferredSize(new Dimension(640, 480));
			} else {
				panel.setPreferredSize(new Dimension(854, 480));
			}
			gameFrame.add(panel, "Center");
			gameFrame.pack();
			gameFrame.setLocationRelativeTo(null);
			gameFrame.setVisible(true);

			applet.setStub(this);

			gameFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(final WindowEvent e) {
					stop();
					destroy();
					gameFrame.setVisible(false);
					gameFrame.dispose();
					try {
						classLoader.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					System.exit(0);
				}
			});

			// Add game's applet to this window
			this.setLayout(new BorderLayout());
			this.add(applet, "Center");
			this.validate();
			gameFrame.removeAll();
			gameFrame.setLayout(new BorderLayout());
			gameFrame.add(this, "Center");
			gameFrame.validate();

			// Start the client
			this.init();
			active = true;
			this.start();
		} catch (Exception ex) {
			System.out.println("A critical error has occurred!");
			ex.printStackTrace();
		}
	}

	@Override
	public void appletResize(int arg0, int arg1) {}

	@Override
	public void update(final Graphics g) {
		this.paint(g);
	}

	@Override
	public boolean isActive() {
		if (context == 0) {
			context = -1;
			try {
				if (this.getAppletContext() != null) {
					context = 1;
				}
			} catch (Exception ex) {}
		}
		if (context == -1) {
			return active;
		}
		return super.isActive();
	}

	@Override
	public void stop() {
		// Shutdown the RPC correctly
		if (discord) DiscordRPC.INSTANCE.Discord_Shutdown();
		if (applet != null) {
			active = false;
			applet.stop();
		}
	}

	@Override
	public void destroy() {
		if (applet != null) {
			applet.destroy();
		}
	}

	@Override
	public void start() {
		if (applet != null) {
			applet.start();
			return;
		}
	}

	@Override
	public void init() {
		if (applet != null) {
			applet.init();
			return;
		}
	}

	@Override
	public URL getDocumentBase() {
		try {
			return new URL("http://www.minecraft.net/game/");
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public URL getCodeBase() {
		try {
			return new URL("http://www.minecraft.net/game/");
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getParameter(final String paramName) {
		System.out.println("Client asked for a parameter: " + paramName);
		if (params.containsKey(paramName)) {
			return params.get(paramName);
		}
		return null;
	}

	static {
		nonOnlineClassic.add("c0.0.1a");
		nonOnlineClassic.add("c0.0.2a");
		nonOnlineClassic.add("c0.0.3a");
		nonOnlineClassic.add("c0.0.4a");
		nonOnlineClassic.add("c0.0.5a");
		nonOnlineClassic.add("c0.0.6a");
		nonOnlineClassic.add("c0.0.7a");
		nonOnlineClassic.add("c0.0.8a");
		nonOnlineClassic.add("c0.0.9a");
		nonOnlineClassic.add("c0.0.10a");
		nonOnlineClassic.add("c0.0.11a");
		nonOnlineClassic.add("c0.0.12a-dev");
		nonOnlineClassic.add("c0.0.12a");
		nonOnlineClassic.add("c0.0.12a_01");
		nonOnlineClassic.add("c0.0.12a_02");
		nonOnlineClassic.add("c0.0.12a_03");
		nonOnlineClassic.add("c0.0.13a-dev");
		nonOnlineClassic.add("c0.0.13a");
		nonOnlineClassic.add("c0.0.13a_01");
		nonOnlineClassic.add("c0.0.13a_02");
		nonOnlineClassic.add("c0.0.13a_03");
		nonOnlineClassic.add("c0.0.14a");
		nonOnlineClassic.add("c0.0.14a_01");
		nonOnlineClassic.add("c0.0.14a_02");
		nonOnlineClassic.add("c0.0.14a_03");
		nonOnlineClassic.add("c0.0.14a_04");
		nonOnlineClassic.add("c0.0.14a_06");
		nonOnlineClassic.add("c0.0.14a_07");
		nonOnlineClassic.add("c0.0.14a_08");
	}
}
