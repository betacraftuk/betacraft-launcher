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

	static JCheckBox retrocraft;
	static JCheckBox open;
	static JLabel label;
	static JButton checkUpdate;
	JButton OK;

	public static String update = "Update check";
	public static String update_not_found = "Couldn't find any newer version of the launcher.";

	public Opcje() {
		Logger.a("Otwarto okno opcji.");
		setSize(350, 386);
		setLayout(null);
		setTitle("Settings");
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);

		retrocraft = new JCheckBox("Use RetroCraft proxy");
		retrocraft.setSelected(Launcher.getProperty(Launcher.SETTINGS, "retrocraft").equals("true") ? true : false);
		retrocraft.setBounds(10, 10, 330, 20);
		this.add(retrocraft);

		open = new JCheckBox("Keep the launcher open");
		open.setSelected(Launcher.getProperty(Launcher.SETTINGS, "keepopen").equals("true") ? true : false);
		open.setBounds(10, 30, 330, 20);
		this.add(open);

		label = new JLabel("Launch arguments:");
		label.setBounds(10, 50, 190, 20);
		label.setForeground(Color.BLACK);
		this.add(label);
		final JTextField field = new JTextField(Launcher.getCustomParameters());
		field.setBounds(25, 75, 300, 25);
		this.add(field);

		OK = new JButton("OK");
		OK.setBounds(10, 320, 60, 20);
		OK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Launcher.setProperty(Launcher.SETTINGS, "launch", "~" + field.getText() + "~");
				Launcher.setProperty(Launcher.SETTINGS, "retrocraft", retrocraft.isSelected() ? "true" : "false");
				Launcher.setProperty(Launcher.SETTINGS, "keepopen", open.isSelected() ? "true" : "false");
				setVisible(false);
			}
		});
		this.add(OK);

		checkUpdate = new JButton("Check for update");
		checkUpdate.setBounds(140, 320, 180, 20);
		this.add(checkUpdate);

		OK.setBackground(Color.LIGHT_GRAY);
		checkUpdate.setBackground(Color.LIGHT_GRAY);

		checkUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Launcher.checkForUpdate()) {
					Launcher.downloadUpdate();
				} else {
					JOptionPane.showMessageDialog(null, update_not_found, update, JOptionPane.INFORMATION_MESSAGE);
				}
				try {
					System.out.println(Window.class.getProtectionDomain().getCodeSource().getLocation().getFile());
				} catch (Exception ex) {
					
				}
			}
		});
		Lang.apply();
	}
}
