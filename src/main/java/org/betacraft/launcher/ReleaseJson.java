package org.betacraft.launcher;

import java.io.File;
import java.util.Date;

import org.betacraft.launcher.Release.VersionInfo;
import org.betacraft.launcher.Util.PropertyFile;

public class ReleaseJson implements VersionInfo {
	private long releaseTimestamp = 0;
	private long timestamp = 0;
	private String download = "";
	private String launchMethod = "";
	private String launchMethodLink = "";
	private String proxyArgs = "-Dhttp.proxyHost=betacraft.uk"; // keep this as default
	private String otherName = "";
	private String protocol = "";
	private int fileVersion = -1;
	protected boolean custom = false;

	private final String version;
	private final File json;
	private final PropertyFile properties;

	private String jsonUrl = null;
	public String sha1 = null;
	public String baseVersion = null;

	public ReleaseJson(String version) {
		this(version, null);
	}

	public ReleaseJson(String version, String url) {
		this.version = version;
		this.json = new File(BC.get() + "versions" + File.separator + "jsons", version + ".info");
		this.properties = new PropertyFile(json);
		
		if (url == null) {
			this.jsonUrl = "http://files.betacraft.uk/launcher/assets/jsons/" + this.getVersion() + ".info";
		} else {
			this.jsonUrl = url;
		}

		this.jsonUrl = this.jsonUrl.replace(" ", "%20");

		readJson();
	}

	public void readJson() {
		try {
			String releaseDate = properties.getProperty("release-date");
			String compileDate = properties.getProperty("compile-date");
			try {
				releaseTimestamp = Long.parseLong(releaseDate);
			} catch (Throwable ex) {}

			try {
				timestamp = Long.parseLong(compileDate);
			} catch (Throwable ex) {}

			download = properties.getProperty("url");
			launchMethod = properties.getProperty("launch-method");
			launchMethodLink = properties.getProperty("launch-method-link");

			String proxy = properties.hasProperty("proxy-args") ? properties.getProperty("proxy-args") : proxyArgs;
			proxyArgs = proxy;
			otherName = properties.getProperty("other-name");
			protocol = properties.getProperty("protocolVersion");
			sha1 = properties.getProperty("sha1");
			baseVersion = properties.getProperty("base-version");

			String custom_flag_str = properties.getProperty("custom");
			if (custom_flag_str != null) {
				try {
					custom = Boolean.parseBoolean(custom_flag_str);
				} catch (Throwable t) {
					System.err.println("Version " + this.version + " has an invalid `custom` parameter.");
				}
			} // if there is no 'custom' property, the version must be clean

			String file_ver = properties.getProperty("file-ver");
			if (file_ver != null) {
				try {
					fileVersion = Integer.parseInt(file_ver);
				} catch (Throwable t) {
					System.err.println("Version " + this.version + " has an invalid `file-ver` parameter.");
					if (!properties.hasProperty("file-ver")) {
						this.fileVersion = 0;
					}
				}
			} // if 'file-ver' is invalid, leave it at -1
		} catch (Exception ex) {
			ex.printStackTrace();
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
		return properties.getProperty(entryname);
	}

	public void setEntry(String entry, String value) {
		properties.setProperty(entry, value);
		properties.flushToDisk();
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

	public File getInfoFile() {
		return json;
	}

	public static boolean exists(String name) {
		return new File(BC.get() + "versions/jsons/", name + ".info").exists();
	}

	public DownloadResult downloadJson() {
		return Launcher.download(this.jsonUrl, getInfoFile());
	}

	public File getJar() {
		return new File(BC.get() + "versions/", version + ".jar");
	}

	public boolean hasJar() {
		File jar = getJar();
		return jar.exists() && jar.isFile() && !jar.isDirectory();
	}
}
