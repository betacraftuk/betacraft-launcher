package pl.betacraft.auth.jsons.mojang;

import pl.betacraft.auth.Credentials;
import pl.betacraft.auth.Request;
import pl.betacraft.auth.RequestUtil;
import pl.betacraft.auth.Response;
import pl.betacraft.auth.BlankResponse;

public class InvalidateRequest extends Request {

	public String accessToken;
	public String clientToken;

	public InvalidateRequest(Credentials c) {
		this.REQUEST_URL = "https://authserver.mojang.com/invalidate";
		this.PROPERTIES.put("Content-Type", "application/json");
		this.accessToken = c.access_token;
		this.clientToken = c.refresh_token;
	}

	@Override
	public Response perform() {
		String response = RequestUtil.performPOSTRequest(this);

		if (response != null && response.equals("")) {
			return new BlankResponse();
		} else {
			return null;
		}
	}
}
