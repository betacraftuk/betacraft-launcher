
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
import java.util.Collections;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class Release {
	// Version list for the user
	public static ArrayList<Release> versions = new ArrayList<Release>();

	public static void initVersions() throws IOException {
		versions.clear();
		Launcher.download("https://betacraft.pl/launcher/assets/jsons.zip", new File(BC.get() + "versions" + File.separator + "jsons" + File.separator + "$jsons.zip"));
		Launcher.Unrar(new File(BC.get() + "versions" + File.separator + "jsons" + File.separator + "$jsons.zip").toPath().toString(), new File(BC.get() + "versions" + File.separator + "jsons" + File.separator).toPath().toString(), false);
		String[] offlineVersions = getOfflineVersions();

		try {
			final URL url = new URL("https://betacraft.pl/launcher/assets/list.txt");

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
				Logger.printException(ex);

				// Every networking bug has been catched before, so this one must be serious
				JOptionPane.showMessageDialog(null, "An error occurred while loading versions list! Report this to: @Moresteck#1688", "Critical error!", JOptionPane.ERROR_MESSAGE);
			}

			// If connection failed, load the offline list
			if (onlineListStream == null) {
				loadOfflineList();
				return;
			}

			// Scan the offline list for online duplicates,
			// and update the offline list at the same time (true)
			Scanner onlineListScanner = new Scanner(onlineListStream, "UTF-8");
			for (String ver : scan(onlineListScanner, true)) {
				// The version storing format goes like this:
				// Name [0], timestamp [1], description [3], wiki link [4]
				//String[] split = ver.split("~");
				for (int i = 0; i < offlineVersions.length; i++) {
					// Check if the version info is valid
					if (offlineVersions[i] != null && ver != null) {
						// From x.jar to x
						// If the version from offline list matches the version from online list 
						if (offlineVersions[i].equals(ver)) {
							// ... Then remove it from the offline versions list
							// Otherwise it would appear doubled in the versions list
							offlineVersions[i] = null;
						}
					}
				}

				// Add the online version to the versions list
				versions.add(new Release(ver, true, false));
			}

			// Add offline versions to the version list
			for (int i = 0; i < offlineVersions.length; i++) {
				// Skip previously removed duplicates
				if (offlineVersions[i] == null) continue;
				versions.add(new Release(offlineVersions[i], false, true));
			}

			// Close the connection
			onlineListScanner.close();
			onlineListStream.close();
		} catch (Exception ex) {
			Logger.a("A critical error occurred while initializing versions list!");
			ex.printStackTrace();
			Logger.printException(ex);

			// This should likely never happen, so the user has found a new bug! Yay!
			JOptionPane.showMessageDialog(null, "An error occurred while loading versions list! Report this to: @Moresteck#1688", "Critical error!", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected static String[] getOfflineVersions() {
		// Get the versions folder
		File file = new File(BC.get() + "versions" + File.separator);
		File file1 = new File(BC.get() + "versions" + File.separator + "jsons" + File.separator);

		// Take only files that are of jar type
		String[] offlineVersions = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fileName) {
				return fileName.endsWith(".jar");
			}
		});
		String[] offlineJsons = file1.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fileName) {
				return fileName.endsWith(".info");
			}
		});
		String[] total = new String[offlineVersions.length + offlineJsons.length];
		int index = 0;
		for (String s1 : offlineVersions) {
			s1 = s1.substring(0, s1.length() - 4);
			total[index] = s1;
			index++;
		}
		for (String s1 : offlineJsons) {
		    s1 = s1.substring(0, s1.length() - 5);
			total[index] = s1;
			index++;
		}
		ArrayList<String> tosort = new ArrayList<String>();
		for (String s2 : total) {
			tosort.add(s2);
		}
		Collections.sort(tosort);
		for (int i = 0; i < tosort.size(); i++) {
			if (i != 0 && tosort.get(i - 1).equals(tosort.get(i))) continue; // Prevent duplicates
			total[i] = tosort.get(i);
		}
		return total;
	}

	private static void loadOfflineList() {
		String[] offlineVersions = getOfflineVersions();
		try {
			// Scan the offline version list, but don't update the file (false)
			Scanner fileScanner = new Scanner(new File(BC.get() + "launcher" + File.separator + "version_index"), "UTF-8");
			ArrayList<String> offlineVersionsList = scan(fileScanner, false);

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
						if (offlineVersions[i].equals(split[0])) {
							// ... Then remove it from the offline versions list
							// Otherwise it would appear doubled in the versions list
							offlineVersions[i] = null;
							addToList = true;
						}
					}
				}
				if (!addToList) continue;
				versions.add(new Release(split[0], false, false));
			}

			// Add offline versions to the versions list
			for (int i = 0; i < offlineVersions.length; i++) {
				// Skip previously removed duplicates
				if (offlineVersions[i] == null) continue;
				versions.add(new Release(offlineVersions[i], false, true));
			}

			// Close the file
			fileScanner.close();
		} catch (Exception ex) {
			Logger.a("An error occurred while loading versions list from file!");
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	protected static ArrayList<String> scan(Scanner scanner, boolean save) {
		ArrayList<String> results = new ArrayList<String>();
		String folder = BC.get() + "launcher" + File.separator;
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

			if (save) filecontent[i] = currentLine;

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
	private ReleaseJson json;
	private boolean custom;

	public Release(String name, boolean online, boolean custom) {
		this.name = name;
		this.custom = custom;
		this.json = new ReleaseJson(name, online, custom);
	}

	public String getName() {
		return this.name;
	}

	public ReleaseJson getJson() {
		return this.json;
	}

	public boolean isCustom() {
		return this.custom;
	}

	private String customPrefix() {
		return this.custom ? Lang.VERSION_CUSTOM : "";
	}

	public String toString() {
		if (!this.json.getOtherName().equals(""))
			return this.customPrefix() + this.name + " (" + this.json.getOtherName() + ")";
		return this.customPrefix() + this.name;
	}

	public static Release getReleaseByName(String name) {
		for (Release r: versions) {
			if (r.getName().equals(name)) {
				return r;
			}
		}
		return null;
	}
}
