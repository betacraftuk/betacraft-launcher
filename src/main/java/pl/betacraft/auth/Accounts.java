package pl.betacraft.auth;

import java.util.List;

public class Accounts {

	public List<Credentials> accounts;
	public String current;

	public boolean addAccount(Credentials c) {
		for (int i = 0; i < this.accounts.size(); i++) {
			if (this.accounts.get(i) == null) {
				continue;
			} else if (c.local_uuid.equals(this.accounts.get(i).local_uuid)) {
				return false;
			}
		}
		accounts.add(c);
		return true;
	}

	public boolean removeAccount(Credentials c) {
		for (int i = 0; i < this.accounts.size(); i++) {
			if (this.accounts.get(i) == null) {
				continue;
			}
			// some cleanup
			if (this.accounts.get(i).local_uuid == null) {
				accounts.remove(i);
				continue;
			}
			if (this.accounts.get(i).local_uuid.equals(c.local_uuid)) {
				accounts.remove(i);
				return true;
			}
		}
		return false;
	}

	public void setCurrent(Credentials c) {
		this.current = c.local_uuid;
	}
}
