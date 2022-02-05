package pl.betacraft.auth;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.betacraft.launcher.Lang;
import org.betacraft.launcher.Launcher;
import org.betacraft.launcher.Window;

import pl.betacraft.auth.Credentials.AccountType;
import pl.betacraft.auth.jsons.microsoft.CheckTokenRequest;
import pl.betacraft.auth.jsons.microsoft.CheckTokenResponse;
import pl.betacraft.auth.jsons.microsoft.MinecraftAuthRequest;
import pl.betacraft.auth.jsons.microsoft.MinecraftAuthResponse;
import pl.betacraft.auth.jsons.microsoft.MinecraftGameOwnRequest;
import pl.betacraft.auth.jsons.microsoft.MinecraftGameOwnResponse;
import pl.betacraft.auth.jsons.microsoft.MinecraftProfileRequest;
import pl.betacraft.auth.jsons.microsoft.MinecraftProfileResponse;
import pl.betacraft.auth.jsons.microsoft.XBLAuthRequest;
import pl.betacraft.auth.jsons.microsoft.XBLXSTSAuthResponse;
import pl.betacraft.auth.jsons.microsoft.XSTSAuthRequest;

public class MicrosoftAuth implements Authenticator {

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
				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(Window.mainWindow, Lang.LOGIN_MICROSOFT_NO_XBOX, Lang.LOGIN_MICROSOFT_ERROR, JOptionPane.ERROR_MESSAGE);
				});
				Launcher.clearCookies();
				// no xbox account registered
				return false;
			} else if (xstsres.XErr == 2148916238L) {
				System.out.println("PARENTAL CONTROL");
				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(Window.mainWindow, Lang.LOGIN_MICROSOFT_PARENT, Lang.LOGIN_MICROSOFT_ERROR, JOptionPane.ERROR_MESSAGE);
				});
				Launcher.clearCookies();
				// parental control thingy, user has to be added to a Family by an adult
				return false;
			} else {
				System.out.println("Unexpected error: " + xstsres.XErr);
				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(Window.mainWindow, String.format(Lang.UNEXPECTED_ERROR, xstsres.XErr), Lang.LOGIN_MICROSOFT_ERROR, JOptionPane.ERROR_MESSAGE);
				});
				Launcher.clearCookies();
			}
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
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(Window.mainWindow, Lang.LOGIN_MICROSOFT_NO_MINECRAFT, Lang.LOGIN_MICROSOFT_ERROR, JOptionPane.ERROR_MESSAGE);
			});
			Launcher.clearCookies();
			if (mcgores.error != null) {
				displayError(mcgores);
			}
			return false;
		}

		MinecraftProfileResponse mcpres = new MinecraftProfileRequest(mcres.access_token).perform();
		if (mcpres == null || mcpres.isEmpty()) {
			System.out.println("MinecraftProfile failed!");
			return false;
		}

		if (mcpres.error != null) {
			displayError(mcpres);
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

	public static void displayError(Object error) {
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
}
