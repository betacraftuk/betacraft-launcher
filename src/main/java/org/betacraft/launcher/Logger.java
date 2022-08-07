package org.betacraft.launcher;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

public class Logger {
	public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static File launcherLogFile = new File(BC.get() + "launcher", "launcher.log");
	public static File clientLogFile = new File(BC.get(), "output-client.log");

	public static void init() {
		if (!BC.wrapped) {
			try {
				launcherLogFile.getParentFile().mkdirs();
				launcherLogFile.createNewFile();
				System.setErr(new BCPrintStream(System.err, new BufferedOutputStream(new FileOutputStream(launcherLogFile, true)), "ERR", true));
				System.setOut(new BCPrintStream(System.out, new BufferedOutputStream(new FileOutputStream(launcherLogFile, true)), "OUT", true));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} else {
			try {
				clientLogFile.getParentFile().mkdirs();
				clientLogFile.createNewFile();
				System.setErr(new BCPrintStream(System.err, new BufferedOutputStream(new FileOutputStream(clientLogFile, true)), "ERR", false));
				System.setOut(new BCPrintStream(System.out, new BufferedOutputStream(new FileOutputStream(clientLogFile, true)), "OUT", false));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	public static void clearClientLog() {
		if (clientLogFile.exists()) clientLogFile.delete();
	}

	public static void clearLauncherLog() {
		if (launcherLogFile.exists()) launcherLogFile.delete();
	}
}
