package org.betacraft.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginPanel extends JFrame {

	static JCheckBox rememberpassword;

	static JLabel emailText;
	static JTextField email;

	static JLabel passwordText;
	static JTextField password;

	static JButton OKButton;

	public LoginPanel() {
		Logger.a("Logging window has been opened.");
		this.setIconImage(Window.img);
		setTitle(Lang.LOGIN_TITLE);
		setResizable(true);
		this.setMinimumSize(new Dimension(360, 240));

		JPanel panel = new InstanceSettings.OptionsPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints constr = new GridBagConstraints();

		constr.gridx = 0;
		constr.gridy = 0;
		constr.fill = GridBagConstraints.BOTH;
		constr.gridwidth = 4;
		constr.weightx = 1.0;
		constr.insets = new Insets(10, 10, 0, 10);

		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.gridwidth = 1;
		constr.gridx = 0;
		constr.weightx = 0.0;

		constr.gridy++;
		emailText = new JLabel(Lang.LOGIN_EMAIL_NICKNAME);
		emailText.setForeground(Color.LIGHT_GRAY);
		panel.add(emailText, constr);

		constr.gridx = 1;
		email = new JTextField(MojangLogging.email, 6);
		panel.add(email, constr);

		constr.gridx = 0;
		constr.gridy++;
		passwordText = new JLabel(Lang.LOGIN_PASSWORD);
		passwordText.setForeground(Color.LIGHT_GRAY);
		panel.add(passwordText, constr);

		constr.gridx = 1;
		password = new JPasswordField(MojangLogging.password, 6);
		panel.add(password, constr);

		constr.gridy++;
		rememberpassword = new JCheckBox(Lang.LOGIN_REMEMBER_PASSWORD);
		rememberpassword.setForeground(Color.LIGHT_GRAY);
		rememberpassword.setOpaque(false);
		//rememberpassword.setEnabled(false);
		String entry = Launcher.getProperty(Launcher.SETTINGS, "remember-password");
		boolean remember = false;
		if (!entry.equals("")) remember = Boolean.parseBoolean(entry);
		rememberpassword.setSelected(remember);
		panel.add(rememberpassword, constr);
		// =================================================================

		JPanel okPanel = new InstanceSettings.OptionsPanel();
		okPanel.setLayout(new GridBagLayout());

		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.gridx = 0;
		constr.gridy = constr.gridy + 1;
		constr.weighty = 1.0;
		constr.insets = new Insets(2, 2, 2, 2);
		constr.gridwidth = GridBagConstraints.RELATIVE;
		constr.gridheight = 1;
		constr.weightx = 1.0;
		OKButton = new JButton(Lang.OPTIONS_OK);
		OKButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (password.getText().equals("")) {
					MojangLogging.email = email.getText();
					Window.nicknameButton.setText(String.format(Lang.WINDOW_USER, MojangLogging.email));
				} else {
					new MojangLogging().authenticate(email.getText(), password.getText());
				}
				Launcher.setProperty(Launcher.SETTINGS, "remember-password", Boolean.toString(rememberpassword.isSelected()));
				if (!rememberpassword.isSelected()) MojangLogging.password = "";
				Launcher.saveLastLogin();
				setVisible(false);
			}
		});
		okPanel.add(OKButton, constr);
		okPanel.setBackground(Color.WHITE);

		this.getContentPane().add(panel, BorderLayout.CENTER);
		this.getContentPane().add(okPanel, BorderLayout.PAGE_END);
		this.pack();
		setLocationRelativeTo(Window.mainWindow);
		setVisible(true);
	}
}
