package org.betacraft.launcher.themes;

import java.awt.Component;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

// Exists because Java dumbness
// Basically, calls to system specific look and feel must be
// contained in a separate class. Otherwise, JNI would complain
// that a look and feel class for a specific OS doesn't exist
// in the current Java installation, and will refuse to start the launcher.
public class WindowsThemeSetter {

	public static void setWindowsLook() throws UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel() {
			// Disable beeps
			@Override
			public void provideErrorFeedback(Component c) {}
		});
	}
}
