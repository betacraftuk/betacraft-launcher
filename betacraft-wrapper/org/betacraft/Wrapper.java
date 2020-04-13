package org.betacraft;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.betacraft.Addon.WhatToDo;
import org.betacraft.launcher.BC;
import org.betacraft.launcher.Lang;
import org.betacraft.launcher.Launcher;
import org.betacraft.launcher.Logger;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;


public class Wrapper extends Applet implements AppletStub {

	/** Client parameters */
	public final Map<String, String> params = new HashMap<String, String>();
	/** Session id for premium authentication */
	public String session;
	/** Instance's folder */
	public String mainFolder;
	/** Version to be launched */
	public String version;
	/** Class loader for linking the game and libraries */
	public BCClassLoader classLoader;
	/** Minecraft's main class */
	public Class mainClass;
	/** Discord RPC */
	public boolean discord = false;
	/** Icon for the window frame */
	public Image icon;

	public Frame gameFrame;
	public JPanel panel;
	/** Applet of the game */
	public Object mainClassInstance = null;
	public int context = 0;
	public boolean active = false;
	/** Name for the window frame */
	public String window_name = "";
	public DiscordThread discordThread = null;

	/** Preferred width of the game applet */
	public int width = 854;
	/** Preferred height of the game applet */
	public int height = 480;

	public String proxyCompat = "www.minecraft.net";
	public String portCompat = "80";
	public String serverAddress = null;
	public String mppass = null;

	/** List of addons to be applied to this instance */
	public ArrayList<Addon> addons = new ArrayList<>();
	public ArrayList<Class<Addon>> ogaddons = new ArrayList<>();

	/**
	 * Initializes the Wrapper class & enables the game
	 * 
	 * @param user - Username
	 * @param ver_prefix - Name for the window frame
	 * @param version - Version of the game to play
	 * @param sessionid - Session id of the account (token:accessToken:profileid)
	 * @param mainFolder - Folder of the instance
	 * @param height - Preferred height of the applet
	 * @param width - Preferred width of the applet
	 * @param RPC - Discord Rich Presence
	 * @param launchMethod - Launch method for the version
	 * @param server - Server parameters
	 * @param mppass - Authentication string for Classic servers
	 * @param USR - Discord RPC username string
	 * @param VER - Discord RPC version string
	 * @param img - Icon for the window frame
	 * @param addons - List of addons to apply to this instance
	 */
	public Wrapper(String user, String ver_prefix, String version, String sessionid, String mainFolder, Integer height, Integer width, Boolean RPC, String launchMethod, String server, String mppass, String USR, String VER, Image img, ArrayList addons) {
		ogaddons = (ArrayList<Class<Addon>>)addons;

		params.put("username", user);
		params.put("sessionid", sessionid);
		params.put("haspaid", "true");

		this.version = version;
		this.session = sessionid;
		launchType = launchMethod;
		this.mainFolder = mainFolder;
		this.height = height;
		this.width = width;
		this.discord = RPC;
		this.serverAddress = server;
		this.mppass = mppass;
		this.icon = img;
		this.window_name = ver_prefix;

		new File(this.mainFolder).mkdirs();

		String proxy = System.getProperty("http.proxyHost");
		String port = System.getProperty("http.proxyPort");

		if (proxy != null) {
			this.proxyCompat = proxy;
		}
		if (port != null) {
			this.portCompat = port;
		}

		if (this.discord) {
			String applicationId = "567450523603566617";
			DiscordEventHandlers handlers = new DiscordEventHandlers();
			DiscordRPC.discordInitialize(applicationId, handlers, true);

			DiscordRichPresence presence = new DiscordRichPresence();
			presence.startTimestamp = System.currentTimeMillis() / 1000;
			presence.state = VER + ": " + version;
			presence.details = String.format(USR, user);
			presence.largeImageKey = "bc";
			presence.largeImageText = "Download at betacraft.pl";
			DiscordRPC.discordUpdatePresence(presence);
			discordThread = new DiscordThread();
		}

		play();
	}

	/**
	 * Loads addons in the correct order
	 * 
	 * @param a - Addon to load
	 */
	public void loadAddon(Addon a) {
		if (addonLoaded(a)) return;
		List<String> applyAfter = a.applyAfter();
		for (String addon : applyAfter) {
			for (Class<Addon> a1 : ogaddons) {
				if (a1.getName().contains(addon)) {
					try {
						loadAddon((Addon) a1.newInstance());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				/*String[] name = a1.split("\\.");
				if (name[name.length - 2].equals(addon)) {
					try {
						loadAddon((Addon) classLoader.loadClass(name[name.length - 2]).newInstance());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}*/
			}
		}
		this.addons.add(a);
	}

	public boolean addonLoaded(Addon a) {
		for (Addon addon : this.addons) {
			if (addon.getName().equals(a.getName())) return true;
		}
		return false;
	}

	public static class DiscordThread extends Thread {

		DiscordThread() {
			super("RPC-Callback-Handler");
		}

		// Update the RPC
		public synchronized void start() {
			while (!Thread.currentThread().isInterrupted()) {
				DiscordRPC.discordRunCallbacks();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ignored) {}
			}
		}
	}

	/**
	 * Asks the client for server credentials.
	 */
	public void askForServer() {
		if (this.serverAddress != null) {
			String[] ipstuff = serverAddress.split(":");
			params.put("server", ipstuff[0]);
			params.put("port", ipstuff[1]);
			params.put("mppass", this.mppass);
		} else {
			String server = JOptionPane.showInputDialog(this, Lang.WRAP_SERVER, Lang.WRAP_SERVER_TITLE, JOptionPane.DEFAULT_OPTION);//JOptionPane.showInputDialog(null, Lang.WRAP_SERVER, "");
			String port = "25565";
			if (server != null && !server.equals("")) {
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
					params.put("mppass", this.mppass);
				}
			}
		}
	}

	public String launchType = null;

	/**
	 * Replaced with {@link #loadMainClass(URL[])}
	 * 
	 * @param url - Array of jar links for the class loader
	 */
	@Deprecated
	public void setPrefixAndLoadMainClass(URL[] url) {
		this.loadMainClass(url);
	}

	/**
	 * Loads the main game class and initializes it.
	 * Additionally it loads the appropriate non-custom launch method if launch type is empty.
	 * 
	 * @param url - Array of jar links for the class loader
	 */
	public void loadMainClass(URL[] url) {

		try {
			classLoader = null;
			URL[] old = url.clone();
			URL[] neww = new URL[old.length/* + ogaddons.size()*/];
			int i;
			for (i = 0; i < old.length; i++) {
				neww[i] = old[i];
			}
			/*if (i < neww.length) {
				for (String c : ogaddons) {
					neww[i] = new File(c).toURI().toURL();
					i++;
				}
			}*/
			classLoader = new BCClassLoader(neww);
			try {
				for (Class<Addon> c : ogaddons) {
					/*String[] split = c.split("\\.");
					String split2[] = split[split.length - 2].split(File.separator);
					Class<?> addon = Launcher.class.getClassLoader().loadClass(split2[split2.length - 1]);*/
					this.loadAddon((Addon) c.newInstance());
					System.out.println("- " + c);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				Logger.printException(ex);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
		String launchType = null;

		// I know this looks terrible, but it works!
		try {
			// Classic
			mainClass = classLoader.loadClass("com.mojang.minecraft.MinecraftApplet");
			launchType = "classicmp"; // Force MP because we have no way of knowing if the client supports MP or not.
		} catch (ClassNotFoundException ex) {
			try {
				mainClass = classLoader.loadClass("com.mojang.rubydung.RubyDung");
				launchType = "rd";

			} catch (ClassNotFoundException ex2) {
				try {
					mainClass = classLoader.loadClass("com.mojang.minecraft.RubyDung");
					launchType = "mc";

				} catch (ClassNotFoundException ex3) {
					try {
						// Indev+
						mainClass = classLoader.loadClass("net.minecraft.client.MinecraftApplet");
						launchType = "indev";
					} catch (ClassNotFoundException ex4) {
						try {
							mainClass = classLoader.loadClass("M");
							launchType = "4k";
						} catch (ClassNotFoundException ex5) {}
					}
				}// catch (Exception ex3) {}
			}// catch (Exception ex2) {}
		}
		// Don't force the launch type if the info file knows it better.
		if (this.launchType.equals("") &&
				launchType.equalsIgnoreCase("classicmp")) {
			new ClassicMPWrapper(params.get("username"), this.window_name, this.version, params.get("sessionid"), this.mainFolder, this.height, this.width, this.discord, launchType, this.serverAddress, this.mppass, Lang.WRAP_USER, Lang.WRAP_VERSION, this.icon, this.ogaddons);
			return;
		} else if (this.launchType.equals("") && (launchType.equalsIgnoreCase("rd") || launchType.equalsIgnoreCase("mc"))) {
			new PreClassicWrapper(params.get("username"), this.window_name, this.version, params.get("sessionid"), this.mainFolder, this.height, this.width, this.discord, launchType, this.serverAddress, this.mppass, Lang.WRAP_USER, Lang.WRAP_VERSION, this.icon, this.ogaddons);
			return;
		} else if (this.launchType.equals("") && launchType.equalsIgnoreCase("4k")) {
			new FkWrapper(params.get("username"), this.window_name, this.version, params.get("sessionid"), this.mainFolder, this.height, this.width, this.discord, launchType, this.serverAddress, this.mppass, Lang.WRAP_USER, Lang.WRAP_VERSION, this.icon, this.ogaddons);
			return;
		}
		if (this.launchType.equals("")) this.launchType = launchType;

		try {
			mainClassInstance = mainClass.newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	/**
	 * Sets the game folder to {@link #mainFolder}
	 */
	public void setGameFolder() {
		try {
			if (launchType.equals("indev")) {
				for (final Field field : mainClass.getDeclaredFields()) {
					final String name = field.getType().getName();
					if (!name.contains("awt") && !name.contains("java") && !name.contains("long")) {
						Field fileField = null;
						final Class clazz = classLoader.loadClass(name);
						for (final Field field1 : clazz.getDeclaredFields()) {
							if (Modifier.isStatic(field1.getModifiers()) && field1.getType().getName().equals("java.io.File")) {
								fileField = field1;
							}
						}
						if (fileField != null) {
							fileField.setAccessible(true);
							fileField.set(null, new File(this.mainFolder));
							break;
						}
					}
				}
			}
		} catch (ClassNotFoundException ex) {
			JOptionPane.showMessageDialog(null, "Error code 6: Couldn't satisfy the wrapper with a valid .jar file. Check your version or launch configuration file.", "Error", JOptionPane.INFORMATION_MESSAGE);
			ex.printStackTrace();
			Logger.printException(ex);
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	public void loadJars() {
		try {
			String[] libs = new File(BC.get(), "bin/").list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String fileName) {
					return fileName.endsWith(".jar");
				}
			});

			// Glue everything Minecraft needs for running
			String[] files = new String[libs.length + 1];

			files[0] = BC.get() + "versions/" + version + ".jar";

			for (int i = 0; i < libs.length; i++) {
				files[i + 1] = BC.get() + "bin/" + libs[i];
			}

			String nativesPath = BC.get() + "bin/natives";
			System.setProperty("org.lwjgl.librarypath", nativesPath);
			System.setProperty("net.java.games.input.librarypath", nativesPath);

			final URL[] url = new URL[files.length];
			for (int i = 0; i < files.length; i++) {
				System.out.println(files[i]);
				url[i] = new File(files[i]).toURI().toURL();
			}

			loadMainClass(url);
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	public void play() {
		try {

			this.loadJars();

			// Replace the main game folder to .betacraft
			// Skip versions prior to Indev. They don't support changing game folders.
			this.setGameFolder();

			// Make a frame for the game
			gameFrame = new Frame();
			gameFrame.setTitle(window_name);
			gameFrame.setIconImage(this.icon);
			gameFrame.setBackground(Color.BLACK);

			// This is needed for the window size
			panel = new JPanel();
			panel.setLayout(new BorderLayout());
			gameFrame.setLayout(new BorderLayout());
			panel.setBackground(Color.BLACK);
			panel.setPreferredSize(new Dimension(width, height)); // 854, 480

			gameFrame.add(panel, "Center");
			gameFrame.pack();
			gameFrame.setLocationRelativeTo(null);
			gameFrame.setVisible(true);

			Applet a = (Applet) mainClassInstance;
			a.setStub(this);
			a.resize(width, height);
			a.setMinimumSize(new Dimension(width, height));

			this.addHooks();

			// Add game's applet to this window
			this.setLayout(new BorderLayout());
			this.add(a, "Center");
			gameFrame.removeAll();
			gameFrame.setLayout(new BorderLayout());
			gameFrame.add(Wrapper.this, "Center");
			this.init();
			active = true;
			this.start();
			gameFrame.validate();

			// Start Discord RPC
			if (discord) discordThread.start();
		} catch (Exception ex) {
			System.out.println("A critical error has occurred!");
			System.out.print(ex.toString());
		}
	}

	public void addHooks() {
		gameFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				stop();
				destroy();
				gameFrame.setVisible(false);
				gameFrame.dispose();
				System.exit(0);
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Wrapper.this.stop();
			}
		});
	}

	@Override
	public void appletResize(int width, int height) {}

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
		if (discord) DiscordRPC.discordShutdown();
		active = false;
		if (mainClassInstance != null) {
			try {
				if (mainClassInstance instanceof Applet) {
					((Applet) mainClassInstance).stop();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}
		}
	}

	@Override
	public void destroy() {
		if (mainClassInstance != null) {
			try {
				if (mainClassInstance instanceof Applet) {
					((Applet) mainClassInstance).destroy();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}
		}
	}

	@Override
	public void start() {
		if (mainClassInstance != null) {
			try {
				if (mainClassInstance instanceof Applet) {
					((Applet) mainClassInstance).start();
					/*try {
						for (Field mcfield : mainClass.getDeclaredFields()) {
							if (!mcfield.getType().getName().contains("java") && !mcfield.getType().getName().contains("long")) {
								mcfield.setAccessible(true);
								Class clazz1 = mcfield.getType().asSubclass(Runnable.class);
								Object mc = mcfield.get(mainClassInstance);
								for (Field resourcethreadfield : clazz1.getDeclaredFields()) {
									try {
										if (resourcethreadfield.getType().getName().equals("com.mojang.minecraft.c")) {
											resourcethreadfield.setAccessible(true);
											Class clazz = resourcethreadfield.getType().asSubclass(Thread.class);
											Thread resourcethread = (Thread) resourcethreadfield.get(mc);
											for (Field fileparentfield : clazz.getDeclaredFields()) {
												if (fileparentfield.getType().getName().equals("java.io.File")) {
													File file = new File(this.mainFolder, "classicresources/");
													file.mkdirs();
													fileparentfield.setAccessible(true);
													fileparentfield.set(resourcethread, file);
													break;
												}
											}
											break;
										}
									} catch (Exception ex) {
										ex.printStackTrace();
									}
								}
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}*/
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}
		}
	}

	@Override
	public void init() {
		if (mainClassInstance != null) {
			if (!this.addonsPreAppletInit(this.addons)) return;
			((Applet)mainClassInstance).init();
			if (!this.addonsPostAppletInit(this.addons)) return;
		}
	}

	public boolean addonsPreAppletInit(ArrayList<Addon> toinit) {
		try {
			boolean stop = false;
			ArrayList<Addon> left = (ArrayList<Addon>) toinit.clone();
			for (Addon a : toinit) {
				left.remove(a);
				WhatToDo todo = a.preAppletInit(this, left);
				if (todo == WhatToDo.STOP_LOOP) break;
				if (todo == WhatToDo.STOP_CODE) stop = true;
			}
			return !stop;
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
			return true;
		}
	}

	public boolean addonsPostAppletInit(ArrayList<Addon> toinit) {
		try {
			boolean stop = false;
			ArrayList<Addon> left = (ArrayList<Addon>) toinit.clone();
			for (Addon a : toinit) {
				left.remove(a);
				WhatToDo todo = a.postAppletInit(this, left);
				if (todo == WhatToDo.STOP_LOOP) break;
				if (todo == WhatToDo.STOP_CODE) stop = true;
			}
			return !stop;
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
			return true;
		}
	}

	@Override
	public URL getDocumentBase() {
		try {
			return new URL("http://www.minecraft.net/game/");
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			Logger.printException(e);
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
			Logger.printException(e);
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

	public class BCClassLoader extends URLClassLoader {

		public BCClassLoader(URL[] u) {
			super(u);
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			return super.loadClass(name);
		}
	}
}
