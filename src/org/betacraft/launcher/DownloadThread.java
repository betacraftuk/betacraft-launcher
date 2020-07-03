package org.betacraft.launcher;

import javax.swing.JFrame;

import org.betacraft.launcher.Release.VersionRepository;

public class DownloadThread extends Thread {

	public void run() {
		new Window();
		Window.mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Window.mainWindow.setVisible(true);
		Lang.refresh(true, true);
		new Thread(new Runnable() {
			public void run() {
				boolean release = (BC.prerelease || BC.nightly) ? false : true;
				if (Launcher.checkForUpdate(release)) {
					if (!BC.nightly) Launcher.downloadUpdate(release);
				}
			}
		}).start();
		if (!MojangLogging.password.equals("")) {
			new MojangLogging().authenticate(MojangLogging.email, MojangLogging.password);
		}
		try {
			Release.loadVersions(VersionRepository.BETACRAFT);
			org.betacraft.launcher.Addon.loadAddons();
			ModsRepository.loadMods();
		} catch (Throwable ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
		Launcher.totalThreads.remove(this);
	}
}
