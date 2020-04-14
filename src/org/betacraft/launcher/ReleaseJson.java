package org.betacraft.launcher;

import java.io.File;
import java.util.Date;

public class ReleaseJson {
	private long releaseTimestamp = 0;
	private long timestamp = 0;
	private String download = "";
	private String launchMethod = "";
	private String launchMethodLink = "";
	private String proxyArgs = "";
	private String otherName = "";
	private String version;
	private File json;

	public boolean online;
	public boolean custom;

	public ReleaseJson(String version, boolean online, boolean custom) {
		this.version = version;
		this.online = online;
		this.custom = custom;
		initJson();
	}

	public void initJson() {
		try {
			if (this.custom && (!jsonExists() || Launcher.forceUpdate)) downloadJson();
			json = new File(BC.get() + "versions" + File.separator + "jsons", version + ".info");
			String[] toSet = Launcher.excludeExistant(json, new String[] {"release-date", "compile-date", "url", "launch-method", "launch-method-link", "proxy-args", "other-name"}, "UTF-8");
			for (String set : toSet) {
				if (set != null) {
					if (set.equals("release-date")) Launcher.setProperty(json, set, Long.toString(releaseTimestamp));
					else if (set.equals("compile-date")) Launcher.setProperty(json, set, Long.toString(timestamp));
					else if (set.equals("launch-method")) Launcher.setProperty(json, set, launchMethod);
					else if (set.equals("proxy-args")) Launcher.setProperty(json, set, proxyArgs);
					else Launcher.setProperty(json, set, "");
				}
			}

			releaseTimestamp = Long.parseLong(Launcher.getProperty(json, "release-date"));
			timestamp = Long.parseLong(Launcher.getProperty(json, "compile-date"));
			download = Launcher.getProperty(json, "url");
			launchMethod = Launcher.getProperty(json, "launch-method");
			launchMethodLink = Launcher.getProperty(json, "launch-method-link");
			proxyArgs = Launcher.getProperty(json, "proxy-args");
			otherName = Launcher.getProperty(json, "other-name");
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	public boolean jsonExists() {
		try {
			File json = new File(BC.get() + "versions" + File.separator + "jsons", version + ".info");
			if (json.exists() && !json.isDirectory() && json.length() > 100) {
				return true;
			}
			return false;
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
			return false;
		}
	}

	public void downloadJson() {
		if (this.online) Launcher.download("https://betacraft.pl/launcher/assets/jsons/" + version + ".info", new File(BC.get() + "versions" + File.separator + "jsons", version + ".info"));
	}

	public String getVersion() {
		return this.version;
	}

	public String getCompileDate() {
		return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date(this.timestamp));
	}

	public String getReleaseDate() {
		return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date(this.releaseTimestamp));
	}

	public long getReleaseTimestamp() {
		return this.releaseTimestamp;
	}

	public String getOtherName() {
		return this.otherName;
	}

	public String getProxyArgs() {
		return this.proxyArgs;
	}

	public String getLaunchMethod() {
		return this.launchMethod;
	}

	public String getLaunchMethodLink() {
		return this.launchMethodLink;
	}

	public String getCustomEntry(String entryname) {
		return Launcher.getProperty(json, entryname);
	}

	public void downloadLaunchMethod() {
		Launcher.download(this.launchMethodLink, new File(BC.get() + "launcher" + File.separator + "launch-methods", launchMethod + ".class"));
	}

	public String getDownloadURL() {
		return this.download;
	}

	public void download() {
		Launcher.download(this.download, new File(BC.get() + "versions", version + ".jar"));
	}
}
