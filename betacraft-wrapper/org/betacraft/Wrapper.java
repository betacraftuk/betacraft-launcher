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
import java.io.IOException;
import java.lang.Thread.State;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.betacraft.launcher.BC;
import org.betacraft.launcher.InstanceList;
import org.betacraft.launcher.Lang;
import org.betacraft.launcher.Logger;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;

public class Wrapper extends Applet implements AppletStub {

	public final Map<String, String> params = new HashMap<String, String>(); // Client parameters
	public String session; // Session id for premium authentication
	public String mainFolder; // .betacraft folder
	public String version; // Version to be launched
	public URLClassLoader classLoader; // Class loader for linking the game and natives
	public Class appletClass; // Minecraft's main class
	public boolean discord = false; // Discord RPC
	public Image icon;

	public Frame gameFrame;
	public Applet applet = null; // Game's applet
	public int context = 0; // Return value for isActive
	public boolean active = false; // If the game has started
	public String ver_prefix = "";
	public DiscordThread discordThread = null;

	public int width = 854;
	public int height = 480;

	public String proxyCompat = "www.minecraft.net";
	public String portCompat = "80";
	public String serverAddress = null;
	public String mppass = null;

	public ArrayList<Addon> addons = new ArrayList<Addon>();

	public Wrapper(String user, String ver_prefix, String version, String sessionid, String mainFolder, Integer height, Integer width, Boolean RPC, String launchMethod, String server, String mppass, String USR, String VER, Image img, ArrayList addons) {
		try {
			ArrayList<Class> li = (ArrayList<Class>) addons;
			for (Class c : li) {
				this.addons.add((Addon)c.newInstance());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
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
		this.ver_prefix = "Minecraft " + ver_prefix;

		new File(this.mainFolder).mkdirs();

		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		String proxy = System.getProperty("http.proxyHost");
		String port = System.getProperty("http.proxyPort");

		if (proxy != null) {
			this.proxyCompat = proxy;
		}
		if (port != null) {
			this.portCompat = port;
		}

		if (this.discord) {
			DiscordRPC lib = DiscordRPC.INSTANCE;
			String applicationId = "567450523603566617";
			DiscordEventHandlers handlers = new DiscordEventHandlers();
			lib.Discord_Initialize(applicationId, handlers, true, "");
			DiscordRichPresence presence = new DiscordRichPresence();
			presence.startTimestamp = System.currentTimeMillis() / 1000;
			presence.state = VER + ": " + version;
			presence.details = USR + " " + user;
			lib.Discord_UpdatePresence(presence);
			discordThread = new DiscordThread(lib);
		}

		try {
			for (Addon a : this.addons) {
				a.preInit(this);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
		play();
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

	public void setPrefixAndLoadMainClass(URL[] url) {
		classLoader = null;
		classLoader = new BCClassLoader(url);
		String launchType = null;

		// I know this looks terrible, but it works!
		try {
			// Classic
			appletClass = classLoader.loadClass("com.mojang.minecraft.MinecraftApplet");
			launchType = "classicmp";
			//ver_prefix = "Classic " + version.substring(1, version.length());
		} catch (ClassNotFoundException ex) {
			try {
				appletClass = classLoader.loadClass("com.mojang.minecraft.MinecraftApplet");
			} catch (ClassNotFoundException ex1) {
				try {
					// rd
					appletClass = classLoader.loadClass("com.mojang.rubydung.RubyDung");
					launchType = "rd";
					//ver_prefix = "Pre-Classic " + version;

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
						launchType = "mc";

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
							launchType = "indev";
							if (version.startsWith("in-")) {
								//ver_prefix = "Indev " + version.substring(3, version.length());
							} else if (version.startsWith("b")) {
								//ver_prefix = "Beta " + version.substring(1, version.length());
							} else if (version.startsWith("a")) {
								//ver_prefix = "Alpha v" + version.substring(1, version.length());
							} else if (version.startsWith("inf-")) {
								//ver_prefix = "Infdev " + version.substring(4, version.length());
							} else {
								//ver_prefix = version;
							}
						} catch (ClassNotFoundException ex4) {
							try {
								appletClass = classLoader.loadClass("M");
								launchType = "4k";
								//ver_prefix = version;
							} catch (ClassNotFoundException ex5) {}
						}
					} catch (Exception ex3) {}
				} catch (Exception ex2) {}
			}
		}
		// Don't force the launch type if the info file knows it better.
		if (this.launchType.equals("")) this.launchType = launchType;

		try {
			applet = (Applet) appletClass.newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	public void setGameFolder() {
		try {
			if (launchType.equals("indev")) {
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

	public void play() {
		try {

			String nativesPath = BC.get() + "bin/natives";

			// Glue everything Minecraft needs for running
			String file = BC.get() + "versions/" + version + ".jar";
			String file1 = BC.get() + "bin/lwjgl.jar";
			String file2 = BC.get() + "bin/lwjgl_util.jar";
			String file3 = BC.get() + "bin/jinput.jar";
			String file4 = BC.get() + "bin/jutils.jar";

			System.setProperty("org.lwjgl.librarypath", nativesPath);
			System.setProperty("net.java.games.input.librarypath", nativesPath);

			final URL[] url = new URL[5];
			url[0] = new File(file).toURI().toURL();
			url[1] = new File(file1).toURI().toURL();
			url[2] = new File(file2).toURI().toURL();
			url[3] = new File(file3).toURI().toURL();
			url[4] = new File(file4).toURI().toURL();

			setPrefixAndLoadMainClass(url);

			// Replace the main game folder to .betacraft
			// Skip versions prior to Indev. They don't support changing game folders.
			this.setGameFolder();

			// Start Discord RPC
			if (discord) discordThread.start();

			// Make a frame for the game
			gameFrame = new Frame();
			gameFrame.setTitle(ver_prefix);
			gameFrame.setIconImage(this.icon);
			gameFrame.setBackground(Color.BLACK);

			// This is needed for the window size
			final JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			gameFrame.setLayout(new BorderLayout());
			panel.setBackground(Color.BLACK);
			panel.setPreferredSize(new Dimension(width, height)); // 854, 480

			gameFrame.add(panel, "Center");
			gameFrame.pack();
			gameFrame.setLocationRelativeTo(null);
			gameFrame.setVisible(true);

			applet.setStub(this);
			applet.resize(width, height);
			applet.setMinimumSize(new Dimension(width, height));

			this.addHooks();

			// Add game's applet to this window
			this.setLayout(new BorderLayout());
			this.add(applet, "Center");
			gameFrame.removeAll();
			gameFrame.setLayout(new BorderLayout());
			gameFrame.add(Wrapper.this, "Center");
			this.init();
			active = true;
			this.start();
			gameFrame.validate();
			//classLoader.loadClass("com.mojang.minecraft.level.tile.a");
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
				try {
					classLoader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					Logger.printException(e1);
				}
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
		if (discord) DiscordRPC.INSTANCE.Discord_Shutdown();
		if (applet != null) {
			active = false;
			try {
				applet.stop();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}
		}
	}

	@Override
	public void destroy() {
		if (applet != null) {
			try {
				applet.destroy();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}
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
			try {
				for (Addon a : this.addons) {
					a.postInit(this);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				Logger.printException(ex);
			}
		}

		// c0.24_st_03 - c0.27_st
		/*if (launchType.equals("c")) {
				try {
					for (final Field minecraftField : appletClass.getDeclaredFields()) {
						final String name = minecraftField.getType().getName();
						if (name.contains("com.mojang.minecraft.k")) {

							// Set Minecraft field accessible
							minecraftField.setAccessible(true);
							// Get the instance
							Object mc = minecraftField.get(applet);

							final Class clazz = classLoader.loadClass(name);
							for (final Field field1 : clazz.getDeclaredFields()) {
								if (field1.getType().getName().equals("com.mojang.minecraft.e.a")) {
									final Class clazz2 = classLoader.loadClass("com.mojang.minecraft.e.a");
									final Class clazz3 = classLoader.loadClass("com.mojang.minecraft.d.b");

									//Field funsafe = Unsafe.class.getDeclaredField("theUnsafe");
									//funsafe.setAccessible(true);
									//Unsafe unsafe = (Unsafe) funsafe.get(null);
									Object survivalClass = clazz2.getConstructor(mc.getClass()).newInstance(mc);


									// Reproduce the init method which is removed in c0.28 - creative c0.30s

									field1.set(mc, survivalClass);
									break;
								}
							}
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}*/
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
			//if (name.equals("com.mojang.minecraft.level.tile.a")) return new SurvivalsTile(2).getClass();
			return super.loadClass(name);
		}
	}

	/*public class SurvivalsTile extends com.mojang.minecraft.level.tile.a {

		protected SurvivalsTile(int i) {
			super(i);
		}

		protected SurvivalsTile(int i, int j) {
			super(i, j);
		}

		@Override
		public void e(final Level level, final int n, final int n2, final int n3) {
			for (int f = this.f(), i = 0; i < f; ++i) {
				if (com.mojang.minecraft.level.tile.a.a.nextFloat() <= n) {
					final float n5 = 0.7f;
	                level.addEntity(new Item(level, n + (com.mojang.minecraft.level.tile.a.a.nextFloat() * n5 + (1.0f - n5) * 0.5f), n2 + (com.mojang.minecraft.level.tile.a.a.nextFloat() * n5 + (1.0f - n5) * 0.5f), n3 + (com.mojang.minecraft.level.tile.a.a.nextFloat() * n5 + (1.0f - n5) * 0.5f), this.ac));
				}
			}
		}
	}*/
}
