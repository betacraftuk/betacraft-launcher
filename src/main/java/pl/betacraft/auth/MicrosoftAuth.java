package pl.betacraft.auth;

import org.betacraft.launcher.Lang;
import org.betacraft.launcher.Launcher;
import org.betacraft.launcher.Window;
import pl.betacraft.auth.Credentials.AccountType;
import pl.betacraft.auth.jsons.microsoft.*;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class MicrosoftAuth implements Authenticator {

	public static final String AUTH_URI = "/msaresponse";
	public static final String REDIRECT_URI = "http://localhost:11799/msaresponse";
	public static final String CLIENT_ID = "8075fa74-4091-4356-a0b8-a7c118ef121c";

	public String code;
	public Credentials credentials;

	public MicrosoftAuth(Credentials c) {
		this.credentials = c;
	}

	private void clearFields() {
		if (this.credentials != null) Launcher.accounts.removeAccount(this.credentials);
		this.credentials = null;
		this.code = null;
	}
	private boolean authError(String langError, String error) {
		return authError(langError, error, null);
	}

	private boolean authError(String langError, String error, Object mcres) {
		if (mcres != null) {
			System.out.println("-Stack of " + error.getClass().getSimpleName() + "-");
			for (Field f : error.getClass().getDeclaredFields()) {
				if (!Modifier.isTransient(f.getModifiers())) {
					f.setAccessible(true);
					try {
						System.out.println(f.getName() + "=" + f.get(error));
					} catch (Throwable t) {}
				}
			}
			System.out.println("-----------------");
			return false;
		}
		if (error != null) System.out.println(error);
		if (langError != null) {
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(Window.mainWindow, langError, Lang.LOGIN_MICROSOFT_ERROR, JOptionPane.ERROR_MESSAGE);
			});
		}
		Launcher.clearCookies();
		return false;
	}

	public boolean authenticate() {
		MicrosoftAuthResponse msres = null;
		if (this.code == null && (this.credentials == null || this.credentials.refresh_token == null)) {
			msres = new MicrosoftAuthRequest(this.code).perform();
		} else if (this.credentials != null && this.credentials.refresh_token != null) {
			msres = new MicrosoftRefreshRequest(this.credentials.refresh_token).perform();
		} else if (this.code != null) { // how
			msres = new MicrosoftAuthRequest(this.code).perform();
		}
		if (msres == null || msres.isEmpty()) {
			System.out.println("MicrosoftAuth failed!");
			return false;
		}

		// Xbox stuff
		XBLXSTSAuthResponse xblres = new XBLAuthRequest(msres.access_token).perform();
		if (xblres == null || xblres.isEmpty()) {
			System.out.println("XBL failed!");
			return false;
		}

		XBLXSTSAuthResponse xstsres = new XSTSAuthRequest(xblres.Token).perform();
		if (xstsres == null || xblres.isEmpty()) {
			System.out.println("XSTS failed!");
			return false;
		}

		if (xstsres.Identity != null) {
			if (xstsres.XErr == 2148916233L) {
				return authError(Lang.LOGIN_MICROSOFT_NO_XBOX, "No Xbox account registered");
			} else if (xstsres.XErr == 2148916238L) {
				return authError(Lang.LOGIN_MICROSOFT_PARENT, "PARENTAL CONTROL");
			}
			return authError(String.format(Lang.UNEXPECTED_ERROR, xstsres.XErr), "Unexpected error: " + xstsres.XErr);
		}

		MinecraftAuthResponse mcres = new MinecraftAuthRequest(xblres.DisplayClaims.xui[0].uhs, xstsres.Token).perform();
		if (mcres == null || mcres.isEmpty()) {
			System.out.println("MinecraftAuth failed!");
			return false;
		}

		MinecraftGameOwnResponse mcgores = new MinecraftGameOwnRequest(mcres.access_token).perform();
		if (mcgores == null || mcgores.isEmpty()) {
			System.out.println("MinecraftOwnership failed!");
			return false;
		}

		if (mcgores.items == null || mcgores.items.length == 0) {
			return authError(Lang.LOGIN_MICROSOFT_NO_MINECRAFT, null, null);
		}

		MinecraftProfileResponse mcpres = new MinecraftProfileRequest(mcres.access_token).perform();
		if (mcpres == null || mcpres.isEmpty()) {
			System.out.println("MinecraftProfile failed!");
			return false;
		}

		if (mcpres.error != null) {
			authError(null, null, mcpres);
		} else {
			this.clearFields();
			this.credentials = new Credentials();
			this.credentials.refresh_token = msres.refresh_token;
			this.credentials.access_token = mcres.access_token;
			this.credentials.username = mcpres.name;
			this.credentials.local_uuid = mcpres.id;
			this.credentials.account_type = AccountType.MICROSOFT;
			Launcher.accounts.addAccount(this.credentials);

			Launcher.accounts.setCurrent(this.getCredentials());
			Launcher.auth = this;
			this.authSuccess();

			System.out.println("USERNAME: " + this.credentials.username);
			System.out.println("ACC_UUID: " + this.credentials.local_uuid);
		}
		return true;
	}

	public boolean invalidate() {
		// TODO: figure this out maybe
		this.clearFields();
		return true;
	}

	public Credentials getCredentials() {
		return this.credentials;
	}
}