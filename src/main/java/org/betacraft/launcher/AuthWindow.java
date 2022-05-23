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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import pl.betacraft.auth.MicrosoftAuth;
import pl.betacraft.auth.MojangAuth;
import pl.betacraft.auth.jsons.microsoft.DeviceCodeRequest;
import pl.betacraft.auth.jsons.microsoft.DeviceCodeResponse;

public class AuthWindow extends JFrame implements LanguageElement {

	static JLabel emailText;
	static JTextField email;

	static JLabel passwordText;
	static JTextField password;

	static JButton OKButton;

	public AuthWindow() {
		Logger.a("Auth window opened.");
		this.setIconImage(Window.img);
		setTitle(Lang.LOGIN_TITLE);
		setResizable(true);
		this.setMinimumSize(new Dimension(400, 200));

		JPanel panel = new InstanceSettings.OptionsPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints constr = new GridBagConstraints();

		constr.gridx = 0;
		constr.gridy = 0;
		constr.fill = GridBagConstraints.BOTH;
		constr.gridwidth = 1;
		constr.weightx = 1.0;
		constr.insets = new Insets(10, 10, 0, 10);

		constr.gridwidth = 3;
		JButton microsoftbrowser = new JButton(Lang.LOGIN_MICROSOFT_BUTTON);
		microsoftbrowser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					DeviceCodeResponse dcr = new DeviceCodeRequest().perform();
					new AwaitingMSALogin(dcr.verification_uri, dcr.user_code, dcr.device_code, dcr.expires_in, dcr.interval);
					setVisible(false);
					Window.loginPanel = null;
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		});
		panel.add(microsoftbrowser, constr);

		constr.gridy++;
		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.gridwidth = 3;
		constr.gridx = 0;
		constr.weightx = 0.0;
		JLabel mojangauth = new JLabel(Lang.LOGIN_MOJANG_HEADER);
		mojangauth.setForeground(Color.LIGHT_GRAY);
		panel.add(mojangauth, constr);

		constr.gridy++;
		emailText = new JLabel(Lang.LOGIN_EMAIL_NICKNAME);
		emailText.setForeground(Color.LIGHT_GRAY);
		panel.add(emailText, constr);

		constr.gridx = 1;
		constr.gridwidth = 3;
		email = new JTextField("", 6);
		panel.add(email, constr);

		constr.gridx = 0;
		constr.gridy++;
		constr.gridwidth = 1;
		passwordText = new JLabel(Lang.LOGIN_PASSWORD);
		passwordText.setForeground(Color.LIGHT_GRAY);
		panel.add(passwordText, constr);

		constr.gridx = 1;
		constr.gridwidth = 3;
		password = new JPasswordField("", 6);
		panel.add(password, constr);

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
			public void actionPerformed(ActionEvent e) {
				MojangAuth auth = new MojangAuth(email.getText(), password.getText());
				if (auth.authenticate()) {
					Launcher.accounts.setCurrent(auth.getCredentials());
					Launcher.auth = auth;
				}

				Util.saveAccounts();
				setVisible(false);
				Window.loginPanel = null;
			}
		});
		okPanel.add(OKButton, constr);
		okPanel.setBackground(Color.WHITE);

		this.getContentPane().add(panel, BorderLayout.CENTER);
		this.getContentPane().add(okPanel, BorderLayout.PAGE_END);
		this.pack();
		setLocationRelativeTo(Window.mainWindow);
		setVisible(true);
		email.requestFocus();
	}

	public static void continueMSA(MicrosoftAuth auth) {
		if (auth.authenticate()) {
			Util.saveAccounts();
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(Window.mainWindow, Lang.LOGIN_FAILED, Lang.LOGIN_MICROSOFT_ERROR, JOptionPane.ERROR_MESSAGE);
				}
			});
		}
		Window.mainWindow.requestFocus();
	}

	public void update() {
		this.setTitle(Lang.LOGIN_TITLE);
		emailText.setText(Lang.LOGIN_EMAIL_NICKNAME);
		passwordText.setText(Lang.LOGIN_PASSWORD);
		OKButton.setText(Lang.OPTIONS_OK);
		this.pack();
	}
}
