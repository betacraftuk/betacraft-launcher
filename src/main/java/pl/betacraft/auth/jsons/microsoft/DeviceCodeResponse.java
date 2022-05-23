package pl.betacraft.auth.jsons.microsoft;

import pl.betacraft.auth.Response;

public class DeviceCodeResponse extends Response {

	public String device_code;
	public String user_code;
	public String message;
	public String verification_uri;
	public long expires_in;
	public int interval;

	public boolean isEmpty() {
		return this.device_code == null && this.user_code == null && this.message == null && this.verification_uri == null;
	}
}
