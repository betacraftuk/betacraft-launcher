package org.betacraft.launcher;

import java.io.File;
import java.text.SimpleDateFormat;

public class Logger {
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String lastMessage = "";

	public static void a(Object obj) {
		log(new File(BC.get() + "launcher", "launcher.log"), true, obj);
	}

	public static void logClient(Object obj) {
		log(new File(BC.get(), "output-client.log"), true, obj);
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

	public static void printException(Throwable ex) {
		//String[] lines = new String[trace.length];
		Logger.a(ex.getClass().getCanonicalName() + ": " + ex.getMessage());
		StackTraceElement[] trace = ex.getStackTrace();
		for (int i = 0; i < trace.length; i++) {
			StackTraceElement line = trace[i];
			Logger.a("    " + line.getClassName() + " | " + line.getMethodName() + " | " + line.getLineNumber());
		}
	}
}
