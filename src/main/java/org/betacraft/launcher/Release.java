
package org.betacraft.launcher;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;

public class Release {
	// Version list for the user
	public static ArrayList<Release> versions = new ArrayList<Release>();

	// So, we have to remove already existing jsons that populate in 1.09-1.09_09,
	// because that was a stupid idea. We can't now simply change versions' names.
	// We can revert that mistake by adding a version parameter to info files.
	// If the version parameter in the file doesn't exist or is lower than the
	// hardcoded launcher one, the file gets deleted. Should do the thing, eh?

	public enum VersionRepository {
		BETACRAFT("http://betacraft.pl/launcher/assets/version_list.txt"),
		CUSTOM(null);

		private String link;

		private VersionRepository(String link) {
			setLink(link);
		}

		public String getLink() {
			return this.link;
		}

		public VersionRepository setLink(String link) {
			this.link = link;
			return this;
		}
	}

	public static void loadVersions(VersionRepository repo) {
		try {
			URL versionlisturl = new URL(repo.getLink());
			Scanner scanner = new Scanner(versionlisturl.openStream(), "UTF-8");
			while (scanner.hasNextLine()) {
				String[] versionNode = scanner.nextLine().split("`");
				VersionInfo info = ReleaseJson.exists(versionNode[0]) ? new ReleaseJson(versionNode[0]) : new NofileVersionInfo(versionNode);
				Release r = new Release(versionNode[0], info);
				versions.add(r);
			}
			scanner.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		for (VersionInfo info : offlineVersionList()) {
			Release r = new Release(info.getVersion(), info);
			versions.add(r);
		}
	}

	public static ArrayList<VersionInfo> offlineVersionList() {
		ArrayList<VersionInfo> list = new ArrayList<>();
		File versionsFolder = new File(BC.get() + "versions/");
		File fakejsonsFolder = new File(versionsFolder, "jsons/");
		// Get all representations of locally saved versions
		String[] offlinejars = versionsFolder.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
		String[] offlinefakejsons = fakejsonsFolder.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".info");
			}
		});
		// Exclude duplicates
		for (int i = 0; i < offlinejars.length; i++) {
			String jar = offlinejars[i].substring(0, offlinejars[i].length() - 4);
			for (String jsondotinfo : offlinefakejsons) {
				String json = jsondotinfo.substring(0, jsondotinfo.length() - 5);
				if (json.equals(jar)) {
					offlinejars[i] = null;
					break;
				}
			}
		}
		// Exclude online versions
		for (int i = 0; i < offlinejars.length; i++) {
			if (offlinejars[i] == null) continue;
			String jar = offlinejars[i].substring(0, offlinejars[i].length() - 4);
			for (Release r : versions) {
				if (r.getName().equals(jar)) {
					offlinejars[i] = null;
				}
			}
		}
		// Same as above, but for ver config files
		for (int i = 0; i < offlinefakejsons.length; i++) {
			if (offlinefakejsons[i] == null) continue;
			String json = offlinefakejsons[i].substring(0, offlinefakejsons[i].length() - 5);
			for (Release r : versions) {
				if (r.getName().equals(json)) {
					offlinefakejsons[i] = null;
				}
			}
		}
		// Remove extensions
		ArrayList<String> stringlist = new ArrayList<>();
		for (int i = 0; i < offlinejars.length; i++) {
			String jar = offlinejars[i];
			if (jar == null) continue;
			stringlist.add(jar.substring(0, jar.length() - 4));
		}
		for (int i = 0; i < offlinefakejsons.length; i++) {
			String json = offlinefakejsons[i];
			if (json == null) continue;
			stringlist.add(json.substring(0, json.length() - 5));
		}
		// Sort by name
		Collections.sort(stringlist);
		// Add to list
		for (String version : stringlist) {
			ReleaseJson json = new ReleaseJson(version);
			json.custom = true;
			list.add(json);
		}
		return list;
	}

	// Represents undownloaded versions
	public static class NofileVersionInfo implements VersionInfo {
		private String name;
		private String othername = "";
		private long compileTime = 0;
		private long releaseTime = 0;
		private String protocol = "";

		public NofileVersionInfo(String name) {
			this.name = name;
		}

		public NofileVersionInfo(String[] node) {
			this.name = node[0];
			this.othername = node[1];
			this.compileTime = Long.parseLong(node[2]);
			this.releaseTime = Long.parseLong(node[3]);
			try {
				this.protocol = node[4];
			} catch (ArrayIndexOutOfBoundsException ex) {}
		}

		public String getVersion() {
			return this.name;
		}

		public int getFileVersion() {
			return Util.jsonVersion; // current default one, it should be the same as in the version's configuration file
		}

		public String getOtherName() {
			return this.othername;
		}

		public Date getCompileDate() {
			return new Date(this.compileTime);
		}

		public Date getReleaseDate() {
			return new Date(this.releaseTime);
		}

		public String getProxyArgs() {
			return "";
		}

		public String getLaunchMethod() {
			return "";
		}

		public String getLaunchMethodURL() {
			return "";
		}

		public String getDownloadURL() {
			return "";
		}

		public String getProtocol() {
			return this.protocol;
		}

		public String getEntry(String entry) {
			return "";
		}

		public boolean isCustom() {
			return false; // it's a version from an online repository! not local one!
		}

		public boolean hasJar() {
			File jar = new File(BC.get() + "versions/", name + ".jar");
			return jar.exists() && jar.isFile();
		}

		public boolean hasFakeJson() {
			File fakeJson = new File(BC.get() + "versions/jsons/", name + ".info");
			return fakeJson.exists() && fakeJson.isFile();
		}
	}

	/*public static void initVersions() throws IOException {
		versions.clear();
		//Launcher.download("https://betacraft.pl/launcher/assets/jsons.zip", new File(BC.get() + "versions" + File.separator + "jsons" + File.separator + "$jsons.zip"));
		//Launcher.totalThreads.add(Util.Unrar(new File(BC.get() + "versions" + File.separator + "jsons" + File.separator + "$jsons.zip").toPath().toString(), new File(BC.get() + "versions" + File.separator + "jsons" + File.separator).toPath().toString(), false));
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
		} catch (Throwable ex) {
			Logger.a("A critical error occurred while initializing versions list!");
			ex.printStackTrace();
			Logger.printException(ex);

			// This should likely never happen, so the user has found a new bug! Yay!
			JOptionPane.showMessageDialog(null, "An error occurred while loading versions list! Update Java! Or, if it doesn't help, contact @Moresteck#1688", "Critical error!", JOptionPane.ERROR_MESSAGE);
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
			Util.write(new File(folder, "version_index"), filecontent, false);
		}
		return results;
	}*/

	private String name;
	private VersionInfo info;

	public Release(String name) {
		this(name, new ReleaseJson(name));
	}

	public Release(String name, VersionInfo info) {
		this.name = name;
		if (info.getFileVersion() < Util.jsonVersion && info.getFileVersion() != -1 && info instanceof ReleaseJson && ReleaseJson.exists(name)) {
			// Terminate all outdated info files
			ReleaseJson info2 = (ReleaseJson) info;
			info2.json.delete();
			Logger.a("Terminated an outdated info file of: " + name);
			info = new ReleaseJson(name);
		}
		this.info = info;
	}

	public String getName() {
		return this.name;
	}

	public VersionInfo getInfo() {
		return this.info;
	}

	public void setInfo(VersionInfo info) {
		this.info = info;
	}

	public String customSuffix() {
		return this.info.isCustom() ? Lang.VERSION_CUSTOM : "";
	}

	public String toString() {
		if (this.info.getOtherName() != null && !"".equals(this.info.getOtherName()))
			return this.name + " (" + this.info.getOtherName() + ")" + this.customSuffix();
		return this.name + this.customSuffix();
	}

	public static Release getReleaseByName(String name) {
		for (Release r: versions) {
			if (r.getName().equals(name)) {
				return r;
			}
		}
		return null;
	}

	public interface VersionInfo {
		public String getOtherName();
		public String getVersion();
		public Date getCompileDate();
		public Date getReleaseDate();
		public String getProxyArgs();
		public String getLaunchMethod();
		public String getLaunchMethodURL();
		public String getDownloadURL();
		public String getProtocol();
		public String getEntry(String entry);
		default void setEntry(String entry, String value) {};
		public int getFileVersion();
		default boolean isCustom() {
			return false;
		}

		default void downloadJson() {
			Launcher.download("http://betacraft.pl/launcher/assets/jsons/" + this.getVersion() + ".info", new File(BC.get() + "versions" + File.separator + "jsons", this.getVersion() + ".info"));
		}
	}
}
