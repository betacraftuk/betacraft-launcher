package org.betacraft;

import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.betacraft.launcher.Logger;

public class ClassicMPWrapper extends ClassicWrapper {

	public ClassicMPWrapper(String user, String ver_prefix, String version, String sessionid, String mainFolder, int height,
			int width, boolean RPC, String launchMethod, String server, String mppass, String USR, String VER,
			Image img, ArrayList addons) {
		super(user, ver_prefix, version, sessionid, mainFolder, height, width, RPC, launchMethod, server, mppass, USR, VER,
				img, addons);
	}

	@Override
	public void play() {
		askForServer();
		super.play();
	}

	@Override
	public URL getDocumentBase() {
		try {
			return new URL("http://www.minecraft.net/game/");
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			Logger.printException(e);
			return null;
		}
	}
}
