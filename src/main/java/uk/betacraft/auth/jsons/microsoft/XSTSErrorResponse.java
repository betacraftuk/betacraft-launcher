package uk.betacraft.auth.jsons.microsoft;

import java.net.URL;

import uk.betacraft.auth.Response;

public class XSTSErrorResponse extends Response {

	public String Identity;
	public long XErr = -1L;
	public String Message;
	public URL Redirect;
}
