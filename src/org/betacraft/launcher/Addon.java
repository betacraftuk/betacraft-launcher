package org.betacraft.launcher;

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

public class Addon {
	public String name;
	public boolean online;
	public JEditorPane info;

	public Addon(String name, boolean online) {
		this.name = name;
		this.online = online;
		this.info = getInfo();
	}

	private JEditorPane getInfo() {
		JEditorPane pane = new JEditorPane();
		pane.setEditable(false);
		pane.setOpaque(false);
		pane.setContentType("text/html;charset=UTF-8");
		pane.addHyperlinkListener(WebsitePanel.EXTERNAL_HYPERLINK_LISTENER);
		try {
			pane.setPage(new URL("https://betacraft.pl/launcher/assets/addons/" + this.name + ".html"));
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
			pane.setText(Lang.ADDON_NO_DESC);
		}
		return pane;
	}

	public static ArrayList<Addon> addons = new ArrayList<Addon>();

	public static void loadAddons() {
		try {
			String[] offlineAddons = new File(BC.get() + "launcher" + File.separator + "addons" + File.separator).list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String fileName) {
					return fileName.endsWith(".class");
				}
			});

			final URL url = new URL("https://betacraft.pl/launcher/assets/addons/list.txt");

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
				JOptionPane.showMessageDialog(null, "An error occurred while loading addons list! Report this to: @Moresteck#1688", "Critical error!", JOptionPane.ERROR_MESSAGE);
			}

			// If connection failed, load the offline list
			if (onlineListStream == null) {
				for (String s : offlineAddons) {
					addons.add(new Addon(s.substring(0, s.length() -6), false));
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
						if (offlineAddons[i].substring(0, offlineAddons[i].length() -6).equals(ver)) {
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
				addons.add(new Addon(offlineAddons[i].substring(0, offlineAddons[i].length() -6), false));
			}

			// Close the connection
			onlineListScanner.close();
			onlineListStream.close();
		} catch (Exception ex) {
			Logger.a("A critical error occurred while initializing addons list!");
			ex.printStackTrace();
			Logger.printException(ex);

			JOptionPane.showMessageDialog(null, "An error occurred while loading addons list! Report this to: @Moresteck#1688", "Critical error!", JOptionPane.ERROR_MESSAGE);
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
