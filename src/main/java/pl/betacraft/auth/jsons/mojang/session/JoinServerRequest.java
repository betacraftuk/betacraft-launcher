package pl.betacraft.auth.jsons.mojang.session;

import java.security.MessageDigest;

import pl.betacraft.auth.BlankResponse;
import pl.betacraft.auth.Request;
import pl.betacraft.auth.RequestUtil;
import pl.betacraft.auth.Response;

public class JoinServerRequest extends Request {

	public String accessToken;
	public String selectedProfile;
	public String serverId;

	public JoinServerRequest(String sessionid, String uuid, String serverSocket) {
		this.REQUEST_URL = "https://sessionserver.mojang.com/session/minecraft/join";
		this.PROPERTIES.put("Content-Type", "application/json");
		this.serverId = sha1(serverSocket);
		this.accessToken = sessionid;
		this.selectedProfile = uuid;
	}

	@Override
	public Response perform() {
		String response = RequestUtil.performPOSTRequest(this);

		if (response != null) {
			return new BlankResponse();
		} else {
			return null;
		}
	}

	public static String sha1(String input) {
		try {
			MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
			byte[] result = mDigest.digest(input.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < result.length; ++i) {
				sb.append(Integer.toString((result[i] & 0xFF) + 256, 16).substring(1));
			}
			return sb.toString();
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
}
