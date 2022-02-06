package pl.betacraft.auth;

import java.util.UUID;

import org.betacraft.launcher.Lang;
import org.betacraft.launcher.Launcher;
import org.betacraft.launcher.Window;

import pl.betacraft.auth.Credentials.AccountType;

public class NoAuth extends Authenticator {
	private Credentials credentials = new Credentials();

	public NoAuth(Credentials c) {
		this.credentials = c;
	}

	public NoAuth(String username) {
		this(username, UUID.randomUUID().toString());
	}

	public NoAuth(String username, String uuid) {
		this.credentials.username = username;
		this.credentials.local_uuid = uuid;
		this.credentials.account_type = AccountType.OFFLINE;
	}

	public boolean authenticate() {
		this.authSuccess();
		return true;
	}

	@Override
	public void authSuccess() {
		Window.nick_input.setText(Launcher.getNickname());
		Window.nick_input.setEnabled(true);
		Window.loginButton.setText(Lang.LOGIN_BUTTON);
	}

	public boolean invalidate() {
		this.credentials = null;
		return true;
	}

	public Credentials getCredentials() {
		return this.credentials;
	}

}
