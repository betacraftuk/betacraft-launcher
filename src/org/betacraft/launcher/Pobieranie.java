package org.betacraft.launcher;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class Pobieranie extends JFrame {

	public Pobieranie(String update) {
		Window.window.setVisible(false);
		setSize(360, 80);
		setLayout(null);
		setTitle("Updating launcher");
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setVisible(true);

		JLabel label = new JLabel("Downloading update " + update);
		label.setBounds(30, 10, 340, 20);
		add(label);
	}
}
