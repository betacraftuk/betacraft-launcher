package pl.betacraft.auth.jsons.microsoft;

import com.google.gson.Gson;

import pl.betacraft.auth.Request;
import pl.betacraft.auth.RequestUtil;

public class MinecraftAuthRequest extends Request {

	public String identityToken;

	public MinecraftAuthRequest(String uhs, String xsts_token) {
		REQUEST_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
		PROPERTIES.put("Content-Type", "application/json");
		PROPERTIES.put("Accept", "application/json");
		identityToken = "XBL3.0 x=" + uhs + ";" + xsts_token;
	}

	@Override
	public MinecraftAuthResponse perform() {
		Gson gson = new Gson();
		String response = RequestUtil.performPOSTRequest(this);

		MinecraftAuthResponse ret;
		try {
			ret = gson.fromJson(response, MinecraftAuthResponse.class);
		} catch (Throwable ex) {
			return null;
		}
		return ret;
	}
}
