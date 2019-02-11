package org.betacraft.launcher;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class LoginPanel extends JPanel {
	Image image = null;
	Image img;

	public LoginPanel() {
		setBounds(0, 290, 160, 800);
		setSize(800, 160); // replace 140 with 450 in case you want to go back
		setLayout(null);
		try {
			image = ImageIO.read(Launcher.class.getResource("/icons/dirt.png")).getScaledInstance(32, 32, 16);
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		}
	}

	public void update(final Graphics graphics) {
        this.paint(graphics);
    }

	public void paintComponent(Graphics g) {
		/*super.paintComponent(g);

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
		g.drawImage(this.img, 0, 0, w * 2, h * 2, null);*/
		final int n = this.getWidth() / 2 + 1;
        final int n2 = this.getHeight() / 2 + 1;
        if (this.img == null || this.img.getWidth(null) != n || this.img.getHeight(null) != n2) {
            this.img = this.createImage(n, n2);
            final Graphics graphics2 = this.img.getGraphics();
            for (int i = 0; i <= n / 32; ++i) {
                for (int j = 0; j <= n2 / 32; ++j) {
                    graphics2.drawImage(this.image, i * 32, j * 32, null);
                }
            }
            if (graphics2 instanceof Graphics2D) {
                final Graphics2D graphics2D = (Graphics2D)graphics2;
                final int n3 = 1;
                graphics2D.setPaint(new GradientPaint(new Point2D.Float(0.0f, 0.0f), new Color(553648127, true), new Point2D.Float(0.0f, n3), new Color(0, true)));
                graphics2D.fillRect(0, 0, n, n3);
                final int n4 = n2;
                graphics2D.setPaint(new GradientPaint(new Point2D.Float(0.0f, 0.0f), new Color(0, true), new Point2D.Float(0.0f, n4), new Color(1610612736, true)));
                graphics2D.fillRect(0, 0, n, n4);
            }
            graphics2.dispose();
        }
        g.drawImage(this.img, 0, 0, n * 2, n2 * 2, null);

		add(Window.play);
		add(Window.nick);
		add(Window.kazu);
		add(Window.about);
		add(Window.nicktext);
		add(Window.options);
		add(Window.lang);
	}
}
