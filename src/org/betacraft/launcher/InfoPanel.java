package org.betacraft.launcher;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.VolatileImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/*
 * Planowane zakladki:
 * Wiki || Client log || Launcher log
 */
public class InfoPanel extends JPanel {
	Image image = null;
	VolatileImage img;
	Divider div;

	public InfoPanel() {
		//setSize(800, 290);
		setLayout(null);
		try {
			image = ImageIO.read(Launcher.class.getResource("/icons/stone.png")).getScaledInstance(32, 32, 16);
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		}
		add(new WebsitePanel().getUpdateNews());
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		final int w = this.getWidth() / 2;
		final int h = this.getHeight() / 2;
		if (this.img == null || this.img.getWidth() != w || this.img.getHeight() != h) {
			this.img = this.createVolatileImage(w, h);
		}
		final Graphics g3 = this.img.getGraphics();
		for (int x = 0; x <= w / 32; ++x) {
			for (int y = 0; y <= h / 32; ++y) {
				g3.drawImage(this.image, x * 32, y * 32, null);
			}
		}
		g3.dispose();
		g.drawImage(this.img, 0, 0, w * 2, h * 2, null);
		div = new Divider();
	}

	class Divider {
		public Divider() {
			
		}
	}
}
