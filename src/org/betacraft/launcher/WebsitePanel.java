package org.betacraft.launcher;

import java.awt.Color;
import java.net.URI;
import java.net.URL;

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
            String news = Integer.parseInt(System.getProperty("java.version").split("\\.")[1]) > 7 ? "Loading update news..." : "Can't view update news in Java 7 and lower.";
            textPane.setText("<html><body><font color=\"#808080\"><br><br><br><br><br><br><br><center><h1>" + news + "</h1></center></font></body></html>");
            textPane.addHyperlinkListener(EXTERNAL_HYPERLINK_LISTENER);
            new Thread() {
                public void run() {
                    try {
                        textPane.setPage(new URL("https://betacraft.ovh/versions/changelog.html?" + Launcher.getProperty(Launcher.SETTINGS, "language")));
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                        textPane.setText("<html><body><font color=\"#808080\"><br><br><br><br><br><br><br><center><h1>Failed to update news</h1><br>" + ex.toString() + "</center></font></body></html>");
                    }
                }
            }.start();
            this.scrollPane = new JScrollPane(textPane);
            this.scrollPane.setBorder(new MatteBorder(2, 2, 2, 2, Color.BLACK));
            this.scrollPane.setWheelScrollingEnabled(true);
            this.scrollPane.setBounds(30, 20, 750, 250);
            this.scrollPane.getViewport().getView().setBackground(Color.BLACK);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return this.scrollPane;
    }
}
