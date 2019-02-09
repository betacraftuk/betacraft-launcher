package org.betacraft.launcher;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

public class AlphaStub extends Applet implements AppletStub {
	final static Map<String, String> params = new HashMap<String, String>();
	private int context = 0;
	protected AlphaStub dys;

	public AlphaStub(String username, String sessionid) {
		params.put("username", username);
		params.put("sessionid", sessionid);
		dys = this;
	}

	public void doo(final Applet applet) {
		Thread t = new Thread() {
			public void run() {
				final Frame launcherFrameFake = new Frame();
				launcherFrameFake.setTitle("Minecraft");
				launcherFrameFake.setBackground(Color.BLACK);
				final JPanel panel = new JPanel();
				launcherFrameFake.setLayout(new BorderLayout());
				panel.setPreferredSize(new Dimension(854, 480));
				launcherFrameFake.add(panel, "Center");
				launcherFrameFake.pack();
				launcherFrameFake.setLocationRelativeTo(null);
				launcherFrameFake.setVisible(true);
				launcherFrameFake.addWindowListener(new WindowAdapter() {
		            @Override
		            public void windowClosing(final WindowEvent e) {
		            	applet.stop();
		                applet.destroy();
		                launcherFrameFake.setVisible(false);
		                launcherFrameFake.dispose();
		            }
		        });
				//Window.window.add(this, "Center");
				//this.setSize(Window.window.getWidth(), Window.window.getHeight());
		        //applet.setSize(Window.window.getWidth(), Window.window.getHeight());
		        launcherFrameFake.removeAll();
				launcherFrameFake.add(dys, "Center");
				launcherFrameFake.validate();
				System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	            System.setProperty("http.proxyPort", "80");
	            System.setProperty("http.proxyHost", "classic.retrocraft.net");
	            System.setProperty("java.library.path", BC.get() + "bin/natives");
	            System.setProperty("org.lwjgl.librarypath", BC.get() + "bin/natives");
		        System.setProperty("net.java.games.input.librarypath", BC.get() + "bin/natives");
				applet.setStub(dys);
				applet.setSize(launcherFrameFake.getWidth(), launcherFrameFake.getHeight());
				setLayout(new BorderLayout());
		        add(applet, "Center");
		        applet.init();
				applet.start();
				//validate();
		        //this.setVisible(true);
		        //this.validate();
			}
		};
		t.setDaemon(true);
		t.start();
		
	}

	 @Override
     public void appletResize(final int width, final int height) {
     }

	 @Override
	 public void init() {
	 }

	 @Override
	 public void update(final Graphics g) {
		 this.paint(g);
	 }

	 public void paint(final Graphics g2) {
	 }

     @Override
     public boolean isActive() {
    	 if (this.context == 0) {
             this.context = -1;
             try {
                 if (this.getAppletContext() != null) {		 
                     this.context = 1;
                 }
             }
             catch (Exception ex) {}
         }
         if (this.context == -1) {
             return true;
         }
         return super.isActive();
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
