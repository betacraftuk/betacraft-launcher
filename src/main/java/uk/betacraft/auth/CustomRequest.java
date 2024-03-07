package uk.betacraft.auth;

import java.util.Map;

public class CustomRequest extends Request {

	public CustomRequest(String url) {
		this(url, null, null, RequestType.GET);
	}

	public CustomRequest(String url, String post_data, Map<String, String> properties, RequestType type) {
		this.type = type;
		this.REQUEST_URL = url;
		if (post_data != null) this.POST_DATA = post_data;
		if (properties != null) this.PROPERTIES = properties;
	}

	public CustomResponse perform() {
		String response = null;
		if (this.type == RequestType.POST) {
			response = RequestUtil.performPOSTRequest(this);
		} else {
			response = RequestUtil.performGETRequest(this);
		}

		return new CustomResponse(response);
	}
}
