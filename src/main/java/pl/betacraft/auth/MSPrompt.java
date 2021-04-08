package pl.betacraft.auth;

import java.awt.Dimension;

import javax.swing.JFrame;

import org.betacraft.launcher.Lang;
import org.betacraft.launcher.LoginPanel;
import org.betacraft.launcher.Window;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class MSPrompt extends JFrame {
	private MicrosoftAuth auth;

	WebView browser;
	WebEngine webEngine;

	public MSPrompt(MicrosoftAuth auth) {
		System.out.println("Opening Microsoft auth prompt");
		this.setAuth(auth);
		this.setIconImage(Window.img);
		this.setTitle(Lang.LOGIN_MICROSOFT_TITLE);
		this.setResizable(true);
		this.setMinimumSize(new Dimension(640, 480));

		JFXPanel jfxPanel = new JFXPanel();
		this.add(jfxPanel);
		Platform.runLater(() -> {
			browser = new WebView();
			webEngine = browser.getEngine();

			jfxPanel.setScene(new Scene(browser));

			webEngine.setJavaScriptEnabled(true);
			webEngine.locationProperty().addListener((obs, oldLocation, newLocation) -> {
				if (newLocation != null && newLocation.startsWith(MicrosoftAuth.REDIRECT_URI)) {
					this.auth.code = newLocation.substring(newLocation.indexOf("=")+1, newLocation.indexOf("&"));
					setVisible(false);
					LoginPanel.continueMSA(this.auth);
				}
			});
			webEngine.load("https://login.live.com/oauth20_authorize.srf" + 
					"?client_id=" + MicrosoftAuth.CLIENT_ID + 
					"&response_type=code" + 
					"&scope=service::user.auth.xboxlive.com::MBI_SSL" +
					"&redirect_uri=" + MicrosoftAuth.REDIRECT_URI);
			//reShow();
		});
		this.setLocationRelativeTo(Window.mainWindow);
		this.setVisible(true);
	}

	public void setAuth(MicrosoftAuth auth) {
		this.auth = auth;
	}

	public void reShow() {
		webEngine.reload();
		setVisible(true);
	}
}