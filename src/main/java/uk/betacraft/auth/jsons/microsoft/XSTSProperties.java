package uk.betacraft.auth.jsons.microsoft;

public class XSTSProperties {

	public String SandboxId = "RETAIL";
	public String[] UserTokens = new String[] {""};

	public XSTSProperties(String xbl_token) {
		UserTokens[0] = xbl_token;
	}
}
