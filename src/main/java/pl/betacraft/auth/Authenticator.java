package pl.betacraft.auth;

import javax.swing.SwingUtilities;

import org.betacraft.launcher.Lang;
import org.betacraft.launcher.Launcher;
import org.betacraft.launcher.Window;

public interface Authenticator {

	//public boolean refreshToken();

	public boolean authenticate();

	public boolean invalidate();

	public Credentials getCredentials();

	default void authSuccess() {
		SwingUtilities.invokeLater(() -> {
			Window.nick_input.setText(Launcher.getNickname());
			Window.nick_input.setEnabled(false);
			Window.loginButton.setText(Lang.LOGOUT_BUTTON);
			Window.setTab(Window.tab);
		});
	};
}
