package uk.betacraft.auth.jsons.microsoft;

import javax.swing.JOptionPane;

import org.betacraft.launcher.Lang;

import com.google.gson.Gson;

import uk.betacraft.auth.MicrosoftAuth;
import uk.betacraft.auth.Request;
import uk.betacraft.auth.RequestUtil;
import uk.betacraft.util.WebData;

public class XBLAuthRequest extends Request {

	public XBLProperties Properties;
	public String RelyingParty = "http://auth.xboxlive.com";
	public String TokenType = "JWT";

	public XBLAuthRequest(String ms_accessToken) {
		REQUEST_URL = "https://user.auth.xboxlive.com/user/authenticate";
		PROPERTIES.put("Content-Type", "application/json");
		PROPERTIES.put("Accept", "application/json");
		Properties = new XBLProperties(ms_accessToken);
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
