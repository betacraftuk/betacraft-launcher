package uk.betacraft.auth;

import java.util.HashMap;
import java.util.Map;

public abstract class Request {

	public transient String REQUEST_URL;
	public transient String POST_DATA;
	public transient Map<String, String> PROPERTIES = new HashMap<String, String>();

	public abstract Response perform();
}
