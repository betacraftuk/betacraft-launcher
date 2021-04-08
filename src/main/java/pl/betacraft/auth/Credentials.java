package pl.betacraft.auth;

import java.io.File;
import java.nio.file.Files;

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
	public AccountType account_type;

	public static Credentials[] load(File credentials) {
		try {
			String jsonContent = new String(Files.readAllBytes(credentials.toPath()), "UTF-8");
			return gson.fromJson(jsonContent, Credentials[].class);
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	public static boolean save(Credentials[] c, File cFile) {
		try {
			Files.write(cFile.toPath(), gsonPretty.toJson(c).getBytes("UTF-8"));
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}

	public enum AccountType {
		MOJANG,
		MICROSOFT,
		OFFLINE,
		TWITCH,
		BETACRAFT;
	}
}
