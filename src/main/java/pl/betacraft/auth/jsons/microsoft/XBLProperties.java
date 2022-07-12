package pl.betacraft.auth.jsons.microsoft;

import java.net.URLEncoder;

public class XBLProperties {

	public String AuthMethod = "RPS";
	public String SiteName = "user.auth.xboxlive.com";
	public String RpsTicket;

	public XBLProperties(String ms_accessToken) {
		try {
			RpsTicket = "d=" + URLEncoder.encode(ms_accessToken, "UTF-8");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
