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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.betacraft.launcher.BC;
import org.betacraft.launcher.Logger;

public class FkWrapper extends Wrapper {

	public FkWrapper(String user, String ver_prefix, String version, String sessionid, String mainFolder, int height,
			int width, boolean RPC, String launchMethod, String server, String mppass, String USR, String VER,
			Image img, ArrayList<Class> addons) {
		super(user, ver_prefix, version, sessionid, mainFolder, height, width, RPC, launchMethod, server, mppass, USR, VER,
				img, addons);
	}

	@Override
	public void setPrefixAndLoadMainClass(URL[] url) {
		try {
			classLoader = new BCClassLoader(url);
			appletClass = classLoader.loadClass("M");
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

			// Add game's applet to this window
			this.setLayout(new BorderLayout());
			this.add(applet, "Center");

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
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

}
