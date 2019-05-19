
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
	public static List<Release> versions = new LinkedList<Release>();

	private static InputStream stream = null;
	private static Scanner scanner = null;

	public static void initVersions() throws IOException {
		File file = new File(BC.get() + "versions/");
		String[] arra = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fileName) {
				return fileName.endsWith(".jar");
			}
		});

		try {
			final URL url = new URL("http://213.32.90.142/version_index");

			try {
				stream = url.openStream();
			} catch (UnknownHostException | SocketTimeoutException | SocketException ex) {
				Logger.a("Brak polaczenia z internetem! (albo serwer padl) ");
			} catch (Exception ex) {
				Logger.a("KRYTYCZNY BLAD!");
				Logger.a("podczas pobierania listy wersji: ");
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, "An error occured while loading version list! Report this to: @Moresteck#1688", "Critical error!", JOptionPane.ERROR_MESSAGE);
			}
			if (stream == null) {
				loadOfflineList();
				return;
			}
			scanner = new Scanner(stream);
			for (String ver : scan(scanner, true)) {
				String[] split = ver.split("~");
				for (int i = 0; i < arra.length; i++) {
					if (arra[i] != null && split[0] != null) {
						if (arra[i].substring(0, arra[i].length() - 4).equals(split[0])) {
							arra[i] = null;
						}
					}
				}
				versions.add(new Release(split[0], split[1], split[2], split[3], split[4]));
			}
			for (int i = 0; i < arra.length; i++) {
				if (arra[i] == null) continue;
				versions.add(new Release(arra[i].substring(0, arra[i].length() - 4), "", "", "", null));
			}

			scanner.close();
		} catch (Exception ex) {
			Logger.a("KRYTYCZNY BLAD!");
			Logger.a("podczas pobierania listy wersji: ");
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "An error occured while loading version list! Report this to: @Moresteck#1688", "Critical error!", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void loadOfflineList() {
		File file = new File(BC.get() + "versions/");
		String[] arra = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fileName) {
				return fileName.endsWith(".jar");
			}
		});
		try {
			Scanner fileScanner = new Scanner(new File(BC.get() + "launcher/version_index"));
			List<String> list = scan(fileScanner, false);

			for (String r: list) {
				String[] split = r.split("~");
				boolean y = false;
				for (int i = 0; i < arra.length; i++) {
					if (arra[i] != null && split[0] != null) {
						if (arra[i].substring(0, arra[i].length() - 4).equals(split[0])) {
							arra[i] = null;
							y = true;
						}
					}
				}
				if (!y) continue;
				versions.add(new Release(split[0], split[1], split[2], split[3], split[4]));
			}
			for (int i = 0; i < arra.length; i++) {
				if (arra[i] == null) continue;
				versions.add(new Release(arra[i].substring(0, arra[i].length() - 4), "", "", "", null));
			}

			fileScanner.close();
		} catch (Exception ex) {
			Logger.a("Nie udalo sie zainicjowac wersji z dysku!");
			ex.printStackTrace();
		}
	}

	private static List<String> scan(Scanner scanner, boolean save) {
		String line = null;

		List<String> list = new ArrayList<String>();
		String folder = BC.get() + "launcher/";
		String[] filecontent = new String[400];
		int i = 1;

		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			if (line.equalsIgnoreCase("")) continue;
			if (line.startsWith("launcher:")) continue;
			if (i == 400) {
				Logger.a("String array overflow. Skipping.");
				continue;
			}
			if (save) {
				filecontent[i] = line;
			}
			
			list.add(line);
			i++;
		}
		// zapisz liste wersji offline
		if (save) {
			Launcher.write(new File(folder, "version_index"), filecontent, false);
		}
		return list;
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
		if (wikilink == null)
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
