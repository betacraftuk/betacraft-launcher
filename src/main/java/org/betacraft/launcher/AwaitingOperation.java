package org.betacraft.launcher;

import javax.swing.JOptionPane;

public class AwaitingOperation extends Thread {

	public void run() {
		try {
			Object[] options = {Lang.CANCEL};
			JOptionPane.showOptionDialog(Window.mainWindow, "Waiting for login feedback...", Lang.LOGIN_MICROSOFT_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
			Window.mainWindow.input();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
