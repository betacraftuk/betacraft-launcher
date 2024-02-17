package uk.betacraft.auth.jsons.microsoft;

import com.google.gson.Gson;

import uk.betacraft.auth.MicrosoftAuth;
import uk.betacraft.auth.Request;
import uk.betacraft.auth.RequestUtil;

public class MinecraftProfileRequest extends Request {

	public MinecraftProfileRequest(String bearer_token) {
		REQUEST_URL = "https://api.minecraftservices.com/minecraft/profile";
		PROPERTIES.put("Authorization", "Bearer " + bearer_token);
	}

	@Override
	public MinecraftProfileResponse perform() {
		Gson gson = new Gson();
		String response = MicrosoftAuth.fireAuthRequest(this);

		MinecraftProfileResponse ret;
		try {
			ret = gson.fromJson(response, MinecraftProfileResponse.class);
		} catch (Throwable ex) {
			return null;
		}
		return ret;
	}
}
