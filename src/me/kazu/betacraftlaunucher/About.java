package me.kazu.betacraftlaunucher;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
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
