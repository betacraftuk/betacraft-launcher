package org.betacraft.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Window extends JFrame implements ActionListener {

	static JLabel selectedInstanceDisplay = null;
	static JButton playButton, selectVersionButton, settingsButton, langButton;
	static JButton tabchangelog, tabservers, tabinstances;
	static JLabel credits, nicktext;
	static JTextField nick_input;
	static JButton loginButton = null;
	static InfoPanel infoPanel = null;
	static BottomPanel bottomPanel = null;
	static Component centerPanel = null;
	public static Window mainWindow = null;

	public static ModsRepository modsRepo = null;
	public static InstanceList instanceList = null;
	public static InstanceSettings instanceSettings = null;
	public static Lang lang = null;
	public static SelectAddons addonsList = null;
	public static SelectVersion versionsList = null;
	public static LoginPanel loginPanel = null;

	public static Tab tab = Tab.CHANGELOG;

	// Launcher's icon
	static BufferedImage img;

	public Window() {
		try {
			InputStream imstream = this.getClass().getClassLoader().getResourceAsStream("icons/icon.png");
			img = ImageIO.read(imstream);
			this.setIconImage(img);
		} catch (Exception ex) {
			Logger.a("An error occurred while loading the window icon!");
			ex.printStackTrace();
			Logger.printException(ex);
		}

		mainWindow = this;
		setMinimumSize(new Dimension(800, 480));
		setPreferredSize(new Dimension(800, 480));
		setTitle(Lang.WINDOW_TITLE);
		setLayout(new BorderLayout());
		setLocationRelativeTo(null);

		// Initialize components
		loginButton = new JButton(Lang.LOGIN_BUTTON);
		playButton = new JButton(Lang.WINDOW_PLAY);
		selectedInstanceDisplay = new JLabel(Launcher.currentInstance.name + " [" + Launcher.currentInstance.version + "]");
		selectVersionButton = new JButton(Lang.WINDOW_SELECT_VERSION);
		credits = new JLabel(Lang.WINDOW_CREDITS);
		nick_input = new JTextField(MojangLogging.username, 16);
		settingsButton = new JButton(Lang.WINDOW_OPTIONS);
		langButton = new JButton(Lang.WINDOW_LANGUAGE);

		nick_input.getDocument().addDocumentListener(new DocumentListener() {
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
				MojangLogging.username = nick_input.getText();
			}
		});

		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!nick_input.isEnabled()) {
					nick_input.setEnabled(true);
					loginButton.setText(Lang.LOGIN_BUTTON);
					MojangLogging.email = "";
					MojangLogging.password = "";
					MojangLogging.userProfile = null;
				} else {
					if (loginPanel == null) new LoginPanel();
					else loginPanel.setVisible(true);
				}
			}
		});

		bottomPanel = new BottomPanel();
		centerPanel = new WebsitePanel().getUpdateNews(true);
		this.add(Window.centerPanel, BorderLayout.CENTER);
		this.add(Window.bottomPanel, BorderLayout.SOUTH);

		JPanel stuffz = new JPanel() {
			public void update(final Graphics graphics) {
				this.paint(graphics);
			}

			public void paintComponent(Graphics g) {
				bottomPanel.paintComponent(g);
			}
		};
		stuffz.setLayout(new GridBagLayout());
		tabchangelog = new JButton(Lang.TAB_CHANGELOG);
		tabinstances = new JButton(Lang.TAB_INSTANCES);
		tabservers = new JButton(Lang.TAB_SERVERS);
		positionButtons();

		tabchangelog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Window.tab != Tab.CHANGELOG) {
					mainWindow.remove(Window.centerPanel);
					centerPanel = new WebsitePanel().getUpdateNews(true);
					Window.tab = Tab.CHANGELOG;
					mainWindow.add(Window.centerPanel, BorderLayout.CENTER);
					mainWindow.pack();
				}
			}
		});
		
		tabinstances.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Window.tab != Tab.INSTANCES) {
					/*mainWindow.remove(Window.centerPanel);
					centerPanel = new WebsitePanel().getInstances();
					Window.tab = Tab.INSTANCES;
					mainWindow.add(Window.centerPanel, BorderLayout.CENTER);
					mainWindow.pack();*/

					if (instanceList == null) new InstanceList();
					else instanceList.setVisible(true);
				}
			}
		});
		
		tabservers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Window.tab != Tab.SERVER_LIST) {
					mainWindow.remove(Window.centerPanel);
					centerPanel = new WebsitePanel().getServers(true);
					Window.tab = Tab.SERVER_LIST;
					mainWindow.add(Window.centerPanel, BorderLayout.CENTER);
					mainWindow.pack();
				}
			}
		});

		GridBagConstraints constr = new GridBagConstraints();
		constr.fill = GridBagConstraints.BOTH;
		constr.gridwidth = GridBagConstraints.RELATIVE;
		constr.gridy = 0;
		constr.gridx = 0;
		constr.weightx = 0.0;
		constr.gridwidth = 1;
		constr.insets = new Insets(0, 2, 0, 2);
		stuffz.add(tabchangelog, constr);

		constr.gridx = 1;
		stuffz.add(tabinstances, constr);

		constr.gridx = 2;
		stuffz.add(tabservers, constr);

		this.add(stuffz, BorderLayout.NORTH);

		// Buttons are being added in LoginPanel.paintComponent()
		// because it will put them above the bottom panel's background

		// Add some texture to the components
		credits.setForeground(Color.LIGHT_GRAY);
		selectedInstanceDisplay.setForeground(Color.WHITE);

		// Add listeners for components
		playButton.addActionListener(this);
		selectVersionButton.addActionListener(this);
		settingsButton.addActionListener(this);
		langButton.addActionListener(this);

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				Logger.a("Deactivation...");
				Window.quit(true);
			}
		});
		this.pack();
	}

	public static void positionButtons() {
		JButton largest = selectVersionButton;
		if (largest.getPreferredSize().getWidth() < langButton.getPreferredSize().getWidth()) {
			largest = langButton;
		}
		if (largest.getPreferredSize().getWidth() < settingsButton.getPreferredSize().getWidth()) {
			largest = settingsButton;
		}
		selectVersionButton.setPreferredSize(largest.getPreferredSize());
		langButton.setPreferredSize(largest.getPreferredSize());
		settingsButton.setPreferredSize(largest.getPreferredSize());

		largest = tabservers;
		if (largest.getPreferredSize().getWidth() < tabinstances.getPreferredSize().getWidth()) {
			largest = tabinstances;
		}
		if (largest.getPreferredSize().getWidth() < tabchangelog.getPreferredSize().getWidth()) {
			largest = tabchangelog;
		}
		tabchangelog.setPreferredSize(largest.getPreferredSize());
		tabinstances.setPreferredSize(largest.getPreferredSize());
		tabservers.setPreferredSize(largest.getPreferredSize());
	}

	public static void quit(boolean close) {
		if (mainWindow != null) mainWindow.setVisible(false);
		if (mainWindow != null) mainWindow.dispose();
		Launcher.saveLastLogin();
		Launcher.setProperty(Launcher.SETTINGS, "tab", tab.name());
		if (close) System.exit(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		// Initialize other windows if needed
		if (source == selectVersionButton) {
			if (versionsList == null) new SelectVersion();
			else versionsList.setVisible(true);
		}
		if (source == settingsButton) {
			if (instanceSettings == null) new InstanceSettings();
			else instanceSettings.setVisible(true);
		}
		if (source == langButton) {
			if (lang == null) new Lang();
			else lang.setVisible(true);
		}

		if (source == playButton) {
			Launcher.saveLastLogin();
			playButton.setEnabled(false);
			try {
				new Thread() {
					public void run() {
						setStatus(playButton, Lang.WINDOW_DOWNLOADING);

						Launcher.initStartup();

						// Update the button state
						setStatus(playButton, Lang.WINDOW_PLAY);
						playButton.setEnabled(true);

						// Start the wrapper
						new Launcher().launchGame(Launcher.currentInstance);
					}
				}.start();
			} catch (Exception ex) {
				Logger.printException(ex);
			}
		}
	}

	public static void setTextInField(final JTextField field, final String toSet) {
		Runnable set = new Runnable() {
			public void run() {
				field.setText(toSet);
			}
		};
		SwingUtilities.invokeLater(set);
	}

	public static void setStatus(final JButton button, final String toSet) {
		Runnable set = new Runnable() {
			public void run() {
				button.setText(toSet);
			}
		};
		SwingUtilities.invokeLater(set);
	}

	public enum Tab {
		CHANGELOG,
		INSTANCES,
		SERVER_LIST;
	}
}
