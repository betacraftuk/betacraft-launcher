package org.betacraft.launcher;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.betacraft.launcher.Window.Tab;

/*
 * TODO list for 1.10:
 * Client log || Launcher log
 */
public class InfoPanel extends JPanel {
	static Image aroundBackground = null;
	static Image renderHelper;

	public InfoPanel(final boolean isConnection) {
		//setLayout(new GridBagLayout());
		setLayout(new GridLayout(2,1));
		setMinimumSize(new Dimension(800, 550));

		// Load background image
		try {
			aroundBackground = ImageIO.read(Launcher.class.getResource("/icons/stone.png")).getScaledInstance(32, 32, 64);
		} catch (IOException e2) {
			e2.printStackTrace();
			Logger.printException(e2);
			return;
		}
		final GridBagConstraints constr = new GridBagConstraints();
		constr.gridx = 0;
		constr.fill = GridBagConstraints.NORTH;
		constr.insets = new Insets(0, 0, 0, 0);
		constr.gridwidth = GridBagConstraints.RELATIVE;
		constr.gridy = 0;
		constr.weightx = 1.0;
		constr.gridheight = 1;
		JButton button = new JButton("bruh");
		add(button, constr);
		constr.gridy = 1;
		//constr.weighty = 1.0;
		constr.gridheight = GridBagConstraints.RELATIVE;

		if (Window.tab == Tab.CHANGELOG)
			add(new WebsitePanel().getUpdateNews(isConnection), constr);
		if (Window.tab == Tab.SERVER_LIST)
			add(new WebsitePanel().getServers(isConnection));
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		final int w = this.getWidth() / 2;
		final int h = this.getHeight() / 2;
		if (w <= 0 || h <= 0) return;
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
