package org.betacraft.launcher;

import javax.swing.JFrame;

import org.betacraft.launcher.Release.VersionRepository;

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
		if (Util.old_lastlogin.exists()) {
			Util.old_lastlogin.delete();
		}
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
		} catch (Throwable ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
		Window.mainWindow.setVisible(true);
		Launcher.totalThreads.remove(this);
	}
}
