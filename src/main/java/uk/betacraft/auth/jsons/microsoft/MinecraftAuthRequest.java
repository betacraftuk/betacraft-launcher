package uk.betacraft.auth.jsons.microsoft;

import javax.swing.JOptionPane;

import org.betacraft.launcher.Lang;

import com.google.gson.Gson;

import uk.betacraft.auth.MicrosoftAuth;
import uk.betacraft.auth.Request;
import uk.betacraft.auth.RequestUtil;
import uk.betacraft.util.WebData;

public class MinecraftAuthRequest extends Request {

	public String identityToken;

	public MinecraftAuthRequest(String uhs, String xsts_token) {
		REQUEST_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
		PROPERTIES.put("Content-Type", "application/json");
		PROPERTIES.put("Accept", "application/json");
		identityToken = "XBL3.0 x=" + uhs + ";" + xsts_token;
		type = RequestType.POST;
	}

	@Override
	public MinecraftAuthResponse perform() {
		Gson gson = new Gson();
		String response = MicrosoftAuth.fireAuthRequest(this);

		MinecraftAuthResponse ret;
		try {
			ret = gson.fromJson(response, MinecraftAuthResponse.class);
		} catch (Throwable ex) {
			return null;
		}
		return ret;
	}
}
