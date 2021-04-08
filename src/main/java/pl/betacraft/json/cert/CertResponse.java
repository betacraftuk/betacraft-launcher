package pl.betacraft.json.cert;

import org.betacraft.launcher.Util;

import pl.betacraft.auth.Response;

public class CertResponse extends Response {
	public CertInfo cert_info;

	public CertResponse(String info) {
		this.cert_info = Util.gsonPretty.fromJson(info, CertInfo.class);
	}
}
