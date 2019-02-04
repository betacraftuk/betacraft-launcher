package org.betacraft.launcher;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class Opcje extends JFrame {

	JButton OK;

	public Opcje() {
		Logger.a("Otwarto okno opcji.");
		setSize(350, 386);
		setLayout(null);
		setTitle("Settings");
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);

		JCheckBox retrocraft = new JCheckBox("Use RetroCraft proxy");
		retrocraft.setSelected(false);
		retrocraft.setBounds(10, 10, 330, 20);
		this.add(retrocraft);

		JCheckBox open = new JCheckBox("Keep the launcher open");
		open.setSelected(false);
		open.setBounds(10, 30, 330, 20);
		this.add(open);

		JLabel label = new JLabel("Launch arguments:");
		label.setBounds(10, 50, 190, 20);
		label.setForeground(Color.BLACK);
		this.add(label);
		JTextField field = new JTextField();
		field.setBounds(25, 75, 300, 25);
		this.add(field);

		OK = new JButton("OK");
		OK.setBounds(10, 320, 60, 20);
		this.add(OK);

		JButton checkUpdate = new JButton("Check for update");
		checkUpdate.setBounds(140, 320, 180, 20);
		this.add(checkUpdate);

		OK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		checkUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Launcher.checkForUpdate()) {
					Launcher.downloadUpdate();
				} else {
					JOptionPane.showMessageDialog(null, "Couldn't find any newer version of the launcher.", "Update check", JOptionPane.INFORMATION_MESSAGE);
				}
				try {
					System.out.println(Window.class.getProtectionDomain().getCodeSource().getLocation().getFile());
				} catch (Exception ex) {
					
				}
			}
		});
	}
}
