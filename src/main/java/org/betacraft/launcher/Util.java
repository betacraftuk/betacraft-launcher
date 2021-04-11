package org.betacraft.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pl.betacraft.auth.Accounts;
import pl.betacraft.auth.Authenticator;
import pl.betacraft.auth.Credentials;
import pl.betacraft.auth.Credentials.AccountType;
import pl.betacraft.auth.DownloadRequest;
import pl.betacraft.auth.DownloadResponse;
import pl.betacraft.auth.MicrosoftAuth;
import pl.betacraft.auth.MojangAuth;
import pl.betacraft.auth.NoAuth;
import pl.betacraft.json.lib.MouseFixMacOSJson;

public class Util {
	public static final Gson gson = new Gson();
	public static final Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
	public static final int jsonVersion = 1;
	public static final File accountsFile = new File(BC.get() + "launcher/accounts.json");
	public static final File cert_file = new File(BC.get() + "launcher/ssl_cert.crt");
	public static final File old_lastlogin = new File(BC.get() + "lastlogin");

	private static void setupAccountConfiguration() {
		try {
			Launcher.auth = new NoAuth("");
			Accounts accs = new Accounts();
			accs.current = Launcher.auth.getCredentials().local_uuid;
			ArrayList<Credentials> list = new ArrayList<>();
			list.add(Launcher.auth.getCredentials());
			accs.accounts = list;
			Launcher.accounts = accs;
			saveAccounts();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void saveAccounts() {
		try {
			if (!accountsFile.exists()) {
				accountsFile.createNewFile();
			}
			Files.write(accountsFile.toPath(), gsonPretty.toJson(Launcher.accounts).getBytes("UTF-8"));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static Authenticator getAuthenticator(Credentials c) {
		if (c.account_type == AccountType.MICROSOFT) {
			return new MicrosoftAuth(c);
		} else if (c.account_type == AccountType.MOJANG) {
			return new MojangAuth(c);
		} else if (c.account_type == AccountType.OFFLINE) {
			return new NoAuth(c);
		}
		return null;
	}

	public static void readAccounts() {
		try {
			if (!accountsFile.exists()) {
				setupAccountConfiguration();
				return;
			}
			Accounts accs = gson.fromJson(new String(Files.readAllBytes(accountsFile.toPath()), "UTF-8"), Accounts.class);
			if (accs.accounts == null) {
				setupAccountConfiguration();
				return;
			}
			for (Credentials c: accs.accounts) {
				if (c.local_uuid != null && c.local_uuid.equals(accs.current)) {
					Launcher.auth = getAuthenticator(c);
				}
			}
			if (Launcher.auth == null) {
				Launcher.auth = getAuthenticator(accs.accounts.get(0));
				accs.current = Launcher.auth.getCredentials().local_uuid;
			}
			Launcher.accounts = accs;
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void write(File file, String[] lines, boolean append) {
		write(file, lines, append, "UTF-8");
	}

	public static void write(File file, String[] lines, boolean append, String charset) {
		try {
			// Create new file, if it doesn't already exist
			file.createNewFile();
		} catch (IOException e) {
			System.out.println(file.toPath().toString());
			e.printStackTrace();
			Logger.printException(e);
		}
		OutputStreamWriter writer = null;
		try {
			// Write in UTF-8
			writer = new OutputStreamWriter(
					new FileOutputStream(file, append), charset);
			for (int i = 0; i < lines.length; i++) {
				// Skip empty lines
				if (lines[i] != null) {
					writer.write(lines[i] + "\n");
				}
			}
		} catch (Exception ex) {
			Logger.a("A critical error occurred while attempting to write to file: " + file);
			ex.printStackTrace();
			Logger.printException(ex);
		} finally {
			// Close the file
			try {writer.close();} catch (Exception ex) {}
		}
	}

	public static void setProperty(File file, String property, String value) {
		setProperty(file, property, value, "UTF-8");
	}

	public static void setProperty(File file, String property, String value, String charset) {
		// Read the lines
		String[] lines = read(file, charset);
		String[] newlines = new String[lines.length + 1];

		// Try to find the property wanted to be set
		boolean found = false;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] == null) continue;
			if (lines[i].startsWith(property + ":")) {
				// The wanted property has been found, so we're going to replace its value
				newlines[i] = property + ":" + value;
				found = true;
				continue;
			}
			// The property didn't match, just take this line further
			newlines[i] = lines[i];
		}

		if (!found) {
			// There was no wanted property in the file, so we're going to append it to the file 
			write(file, new String[] {property + ":" + value}, true, charset);
			return;
		}

		// Write to file, without appending
		write(file, newlines, false, charset);
	}

	public static String getProperty(File file, String property) {
		return getProperty(file, property, "UTF-8");
	}

	public static String getProperty(File file, String property, String charset) {
		String[] lines = read(file, charset);
		String value = null;
		for (int i = 0; i < lines.length; i++) {
			// If the array is empty, ignore it
			if (lines[i] == null) continue;

			// Check if the property matches
			if (lines[i].startsWith(property + ":")) {
				value = lines[i].substring(property.length()+1, lines[i].length());
				break;
			}
		}
		return value;
	}

	public static boolean hasProperty(File file, String property, String charset) {
		String[] lines = read(file, charset);
		for (int i = 0; i < lines.length; i++) {
			// If the line is empty, ignore it
			if (lines[i] == null) continue;

			// Check if the property matches
			if (lines[i].startsWith(property + ":")) {
				return true;
			}
		}
		return false;
	}

	public static String[] excludeExistant(File file, String[] properties, String charset) {
		String[] lines = read(file, charset);
		for (int i = 0; i < lines.length; i++) {
			// If the line is empty, ignore it
			if (lines[i] == null) continue;

			for (int i1 = 0; i1 < properties.length; i1++) {
				// If the property matches, remove it from array
				if (lines[i].startsWith(properties[i1] + ":")) {
					properties[i1] = null;
				}
			}
		}
		return properties;
	}

	public static Thread unzip(File source, File dest_folder, boolean delete) {
		return unzip(source.toPath().toString(), dest_folder.toPath().toString(), delete);
	}

	public static Thread unzip(String source, String dest_folder, boolean delete) {
		Thread unrarthread = new Thread() {
			public void run() {
				FileInputStream fis;
				byte[] buffer = new byte[1024];
				try {
					fis = new FileInputStream(source);
					ZipInputStream zis = new ZipInputStream(fis);
					ZipEntry entry = zis.getNextEntry();
					while (entry != null) {
						if (entry.isDirectory()) {
							entry = zis.getNextEntry();
							continue;
						}
						String fileName = entry.getName();
						File newFile = new File(dest_folder + File.separator + fileName);

						new File(newFile.getParent()).mkdirs();
						FileOutputStream fos = new FileOutputStream(newFile);
						int length;
						while ((length = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, length);
						}

						fos.close();
						zis.closeEntry();
						entry = zis.getNextEntry();
					}
					zis.closeEntry();
					zis.close();
					fis.close();
				} catch (Exception ex) {
					ex.printStackTrace();
					Logger.printException(ex);
				}
				if (delete) new File(source).delete();
				if (!Util.isStandalone()) Launcher.totalThreads.remove(this);
			}
		};
		unrarthread.start();
		return unrarthread;
	}

	public static String[] read(File file, String charset) {
		try {
			if (!file.exists()) return new String[] {};
			// Create new, if doesn't exist
			if (file.createNewFile()) {
				Logger.a("Created a new file: " + file);
			}
		} catch (IOException e) {
			System.out.println(file.toPath().toString());
			e.printStackTrace();
			Logger.printException(e);
		}
		InputStreamReader reader = null;
		try {
			// Read in UTF-8
			reader = new InputStreamReader(
					new FileInputStream(file), charset);
			StringBuilder inputB = new StringBuilder();
			char[] buffer = new char[1024];
			while (true) {
				int readcount = reader.read(buffer);
				if (readcount < 0) break;
				inputB.append(buffer, 0, readcount);
			}
			return inputB.toString().split("\n");
		} catch (Exception ex) {
			Logger.a("A critical error occurred while reading from file: " + file);
			ex.printStackTrace();
			Logger.printException(ex);
		} finally {
			// Close the file
			try {reader.close();} catch (Exception ex) {}
		}
		return null;
	}

	public static boolean isStandalone() {
		try {
			Launcher.VERSION.length();
			return false;
		} catch (Throwable ex) {
			return true;
		}
	}

	public static String getSHA1(File file) {
		try {
			InputStream fis =  new FileInputStream(file);

			byte[] buffer = new byte[1024];
			MessageDigest msgdig = MessageDigest.getInstance("SHA-1");
			int numRead;

			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					msgdig.update(buffer, 0, numRead);
				}
			} while (numRead != -1);

			fis.close();
			byte[] digest = msgdig.digest();
			String str_result = "";
			for (int i = 0; i < digest.length; i++) {
				str_result += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
			}
			return str_result;
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	public static boolean installMacOSFix(MouseFixMacOSJson json, boolean force) {
		File javaagent = new File(BC.get() + "launcher/macos-javaagent.jar");
		File lwjgl = new File(BC.get() + "launcher/macos-mousefix-lwjgl.jar");

		File classes_folder = new File(BC.get() + "launcher/macos-java-mod/");
		File classes_temp_zip = new File(BC.get() + "launcher/macos-mousefix.zip");

		String local_javaagent_sha1 = javaagent.exists() ? getSHA1(javaagent) : null;
		String local_lwjgl_sha1 = lwjgl.exists() ? getSHA1(lwjgl): null;
		String local_javamod_sha1 = Util.getProperty(BC.SETTINGS, "macosMouseFixClassesVersion");

		try {
			if (local_javaagent_sha1 == null || !local_javaagent_sha1.equals(json.agent_sha1) || force) {
				DownloadResponse agent_req = new DownloadRequest(json.agent_url, javaagent.toPath().toString(), json.agent_sha1, false).perform();
				if (agent_req.result != DownloadResult.OK) {
					Logger.a("Failed to download macos javaagent");
					return false;
				}
			}
			if (local_lwjgl_sha1 == null || !local_lwjgl_sha1.equals(json.lwjgl_sha1) || force) {
				DownloadResponse lwjgl_req = new DownloadRequest(json.lwjgl_url, lwjgl.toPath().toString(), json.lwjgl_sha1, false).perform();
				if (lwjgl_req.result != DownloadResult.OK) {
					Logger.a("Failed to download macos-mousefix.zip");
					return false;
				}
			}
			if (local_javamod_sha1 == null || !local_javamod_sha1.equals(json.classes_sha1) || force) {
				DownloadResponse classes_req = new DownloadRequest(json.classes_url, classes_temp_zip.toPath().toString(), json.classes_sha1, false).perform();
				if (classes_req.result != DownloadResult.OK) {
					Logger.a("Failed to download macos-mousefix.zip");
					return false;
				} else {
					if (classes_folder.exists() && classes_folder.list().length != 0) {
						Launcher.removeRecursively(classes_folder, false, false);
					}
					classes_folder.mkdirs();
					Util.setProperty(BC.SETTINGS, "macosMouseFixClassesVersion", json.classes_sha1);
					Launcher.totalThreads.add(unzip(classes_temp_zip, classes_folder, true));
				}
			}
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}

	public static boolean hasJFX() {
		try {
			javafx.scene.Parent.class.getName();
			return true;
		} catch (Throwable t) {
			return false;
		}
	}
}
