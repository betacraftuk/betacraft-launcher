package org.betacraft;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.betacraft.launcher.Logger;

public class FkWrapper extends Wrapper {

	public FkWrapper(String user, String ver_prefix, String version, String sessionid, String mainFolder, int height,
			int width, boolean RPC, String launchMethod, String server, String mppass, String USR, String VER,
			Image img, ArrayList addons) {
		super(user, ver_prefix, version, sessionid, mainFolder, height, width, RPC, launchMethod, server, mppass, USR, VER,
				img, addons);
	}

	@Override
	public void loadMainClass(URL[] url) {
		try {
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
					Class<?> addon = classLoader.loadClass(split2[split2.length - 1]);*/
					this.loadAddon((Addon) c.newInstance());
					System.out.println("- " + c);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				Logger.printException(ex);
			}
			mainClass = classLoader.loadClass("M");
			mainClassInstance = mainClass.newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	@Override
	public void play() {
		try {
			this.loadJars();

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

			// Add game's applet to this window
			this.setLayout(new BorderLayout());
			this.add(a, "Center");

			gameFrame.removeAll();
			gameFrame.setLayout(new BorderLayout());
			gameFrame.add(this, "Center");
			this.init();
			active = true;
			this.start();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					FkWrapper.this.stop();
				}
			});
			gameFrame.validate();

			// Start Discord RPC
			if (discord) discordThread.start();
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

}
