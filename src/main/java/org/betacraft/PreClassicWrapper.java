package org.betacraft;

import java.awt.Image;
import java.io.File;
import java.lang.Thread.State;
import java.net.URL;
import java.util.ArrayList;

import org.betacraft.launcher.Logger;

import net.arikia.dev.drpc.DiscordRPC;

public class PreClassicWrapper extends Wrapper {

	public PreClassicWrapper(String user, String ver_prefix, String version, String sessionid, String mainFolder,
			int height, int width, boolean RPC, String launchMethod, String server, String mppass, String USR,
			String VER, Image img, ArrayList addons) {
		super(user, ver_prefix, version, sessionid, mainFolder, height, width, RPC, launchMethod, server, mppass, USR, VER,
				img, addons);
	}

	@Override
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
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}

		try {
			// rd-
			mainClass = classLoader.loadClass("com.mojang.rubydung.RubyDung");
		} catch (ClassNotFoundException ex) {
			try {
				// mc-
				mainClass = classLoader.loadClass("com.mojang.minecraft.RubyDung");
			} catch (ClassNotFoundException ex1) {
				ex1.printStackTrace();
				Logger.printException(ex1);
			}
		}
		try {
			for (Class<Addon> c : ogaddons) {
				/*String[] split = c.split("\\.");
				String split2[] = split[split.length - 2].split(File.separator);
				Class<?> addon = classLoader.loadClass(split2[split2.length - 1]);*/
				this.loadAddon((Addon) c.newInstance());
				System.out.println("- " + c);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
		try {
			//PreClassicHooker r = new PreClassicHooker(this.width, this.height, this.ver_prefix, this.icon, appletClass);
			//Class e = classLoader.loadClass("org.betacraft.PreClassicHooker");
			//Constructor constr = e.getConstructor(int.class, int.class, String.class, Image.class, Class.class);
			//PreClassicHooker run = (PreClassicHooker) constr.newInstance(this.width, this.height, this.ver_prefix, this.icon, appletClass);
			mainClassInstance = mainClass.newInstance();
			if (!this.addonsPreAppletInit(this.addons)) return;
			System.out.println(mainClassInstance.getClass().getName());
			Thread t = new Thread() {
				public void run() {
					((Runnable)mainClassInstance).run();
				}
			};
			t.start();
			if (!this.addonsPostAppletInit(this.addons)) return;
			while (t.getState() == State.RUNNABLE || t.getState() == State.NEW) {
				if (discordThread != null) DiscordRPC.discordRunCallbacks();
				Thread.sleep(2000);
			}
			this.stop();

			return;
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	@Override
	public void play() {
		try {
			this.loadJars();
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}
}
