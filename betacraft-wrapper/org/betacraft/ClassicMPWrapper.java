package org.betacraft;

import java.awt.Image;
import java.util.ArrayList;

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
}
