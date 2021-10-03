package pl.betacraft.auth.jsons.microsoft;

import java.net.URL;

import pl.betacraft.auth.Response;

public class XSTSErrorResponse extends Response {

	public String Identity;
	public Long XErr = -1L;
	public String Message;
	public URL Redirect;
}
