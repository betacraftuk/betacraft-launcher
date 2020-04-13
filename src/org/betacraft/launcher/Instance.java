package org.betacraft.launcher;

import java.awt.Image;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class Instance {

	public final String name;
	public String version;
	public String launchArgs;
	public int width;
	public int height;
	public boolean proxy;
	public boolean keepopen;
	public boolean RPC;
	public List<String> addons;
	public String gameDir;

	private Instance(String name, String launchArgs, String version, int width, int height, boolean proxy, boolean keepopen, boolean RPC, List<String> addons, String gameDir) {
		this.name = name;
		this.launchArgs = launchArgs;
		this.width = width;
		this.height = height;
		this.proxy = proxy;
		this.keepopen = keepopen;
		this.RPC = RPC;
		this.addons = addons;
		this.version = version;
		this.gameDir = gameDir;
	}

	public static Instance newInstance(String name) {
		// Use default settings
		return new Instance(name, "-Xmx512M", "b1.5_01", 854, 480, true, false, false, new ArrayList<String>(), BC.get() + name + "/");
	}

	public static Instance loadInstance(String name) {
		try {
			File instanceFile = new File(BC.get() + "launcher" + File.separator + "instances", name + ".txt");
			if (!instanceFile.exists()) {
				System.out.println(instanceFile.toPath().toString());
				Logger.printException(new Exception("Instance file is null!"));
				return null;
			}
			Instance instance = newInstance(name);
			instance.launchArgs = Launcher.getProperty(instanceFile, "launchArgs");
			String addonz1 = Launcher.getProperty(instanceFile, "addons");
			String[] addonz = addonz1.split(",");
			if (!addonz1.equals("")) {
				for (String addon : addonz) {
					if (instance.addons.contains(addon)) continue;
					instance.addons.add(addon);
				}
			}
			instance.gameDir = Launcher.getProperty(instanceFile, "gameDir");
			instance.version = Launcher.getProperty(instanceFile, "version");

			String width = Launcher.getProperty(instanceFile, "width");
			String height = Launcher.getProperty(instanceFile, "height");

			try {
				instance.width = Integer.parseInt(width);
				instance.height = Integer.parseInt(height);
			} catch (NumberFormatException exx) {
				Logger.a("Failed to parse width and height parameters in instance: " + name);
				return null;
			}

			instance.proxy = Boolean.parseBoolean(Launcher.getProperty(instanceFile, "proxy"));
			instance.keepopen = Boolean.parseBoolean(Launcher.getProperty(instanceFile, "keepopen"));
			instance.RPC = Boolean.parseBoolean(Launcher.getProperty(instanceFile, "RPC"));
			return instance;
		} catch (Exception ex) {
			Logger.a("Failed to load instance: " + name);
			ex.printStackTrace();
			Logger.printException(ex);
			return null;
		}
	}

	public void saveInstance() {
		try {
			File instanceFile = new File(BC.get() + "launcher" + File.separator + "instances", this.name + ".txt");
			if (!instanceFile.exists()) instanceFile.createNewFile();
			Launcher.setProperty(instanceFile, "name", this.name);
			Launcher.setProperty(instanceFile, "launchArgs", this.launchArgs);
			Launcher.setProperty(instanceFile, "width", Integer.toString(this.width));
			Launcher.setProperty(instanceFile, "height", Integer.toString(this.height));
			Launcher.setProperty(instanceFile, "proxy", Boolean.toString(this.proxy));
			Launcher.setProperty(instanceFile, "keepopen", Boolean.toString(this.keepopen));
			Launcher.setProperty(instanceFile, "RPC", Boolean.toString(this.RPC));
			StringBuilder builder = new StringBuilder();
			String addons = "";
			if (this.addons.size() > 0) {
				for (String addon : this.addons) {
					builder.append(addon + ",");
				}
				addons = builder.toString().substring(0, builder.toString().length() - 1);
			}
			Launcher.setProperty(instanceFile, "addons", addons);
			Launcher.setProperty(instanceFile, "gameDir", this.gameDir);
			Launcher.setProperty(instanceFile, "version", this.version);
			Logger.a("Saved instance: " + this.name);
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	public void removeInstance() {
		File instanceFile = new File(BC.get() + "launcher" + File.separator + "instances", this.name + ".txt");
		if (instanceFile.exists()) instanceFile.delete();
	}

	public Instance renameInstance(String newName) {
		Instance renamed = this.clone(newName);

		File instanceFile = new File(BC.get() + "launcher" + File.separator + "instances", this.name + ".txt");
		if (instanceFile.exists()) instanceFile.delete();
		File iconFile = new File(BC.get() + "launcher" + File.separator + "instances", this.name + ".png");
		if (iconFile.exists()) iconFile.renameTo(new File(BC.get() + "launcher" + File.separator + "instances", newName + ".png"));

		return renamed;
	}

	public Instance clone(String name) {
		Instance cloned = newInstance(name);
		cloned.launchArgs = this.launchArgs;
		cloned.proxy = this.proxy;
		cloned.keepopen = this.keepopen;
		cloned.height = this.height;
		cloned.width = this.width;
		cloned.RPC = this.RPC;
		cloned.addons = this.addons;
		cloned.gameDir = this.gameDir;
		cloned.version = this.version;
		return cloned;
	}

	public Image getIcon() {
		try {
			File imgFile = new File(BC.get() + "launcher" + File.separator + "instances", this.name + ".png");
			if (!imgFile.exists()) {
				return ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("icons/favicon.png"));
			}
			return ImageIO.read(imgFile).getScaledInstance(32, 32, 16);
		} catch (Exception e2) {
			e2.printStackTrace();
			Logger.printException(e2);
			this.setIcon(null);
			try {
				return ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("icons/favicon.png"));
			} catch (IOException e) { return null; }
		}
	}

	public void setIcon(File path) {
		try {
			File imgFile = new File(BC.get() + "launcher" + File.separator + "instances", this.name + ".png");
			if (path == null) {
				imgFile.delete();
				return;
			}
			Files.copy(path.toPath(), imgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e2) {
			e2.printStackTrace();
			Logger.printException(e2);
		}
	}

	public void setAddons(List<String> list) {
		this.addons.clear();
		this.addons.addAll(list);
	}

	public void removeAddon(String name) {
		if (this.addons.contains(name))
			this.addons.remove(name);
	}

	public void addAddon(String name) {
		if (this.addons.contains(name)) return;
		this.addons.add(name);
	}

	public static ArrayList<String> getInstances() {
		ArrayList<String> list = new ArrayList<String>();
		File file = new File(BC.get() + "launcher" + File.separator + "instances");
		String[] rawlist = file.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".txt");
			}
		});
		for (int i = 0; i < rawlist.length; i++) {
			list.add(rawlist[i].substring(0, rawlist[i].length() - 4));
		}
		return list;
	}

	public static int getCurrentInstanceIndex() {
		ArrayList<String> list = getInstances();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(Launcher.currentInstance.name)) {
				return i;
			}
		}
		return -1;
	}

}
