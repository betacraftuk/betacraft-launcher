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
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.betacraft.launcher.BC;
import org.betacraft.launcher.Logger;

public class ClassicWrapper extends Wrapper {

	public ClassicWrapper(String user, String ver_prefix, String version, String sessionid, String mainFolder, int height,
			int width, boolean RPC, String launchMethod, String server, String mppass, String USR, String VER,
			Image img, ArrayList<Class> addons) {
		super(user, ver_prefix, version, sessionid, mainFolder, height, width, RPC, launchMethod, server, mppass, USR, VER,
				img, addons);
	}

	@Override
	public void setPrefixAndLoadMainClass(URL[] url) {
		try {
			classLoader = new BCClassLoader(url);
			appletClass = classLoader.loadClass("com.mojang.minecraft.MinecraftApplet");
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
			JLabel infolabel1 = new JLabel("<html><font size=5>Resize the window to the size you want to play on.<br />Click anywhere inside this window to start the game.</font></html>");
			infolabel1.setBackground(Color.BLACK);
			infolabel1.setForeground(Color.WHITE);
			panel.add(infolabel1, BorderLayout.CENTER);
			panel.setBackground(Color.BLACK);
			panel.setPreferredSize(new Dimension(width, height));

			final ComponentAdapter listener = new ComponentAdapter() {
				public void componentResized(ComponentEvent componentEvent) {
					applet.resize(panel.getWidth(), panel.getHeight());
					applet.setSize(new Dimension(panel.getWidth(), panel.getHeight()));
				}
			};

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

			gameFrame.addComponentListener(listener);

			// Add game's applet to this window
			this.setLayout(new BorderLayout());
			this.add(applet, "Center");
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	@Override
	public URL getDocumentBase() {
		try {
			return new URL("http://www.minecraft.net" + proxyCompat + "/game/");
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			Logger.printException(e);
			return null;
		}
	}
}
