package org.betacraft.launcher;

import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.MatteBorder;

public class Addon {
	public String name;
	public boolean online;
	public JScrollPane info;

	// When you click on "Show info" button, you get a new window popping,
	// with just the page embed about the addon. Same for mods. TODO

	public Addon(String name, boolean online) {
		this.name = name;
		this.online = online;
		this.info = getInfo();
	}

	public JScrollPane getInfo() {
		JEditorPane pane = new JEditorPane();
		pane.setEditable(false);
		pane.setOpaque(false);
		pane.setContentType("text/html;charset=UTF-8");
		pane.addHyperlinkListener(WebsitePanel.EXTERNAL_HYPERLINK_LISTENER);
		try {
			pane.setPage(new URL("http://files.betacraft.pl/launcher/assets/addons/" + this.name + ".html"));
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
			pane.setText(Lang.ADDON_NO_DESC);
		}
		JScrollPane scrlPane = new JScrollPane(pane);
		scrlPane.setBorder(null);
		scrlPane.setWheelScrollingEnabled(true);
		return scrlPane;
	}

	public static ArrayList<Addon> addons = new ArrayList<Addon>();

	public static void loadAddons() {
		try {
			String[] offlineAddons = new File(BC.get() + "launcher" + File.separator + "addons" + File.separator).list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String fileName) {
					return fileName.endsWith(".jar");
				}
			});

			final URL url = new URL("http://files.betacraft.pl/launcher/assets/addons/1.09_10/list.txt");

			InputStream onlineListStream = null;
			try {
				onlineListStream = url.openStream();
			} catch (UnknownHostException ex) {
				Logger.a(null);
			} catch (SocketTimeoutException ex) {
				Logger.a(null);
			} catch (SocketException ex) {
				Logger.a(null);
			} catch (Exception ex) {
				Logger.a("A critical error has occurred while attempting to get the online addons list!");
				ex.printStackTrace();
				Logger.printException(ex);

				// Every networking bug has been catched before, so this one must be serious
			}

			// If connection failed, load the offline list
			if (onlineListStream == null) {
				for (String s : offlineAddons) {
					addons.add(new Addon(s.substring(0, s.length() -4), false));
				}
				return;
			}

			// Scan the offline list for online duplicates,
			Scanner onlineListScanner = new Scanner(onlineListStream, "UTF-8");
			for (String ver : scan(onlineListScanner)) {

				for (int i = 0; i < offlineAddons.length; i++) {
					if (offlineAddons[i] != null && ver != null) {
						// From x.class to x
						// If the addon from offline list matches the addon from online list 
						if (offlineAddons[i].substring(0, offlineAddons[i].length() -4).equals(ver)) {
							// ... Then remove it from the offline addons list
							// Otherwise it would appear doubled in the list
							offlineAddons[i] = null;
						}
					}
				}

				// Add the online addon to the addons list
				addons.add(new Addon(ver, true));
			}

			// Add offline addons to the addons list
			for (int i = 0; i < offlineAddons.length; i++) {
				// Skip previously removed duplicates
				if (offlineAddons[i] == null) continue;
				addons.add(new Addon(offlineAddons[i].substring(0, offlineAddons[i].length() -4), false));
			}

			// Close the connection
			onlineListScanner.close();
			onlineListStream.close();
		} catch (Exception ex) {
			Logger.a("A critical error occurred while initializing addons list!");
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	protected static List<String> scan(Scanner scanner) {
		List<String> results = new ArrayList<String>();

		String currentLine = null;
		while (scanner.hasNextLine()) {
			currentLine = scanner.nextLine();

			// If the line is empty, ignore it
			if (currentLine.equalsIgnoreCase("")) continue;

			results.add(currentLine);
		}
		return results;
	}
}
