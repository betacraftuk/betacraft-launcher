package org.betacraft.launcher;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.Graphics;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AlphaStub extends Applet implements AppletStub {
	final static Map<String, String> params = new HashMap<String, String>();

	public AlphaStub(String username, String sessionid) {
		params.put("username", username);
		params.put("sessionid", sessionid);
	}

	@Override
	public void appletResize(final int width, final int height) {}

	@Override
	public void update(final Graphics g) {
		this.paint(g);
	}

	@Override
    public boolean isActive() {
        return true;
	}

	public void stop() {}

    @Override
    public URL getDocumentBase() {
    	try {
    		return new URL("http://www.minecraft.net/game/");
    	}
    	catch (MalformedURLException e) {
    		e.printStackTrace();
    		return null;
    	}
    }

    @Override
    public URL getCodeBase() {
    	try {
    		return new URL("http://www.minecraft.net/game/");
    	}
    	catch (MalformedURLException e) {
    		e.printStackTrace();
    		return null;
    	}
    }

    @Override
    public String getParameter(final String paramName) {
    	System.err.println("Client asked for parameter: " + paramName);
    	if (params.containsKey(paramName)) {
    		return params.get(paramName);
    	}
    	return null;
    }
}
