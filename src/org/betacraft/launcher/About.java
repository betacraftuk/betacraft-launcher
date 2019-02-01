package org.betacraft.launcher;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class About extends JFrame {

	ImageIcon image = new ImageIcon();

	public About() {
		setSize(252, 156);
		setTitle("Betacraft Launcher " + Launcher.VERSION);
		setLayout(null);
		setLocationRelativeTo(null);
		setResizable(false);
	}
}
