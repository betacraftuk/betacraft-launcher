package org.betacraft;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.betacraft.launcher.BC;

public class FkWrapper extends Wrapper {

	public FkWrapper(String user, String ver_prefix, String version, String sessionid, String mainFolder, int height,
			int width, boolean RPC, String launchMethod, String server, String mppass, String USR, String VER,
			Image img, ArrayList addons) {
		super(user, ver_prefix, version, sessionid, mainFolder, height, width, RPC, launchMethod, server, mppass, null, USR, VER,
				img, addons);
	}

	public void loadJars() {
		try {
			final URL[] url = new URL[1];
			url[0] = new File(BC.get() + "versions/" + version + ".jar").toURI().toURL();

			loadMainClass(url);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void loadMainClass(URL[] url) {
		try {
			URL[] old = url.clone();
			URL[] neww = new URL[old.length];
			int i;
			for (i = 0; i < old.length; i++) {
				neww[i] = old[i];
			}
			classLoader = new URLClassLoader(neww);
			try {
				System.out.println();
				System.out.println("Loading addons:");
				for (Class<Addon> c : ogaddons) {
					this.loadAddon((Addon) c.newInstance());
					System.out.println("- " + c);
				}
				System.out.println();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			mainClass = classLoader.loadClass("M");
			mainClassInstance = mainClass.newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void play() {
		if (this.ask_for_server) this.askForServer();
		try {
			this.loadJars();

			// Make a frame for the game
			gameFrame = new Frame();
			gameFrame.setTitle(window_name);
			gameFrame.setIconImage(this.icon);
			gameFrame.setBackground(Color.BLACK);
			this.addHooks();

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
			a.setSize(new Dimension(width, height));

			// Add game's applet to this window
			this.setLayout(new BorderLayout());
			this.add(a, "Center");

			gameFrame.removeAll();
			gameFrame.setLayout(new BorderLayout());
			gameFrame.add(this, "Center");
			this.init();
			active = true;
			this.start();

			gameFrame.validate();

			// Start Discord RPC
			if (discord) discordThread.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
