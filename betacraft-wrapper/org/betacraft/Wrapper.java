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
import java.lang.Thread.State;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.betacraft.launcher.Lang;
import org.betacraft.launcher.Launcher;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;

public class Wrapper extends Applet implements AppletStub {

	final static Map<String, String> params = new HashMap<String, String>(); // Custom parameters
	public static String session; // Session id for premium authentication
	public static String mainFolder; // .betacraft folder
	public static String version; // Version to be launched
	private static URLClassLoader classLoader; // Class loader for linking the game and natives
	public static Class appletClass; // Minecraft's main class
	private static boolean discord = false; // Discord RPC
	public static String[] arguments;

	private static Applet applet = null; // Game's applet
	private static int context = 0; // Return value for isActive
	private static boolean active = false; // If the game has started
	public static String ver_prefix = ""; // Version's official name, eg. Infdev, Alpha, etc.
	public static List<String> nonOnlineClassic = new ArrayList<String>(); // A list of Classic versions which cannot play online
	public static List<String> onlineInfdev = new ArrayList<String>(); // A list of online Infdev versions (without world saving)
	public static DiscordThread discordThread = null;

	public static int width = 854;
	public static int height = 480;

	public static String proxyCompat = "www.minecraft.net";
	public static int portCompat = 80;

	public static boolean isIndevPlus = false;

	public static void main(String[] args) {
		System.out.println("Starting BetaCraftWrapper v" + Launcher.VERSION);
		try {
			if (args.length < 2) {
				JOptionPane.showMessageDialog(null, "Error code 1: Could not initialize wrapper (arguments too short)", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			// Our wrapper arguments are differenciated by colons
			arguments = args[0].split(":");
			System.out.println("Wrapper arguments: " + args[0]);

			// Get mppass and version
			session = arguments[1];
			version = arguments[2];

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
			params.put("username", arguments[0]);
			params.put("sessionid", arguments[1]);
			params.put("haspaid", "true");
			params.put("stand-alone", "true");

			// Fix crashing when teleporting with Java 8+
			System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

			String sysProxy = System.getProperty("http.proxyHost");
			String sysPort = System.getProperty("http.proxyPort");

			// Turn on proxy if wanted
			if (arguments[3].equals("true") && sysProxy == null) {
				System.setProperty("http.proxyHost", "betacraft.pl");
			}
			if (arguments[3].equals("true") && sysPort == null) {
				System.setProperty("http.proxyPort", "80");
			}

			if (sysProxy != null)
				proxyCompat = sysProxy;
			if (sysPort != null) {
				try {
					portCompat = Integer.parseInt(sysPort);
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(null, "Error code 7: Proxy port is not a valid number!", "Error", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
					return;
				}
			}

			try {
				if (!Launcher.getProperty(Launcher.SETTINGS, "dimensions1").equalsIgnoreCase("")) {
					width = Integer.parseInt(Launcher.getProperty(Launcher.SETTINGS, "dimensions1"));
				}
				if (!Launcher.getProperty(Launcher.SETTINGS, "dimensions2").equalsIgnoreCase("")) {
					height = Integer.parseInt(Launcher.getProperty(Launcher.SETTINGS, "dimensions2"));
				}
				if (width <= 100 || height <= 100) {
					JOptionPane.showMessageDialog(null, "Error code 5: Window dimensions are too small!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(null, "Error code 4: Window dimensions are illegal!", "Error", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
				return;
			}

			// Initialize Discord RPC if wanted
			if (discord = Launcher.getProperty(Launcher.SETTINGS, "RPC").equalsIgnoreCase("true")) {
				DiscordRPC lib = DiscordRPC.INSTANCE;
				String applicationId = "567450523603566617";
				DiscordEventHandlers handlers = new DiscordEventHandlers();
				lib.Discord_Initialize(applicationId, handlers, true, "");
				DiscordRichPresence presence = new DiscordRichPresence();
				presence.startTimestamp = System.currentTimeMillis() / 1000;
				presence.state = Lang.get("version") + ": " + version;
				presence.details = Lang.get("nick") + ": " + arguments[0];
				lib.Discord_UpdatePresence(presence);
				discordThread = new DiscordThread(lib);
			}

			// Start the game
			new Wrapper().play();
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

	public void askForServer() {
		if (ver_prefix.startsWith("Classic") && !nonOnlineClassic.contains(version)) {
			if (arguments.length >= 5) {
				String[] ipstuff = arguments[4].split("/");
				params.put("server", ipstuff[0]);
				params.put("port", ipstuff[1]);
				params.put("mppass", session);
			} else {
				String server = JOptionPane.showInputDialog(null, Lang.get("server"), Launcher.getProperty(Launcher.SETTINGS, "server"));
				String port = "25565";
				if (server != null) {
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
						params.put("mppass", "0");
					}
				}
			}
		}
	}

	public void setPrefixAndLoadMainClass(URL[] url) {
		classLoader = null;
		classLoader = new URLClassLoader(url);

		// I know this looks terrible, but it works!
		try {
			// Classic
			appletClass = classLoader.loadClass("com.mojang.minecraft.MinecraftApplet");
			ver_prefix = "Classic " + version.substring(1, version.length());
		} catch (ClassNotFoundException ex) {
			try {
				appletClass = classLoader.loadClass("com.mojang.minecraft.MinecraftApplet");
			} catch (ClassNotFoundException ex1) {
				try {
					// rd
					appletClass = classLoader.loadClass("com.mojang.rubydung.RubyDung");
					ver_prefix = "Pre-Classic " + version;

					// This is a special case where we need to avoid endless running in the background
					// and false reporting of Discord RPC.
					Thread t = new Thread((Runnable) appletClass.newInstance());
					t.start();
					while (t.getState() == State.RUNNABLE || t.getState() == State.NEW) {
						if (discordThread != null) discordThread.rpc.Discord_RunCallbacks();
						Thread.sleep(2000);
					}
					this.stop();

					return;
				} catch (ClassNotFoundException ex2) {
					try {
						appletClass = classLoader.loadClass("com.mojang.minecraft.RubyDung");

						// This is a special case where we need to avoid endless running in the background
						// and false reporting of Discord RPC.
						Thread t = new Thread((Runnable) appletClass.newInstance());
						t.start();
						while (t.getState() == State.RUNNABLE || t.getState() == State.NEW) {
							if (discordThread != null) discordThread.rpc.Discord_RunCallbacks();
							Thread.sleep(2000);
						}
						this.stop();

						return;
					} catch (ClassNotFoundException ex3) {
						try {
							// Indev+
							appletClass = classLoader.loadClass("net.minecraft.client.MinecraftApplet");
							isIndevPlus = true;
							if (version.startsWith("in-")) {
								ver_prefix = "Indev " + version.substring(3, version.length());
							} else if (version.startsWith("b")) {
								ver_prefix = "Beta " + version.substring(1, version.length());
							} else if (version.startsWith("a")) {
								ver_prefix = "Alpha v" + version.substring(1, version.length());
							} else if (version.startsWith("inf-")) {
								ver_prefix = "Infdev " + version.substring(4, version.length());
							} else {
								ver_prefix = version;
							}
						} catch (ClassNotFoundException ex4) {
							try {
								appletClass = classLoader.loadClass("M");
								ver_prefix = version;
								width = 854;
								height = 480;
							} catch (ClassNotFoundException ex5) {}
						}
					} catch (Exception ex3) {}
				} catch (Exception ex2) {}
			}
		}

		try {
			applet = (Applet) appletClass.newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void play() {
		try {

			String nativesPath = mainFolder + "bin/natives";

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

			setPrefixAndLoadMainClass(url);

			// Allow joining servers for Classic MP versions
			askForServer();

			// Replace the main game folder to .betacraft
			// Skip versions prior to Indev. They don't support changing game folders.
			if (!version.startsWith("4k") && !version.startsWith("c") && !version.startsWith("rd-") && !version.startsWith("mc-")) {
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
			panel.setPreferredSize(new Dimension(width, height)); // 854, 480

			gameFrame.add(panel, "Center");
			gameFrame.pack();
			gameFrame.setLocationRelativeTo(null);
			gameFrame.setVisible(true);

			applet.setStub(this);
			applet.resize(width, height);
			applet.setSize(width, height);

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
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					applet.stop();
				}
			});

			// Start Discord RPC
			discordThread.start();
		} catch (ClassNotFoundException ex) {
			JOptionPane.showMessageDialog(null, "Error code 6: Couldn't satisfy the wrapper with a valid .jar file. Check your version.", "Error", JOptionPane.INFORMATION_MESSAGE);
			ex.printStackTrace();
		} catch (Exception ex) {
			System.out.println("A critical error has occurred!");
			ex.printStackTrace();
		}
	}

	@Override
	public void appletResize(int width, int height) {
		applet.resize(width, height);
		applet.setSize(width, height);
	}

	@Override
	public void resize(int width, int height) {
		applet.resize(width, height);
		applet.setSize(width, height);
	}

	@Override
	public void resize(Dimension d) {
		applet.resize(d);
		applet.setSize(d);
	}

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

			// Quit Game button for Beta :D
			if (!isIndevPlus || version.startsWith("in") || version.startsWith("a") || !version.startsWith("b")) return;
			try {
				for (final Field field : appletClass.getDeclaredFields()) {
					final String name = field.getType().getName();
					if (!name.contains("awt") && !name.contains("java")) {
						Field buttonTriggerField = null;

						// Set Minecraft field accessible
						field.setAccessible(true);
						// Get the instance
						Object mc = field.get(applet);

						final Class clazz = classLoader.loadClass(name);
						for (final Field field1 : clazz.getDeclaredFields()) {
							if (Modifier.isPublic(field1.getModifiers())) {

								if (!field1.getType().isPrimitive()) continue;

								// The trigger field for the button is always public,
								// and is always the first to be found by this code.
								// The next public boolean field is responsible for
								// something different, so we need to stop the code
								// after we found the first occurrence.
								if (field1.getType().getName().equals("boolean")) {
									boolean bol = field1.getBoolean(mc);
									if (bol) {
										buttonTriggerField = field1;
										break;
									}
								}
							}
						}
						if (buttonTriggerField != null) {
							buttonTriggerField.setAccessible(true);
							buttonTriggerField.set(mc, false);
							break;
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public URL getDocumentBase() {
		try {
			if (nonOnlineClassic.contains(version)) {
				return new URL("http://www.minecraft.net:" + portCompat + "/game/");
			} else {
				return new URL("http://www.minecraft.net/game/");
			}
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		applet.setVisible(b);
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

		onlineInfdev.add("inf-20100227-1");
		onlineInfdev.add("inf-20100227-2");
		onlineInfdev.add("inf-20100227");
		onlineInfdev.add("inf-20100313");
		onlineInfdev.add("inf-20100316");
		onlineInfdev.add("inf-20100320");
		onlineInfdev.add("inf-20100321");
		onlineInfdev.add("inf-20100325");
	}
}
