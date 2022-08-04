package uk.betacraft.auth.jsons.mojang;

import uk.betacraft.auth.Credentials;

public class Profile {

	public String agent;
	public String id;
	public String name;
	public String userId;
	public long createdAt;
	public boolean legacyProfile;
	public boolean suspended;
	public boolean paid;
	public boolean migrated;

	public Profile(Credentials c) {
		this.id = c.local_uuid;
		this.name = c.username;
	}
}
