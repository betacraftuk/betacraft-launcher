package pl.betacraft.auth.jsons.microsoft;

import pl.betacraft.auth.Response;

public class MicrosoftAuthResponse extends Response {

	public String token_type;
	public long expires_in;
	public String scope;
	public String access_token;
	public String refresh_token;
	public String user_id;
	public String foci;
}
