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
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;

public class Wrapper extends Applet implements AppletStub {

    final static Map<String, String> params = new HashMap<String, String>();
	private static int session;
	public static String mainFolder;
	public static String version;
	private static URLClassLoader classLoader;

	private static Applet applet = null;
	private static int context = 0;
	private static boolean active = false;
	public static String wrapper = "1.0_01";
	public static String ver_prefix = "";

	public static void main(String[] args) {
		System.out.println("Starting BetacraftWrapper v" + wrapper);
		try {
			if (args.length < 2) {
				JOptionPane.showMessageDialog(null, "Error code 1: could not initialize wrapper (arguments too short)", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String[] info = args[0].split(":");
			System.out.println("arguments: " + args[0]);
			try {
				session = Integer.parseInt(info[1]);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Error code 2: could not initialize wrapper (sessionid is not a valid number)", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			version = info[2];
			String path = "";
			for (int i = 1; i < args.length; i++) {
				path = path + " " + args[i];
			}
			if (path.equals("")) {
				JOptionPane.showMessageDialog(null, "Error code 3: could not initialize wrapper (mainFolder argument is empty)", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			path = path.substring(1, path.length());
			mainFolder = path;
			params.put("username", info[0]);
			params.put("sessionid", info[1]);
			params.put("stand-alone", "true");
			System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
			if (info[3].equals("true")) {
		        System.setProperty("http.proxyPort", "80");
		        if (info[2].startsWith("c0") || info[2].startsWith("in")) {
		        	System.setProperty("http.proxyHost", "classic.retrocraft.net");
		        } else {
		        	System.setProperty("http.proxyHost", "retrocraft.net");
		        }
			}
			if (version.startsWith("b1.")) {
			    ver_prefix = "Beta " + version.substring(1, version.length());
			} else if (version.startsWith("a1.")) {
			    ver_prefix = "Alpha " + version.substring(1, version.length());
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
			new Wrapper().play();
			DiscordRPC lib = DiscordRPC.INSTANCE;
			String applicationId = "567450523603566617";
			DiscordEventHandlers handlers = new DiscordEventHandlers();
			lib.Discord_Initialize(applicationId, handlers, true, "");
			DiscordRichPresence presence = new DiscordRichPresence();
			presence.startTimestamp = System.currentTimeMillis() / 1000;
			presence.state = "Minecraft version: " + version;
			presence.details = "Nick: " + info[0];
			lib.Discord_UpdatePresence(presence);
			new DiscordThread(lib).start();
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

        public void start() {
            while (!Thread.currentThread().isInterrupted()) {
                rpc.Discord_RunCallbacks();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }
    }

	public void unloadNatives() {
		File file = new File(mainFolder + "versions/" + version + session);
		String[] entries = file.list();
		if (entries == null) return;
		for (String s: entries) {
		    File currentFile = new File(file.getPath(), s);
		    if (!currentFile.isDirectory()) {
		    	try {
					Files.delete(currentFile.toPath());
				} catch (Exception ex) {}
		    }
		}
	}

	public static void applyNatives() {
		File lwjgl;
		File lwjgl64;
		File jinput;
		File jinput64;
		File openal;
		File openal64;

		File jinputdx8 = null;
		File jinputdx864 = null;

		String lwj;
		String lwj64;
		String jin;
		String jin64;
		String ope;
		String ope64;

		String jindx = null;
		String jindx64 = null;
		if (Sys.isLinux() || Sys.isSolaris()) {
			lwj = "liblwjgl.so";
			lwj64 = "liblwjgl64.so";
			jin = "libjinput-linux.so";
			jin64 = "libjinput-linux64.so";
			ope = "libopenal.so";
			ope64 = "libopenal64.so";
		} else if (Sys.isWindows()) {
			lwj = "lwjgl.dll";
			lwj64 = "lwjgl64.dll";
			jin = "jinput-raw.dll";
			jin64 = "jinput-raw_64.dll";
			ope = "OpenAL32.dll";
			ope64 = "OpenAL64.dll";
			jindx = "jinput-dx8.dll";
			jindx64 = "jinput-dx8_64.dll";
		} else {
			System.exit(1);
			return;
		}
		lwjgl = new File(mainFolder + "versions/" + version + session, lwj);
		lwjgl64 = new File(mainFolder + "versions/" + version + session, lwj64);
		jinput = new File(mainFolder + "versions/" + version + session, jin);
		jinput64 = new File(mainFolder + "versions/" + version + session, jin64);
		openal = new File(mainFolder + "versions/" + version + session, ope);
		openal64 = new File(mainFolder + "versions/" + version + session, ope64);
		if (jindx != null) {
			jinputdx8 = new File(mainFolder + "versions/" + version + session, jindx);
			jinputdx864 = new File(mainFolder + "versions/" + version + session, jindx64);
		}

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
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
    public void play() {
		try {
			String nativesPath = mainFolder + "versions/" + version + session;
			final File natives = new File(nativesPath);
			natives.mkdirs();
			applyNatives();
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

	        final Class<?> appletClass;
			if (version.startsWith("c")) {
				appletClass = (Class<Applet>) classLoader.loadClass("com.mojang.minecraft.MinecraftApplet");
			} else if (version.startsWith("rd")) {
				appletClass = (Class<Applet>) classLoader.loadClass("com.mojang.rubydung.RubyDung");
				Runnable run = (Runnable) appletClass.newInstance();
				run.run();
				return;
			} else if (version.startsWith("mc")) {
				appletClass = (Class<Applet>) classLoader.loadClass("com.mojang.minecraft.RubyDung");
				Runnable run = (Runnable) appletClass.newInstance();
				run.run();
				return;
			} else {
				appletClass = (Class<Applet>) classLoader.loadClass("net.minecraft.client.MinecraftApplet");
			}
			applet = (Applet) appletClass.newInstance();

			if (version.startsWith("in") || version.startsWith("a1.") || version.startsWith("b1.")) {
				for (final Field field : appletClass.getDeclaredFields()) {
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
							fileField.set(null, new File(mainFolder));
							break;
						}
					}
				}
			}

			final Frame launcherFrameFake = new Frame();
			launcherFrameFake.setLocationRelativeTo(null);
			launcherFrameFake.setTitle("Minecraft " + ver_prefix);
			launcherFrameFake.setBackground(Color.BLACK);
			final JPanel panel = new JPanel();
			launcherFrameFake.setLayout(new BorderLayout());
			panel.setPreferredSize(new Dimension(854, 480));
			launcherFrameFake.add(panel, "Center");
			launcherFrameFake.pack();
			launcherFrameFake.setLocationRelativeTo(null);
			launcherFrameFake.setVisible(true);
			applet.setStub(this);
			launcherFrameFake.addWindowListener(new WindowAdapter() {
	            @Override
	            public void windowClosing(final WindowEvent e) {
	            	stop();
	                destroy();
	                launcherFrameFake.setVisible(false);
	                launcherFrameFake.dispose();
	                try {
						classLoader.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
	                unloadNatives();
	                System.exit(1);
	            }
	        });
			this.setLayout(new BorderLayout());
			this.add(applet, "Center");
			this.validate();
			launcherFrameFake.removeAll();
			launcherFrameFake.setLayout(new BorderLayout());
			launcherFrameFake.add(this, "Center");
			launcherFrameFake.validate();
			this.init();
			active = true;
			this.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void appletResize(final int width, final int height) {}

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
	    DiscordRPC.INSTANCE.Discord_Shutdown();
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
    	if (params.containsKey(paramName)) {
    		return params.get(paramName);
    	}
    	System.err.println("Client asked for parameter: " + paramName);
    	return null;
    }
}
