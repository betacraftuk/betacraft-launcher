
package org.betacraft.launcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Release {
	public static List<Release> versions = new LinkedList<Release>();

	public static void initVersions() throws IOException {
		try {
			URL url = new URL("https://betacraft.ovh/version_index");

			Scanner s = new Scanner(url.openStream());
			scan(s, true);

			s.close();
		} catch (UnknownHostException ex) {
			Logger.a("Brak polaczenia z internetem! (albo serwer padl) ");

			try {
				Scanner fileScanner = new Scanner(new File(BC.get() + "version_index"));
				scan(fileScanner, false);

				fileScanner.close();
			} catch (Exception ex1) {
				Logger.a("Nie udalo sie zainicjowac wersji z dysku!");
				Logger.a(ex1.getMessage());
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			Logger.a("KRYTYCZNY BLAD!");
			Logger.a("podczas pobierania listy wersji: ");
			Logger.a(ex.getMessage());
			ex.printStackTrace();
		}
	}

	private static void scan(Scanner scanner, boolean save) {
		String line = null;

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
			if (save) filecontent[i] = line;
			String[] split = line.split("~");
			Release release = null;
			if (split[0].contains("pre") || split[0].contains("test_build")) {
				release = new Prerelease(split[0], split[1], null);
			} else {
				release = new Release(split[0], split[1], null);
			}
			versions.add(release);
			i++;
		}
		// zapisz liste wersji offline
		if (save) Launcher.write(new File(folder, "version_index"), filecontent, false);
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
