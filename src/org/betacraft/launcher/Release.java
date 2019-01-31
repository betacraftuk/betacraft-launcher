package org.betacraft.launcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

	        String folder = null;
            if (OS.isLinux()) {
            	folder = System.getProperty("user.home") + "/.betacraft/";
            } else if (OS.isiOS()) {
            	folder = System.getProperty("user.home") + "/Application Support/betacraft/";
            } else if (OS.isWindows()) {
            	folder = System.getenv("APPDATA") + "/.betacraft/";
            } else {
            	System.out.println("Your system is not supported. Quitting.");
            	s.close();
            	Window.quit();
            	return;
            }
            System.out.println(folder);
            File betacraft = new File(folder);
            betacraft.mkdirs();

            BufferedWriter writer = null;

            try {
                writer = new BufferedWriter(new OutputStreamWriter(
                      new FileOutputStream(folder + "version_index"), "utf-8"));
                for (int i = 0; i < 250; i++) {
    	        	if (s.hasNextLine()) {
    	        		line = s.nextLine();
    	        		if (!line.equalsIgnoreCase("null")) {
    		        		String[] split = line.split("~");
    			            Release version = new Release(split[0], split[1], null);
    			            versions.add(version);

    			            // zapisz wersje na kompie, w razie jakby ktos chcial uzywac launchera offline
    			            writer.write(line);
    			            writer.newLine();

    			            // dodaj kod ktory doda te wersje do listy w ramce (ktorej jeszcze nie ma xD)
    		        	}
    	        	}
    	        }
                
            } catch (Exception ex) {
                
            } finally {
               try {writer.close();} catch (Exception ex) {}
            }
	        
	        s.close();
		} catch (UnknownHostException ex) {
			System.out.println("Brak połączenia z internetem! (albo serwer padł)");
			// TODO kod na offline wlaczanie
		} catch (Exception ex) {
			System.out.println("FATALNY ERROR");
			System.out.println("podczas pobierania listy wersji:");
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
