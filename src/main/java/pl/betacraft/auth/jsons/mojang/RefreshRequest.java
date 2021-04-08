package pl.betacraft.auth.jsons.mojang;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import pl.betacraft.auth.Credentials;
import pl.betacraft.auth.Request;
import pl.betacraft.auth.RequestUtil;

public class RefreshRequest extends Request {

	public String accessToken;
	public String clientToken;
	//public Profile selectedProfile;
	public boolean requestUser = false;

	public RefreshRequest(Credentials c, boolean requestUser) {
		this.REQUEST_URL = "https://authserver.mojang.com/refresh";
		this.PROPERTIES.put("Content-Type", "application/json");
		this.requestUser = requestUser;
		this.accessToken = c.access_token;
		this.clientToken = c.refresh_token;
		//this.selectedProfile = new Profile(c);
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
