package pl.betacraft.auth.jsons.mojang;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import pl.betacraft.auth.Request;
import pl.betacraft.auth.RequestUtil;

public class AuthRequest extends Request {

	public MinecraftAgent agent = new MinecraftAgent();
	public String username;
	public String password;
	public String clientToken;
	public boolean requestUser = false;

	public AuthRequest(String username, String password, String clientToken, boolean requestUser) {
		this.REQUEST_URL = "https://authserver.mojang.com/authenticate";
		this.PROPERTIES.put("Content-Type", "application/json");
		this.username = username;
		this.password = password;
		this.clientToken = clientToken;
		this.requestUser = requestUser;
	}

	@Override
	public AuthResponse perform() {
		Gson gson = new Gson();
		String response = RequestUtil.performPOSTRequest(this);

		AuthResponse ret;
		try {
			ret = gson.fromJson(response, AuthResponse.class);
		} catch (JsonParseException ex) {
			return null;
		}
		return ret;
	}
}
