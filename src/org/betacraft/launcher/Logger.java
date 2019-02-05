package org.betacraft.launcher;

import java.io.File;
import java.text.SimpleDateFormat;

public class Logger {
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static void a(Object obj) {
		String str = null;
		if (obj instanceof String) {
			str = (String) obj;
		} else if (obj instanceof Integer) {
			str = ((Integer)obj).toString();
		} else {
			str = obj.toString();
		}
		String date = format.format(Long.valueOf(System.currentTimeMillis()));
		String all = "[" + date + "] " + str;

		System.out.println(all);
		Launcher.write(new File(BC.get(), "launcher.log"), new String[] {all}, true);
	}
}
