package uk.betacraft.auth;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.betacraft.launcher.Lang;
import org.betacraft.launcher.Launcher;
import org.betacraft.launcher.Window;

import uk.betacraft.auth.Credentials.AccountType;
import uk.betacraft.auth.jsons.microsoft.CheckTokenRequest;
import uk.betacraft.auth.jsons.microsoft.CheckTokenResponse;
import uk.betacraft.auth.jsons.microsoft.MinecraftAuthRequest;
import uk.betacraft.auth.jsons.microsoft.MinecraftAuthResponse;
import uk.betacraft.auth.jsons.microsoft.MinecraftGameOwnRequest;
import uk.betacraft.auth.jsons.microsoft.MinecraftGameOwnResponse;
import uk.betacraft.auth.jsons.microsoft.MinecraftProfileRequest;
import uk.betacraft.auth.jsons.microsoft.MinecraftProfileResponse;
import uk.betacraft.auth.jsons.microsoft.XBLAuthRequest;
import uk.betacraft.auth.jsons.microsoft.XBLXSTSAuthResponse;
import uk.betacraft.auth.jsons.microsoft.XSTSAuthRequest;
import uk.betacraft.util.WebData;

public class MicrosoftAuth extends Authenticator {

	public static final String CLIENT_ID = "8075fa74-4091-4356-a0b8-a7c118ef121c";

	public Credentials credentials = null;

	public MicrosoftAuth(Credentials c) {
		this.credentials = c;
	}

	private void clearFields() {
		if (this.credentials != null) Launcher.accounts.removeAccount(this.credentials);
		this.credentials = null;
	}

	public boolean authenticate() {
		if (this.credentials.refresh_token == null)
			return false;
		
		CheckTokenResponse ctres = new CheckTokenRequest(null, this.credentials.refresh_token).perform();

		if (ctres == null) {
			// token most likely timed out
			displayError(null, Lang.LOGIN_RELOGIN, Lang.LOGIN_MICROSOFT_ERROR);
			return false;
		}

		// Xbox stuff
		XBLXSTSAuthResponse xblres = new XBLAuthRequest(ctres.access_token).perform();
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
				System.out.println("No Xbox account registered");
				displayError(xstsres, Lang.LOGIN_MICROSOFT_NO_XBOX, Lang.LOGIN_MICROSOFT_ERROR);
				// no xbox account registered
				return false;
			} else if (xstsres.XErr == 2148916238L) {
				System.out.println("PARENTAL CONTROL");
				displayError(xstsres, Lang.LOGIN_MICROSOFT_PARENT, Lang.LOGIN_MICROSOFT_ERROR);
				// parental control thingy, user has to be added to a Family by an adult
				return false;
			} else {
				System.out.println("Unexpected error: " + xstsres.XErr);
				displayError(xstsres, String.format(Lang.UNEXPECTED_ERROR, xstsres.XErr), Lang.LOGIN_MICROSOFT_ERROR);
			}
		}

		MinecraftAuthResponse mcres = new MinecraftAuthRequest(xblres.DisplayClaims.xui[0].uhs, xstsres.Token).perform();
		if (mcres == null || mcres.isEmpty()) {
			System.out.println("MinecraftAuth failed!");
			displayError(mcres, Lang.LOGIN_MICROSOFT_NO_MINECRAFT, Lang.LOGIN_MICROSOFT_ERROR);
			return false;
		}

		MinecraftGameOwnResponse mcgores = new MinecraftGameOwnRequest(mcres.access_token).perform();
		if (mcgores == null || mcgores.isEmpty()) {
			System.out.println("MinecraftOwnership failed!");
			displayError(mcgores, Lang.LOGIN_MICROSOFT_NO_MINECRAFT, Lang.LOGIN_MICROSOFT_ERROR);
			return false;
		}

		if (mcgores.items == null || mcgores.items.length == 0) {
			if (mcgores.error != null) {
				displayError(mcgores, Lang.LOGIN_MICROSOFT_NO_MINECRAFT, Lang.LOGIN_MICROSOFT_ERROR);
			}
			return false;
		}

		MinecraftProfileResponse mcpres = new MinecraftProfileRequest(mcres.access_token).perform();
		if (mcpres == null || mcpres.isEmpty()) {
			System.out.println("MinecraftProfile failed!");
			displayError(mcpres, Lang.LOGIN_MICROSOFT_NO_MINECRAFT, Lang.LOGIN_MICROSOFT_ERROR);
			return false;
		}

		if (mcpres.error != null) {
			displayError(mcpres, Lang.LOGIN_MICROSOFT_NO_MINECRAFT, Lang.LOGIN_MICROSOFT_ERROR);
			return false;
		} else {
			this.clearFields();
			this.credentials = new Credentials();
			this.credentials.expires_at = ctres.expires_in*1000 + System.currentTimeMillis();
			this.credentials.refresh_token = ctres.refresh_token;
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

	public static void displayError(Object error, final String title, final String msg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(Window.mainWindow, msg, title, JOptionPane.ERROR_MESSAGE);
			}
		});

		if (error == null)
			return;

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
	}

	public static String fireAuthRequest(Request req) {
		WebData data = RequestUtil.performRawPOSTRequest(req);

		if (data.getResponseCode() == -2) {
			JOptionPane.showMessageDialog(null, String.format(Lang.JAVA_SSL_NOT_SUPPORTED, Lang.JAVA_SSL_TO_MICROSOFT_ACCOUNT), "", JOptionPane.ERROR_MESSAGE);
		}

		return RequestUtil.webDataToString(data);
	}
}
