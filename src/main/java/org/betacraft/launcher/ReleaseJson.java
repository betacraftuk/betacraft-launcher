package org.betacraft.launcher;

import java.io.File;
import java.util.Date;

import org.betacraft.launcher.Release.VersionInfo;

public class ReleaseJson implements VersionInfo {
	private long releaseTimestamp = 0;
	private long timestamp = 0;
	private String download = "";
	private String launchMethod = "";
	private String launchMethodLink = "";
	private String proxyArgs = "-Dhttp.proxyHost=betacraft.pl"; // keep this as default
	private String otherName = "";
	private String protocol = "";
	private int fileVersion = -1;
	private final String version;
	public final File json;

	public boolean custom = false;

	public ReleaseJson(String version) {
		this.version = version;
		this.json = new File(BC.path() + "versions" + File.separator + "jsons", version + ".info");
		readJson();
	}

	public void readJson() {
		try {
			String releaseDate = Util.getProperty(json, "release-date");
			String compileDate = Util.getProperty(json, "compile-date");
			try {
				releaseTimestamp = Long.parseLong(releaseDate);
			} catch (Throwable ex) {}

			try {
				timestamp = Long.parseLong(compileDate);
			} catch (Throwable ex) {}

			download = Util.getProperty(json, "url");
			launchMethod = Util.getProperty(json, "launch-method");
			launchMethodLink = Util.getProperty(json, "launch-method-link");

			String proxy = Util.hasProperty(json, "proxy-args", "UTF-8") ? Util.getProperty(json, "proxy-args") : proxyArgs;
			proxyArgs = proxy;
			otherName = Util.getProperty(json, "other-name");
			protocol = Util.getProperty(json, "protocolVersion");

			String custom_flag_str = Util.getProperty(json, "custom");
			if (custom_flag_str != null) {
				try {
					custom = Boolean.parseBoolean(custom_flag_str);
				} catch (Throwable t) {
					Logger.a("Version " + this.version + " has an invalid `custom` parameter.");
				}
			} // if there is no 'custom' property, the version must be clean

			String file_ver = Util.getProperty(json, "file-ver");
			if (file_ver != null) {
				try {
					fileVersion = Integer.parseInt(file_ver);
				} catch (Throwable t) {
					Logger.a("Version " + this.version + " has an invalid `file-ver` parameter.");
					if (!Util.hasProperty(json, "file-ver", "UTF-8")) {
						this.fileVersion = 0;
					}
				}
			} // if 'file-ver' is not set, leave it at -1
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	public String getVersion() {
		return this.version;
	}

	public Date getCompileDate() {
		return new Date(this.timestamp);
	}

	public Date getReleaseDate() {
		return new Date(this.releaseTimestamp);
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

	public String getLaunchMethodURL() {
		return this.launchMethodLink;
	}

	public String getEntry(String entryname) {
		return Util.getProperty(json, entryname);
	}

	public void setEntry(String entry, String value) {
		Util.setProperty(json, entry, value);
		this.readJson();
	}

	public String getDownloadURL() {
		return this.download;
	}

	public String getProtocol() {
		return this.protocol;
	}

	public int getFileVersion() {
		return this.fileVersion;
	}

	public boolean isCustom() {
		return this.custom;
	}

	public static boolean exists(String name) {
		return new File(BC.path() + "versions/jsons/", name + ".info").exists();
	}
}
