package org.betacraft.launcher;

import java.awt.Color;
import java.net.URI;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.MatteBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class WebsitePanel extends JPanel {
	JScrollPane scrollPane = null;
	private static final HyperlinkListener EXTERNAL_HYPERLINK_LISTENER = new HyperlinkListener() {
        public void hyperlinkUpdate(final HyperlinkEvent hyperlinkEvent) {
            if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    openLink(hyperlinkEvent.getURL().toURI());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };

	public WebsitePanel() {}

	public static void openLink(final URI uri) {
        try {
            final Object invoke = Class.forName("java.awt.Desktop").getMethod("getDesktop", (Class<?>[])new Class[0]).invoke(null, new Object[0]);
            invoke.getClass().getMethod("browse", URI.class).invoke(invoke, uri);
        }
        catch (Throwable t) {
            System.out.println("Failed to open link " + uri.toString());
        }
    }	

	public JScrollPane getUpdateNews() {
        if (this.scrollPane != null) {
            return this.scrollPane;
        }
        try {
            final JTextPane textPane = new JTextPane();
            textPane.setEditable(false);
            textPane.setBackground(Color.BLACK);
            textPane.setContentType("text/html;charset=UTF-8");
            String news = Integer.parseInt(System.getProperty("java.version").split("\\.")[1]) > 7 ? Lang.get("cl_loading") : Lang.get("cl_failed");
            String news1 = "<html><body><font color=\"#808080\"><br><br><br><br><br><center><h1>" + news + "</h1></center></font></body></html>";
            textPane.setText(news1);
            textPane.addHyperlinkListener(EXTERNAL_HYPERLINK_LISTENER);
            new Thread() {
                public void run() {
                    try {
                        textPane.setPage(new URL("https://betacraft.ovh/versions/changelog.html?" + Launcher.getProperty(Launcher.SETTINGS, "language")));
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                        textPane.setText("<html><body><font color=\"#808080\"><br><br><br><br><br><center><h1>" + Lang.get("cl_failed") + "</h1><br>" + ex.toString() + "</center></font></body></html>");
                    }
                }
            }.start();
            Timer t = new Timer();
            t.schedule(new TimerTask() {
            	public void run() {
            		if (textPane.getText().contains("<font color=\"#808080\"><br><br><br><br><br>")) {
            			textPane.setText("<html><body><font color=\"#808080\"><br><br><br><br><br><center><h1>" + Lang.get("cl_failed") + "</h1><br>" + Lang.get("cl_internet") + "</center></font></body></html>");
            		}
            	}
            }, 600L);
            this.scrollPane = new JScrollPane(textPane);
            this.scrollPane.setBorder(new MatteBorder(1, 1, 1, 1, Color.BLACK));
            this.scrollPane.setWheelScrollingEnabled(true);
            this.scrollPane.setBounds(20, 20, 750, 250);
            //this.scrollPane.getViewport().getView().setBackground(Color.BLACK);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return this.scrollPane;
    }
}
