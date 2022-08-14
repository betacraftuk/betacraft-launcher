package uk.betacraft.auth.jsons.microsoft;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import uk.betacraft.auth.MicrosoftAuth;
import uk.betacraft.auth.Request;
import uk.betacraft.auth.RequestUtil;

public class DeviceCodeRequest extends Request {

	public DeviceCodeRequest() {
		REQUEST_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";
		PROPERTIES.put("Content-Type", "application/x-www-form-urlencoded");

		POST_DATA = "client_id=" + MicrosoftAuth.CLIENT_ID +
				"&scope=XboxLive.signin offline_access";
	}

	@Override
	public DeviceCodeResponse perform() {
		Gson gson = new Gson();
		String response = RequestUtil.performPOSTRequest(this);

		DeviceCodeResponse ret;
		try {
			ret = gson.fromJson(response, DeviceCodeResponse.class);
		} catch (JsonParseException ex) {
			return null;
		}
		return ret;
	}
}
