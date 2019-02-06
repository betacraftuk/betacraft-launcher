package org.betacraft.launcher;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.VolatileImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Background extends JPanel {
	Image image = null;
	VolatileImage img;

	public Background() {
		setSize(800, 450);
		setLayout(null);
		try {
			image = ImageIO.read(Launcher.class.getResource("/gui/dirt.png")).getScaledInstance(32, 32, 16);
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		}
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
        //g3.setColor(Color.LIGHT_GRAY);
        //final String msg = "Minecraft Launcher";
        //g3.setFont(new Font(null, 1, 20));
        //final FontMetrics fm = g3.getFontMetrics();
        //g3.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 - fm.getHeight() * 2);
        g3.dispose();
        g.drawImage(this.img, 0, 0, w * 2, h * 2, null);

        add(Window.play);
		add(Window.nick);
		add(Window.kazu);
		add(Window.about);
		add(Window.nicktext);
		add(Window.options);
	}

	/*public void paint(final Graphics g2) {
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
        //g3.setColor(Color.LIGHT_GRAY);
        //final String msg = "Minecraft Launcher";
        //g3.setFont(new Font(null, 1, 20));
        //final FontMetrics fm = g3.getFontMetrics();
        //g3.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 - fm.getHeight() * 2);
        g3.dispose();
        g2.drawImage(this.img, 0, 0, w * 2, h * 2, null);

        add(Window.play);
		add(Window.nick);
		add(Window.kazu);
		add(Window.about);
		add(Window.nicktext);
		add(Window.options);
    }*/
}
