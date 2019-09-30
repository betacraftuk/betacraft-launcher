package org.betacraft.launcher;

import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/*
 * TODO list for 1.10:
 * Wiki ? || Client log || Launcher log || Retrocraft server list
 */
public class InfoPanel extends JPanel {
	Image aroundBackground = null;
	Image renderHelper;

	public InfoPanel(final boolean isConnection) {
		setLayout(null);

		// Load background image
		try {
			aroundBackground = ImageIO.read(Launcher.class.getResource("/icons/stone.png")).getScaledInstance(64, 64, 64);
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		}

		// Putting this into separate thread *might* improve the on-load performance
		new Thread() {
			public void run() {
				add(new WebsitePanel().getUpdateNews(isConnection));
			}
		}.start();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		final int w = this.getWidth() / 2;
		final int h = this.getHeight() / 2;
		if (this.renderHelper == null) {
			this.renderHelper = this.createImage(w, h);
		}
		final Graphics g3 = this.renderHelper.getGraphics();
		for (int x = 0; x <= w / 32; ++x) {
			for (int y = 0; y <= h / 32; ++y) {
				g3.drawImage(this.aroundBackground, x * 32, y * 32, null);
			}
		}
		g3.dispose();
		g.drawImage(this.renderHelper, 0, 0, w * 2, h * 2, null);
	}
}
