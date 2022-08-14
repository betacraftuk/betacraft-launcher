package org.betacraft.launcher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JTextField;

// For Java 5 support; it can't open browser
public class SimpleWebAddressFrame extends JFrame {

	public SimpleWebAddressFrame(String url) {
		this.setIconImage(Window.img);
		this.setResizable(false);
		this.setTitle(Lang.LINK);
		this.setMinimumSize(new Dimension(300, 100));

		GridBagConstraints constr = new GridBagConstraints();

		constr.gridx = 0;
		constr.gridy = 0;
		constr.gridwidth = 3;
		constr.weightx = 1.0;
		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.insets = new Insets(10, 10, 0, 10);

		JTextField tf = new JTextField(url);
		tf.setEditable(false);
		tf.addMouseListener(AwaitingMSALogin.autofocus);

		this.getContentPane().add(tf, BorderLayout.CENTER);
		this.pack();
		setLocationRelativeTo(Window.mainWindow);
		setVisible(true);
	}
}
