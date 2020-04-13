package org.betacraft;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.betacraft.launcher.Lang;
import org.betacraft.launcher.Logger;
import org.betacraft.launcher.OS;

// Pretends to be MinecraftApplet
public class Classic12aWrapper extends Wrapper {
	public Runnable run;
	public Thread thread;

	public Classic12aWrapper(String user, String ver_prefix, String version, String sessionid, String mainFolder,
			Integer height, Integer width, Boolean RPC, String launchMethod, String server, String mppass, String USR,
			String VER, Image img, ArrayList addons) {
		super(user, ver_prefix, version, sessionid, mainFolder, height, width, RPC, launchMethod, server, mppass, USR, VER, img,
				addons);
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

			panel.addMouseListener(new MouseListener() {

				public void mouseClicked(MouseEvent e) {
					gameFrame.removeAll();
					gameFrame.setLayout(new BorderLayout());
					gameFrame.add(Classic12aWrapper.this, "Center");
					Classic12aWrapper.this.init();
					active = true;
					Classic12aWrapper.this.start();
					Runtime.getRuntime().addShutdownHook(new Thread() {
						@Override
						public void run() {
							Classic12aWrapper.this.stop();
						}
					});
					gameFrame.validate();
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

			// Start Discord RPC
			if (discord) discordThread.start();
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	@Override
	public void start() {}

	@Override
	public void init() {
		try {
			if (!this.addonsPreAppletInit(this.addons)) return;
			Canvas canvas = new Canvas();
			for (final Field minecraftField : mainClass.getDeclaredFields()) {
				String name = minecraftField.getType().getName();
				if (name.contains("mojang")) {
					final Class<?> clazz = classLoader.loadClass(name);
					Constructor<?> con = clazz.getConstructor(java.awt.Canvas.class, int.class, int.class, boolean.class);
					run = (Runnable) con.newInstance(params.containsKey("fullscreen") ? null : canvas, panel.getWidth(), panel.getHeight(), params.containsKey("fullscreen"));
					mainClassInstance = run;
					setResolution(run, canvas, clazz);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	@Override
	public void stop() {
		try {
			for (final Field mcField : mainClass.getDeclaredFields()) {
				String name = mcField.getType().getName();
				if (name.contains("mojang")) {
					final Class<?> clazz = classLoader.loadClass(name);
					mcField.setAccessible(true);
					for (Field pauseField : clazz.getDeclaredFields()) {
						int mod = pauseField.getModifiers();
						if (Modifier.isVolatile(mod) && Modifier.isPublic(mod) && pauseField.getType().getName().equals("boolean")) {
							pauseField.set(run, true);
						}
					}
					for (Field runningField : clazz.getDeclaredFields()) {
						int mod = runningField.getModifiers();
						if (Modifier.isVolatile(mod) && !Modifier.isPublic(mod) && runningField.getType().getName().equals("boolean")) {
							runningField.setAccessible(true);
							runningField.set(run, false);
						}
					}
					clazz.getDeclaredMethod("stop", null).invoke(run);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	@Override
	public void destroy() {
		this.stop();
		try {
			this.thread.join(5000L);
		} catch (InterruptedException e) {
            try {
            	for (final Field mcField : mainClass.getDeclaredFields()) {
    				String name = mcField.getType().getName();
    				if (name.contains("mojang")) {
    					final Class<?> clazz = classLoader.loadClass(name);
    					mcField.setAccessible(true);
    					clazz.getDeclaredMethod("destroy", null).invoke(run);
    				}
    			}
            }
            catch (Exception ee) {
                ee.printStackTrace();
            }
        } catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	public void setResolution(Runnable run, Canvas canvas, Class<?> clazz) {
		try {
			for (final Field appletModeField : clazz.getDeclaredFields()) {
				if (appletModeField.getType().getName().equalsIgnoreCase("boolean") && Modifier.isPublic(appletModeField.getModifiers())) {
					appletModeField.setAccessible(true);
					appletModeField.set(run, OS.isLinux() || params.containsKey("fullscreen") ? false : true);
					break;
				}
			}
			for (final Field fullscreenField : clazz.getDeclaredFields()) {
				if (fullscreenField.getType().getName().equalsIgnoreCase("boolean") && Modifier.isPrivate(fullscreenField.getModifiers())) {
					fullscreenField.setAccessible(true);
					fullscreenField.set(run, params.containsKey("fullscreen"));
					break;
				}
			}
			this.setLayout(new BorderLayout());
			this.add(canvas, "Center");
			(thread = new Thread(run)).start();
			canvas.setFocusable(true);
			this.validate();
			addonsPostAppletInit(this.addons);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
