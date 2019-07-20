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

public class Options extends JFrame {

	static JCheckBox proxyCheck;
	static JCheckBox keepOpenCheck;
	static JCheckBox RPCCheck;
	static JLabel parametersText;
	static JButton checkUpdateButton;
	static JButton OKButton;
	static JTextField parameters;

	public static String update = "Update check";
	public static String update_not_found = "Couldn't find any newer version of the launcher.";

	public Options() {
		Logger.a("Options window has been opened.");
		this.setIconImage(Window.img);
		setSize(350, 386);
		setLayout(null);
		setTitle("Settings");
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);

		proxyCheck = new JCheckBox("Use skin & sound proxy");

		String value = Launcher.getProperty(Launcher.SETTINGS, "proxy");
		proxyCheck.setSelected(value.equals("true") ? true : value.equals("") ? true : false);
		proxyCheck.setBounds(10, 10, 330, 20);
		this.add(proxyCheck);

		keepOpenCheck = new JCheckBox("Keep the launcher open");
		keepOpenCheck.setSelected(Launcher.getProperty(Launcher.SETTINGS, "keepopen").equals("true") ? true : false);
		keepOpenCheck.setBounds(10, 30, 330, 20);
		this.add(keepOpenCheck);

		RPCCheck = new JCheckBox("Discord RPC");

		value = Launcher.getProperty(Launcher.SETTINGS, "RPC");
		RPCCheck.setSelected(value.equals("true") ? true : value.equals("") ? true : false);
		RPCCheck.setBounds(10, 50, 330, 20);
		this.add(RPCCheck);

		parametersText = new JLabel("Launch arguments:");
		parametersText.setBounds(10, 70, 190, 20);
		parametersText.setForeground(Color.BLACK);
		this.add(parametersText);

		parameters = new JTextField(Launcher.getProperty(Launcher.SETTINGS, "launch"));
		parameters.setBounds(25, 95, 300, 25);
		this.add(parameters);

		OKButton = new JButton("OK");
		OKButton.setBounds(10, 320, 60, 20);
		OKButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveOptions();
				setVisible(false);
			}
		});
		this.add(OKButton);

		checkUpdateButton = new JButton("Check for update");
		checkUpdateButton.setBounds(140, 320, 180, 20);
		this.add(checkUpdateButton);

		OKButton.setBackground(Color.LIGHT_GRAY);
		checkUpdateButton.setBackground(Color.LIGHT_GRAY);

		checkUpdateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Launcher.checkForUpdate()) {
					Launcher.downloadUpdate();
				} else {
					JOptionPane.showMessageDialog(null, update_not_found, update, JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});

		// Update messages for the chosen language
		Lang.apply();
	}

	public void saveOptions() {
		Launcher.setProperty(Launcher.SETTINGS, "launch", parameters.getText());
		Launcher.setProperty(Launcher.SETTINGS, "proxy", proxyCheck.isSelected() ? "true" : "false");
		Launcher.setProperty(Launcher.SETTINGS, "keepopen", keepOpenCheck.isSelected() ? "true" : "false");
		Launcher.setProperty(Launcher.SETTINGS, "RPC", RPCCheck.isSelected() ? "true" : "false");
	}
}
