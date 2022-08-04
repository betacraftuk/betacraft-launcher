package uk.betacraft.auth.jsons.microsoft;

import com.google.gson.Gson;

import uk.betacraft.auth.Request;
import uk.betacraft.auth.RequestUtil;

public class MinecraftGameOwnRequest extends Request {

	public MinecraftGameOwnRequest(String bearer_token) {
		REQUEST_URL = "https://api.minecraftservices.com/entitlements/mcstore";
		PROPERTIES.put("Authorization", "Bearer " + bearer_token);
	}

	@Override
	public MinecraftGameOwnResponse perform() {
		Gson gson = new Gson();
		String response = RequestUtil.performGETRequest(this);

		MinecraftGameOwnResponse ret;
		try {
			ret = gson.fromJson(response, MinecraftGameOwnResponse.class);
		} catch (Throwable ex) {
			return null;
		}
		return ret;
	}
}
