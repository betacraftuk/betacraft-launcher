package pl.betacraft.auth.jsons.microsoft;

public class MinecraftProfileResponse extends MinecraftErrorResponse {

	public String id;
	public String name;
	public MinecraftTextureProperty[] skins;
	public MinecraftTextureProperty[] capes;
}
