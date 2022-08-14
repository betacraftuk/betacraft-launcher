package uk.betacraft.auth.jsons.mojang;

public class Userdata {

	public String id;
	public String email;
	public String username;
	public String registerIp;
	public String migratedFrom;
	public long migratedAt;
	public long registeredAt;
	public long passwordChangedAt;
	public long dateOfBirth;
	public boolean suspended = false;
	public boolean blocked = false;
	public boolean secured;
	public boolean migrated = false; // Always false if you don't use Microsoft authentication
	public boolean emailVerified = true;
	public boolean legacyUser = false;
	public boolean verifiedByParent = false;
	public Property[] properties;
}
