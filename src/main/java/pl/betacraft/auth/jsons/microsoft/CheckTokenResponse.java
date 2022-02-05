package pl.betacraft.auth.jsons.microsoft;

public class CheckTokenResponse extends MSErrorResponse { // can respond with error

	public String token_type;
	public String scope;
	public String access_token;
	public String refresh_token;
	public String id_token;
	public long expires_in;
}
