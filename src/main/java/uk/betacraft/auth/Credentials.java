package uk.betacraft.auth;

import java.io.File;

import org.betacraft.launcher.Util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Credentials {

	private static transient Gson gson = new Gson();
	private static transient Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
	public String refresh_token;
	public String access_token;
	public String local_uuid;
	public String username;
	public String name;
	public Long expires_at;
	public AccountType account_type;

	public static Credentials[] load(File credentials) {
		try {
			String jsonContent = new String(Util.readBytes(credentials), "UTF-8");
			return gson.fromJson(jsonContent, Credentials[].class);
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	public static boolean save(Credentials[] c, File cFile) {
		try {
			Util.writeBytes(cFile, gsonPretty.toJson(c).getBytes("UTF-8"));
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}

	public enum AccountType {
		MOJANG,
		MICROSOFT,
		OFFLINE;
	}
}
