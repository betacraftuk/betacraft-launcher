package org.betacraft.launcher;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

public class MojangLogging {
	protected static Map<String, String> userProfile;
	protected static String email = "";
	protected static String username = "";
	protected static String password = "";

	public boolean authenticate(String player, String password) {
		MojangLogging.email = player;
		MojangLogging.password = password;

		HashMap<String, String> responsemap = new HashMap<String, String>();
		InputStream in = null;
		HttpURLConnection con = null;
		try {
			URL url = new URL("https://authserver.mojang.com/authenticate");
			con = (HttpURLConnection) url.openConnection();
			con.setUseCaches(false);
			con.setRequestMethod("POST");
			con.setConnectTimeout(15000);
			con.setReadTimeout(15000);
			String request = "{\"agent\": "
							+ "{\"name\": \"Minecraft\", "
							+ "\"version\": 1}, "
					+ "\"username\": \"" + player + "\", "
					+ "\"password\": \"" + password + "\"}";
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);
			con.setDoInput(true);
			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			out.writeBytes(request);
			out.flush();
			out.close();
			in = con.getInputStream();
			byte[] bytearr = new byte[in.available()];
			in.read(bytearr);
			in.close();
			String response = new String(bytearr, Charset.forName("UTF-8"));
			System.out.println(response);
			String selectedProfile = response.substring(response.indexOf("\"selectedProfile\""));
			String accessToken = response.substring(response.indexOf("\"accessToken\"")).split("\"")[3];
			String clientToken = response.substring(response.indexOf("\"clientToken\"")).split("\"")[3];
			String name = selectedProfile.substring(selectedProfile.indexOf("\"name\"")).split("\"")[3];
			String id = selectedProfile.substring(selectedProfile.indexOf("\"id\"")).split("\"")[3];

			responsemap.put("accessToken", accessToken);
			responsemap.put("clientToken", clientToken);
			responsemap.put("selectedProfile.name", name);
			responsemap.put("selectedProfile.id", id);
			userProfile = responsemap;

			username = name;
			Window.nick_input.setText(Launcher.getNickname());
			Window.nick_input.setEnabled(false);
			Window.loginButton.setText(Lang.LOGOUT_BUTTON);
			Logger.a("Logged in successfully.");
			return true;
		} catch (ArrayIndexOutOfBoundsException ex) {
			ex.printStackTrace();
			Logger.printException(ex);
			userProfile = null;
			JOptionPane.showMessageDialog(null, Lang.LOGIN_FAILED_METHOD, Lang.LOGIN_FAILED, JOptionPane.ERROR_MESSAGE);
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
			Logger.printException(ex);
			Logger.a(null);
		} catch (SocketTimeoutException ex) {
			ex.printStackTrace();
			Logger.printException(ex);
			Logger.a(null);
		} catch (SocketException ex) {
			ex.printStackTrace();
			Logger.printException(ex);
			Logger.a(null);
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
			userProfile = null;
			JOptionPane.showMessageDialog(null, Lang.LOGIN_FAILED_INVALID_CREDENTIALS, Lang.LOGIN_FAILED, JOptionPane.ERROR_MESSAGE);
			try {
				in = con.getErrorStream();
				byte[] bytearr = new byte[in.available()];
				in.read(bytearr);
				String response = new String(bytearr, Charset.forName("UTF-8"));
				System.out.println(response);
				Logger.a(response);
				in.close();
			} catch (Exception ex1) {
				ex1.printStackTrace();
				Logger.printException(ex1);
			}
		}
		return false;
	}
}
