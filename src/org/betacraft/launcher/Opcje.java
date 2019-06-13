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

	static JCheckBox proxy;
	static JCheckBox open;
	static JCheckBox RPC;
	static JLabel label;
	static JButton checkUpdate;
	static JButton OK;
	static JTextField parameters;

	public static String update = "Update check";
	public static String update_not_found = "Couldn't find any newer version of the launcher.";

	public Opcje() {
		Logger.a("Otwarto okno opcji.");
		this.setIconImage(Window.img);
		setSize(350, 386);
		setLayout(null);
		setTitle("Settings");
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);

		proxy = new JCheckBox("Use skin & sound proxy");
		proxy.setSelected(Launcher.getProperty(Launcher.SETTINGS, "proxy").equals("true") ? true : false);
		proxy.setBounds(10, 10, 330, 20);
		this.add(proxy);

		open = new JCheckBox("Keep the launcher open");
		open.setSelected(Launcher.getProperty(Launcher.SETTINGS, "keepopen").equals("true") ? true : false);
		open.setBounds(10, 30, 330, 20);
		this.add(open);

		RPC = new JCheckBox("Discord RPC");
		RPC.setSelected(Launcher.getProperty(Launcher.SETTINGS, "RPC").equals("true") ? true : false);
		RPC.setBounds(10, 50, 330, 20);
		this.add(RPC);

		label = new JLabel("Launch arguments:");
		label.setBounds(10, 70, 190, 20);
		label.setForeground(Color.BLACK);
		this.add(label);
		parameters = new JTextField(Launcher.getCustomParameters());
		parameters.setBounds(25, 95, 300, 25);
		this.add(parameters);

		OK = new JButton("OK");
		OK.setBounds(10, 320, 60, 20);
		OK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveOptions();
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

	public void saveOptions() {
		Launcher.setProperty(Launcher.SETTINGS, "launch", parameters.getText());
		Launcher.setProperty(Launcher.SETTINGS, "proxy", proxy.isSelected() ? "true" : "false");
		Launcher.setProperty(Launcher.SETTINGS, "keepopen", open.isSelected() ? "true" : "false");
		Launcher.setProperty(Launcher.SETTINGS, "RPC", RPC.isSelected() ? "true" : "false");
		setVisible(false);
	}
}
