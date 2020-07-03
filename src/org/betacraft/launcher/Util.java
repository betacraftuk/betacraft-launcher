package org.betacraft.launcher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class Util {
	public static final int jsonVersion = 1;

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

	public static void readLastLogin() {
		try {
			final File lastLogin = new File(BC.get(), "lastlogin");
			final Cipher cipher = getCipher(2, "bcpasswordfile");
			DataInputStream dis;
			if (cipher != null) {
				dis = new DataInputStream(new CipherInputStream(new FileInputStream(lastLogin), cipher));
			}
			else {
				dis = new DataInputStream(new FileInputStream(lastLogin));
			}
			MojangLogging.email = dis.readUTF();
			MojangLogging.password = dis.readUTF();
			MojangLogging.username = dis.readUTF();
			dis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	public static void saveLastLogin() {
		try {
			final File lastLogin = new File(BC.get(), "lastlogin");
			final Cipher cipher = getCipher(1, "bcpasswordfile");
			DataOutputStream dos;
			if (cipher != null) {
				dos = new DataOutputStream(new CipherOutputStream(new FileOutputStream(lastLogin), cipher));
			}
			else {
				dos = new DataOutputStream(new FileOutputStream(lastLogin));
			}
			boolean rememberpassword = getProperty(BC.SETTINGS, "remember-password").equals("true");
			dos.writeUTF(MojangLogging.email);
			if (rememberpassword) {
				dos.writeUTF(MojangLogging.password);
			} else {
				dos.writeUTF("");
			}
			dos.writeUTF(MojangLogging.username);

			dos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	private static Cipher getCipher(final int mode, final String password) throws Exception {
		final Random random = new Random(37635689L);
		final byte[] salt = new byte[8];
		random.nextBytes(salt);
		final PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);
		final SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
		final Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(mode, pbeKey, pbeParamSpec);
		return cipher;
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
		String value = "";
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
			// If the array is empty, ignore it
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
			// If the array is empty, ignore it
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

	public static Thread Unrar(String filepath, String SRC, boolean delete) {
		Thread unrarthread = new Thread() {
			public void run() {
				FileInputStream fis;
				byte[] buffer = new byte[1024];
				try {
					fis = new FileInputStream(filepath);
					ZipInputStream zis = new ZipInputStream(fis);
					ZipEntry entry = zis.getNextEntry();
					while (entry != null) {
						if (entry.isDirectory()) {
							entry = zis.getNextEntry();
							continue;
						}
						String fileName = entry.getName();
						File newFile = new File(SRC + File.separator + fileName);

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
				if (delete) new File(filepath).delete();
				if (!Util.isStandalone()) Launcher.totalThreads.remove(this);
			}
		};
		unrarthread.start();
		return unrarthread;
	}

	public static void zip(String filepath, String SRC, boolean delete) {
		Thread zipthread = new Thread() {
			public void run() {
				FileInputStream fis;
				byte[] buffer = new byte[1024];
				try {
					fis = new FileInputStream(filepath);
					ZipInputStream zis = new ZipInputStream(fis);
					ZipEntry entry = zis.getNextEntry();
					while (entry != null) {
						if (entry.isDirectory()) {
							entry = zis.getNextEntry();
							continue;
						}
						String fileName = entry.getName();
						File newFile = new File(SRC + File.separator + fileName);

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
				if (delete) new File(filepath).delete();
				if (!isStandalone()) Launcher.totalThreads.remove(this);
			}
		};
		zipthread.start();
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
			System.out.println(Launcher.VERSION);
			return false;
		} catch (Throwable ex) {
			return true;
		}
	}
}
