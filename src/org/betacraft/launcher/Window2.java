package org.betacraft.launcher;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Window2 extends JFrame {

	public Window2() {
		setSize(800, 450);
		setResizable(false);
		setLocationRelativeTo(null);
		setBackground(Color.BLACK);

		EEPanel panel = new EEPanel();
		add(panel);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	class EEPanel extends JPanel {
		Image image = null;
		Image img;

		EEPanel() {
			setLayout(null);
			setBackground(Color.BLACK);
			try {
				image = ImageIO.read(Launcher.class.getResource("/icons/hassus.png"));
				setIconImage(image);
			} catch (IOException e2) {
				e2.printStackTrace();
				return;
			}
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			final int w = this.getWidth() / 2;
			final int h = this.getHeight() / 2;
			if (this.img == null) {
				this.img = this.createImage(w, h);
			}
			final Graphics g3 = this.img.getGraphics();
			g3.drawImage(this.image, 0, 0, w, h, null);
			g3.dispose();
			g.drawImage(this.img, 0, 0, w * 2, h * 2, null);
		}
	}
}
