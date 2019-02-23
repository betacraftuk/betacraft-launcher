
package org.betacraft.launcher;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Release {
	public static List<Release> versions = new LinkedList<Release>();

	public static void initVersions() throws IOException {
		File file = new File(BC.get() + "versions/");
		String[] arra = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fileName) {
				return fileName.endsWith(".jar");
			}
		});

		try {
			URL url = new URL("https://betacraft.ovh/version_index");

			Scanner s = new Scanner(url.openStream());
			for (String ver : scan(s, true)) {
				String[] split = ver.split("~");
				for (int i = 0; i < arra.length; i++) {
					if (arra[i] != null && split[0] != null) {
						if (arra[i].substring(0, arra[i].length() - 4).equals(split[0])) {
							arra[i] = null;
						}
					}
				}
				versions.add(new Release(split[0], split[1], null));
			}
			for (int i = 0; i < arra.length; i++) {
				if (arra[i] == null) continue;
				versions.add(new Release(arra[i].substring(0, arra[i].length() - 4), null, null));
			}

			s.close();
		} catch (UnknownHostException ex) {
			Logger.a("Brak polaczenia z internetem! (albo serwer padl) ");

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
					versions.add(new Release(split[0], split[1], null));
				}
				for (int i = 0; i < arra.length; i++) {
					if (arra[i] == null) continue;
					versions.add(new Release(arra[i].substring(0, arra[i].length() - 4), null, null));
				}

				fileScanner.close();
			} catch (Exception ex1) {
				ex1.printStackTrace();
				Logger.a("Nie udalo sie zainicjowac wersji z dysku!");
				Logger.a(ex1.getMessage());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.a("KRYTYCZNY BLAD!");
			Logger.a("podczas pobierania listy wersji: ");
			Logger.a(ex.getMessage());
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
			//Logger.a("Rejestrowanie wersji " + line);
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
	private String desc;

	public Release(String name, String date, String description) {
		this.name = name;
		this.date = date;
		this.desc = description;
	}

	public String getName() {
		return this.name;
	}

	public String getDate() {
		return this.date;
	}

	public String[] getDescription() {
		return this.desc.split("`");
	}

	@Override
	public String toString() {
		if (this.name.equalsIgnoreCase("inf-20100618")) {
			return this.name + " (Seecret Friday 1)";
		}
		if (this.name.equalsIgnoreCase("inf-20100625b")) {
			return this.name + " (Seecret Friday 2)";
		}
		if (this.name.equalsIgnoreCase("a1.0.1")) {
			return this.name + " (Seecret Friday 3)";
		}
		if (this.name.equalsIgnoreCase("a1.0.4")) {
			return this.name + " (Seecret Friday 4)";
		}
		if (this.name.equalsIgnoreCase("a1.0.6")) {
			return this.name + " (Seecret Friday 5)";
		}
		if (this.name.equalsIgnoreCase("a1.0.11")) {
			return this.name + " (Seecret Friday 6)";
		}
		if (this.name.equalsIgnoreCase("a1.0.14a")) {
			return this.name + " (Seecret Friday 7)";
		}
		if (this.name.equalsIgnoreCase("a1.0.17")) {
			return this.name + " (Seecret Friday 8)";
		}
		if (this.name.equalsIgnoreCase("a1.1.0a")) {
			return this.name + " (Seecret Friday 9)";
		}
		if (this.name.equalsIgnoreCase("a1.1.1")) {
			return this.name + " (Seecret Saturday)";
		}
		if (this.name.equalsIgnoreCase("inf-20100630b")) {
			return this.name + " (Alpha v1.0.0)";
		}
		if (this.name.equalsIgnoreCase("a1.2.0")) {
			return this.name + " (Halloween Update)";
		}
		if (this.name.equalsIgnoreCase("b1.8")) {
			return this.name + " (Adventure Update)";
		}
		return this.name;
	}
}
