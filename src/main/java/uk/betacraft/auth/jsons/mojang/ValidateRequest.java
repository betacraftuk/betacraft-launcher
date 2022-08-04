package uk.betacraft.auth.jsons.mojang;

import uk.betacraft.auth.BlankResponse;
import uk.betacraft.auth.Credentials;
import uk.betacraft.auth.Request;
import uk.betacraft.auth.RequestUtil;
import uk.betacraft.auth.Response;

public class ValidateRequest extends Request {

	public String accessToken;
	public String clientToken;

	public ValidateRequest(Credentials c) {
		this.REQUEST_URL = "https://authserver.mojang.com/validate";
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
