package org.betacraft.launcher;

import java.io.File;
import java.text.SimpleDateFormat;

public class Logger {
	public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String lastMessage = "";
	public static File launcherLogFile = new File(BC.get() + "launcher", "launcher.log");
	public static File clientLogFile = new File(BC.get(), "output-client.log");

	public static void a(Object obj) {
		log(launcherLogFile, true, obj);
	}

	public static void logClient(Object obj) {
		log(clientLogFile, true, obj);
	}

	public static void log(File file, boolean append, Object value) {
		String str = null;
		if (value instanceof String) {
			str = (String) value;
		} else if (value instanceof Integer) {
			str = ((Integer)value).toString();
		} else if (value == null) {
			str = "No internet connection, or the server is down.";
		} else {
			str = value.toString();
		}
		String date = format.format(Long.valueOf(System.currentTimeMillis()));
		String all = "[" + date + "] " + str;

		System.out.println(all);
		lastMessage = all;
		Util.write(file, new String[] {all}, append);
	}

	public static void clearClientLog() {
		clientLogFile.delete();
	}

	public static void clearLauncherLog() {
		launcherLogFile.delete();
	}

	public static void printException(Throwable ex) {
		if (BC.wrapped) return; // don't do that on wrapper runs

		String msg = ex.getClass().getCanonicalName() + ": " + ex.getMessage();
		Logger.a(msg);

		StackTraceElement[] trace = ex.getStackTrace();

		for (int i = 0; i < trace.length; i++) {
			StackTraceElement line = trace[i];
			String lineStr = "    " + line.getClassName() + " | " + line.getMethodName() + " | " + line.getLineNumber();
			Logger.a(lineStr);
		}
	}
}
