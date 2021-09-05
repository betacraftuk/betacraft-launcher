package pl.betacraft.auth.jsons.microsoft;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import pl.betacraft.auth.MicrosoftAuth;
import pl.betacraft.auth.Request;
import pl.betacraft.auth.RequestUtil;

public class MicrosoftAuthRequest extends Request {

	public MicrosoftAuthRequest(String code) {
		REQUEST_URL = "https://login.live.com/oauth20_token.srf";
		POST_DATA = "client_id=" + MicrosoftAuth.CLIENT_ID +
				"&code=" + code +
				"&grant_type=authorization_code" +
				"&redirect_uri=" + MicrosoftAuth.REDIRECT_URI;
		PROPERTIES.put("Content-Type", "application/x-www-form-urlencoded");
	}

	@Override
	public MicrosoftAuthResponse perform() {
		Gson gson = new Gson();
		String response = RequestUtil.performPOSTRequest(this);

		MicrosoftAuthResponse ret;
		try {
			ret = gson.fromJson(response, MicrosoftAuthResponse.class);
		} catch (JsonParseException ex) {
			return null;
		}
		return ret;
	}
}
