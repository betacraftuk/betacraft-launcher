package org.betacraft;

import java.applet.Applet;
import java.awt.Image;
import java.io.File;
import java.lang.Thread.State;
import java.net.URL;
import java.util.ArrayList;

import org.betacraft.launcher.BC;
import org.betacraft.launcher.Logger;

public class PreClassicWrapper extends Wrapper {

	public PreClassicWrapper(String user, String ver_prefix, String version, String sessionid, String mainFolder,
			int height, int width, boolean RPC, String launchMethod, String server, String mppass, String USR,
			String VER, Image img, ArrayList<Class> addons) {
		super(user, ver_prefix, version, sessionid, mainFolder, height, width, RPC, launchMethod, server, mppass, USR, VER,
				img, addons);
	}

	@Override
	public void setPrefixAndLoadMainClass(URL[] url) {
		try {
			classLoader = new BCClassLoader(url);

			try {
				// rd-
				appletClass = classLoader.loadClass("com.mojang.rubydung.RubyDung");

				Thread t = new Thread((Runnable) appletClass.newInstance());
				t.start();
				while (t.getState() == State.RUNNABLE || t.getState() == State.NEW) {
					if (discordThread != null) discordThread.rpc.Discord_RunCallbacks();
					Thread.sleep(2000);
				}
				this.stop();

				return;
			} catch (ClassNotFoundException ex) {
				try {
					// mc-
					appletClass = classLoader.loadClass("com.mojang.minecraft.RubyDung");

					Thread t = new Thread((Runnable) appletClass.newInstance());
					t.start();
					while (t.getState() == State.RUNNABLE || t.getState() == State.NEW) {
						if (discordThread != null) discordThread.rpc.Discord_RunCallbacks();
						Thread.sleep(2000);
					}
					this.stop();

					return;
				} catch (ClassNotFoundException ex1) {
					ex1.printStackTrace();
					Logger.printException(ex1);
				}
			}
			applet = (Applet) appletClass.newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	@Override
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
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

}
