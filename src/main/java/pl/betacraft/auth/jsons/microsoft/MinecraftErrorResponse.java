package pl.betacraft.auth.jsons.microsoft;

import pl.betacraft.auth.Response;

public class MinecraftErrorResponse extends Response {

	public String path;
	public String errorType;
	public String error = null;
	public String errorMessage;
	public String developerMessage;
}
