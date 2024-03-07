package uk.betacraft.auth.jsons.microsoft;

import com.google.gson.Gson;

import uk.betacraft.auth.MicrosoftAuth;
import uk.betacraft.auth.Request;
import uk.betacraft.auth.RequestUtil;

public class XSTSAuthRequest extends Request {

	public XSTSProperties Properties;
	public String RelyingParty = "rp://api.minecraftservices.com/";
	public String TokenType = "JWT";

	public XSTSAuthRequest(String xbl_token) {
		REQUEST_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
		PROPERTIES.put("Content-Type", "application/json");
		PROPERTIES.put("Accept", "application/json");
		Properties = new XSTSProperties(xbl_token);
		type = RequestType.POST;
	}

	@Override
	public XBLXSTSAuthResponse perform() {
		Gson gson = new Gson();
		String response = MicrosoftAuth.fireAuthRequest(this);

		XBLXSTSAuthResponse ret;
		try {
			ret = gson.fromJson(response, XBLXSTSAuthResponse.class);
		} catch (Throwable ex) {
			return null;
		}
		return ret;
	}
}
