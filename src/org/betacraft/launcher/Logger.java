package org.betacraft.launcher;

import java.io.File;
import java.text.SimpleDateFormat;

public class Logger {
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String lastMessage = "";

	public static void a(Object obj) {
		String str = null;
		if (obj instanceof String) {
			str = (String) obj;
		} else if (obj instanceof Integer) {
			str = ((Integer)obj).toString();
		} else if (obj == null) {
			str = "No internet connection, or the server is down.";
		} else {
			str = obj.toString();
		}
		String date = format.format(Long.valueOf(System.currentTimeMillis()));
		String all = "[" + date + "] " + str;

		System.out.println(all);
		lastMessage = all;
		Launcher.write(new File(BC.get() + "launcher", "launcher.log"), new String[] {all}, true);
	}

	public static void printException(Exception ex) {
		//String[] lines = new String[trace.length];
		Logger.a(ex.getClass().getCanonicalName() + ": " + ex.getMessage());
		StackTraceElement[] trace = ex.getStackTrace();
		for (int i = 0; i < trace.length; i++) {
			StackTraceElement line = trace[i];
			Logger.a("    " + line.getClassName() + " | " + line.getMethodName() + " | " + line.getLineNumber());
		}
	}
}
