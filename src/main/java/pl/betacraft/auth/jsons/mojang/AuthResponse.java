package pl.betacraft.auth.jsons.mojang;

public class AuthResponse extends BaseResponse {

	public String accessToken;
	public String clientToken;
	public Profile[] availableProfiles;
	public Profile selectedProfile;
	public Userdata user;
}
