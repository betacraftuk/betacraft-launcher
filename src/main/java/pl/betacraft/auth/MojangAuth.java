package pl.betacraft.auth;

import java.util.UUID;

import javax.swing.JOptionPane;

import org.betacraft.launcher.Lang;
import org.betacraft.launcher.Launcher;
import org.betacraft.launcher.Window;

import pl.betacraft.auth.Credentials.AccountType;
import pl.betacraft.auth.jsons.mojang.AuthRequest;
import pl.betacraft.auth.jsons.mojang.AuthResponse;
import pl.betacraft.auth.jsons.mojang.InvalidateRequest;
import pl.betacraft.auth.jsons.mojang.RefreshRequest;

public class MojangAuth implements Authenticator {

	private Credentials credentials;
	private String username;
	private String password;

	public MojangAuth(Credentials c) {
		this.credentials = c;
	}

	public MojangAuth(String username, String password) {
		this.username = username;
		this.password = password;
	}

	private void clearFields() {
		if (this.credentials != null) Launcher.accounts.removeAccount(this.credentials);
		this.credentials = null;
		this.username = null;
		this.password = null;
	}

	public boolean authenticate() {
		AuthResponse res = null;
		if (this.credentials != null) {
			res = new RefreshRequest(this.credentials, false).perform();
		} else if (this.password != null && this.username != null) {
			res = new AuthRequest(username, password, UUID.randomUUID().toString(), false).perform();
		} else {
			return false;
		}

		if (res == null || res.selectedProfile == null) {
			JOptionPane.showMessageDialog(Window.mainWindow, Lang.LOGIN_FAILED_INVALID_CREDENTIALS, Lang.LOGIN_FAILED, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		this.clearFields();
		this.credentials = new Credentials();
		this.credentials.access_token = res.accessToken;
		this.credentials.local_uuid = res.selectedProfile.id;
		this.credentials.username = res.selectedProfile.name;
		this.credentials.refresh_token = res.clientToken;
		this.credentials.account_type = AccountType.MOJANG;
		Launcher.accounts.addAccount(this.credentials);
		this.authSuccess();
		System.out.println("USERNAME: " + this.credentials.username);
		System.out.println("ACC_UUID: " + this.credentials.local_uuid);
		return true;
	}

	public boolean invalidate() {
		Response res = null;
		if (this.credentials == null) {
			return false;
		}
		res = new InvalidateRequest(this.credentials).perform();
		this.clearFields();
		if (res != null && res instanceof BlankResponse) {
			return true;
		} else {
			return false;
		}
	}

	public Credentials getCredentials() {
		return this.credentials;
	}
}
