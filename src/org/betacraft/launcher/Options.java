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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Options extends JFrame {

	static JCheckBox proxyCheck;
	static JCheckBox keepOpenCheck;
	static JCheckBox RPCCheck;

	static JLabel parametersText;
	static JTextField parameters;

	static JLabel dimensions1Text;
	static JLabel dimensions2Text;
	static JTextField dimensions1;
	static JTextField dimensions2;

	static JButton checkUpdateButton;
	static JButton OKButton;

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

		dimensions1Text = new JLabel("w:");
		dimensions1Text.setBounds(9, 125, 15, 25);
		this.add(dimensions1Text);
		dimensions1 = new JTextField(Launcher.getProperty(Launcher.SETTINGS, "dimensions1"));
		dimensions1.setBounds(25, 125, 60, 25);
		this.add(dimensions1);

		dimensions2Text = new JLabel("h:");
		dimensions2Text.setBounds(90, 125, 15, 25);
		this.add(dimensions2Text);
		dimensions2 = new JTextField(Launcher.getProperty(Launcher.SETTINGS, "dimensions2"));
		dimensions2.setBounds(105, 125, 60, 25);
		this.add(dimensions2);

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

		DocumentListener doc = new DocumentListener() {

			public void changedUpdate(DocumentEvent e) {
				change();
			}
			public void removeUpdate(DocumentEvent e) {
				change();
			}
			public void insertUpdate(DocumentEvent e) {
				change();
			}

			public void change() {
				if (dimensions1.getText().length() > 9) {
					Window.setTextInField(dimensions1, "");
				}
				if (dimensions2.getText().length() > 9) {
					Window.setTextInField(dimensions2, "");
				}
				for (int i = 0; i < dimensions1.getText().length(); i++) {
					if ("0123456789".indexOf(dimensions1.getText().charAt(i)) < 0) {
						Window.setTextInField(dimensions1, "");
					}
				}
				for (int i = 0; i < dimensions2.getText().length(); i++) {
					if ("0123456789".indexOf(dimensions2.getText().charAt(i)) < 0) {
						Window.setTextInField(dimensions2, "");
					}
				}
			}
		};
		dimensions1.getDocument().addDocumentListener(doc);
		dimensions2.getDocument().addDocumentListener(doc);

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
		Launcher.setProperty(Launcher.SETTINGS, "dimensions1", dimensions1.getText());
		Launcher.setProperty(Launcher.SETTINGS, "dimensions2", dimensions2.getText());
	}
}
