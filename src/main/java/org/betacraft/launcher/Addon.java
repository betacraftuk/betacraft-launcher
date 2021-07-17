package org.betacraft.launcher;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

public class Addon {
	public static String addonVer = "1.09_13";
	public String name;
	public String onlinehash;
	public boolean online;
	public JScrollPane info;

	// When you click on "Show info" button, you get a new window popping,
	// with just the page embed about the addon. Same for mods. TODO

	public Addon(String name, String hash, boolean online) {
		this.name = name;
		this.onlinehash = hash;
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
			pane.setPage(new URL("http://files.betacraft.pl/launcher/assets/addons/" + addonVer + "/" + this.name + ".html"));
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

	public static HashMap<String, Addon> addons = new HashMap<String, Addon>();

	public static void loadAddons() {
		try {
			String[] offlineAddons = new File(BC.path() + "launcher" + File.separator + "addons" + File.separator).list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String fileName) {
					return fileName.endsWith(".jar");
				}
			});

			final URL url = new URL("http://files.betacraft.pl/launcher/assets/addons/" + addonVer + "/list.txt");

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
					String name = s.substring(0, s.length() -4);
					addons.put(name, new Addon(name, null, false));
				}
				return;
			}

			// Scan the offline list for online duplicates,
			Scanner onlineListScanner = new Scanner(onlineListStream, "UTF-8");
			for (String ver : scan(onlineListScanner)) {
				if (ver == null) continue;
				String[] split = ver.split("`");
				String addonname = split[0];
				String addonhash = split[1];

				for (int i = 0; i < offlineAddons.length; i++) {
					if (offlineAddons[i] != null) {
						// From x.class to x
						// If the addon from offline list matches the addon from online list 
						if (offlineAddons[i].substring(0, offlineAddons[i].length() -4).equals(addonname)) {
							// ... Then remove it from the offline addons list
							// Otherwise it would appear doubled in the list
							offlineAddons[i] = null;
						}
					}
				}

				// Add the online addon to the addons list
				addons.put(addonname, new Addon(addonname, addonhash, true));
			}

			// Add offline addons to the addons list
			for (int i = 0; i < offlineAddons.length; i++) {
				// Skip previously removed duplicates
				if (offlineAddons[i] == null) continue;
				String name = offlineAddons[i].substring(0, offlineAddons[i].length() -4);
				addons.put(name, new Addon(name, null, false));
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
