package org.betacraft;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.betacraft.launcher.Lang;
import org.betacraft.launcher.Logger;

public class ClassicWrapper extends Wrapper {

	public ClassicWrapper(String user, String ver_prefix, String version, String sessionid, String mainFolder, int height,
			int width, boolean RPC, String launchMethod, String server, String mppass, String USR, String VER,
			Image img, ArrayList addons) {
		super(user, ver_prefix, version, sessionid, mainFolder, height, width, RPC, launchMethod, server, mppass, USR, VER,
				img, addons);
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

			classLoader = new BCClassLoader(neww);
			try {
				for (Class<Addon> c : ogaddons) {
					this.loadAddon((Addon) c.newInstance());
					System.err.println("- " + c);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				Logger.printException(ex);
			}
			mainClass = classLoader.loadClass("com.mojang.minecraft.MinecraftApplet");
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
			JLabel infolabel1 = new JLabel(Lang.WRAP_CLASSIC_RESIZE);
			infolabel1.setBackground(Color.BLACK);
			infolabel1.setForeground(Color.WHITE);
			panel.add(infolabel1, BorderLayout.CENTER);
			panel.setBackground(Color.BLACK);
			panel.setPreferredSize(new Dimension(width, height));
			Applet a = (Applet) mainClassInstance;

			final ComponentAdapter listener = new ComponentAdapter() {
				public void componentResized(ComponentEvent componentEvent) {
					a.resize(panel.getWidth(), panel.getHeight());
					a.setSize(new Dimension(panel.getWidth(), panel.getHeight()));
				}
			};

			gameFrame.addComponentListener(listener);

			panel.addMouseListener(new MouseListener() {

				public void mouseClicked(MouseEvent e) {
					gameFrame.removeAll();
					gameFrame.setLayout(new BorderLayout());
					gameFrame.add(ClassicWrapper.this, "Center");
					ClassicWrapper.this.init();
					active = true;
					ClassicWrapper.this.start();
					Runtime.getRuntime().addShutdownHook(new Thread() {
						@Override
						public void run() {
							ClassicWrapper.this.stop();
						}
					});
					gameFrame.validate();
					gameFrame.removeComponentListener(listener);
				}

				public void mouseEntered(MouseEvent arg0) {}
				public void mouseExited(MouseEvent arg0) {}
				public void mousePressed(MouseEvent arg0) {}
				public void mouseReleased(MouseEvent arg0) {}

			});

			gameFrame.add(panel, "Center");
			gameFrame.pack();
			gameFrame.setLocationRelativeTo(null);
			gameFrame.setVisible(true);

			a.setStub(this);
			a.resize(width, height);
			a.setMinimumSize(new Dimension(width, height));

			gameFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(final WindowEvent e) {
					stop();
					destroy();
					ClassicWrapper.this.destroy();
					gameFrame.setVisible(false);
					gameFrame.dispose();
					System.exit(0);
				}
			});

			// Add game's applet to this window
			this.setLayout(new BorderLayout());
			this.add(a, "Center");

			// Start Discord RPC
			if (discord) discordThread.start();
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	@Override
	public void stop() {
		try {
			((Applet)mainClassInstance).stop();
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}
}
