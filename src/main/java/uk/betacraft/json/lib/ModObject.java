package uk.betacraft.json.lib;

public class ModObject {

	public String info_file_url;
	public String website;
	public String full_name;
	public String version;
	// if true, the launcher will always re-download the info file on launch.
	// this is to make updating easy
	public boolean autoupdate = false;

	@Override
	public String toString() {
		return this.version;
	}
}
