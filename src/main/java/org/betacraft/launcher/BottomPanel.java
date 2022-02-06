package org.betacraft.launcher;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.geom.Point2D;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class BottomPanel extends JPanel {
	Image image = null;
	Image img;

	public BottomPanel() {
		setLayout(new GridBagLayout());
		try {
			image = ImageIO.read(Launcher.class.getResource("/icons/dirt.png")).getScaledInstance(32, 32, 16);
		} catch (IOException e2) {
			e2.printStackTrace();
			Logger.printException(e2);
			return;
		}


		GridBagConstraints constr = new GridBagConstraints();

		JPanel left = new JPanel();
		left.setLayout(new GridBagLayout());
		left.setOpaque(false);

		constr.gridx = 0;
		constr.fill = GridBagConstraints.BOTH;
		constr.weightx = 0.0;
		constr.weighty = 1.0;
		constr.gridwidth = GridBagConstraints.RELATIVE;
		constr.gridy = 1;
		constr.insets = new Insets(25, 50, 2, 50);
		left.add(Window.selectVersionButton, constr);

		constr.gridy = 2;
		constr.insets = new Insets(0, 50, 0, 50);
		left.add(Window.settingsButton, constr);

		JPanel center = new JPanel();
		center.setLayout(new GridBagLayout());
		center.setOpaque(false);

		constr.gridy = 0;
		constr.insets = new Insets(10, 2, 4, 0);
		constr.weightx = 0.0;
		constr.gridwidth = GridBagConstraints.RELATIVE;
		constr.fill = GridBagConstraints.BOTH;
		constr.gridx = 1;
		center.add(Window.nick_input, constr);

		constr.insets = new Insets(10, 2, 4, 0);
		constr.gridx = 2;
		center.add(Window.loginButton, constr);

		constr.gridy = 1;
		constr.gridx = 1;
		constr.weightx = 0.0;
		constr.gridwidth = 2;
		constr.fill = GridBagConstraints.BOTH;
		constr.insets = new Insets(0, 2, 0, 0);
		center.add(Window.playButton, constr);

		JPanel right = new JPanel();
		right.setLayout(new GridBagLayout());
		right.setOpaque(false);

		constr.gridx = 0;
		constr.ipady = 0;
		constr.gridy = 0;
		constr.insets = new Insets(25, 50, 2, 50);
		constr.gridwidth = 1;
		constr.weightx = 0.0;
		constr.weighty = 0.0;
		constr.fill = GridBagConstraints.BOTH;
		right.add(Window.langButton, constr);

		JPanel south = new JPanel();
		south.setLayout(new GridBagLayout());
		south.setOpaque(false);

		constr.gridy = 0;
		constr.fill = GridBagConstraints.BOTH;
		constr.gridwidth = 1;
		constr.gridx = 0;
		constr.weightx = 1.0;
		constr.insets = new Insets(4, 10, 10, 0);
		south.add(Window.selectedInstanceDisplay, constr);


		GridBagConstraints constr1 = new GridBagConstraints();

		constr1.fill = GridBagConstraints.BOTH;
		constr1.gridwidth = 1;
		constr1.gridx = 0;
		constr1.gridy = 1;
		constr1.weighty = 0.0;
		constr1.weightx = 1.0;
		constr1.insets = new Insets(0, 0, 0, 0);
		this.add(left, constr1);

		constr1.gridx = 1;
		constr1.fill = GridBagConstraints.HORIZONTAL;
		this.add(center, constr1);

		constr1.gridx = 2;
		this.add(right, constr1);

		constr1.gridx = 0;
		constr1.gridy = 2;
		constr1.fill = GridBagConstraints.BOTH;
		constr1.gridwidth = 3;
		constr1.weightx = 1.0;
		this.add(south, constr1);
	}

	public void update(final Graphics graphics) {
		this.paint(graphics);
	}

	public void paintComponent(Graphics g) {
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
	}
}
