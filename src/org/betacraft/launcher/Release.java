
package org.betacraft.launcher;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class Release {
	// Version list for the user
	public static List<Release> versions = new LinkedList<Release>();

	public static void initVersions() throws IOException {
		String[] offlineVersions = getOfflineVersions();

		try {
			final URL url = new URL("http://betacraft.pl/version_index");

			InputStream onlineListStream = null;
			try {
				// Try to get the online version list
				onlineListStream = url.openStream();
			} catch (UnknownHostException ex) {
				Logger.a(null);
			} catch (SocketTimeoutException ex) {
				Logger.a(null);
			} catch (SocketException ex) {
				Logger.a(null);
			} catch (Exception ex) {
				Logger.a("A critical error has occurred while attempting to get the online versions list!");
				ex.printStackTrace();

				// Every networking bug has been catched before, so this one must be serious
				JOptionPane.showMessageDialog(null, "An error occurred while loading version list! Report this to: @Moresteck#1688", "Critical error!", JOptionPane.ERROR_MESSAGE);
			}

			// If connection failed, load the offline list
			if (onlineListStream == null) {
				loadOfflineList();
				return;
			}

			// Scan the offline list for online duplicates,
			// and update the offline list at the same time (true)
			Scanner onlineListScanner = new Scanner(onlineListStream);
			for (String ver : scan(onlineListScanner, true)) {
				// The version storing format goes like this:
				// Name [0], release date [1], release time [2], description [3], wiki link [4]
				String[] split = ver.split("~");
				for (int i = 0; i < offlineVersions.length; i++) {
					// Check if the version info is valid
					if (offlineVersions[i] != null && split[0] != null) {
						// From x.jar to x
						// If the version from offline list matches the version from online list 
						if (offlineVersions[i].substring(0, offlineVersions[i].length() - 4).equals(split[0])) {
							// ... Then remove it from the offline versions list
							// Otherwise it would appear double in the version list
							offlineVersions[i] = null;
						}
					}
				}

				// Add the online version to the version list
				versions.add(new Release(split[0], split[1], split[2], split[3], split[4]));
			}

			// Add offline versions to the version list
			for (int i = 0; i < offlineVersions.length; i++) {
				// Skip previously removed duplicates
				if (offlineVersions[i] == null) continue;
				versions.add(new Release(offlineVersions[i].substring(0, offlineVersions[i].length() - 4), "", "", "", null));
			}

			// Close the connection
			onlineListScanner.close();
			onlineListStream.close();
		} catch (Exception ex) {
			Logger.a("A critical error occurred while initializing versions list!");
			ex.printStackTrace();

			// This should likely never happen, so the user has found a new bug! Yay!
			JOptionPane.showMessageDialog(null, "An error occurred while loading version list! Report this to: @Moresteck#1688", "Critical error!", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected static String[] getOfflineVersions() {
		// Get the versions folder
		File file = new File(BC.get() + "versions/");

		// Take only files that are of jar type
		String[] offlineVersions = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fileName) {
				return fileName.endsWith(".jar");
			}
		});
		return offlineVersions;
	}

	protected static void loadOfflineList() {
		String[] offlineVersions = getOfflineVersions();
		try {
			// Scan the offline version list, but don't update the file (false)
			Scanner fileScanner = new Scanner(new File(BC.get() + "launcher/version_index"));
			List<String> offlineVersionsList = scan(fileScanner, false);

			for (String r: offlineVersionsList) {
				// The version storing format goes like this:
				// Name [0], release date [1], release time [2], description [3], wiki link [4]
				String[] split = r.split("~");
				boolean addToList = false;
				for (int i = 0; i < offlineVersions.length; i++) {
					// Check if the version info is valid
					if (offlineVersions[i] != null && split[0] != null) {
						// From x.jar to x
						// If the version from offline list matches the version from online list
						if (offlineVersions[i].substring(0, offlineVersions[i].length() - 4).equals(split[0])) {
							// ... Then remove it from the offline versions list
							// Otherwise it would appear double in the version list
							offlineVersions[i] = null;
							addToList = true;
						}
					}
				}
				if (!addToList) continue;
				versions.add(new Release(split[0], split[1], split[2], split[3], split[4]));
			}

			// Add offline versions to the version list
			for (int i = 0; i < offlineVersions.length; i++) {
				// Skip previously removed duplicates
				if (offlineVersions[i] == null) continue;
				versions.add(new Release(offlineVersions[i].substring(0, offlineVersions[i].length() - 4), "", "", "", null));
			}

			// Close the file
			fileScanner.close();
		} catch (Exception ex) {
			Logger.a("An error occurred while loading versions list from file!");
			ex.printStackTrace();
		}
	}

	protected static List<String> scan(Scanner scanner, boolean save) {
		List<String> results = new ArrayList<String>();
		String folder = BC.get() + "launcher/";
		String[] filecontent = new String[400];
		int i = 1;

		String currentLine = null;
		while (scanner.hasNextLine()) {
			currentLine = scanner.nextLine();

			// If the line is empty, ignore it
			if (currentLine.equalsIgnoreCase("")) continue;

			// If the line is the launcher version line, ignore it
			if (currentLine.startsWith("launcher:")) continue;

			// If we reached a limit of lines, break the cycle
			if (i == 400) {
				Logger.a("Scanner lines overflow! Skipping all next entries.");
				break;
			}

			// If we've chosen to save the list to file, add the current line to the array
			if (save) {
				filecontent[i] = currentLine;
			}

			// Add the current line to result list
			results.add(currentLine);
			i++;
		}

		// Save the list to the file if we've chosen to
		if (save) {
			Launcher.write(new File(folder, "version_index"), filecontent, false);
		}
		return results;
	}

	private String name;
	private String date;
	private String time;
	private String desc;
	private String wikilink;

	public Release(String name, String date, String time, String description, String wikilink) {
		this.name = name;
		this.date = date;
		this.time = time;
		this.desc = description;
		this.wikilink = wikilink;
	}

	public String getName() {
		return this.name;
	}

	public String getDate() {
		return this.date;
	}

	public String getTime() {
		return this.time;
	}

	public String getDescription() {
		return this.desc;
	}

	public URL getWikiLink() {
		if (wikilink == null) // Avoid NPE
			return null;
		try {
			return new URL("https://minecraft.gamepedia.com/" + this.wikilink);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public boolean hasSpecialName() {
		return !this.toString().equals(this.getName());
	}

	public String getSpecialName() {
		if (this.name.equalsIgnoreCase("inf-20100618")) {
			return "Seecret Friday 1";
		}
		if (this.name.equalsIgnoreCase("inf-20100625-2")) {
			return "Seecret Friday 2";
		}
		if (this.name.equalsIgnoreCase("a1.0.1")) {
			return "Seecret Friday 3";
		}
		if (this.name.equalsIgnoreCase("a1.0.4")) {
			return "Seecret Friday 4";
		}
		if (this.name.equalsIgnoreCase("a1.0.6")) {
			return "Seecret Friday 5";
		}
		if (this.name.equalsIgnoreCase("a1.0.11")) {
			return "Seecret Friday 6";
		}
		if (this.name.equalsIgnoreCase("a1.0.14-1")) {
			return "Seecret Friday 7";
		}
		if (this.name.equalsIgnoreCase("a1.0.17")) {
			return "Seecret Friday 8";
		}
		if (this.name.equalsIgnoreCase("a1.1.0-1")) {
			return "Seecret Friday 9";
		}
		if (this.name.equalsIgnoreCase("a1.1.1")) {
			return "Seecret Saturday";
		}
		if (this.name.equalsIgnoreCase("inf-20100630-2")) {
			return "Alpha v1.0.0";
		}
		if (this.name.equalsIgnoreCase("a1.2.0")) {
			return "Halloween Update";
		}
		if (this.name.equalsIgnoreCase("b1.8")) {
			return "Adventure Update";
		} else {
			return "";
		}
	}

	@Override
	public String toString() {
		if (this.getSpecialName().equals("")) {
			return this.name;
		}
		return this.name + " (" + this.getSpecialName() + ")";
	}
}
