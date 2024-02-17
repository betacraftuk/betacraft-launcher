package uk.betacraft.util;

public class WebData {

	private final byte[] data;
	private final int http;
	
	public WebData(byte[] data, int http) {
		this.data = data;
		this.http = http;
	}

	public byte[] getData() {
		return this.data;
	}

	public int getResponseCode() {
		return this.http;
	}

	public boolean successful() {
		return this.http < 400 && this.http >= 200;
	}
}
