package org.betacraft.launcher;

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
			String line = null;

			String folder = Launcher.getBetacraft();
			String[] filecontent = new String[16384]; // 4096 x 4
			int i = 0;

			while (s.hasNextLine()) {
				line = s.nextLine();
				if (line.equalsIgnoreCase("")) {
					continue;
				} if (line.startsWith("launcher:")) {
					continue;
				}
				if (i == 16383) {
					System.out.println("String array overflow. Skipping.");
					continue;
				}
				System.out.println(line);
				filecontent[i] = line;
				String[] split = line.split("~");
				Release release = null;
				if (split[0].contains("w")) {
					release = new Prerelease(split[0], split[1], null);
				} else {
					release = new Release(split[0], split[1], null);
				}
				versions.add(release);
				i++;
			}
			// zapisz liste wersji offline
			Launcher.write(folder + "version_index", filecontent);

			s.close();
		} catch (UnknownHostException ex) {
			System.out.println("Brak połączenia z internetem! (albo serwer padł) ");
			// TODO kod na offline wlaczanie
		} catch (Exception ex) {
			System.out.println("KRYTYCZNY BŁĄD!");
			System.out.println("podczas pobierania listy wersji: ");
			ex.printStackTrace();
		}
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
}
