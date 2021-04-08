package pl.betacraft.json.cert;

import pl.betacraft.auth.Request;
import pl.betacraft.auth.RequestUtil;

public class CertRequest extends Request {

	public CertRequest() {
		this.REQUEST_URL = "http://betacraft.pl/api/certificate.jsp";
	}

	public CertResponse perform() {
		String response = RequestUtil.performGETRequest(this);

		return new CertResponse(response);
	}

}
