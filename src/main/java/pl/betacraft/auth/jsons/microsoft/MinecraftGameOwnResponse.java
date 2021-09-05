package pl.betacraft.auth.jsons.microsoft;

public class MinecraftGameOwnResponse extends MinecraftErrorResponse {

	public MinecraftItemProperty[] items;
	public String signature;
	public String keyId;

	public boolean isEmpty() {
		return this.signature == null && this.keyId == null;
	}
}
