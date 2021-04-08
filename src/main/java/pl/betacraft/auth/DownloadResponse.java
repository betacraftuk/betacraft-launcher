package pl.betacraft.auth;

import org.betacraft.launcher.DownloadResult;

public class DownloadResponse extends Response {

	public DownloadResult result;
	public String err_response;

	public DownloadResponse(DownloadResult result, String res) {
		this.result = result;
		this.err_response = res;
	}
}
