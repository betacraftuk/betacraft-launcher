package org.betacraft.launcher;

import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JFrame;

import org.betacraft.launcher.Release.VersionRepository;

import pl.betacraft.json.lib.LaunchMethods;

public class StartThread extends Thread {

	public void run() {
		new Window();
		Window.mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Lang.refresh(true, true);
		new Thread(new Runnable() {
			public void run() {
				boolean release = (BC.prerelease || BC.nightly) ? false : true;
				if (Launcher.checkForUpdate(release)) {
					if (!BC.nightly) Launcher.downloadUpdate(release);
				}
			}
		}).start();
		if (!Launcher.auth.authenticate()) {
			Launcher.accounts.removeAccount(Launcher.auth.getCredentials());
			// replacement for broken account
			Launcher.auth = Util.getAuthenticator(Launcher.accounts.accounts.get(0));
			Launcher.accounts.setCurrent(Launcher.accounts.accounts.get(0));
			Launcher.auth.authenticate();
		}
		try {
			Release.loadVersions(VersionRepository.BETACRAFT);
			org.betacraft.launcher.Addon.loadAddons();
			ModsRepository.loadMods();

			URL launchmethods = new URL("http://files.betacraft.uk/launcher/assets/launch-methods/list.json");
			Launcher.launchMethods = Util.gson.fromJson(
					new InputStreamReader(
							launchmethods.openStream(), "UTF-8"), LaunchMethods.class);
		} catch (Throwable ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
		Window.mainWindow.setVisible(true);
		Launcher.totalThreads.remove(this);
	}
}
