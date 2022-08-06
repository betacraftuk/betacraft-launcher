
package org.betacraft.launcher;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;

import uk.betacraft.json.lib.ModObject;

public class Release {
	// Version list for the user
	public static ArrayList<Release> versions = new ArrayList<Release>();

	public enum VersionRepository {
		BETACRAFT("http://files.betacraft.uk/launcher/assets/version_list.txt"),
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
		ArrayList<VersionInfo> list = new ArrayList<VersionInfo>();
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

		// Exclude when overlap occurs
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
		ArrayList<String> stringlist = new ArrayList<String>();
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

			// flag just jars as custom
			if (json.hasJar() && !ReleaseJson.exists(version))
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

		public File getInfoFile() {
			return new File(BC.get() + "versions/jsons/", name + ".info");
		}

		public boolean hasJar() {
			File jar = new File(BC.get() + "versions/", name + ".jar");
			return jar.exists() && jar.isFile();
		}

		public void setEntry(String entry, String value) {}

		public DownloadResult downloadJson() {
			return Launcher.download("http://files.betacraft.uk/launcher/assets/jsons/" + this.getVersion() + ".info", getInfoFile());
		}
	}

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
			info2.getInfoFile().delete();
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
		return (this.info.isCustom() || ModsRepository.getMod(this.name) != null) ? Lang.VERSION_CUSTOM : "";
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
		// Match not found, check for not downloaded mods
		ModObject modmatch = ModsRepository.getMod(name);
		if (modmatch != null) {
			new ReleaseJson(name, modmatch.info_file_url).downloadJson();
			Release.loadVersions(VersionRepository.BETACRAFT);
			return getReleaseByName(name);
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
		public void setEntry(String entry, String value);
		public int getFileVersion();
		public boolean isCustom();
		public File getInfoFile();

		public DownloadResult downloadJson();
	}
}
