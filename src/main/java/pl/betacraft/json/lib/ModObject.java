package pl.betacraft.json.lib;

public class ModObject {

	public String infoFileURL;
	public String website;
	public String name;
	// if true, the launcher will always re-download the info file on launch.
	// this is to make updating easy
	public boolean checkUpdate = false;

	@Override
	public String toString() {
		return this.name;
	}
}
