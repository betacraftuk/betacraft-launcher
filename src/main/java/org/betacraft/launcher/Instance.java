package org.betacraft.launcher;

import java.awt.Image;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
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
		return new Instance(name, "-Xmx512M", "b1.7.3", 854, 480, true, false, true, new ArrayList<String>(), BC.get() + name + "/");
	}

	public static Instance loadInstance(String name) {
		try {
			File instanceFile = new File(BC.get() + "launcher" + File.separator + "instances", name + ".txt");
			if (!instanceFile.exists()) {
				System.out.println(instanceFile.getAbsolutePath());
				Logger.printException(new Exception("Instance file is null!"));
				return null;
			}
			Instance instance = newInstance(name);
			try {
				instance.launchArgs = Util.getProperty(instanceFile, "launchArgs");
				String addonz1 = Util.getProperty(instanceFile, "addons");
				String[] addonz = addonz1.split(",");
				if (!addonz1.equals("")) {
					for (String addon : addonz) {
						if (instance.addons.contains(addon)) continue;
						instance.addons.add(addon);
					}
				}
				instance.gameDir = Util.getProperty(instanceFile, "gameDir");
				instance.version = Util.getProperty(instanceFile, "version");

				String width = Util.getProperty(instanceFile, "width");
				String height = Util.getProperty(instanceFile, "height");

				try {
					instance.width = Integer.parseInt(width);
					instance.height = Integer.parseInt(height);
				} catch (NumberFormatException exx) {
					Logger.a("Failed to parse width and height parameters in instance: " + name);
					return null;
				}

				instance.proxy = Boolean.parseBoolean(Util.getProperty(instanceFile, "proxy"));
				instance.keepopen = Boolean.parseBoolean(Util.getProperty(instanceFile, "keepopen"));
				instance.RPC = Boolean.parseBoolean(Util.getProperty(instanceFile, "RPC"));
			} catch (Throwable t) {
				Logger.a("Instance '" + name + "' is corrupted!");
				t.printStackTrace();
			}
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

			Util.setProperty(instanceFile, "name", this.name);
			Util.setProperty(instanceFile, "launchArgs", this.launchArgs);
			Util.setProperty(instanceFile, "width", Integer.toString(this.width));
			Util.setProperty(instanceFile, "height", Integer.toString(this.height));
			Util.setProperty(instanceFile, "proxy", Boolean.toString(this.proxy));
			Util.setProperty(instanceFile, "keepopen", Boolean.toString(this.keepopen));
			Util.setProperty(instanceFile, "RPC", Boolean.toString(this.RPC));

			StringBuilder builder = new StringBuilder();
			String addons = "";
			if (this.addons.size() > 0) {
				for (String addon : this.addons) {
					builder.append(addon + ",");
				}
				addons = builder.toString().substring(0, builder.toString().length() - 1);
			}

			Util.setProperty(instanceFile, "addons", addons);
			Util.setProperty(instanceFile, "gameDir", this.gameDir);
			Util.setProperty(instanceFile, "version", this.version);
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

	public String getIconLocation() throws IOException {
		File imgFile = new File(BC.get() + "launcher" + File.separator + "instances", this.name + ".png");
		File defaultImg = new File(BC.get() + "launcher" + File.separator + "default_icon.png");

		if (!imgFile.exists()) {
			Util.copy(this.getClass().getClassLoader().getResourceAsStream("icons/favicon.png"), defaultImg);

			return defaultImg.getAbsolutePath();
		}
		return imgFile.getAbsolutePath();
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
			Util.copy(path, imgFile);
		} catch (Throwable e2) {
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
