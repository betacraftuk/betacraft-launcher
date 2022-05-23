package org.betacraft.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class BrowserWindow extends JFrame implements LanguageElement {

	public BrowserWindow(JScrollPane scrlp) {
		Logger.a("Opened info viewer.");
		this.setIconImage(Window.img);
		this.setBackground(Color.BLACK);
		this.setMinimumSize(new Dimension(360, 360));
		this.setPreferredSize(new Dimension(360, 360));
		this.setTitle(Lang.BROWSER_TITLE);
		this.setResizable(true);
		this.setLayout(new BorderLayout());

		this.add(scrlp, BorderLayout.CENTER);
		this.pack();
		this.setLocationRelativeTo(Window.mainWindow);
		this.setVisible(true);
	}

	public void update() {
		this.setTitle(Lang.BROWSER_TITLE);
	}
}
