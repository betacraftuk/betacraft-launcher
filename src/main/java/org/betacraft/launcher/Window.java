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

import pl.betacraft.auth.NoAuth;

public class Window extends JFrame implements ActionListener, LanguageElement {

	public static JLabel selectedInstanceDisplay = null;
	public static JButton playButton, selectVersionButton, settingsButton, langButton;
	public static JButton tabchangelog, tabservers, tabinstances;
	public static JLabel nicktext;
	public static JTextField nick_input;
	public static JButton loginButton = null;
	public static InfoPanel infoPanel = null;
	public static BottomPanel bottomPanel = null;
	public static Component centerPanel = null;
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
	public static BufferedImage img;

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
		setTitle(Lang.WINDOW_TITLE + (BC.nightly ? " [NIGHTLY]" : ""));
		setLayout(new BorderLayout());
		setLocationRelativeTo(null);

		// Initialize components
		loginButton = new JButton(Lang.LOGIN_BUTTON);
		playButton = new JButton(Lang.WINDOW_PLAY);
		selectedInstanceDisplay = new JLabel(Launcher.currentInstance.name + " [" + Launcher.currentInstance.version + "]");
		selectVersionButton = new JButton(Lang.WINDOW_SELECT_VERSION);
		nick_input = new JTextField(Launcher.getNickname(), 16);
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
				Launcher.auth.getCredentials().username = nick_input.getText();
				//System.out.println(Launcher.auth.getCredentials().username); // DEBUG
			}
		});

		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!nick_input.isEnabled()) {
					Launcher.auth.invalidate();
					Launcher.auth = Util.getAuthenticator(Launcher.accounts.accounts.get(0));
					Launcher.accounts.setCurrent(Launcher.accounts.accounts.get(0));
					Launcher.auth.authenticate();

					Window.nick_input.setText(Launcher.getNickname());
					Window.setTab(Window.tab);
					// since there's basically no account selector, this must do... awkward
					if (Launcher.auth instanceof NoAuth) {
						nick_input.setEnabled(true);
						loginButton.setText(Lang.LOGIN_BUTTON);
					}
				} else {
					if (loginPanel == null) new LoginPanel();
					else loginPanel.setVisible(true);
				}
			}
		});

		bottomPanel = new BottomPanel();
		String tabname = Util.getProperty(BC.SETTINGS, "tab");
		Tab tab = tabname.equals("") ? Tab.CHANGELOG : Tab.valueOf(tabname.toUpperCase());
		setTab(tab);
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
				setTab(Tab.CHANGELOG);
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
				setTab(Tab.SERVER_LIST);
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
		playButton.requestFocus();
	}

	public void update() {
		this.setTitle(Lang.WINDOW_TITLE + (BC.nightly ? " [NIGHTLY]" : ""));
		if (Launcher.auth instanceof NoAuth) loginButton.setText(Lang.LOGIN_BUTTON);
		else loginButton.setText(Lang.LOGOUT_BUTTON);
		playButton.setText(Lang.WINDOW_PLAY);
		selectVersionButton.setText(Lang.WINDOW_SELECT_VERSION);
		settingsButton.setText(Lang.WINDOW_OPTIONS);
		langButton.setText(Lang.WINDOW_LANGUAGE);
		tabchangelog.setText(Lang.TAB_CHANGELOG);
		tabinstances.setText(Lang.TAB_INSTANCES);
		tabservers.setText(Lang.TAB_SERVERS);
		positionButtons();
		this.pack();
	}

	public static void setTab(Tab tab) {
		if (Window.centerPanel != null) mainWindow.remove(Window.centerPanel);
		centerPanel = new WebsitePanel().getEmptyTabFor(tab);
		Window.tab = Tab.SERVER_LIST;
		mainWindow.add(Window.centerPanel, BorderLayout.CENTER);
		mainWindow.setPreferredSize(mainWindow.getSize());
		mainWindow.pack();
		if (tab == Tab.CHANGELOG) {
			new Thread() {
				public void run() {
					if (Window.centerPanel != null) mainWindow.remove(Window.centerPanel);
					centerPanel = new WebsitePanel().getUpdateNews(true);
					Window.tab = Tab.CHANGELOG;
					mainWindow.add(Window.centerPanel, BorderLayout.CENTER);
					mainWindow.pack();
				}
			}.start();
		} else if (tab == Tab.SERVER_LIST) {
			new Thread() {
				public void run() {
					if (Window.centerPanel != null) mainWindow.remove(Window.centerPanel);
					centerPanel = new WebsitePanel().getServers(true);
					Window.tab = Tab.SERVER_LIST;
					mainWindow.add(Window.centerPanel, BorderLayout.CENTER);
					mainWindow.pack();
				}
			}.start();
		}
	}

	public static void positionButtons() {
		// Find the most wide button and set its width to other buttons
		// Make copies to allow for estimating the preferred size on runtime
		JButton copyVersion = new JButton(selectVersionButton.getText());
		JButton copyLanguag = new JButton(langButton.getText());
		JButton copyInstanc = new JButton(settingsButton.getText());
		JButton largest = copyVersion;
		if (largest.getPreferredSize().getWidth() < copyLanguag.getPreferredSize().getWidth()) {
			largest = copyLanguag;
		}
		if (largest.getPreferredSize().getWidth() < copyInstanc.getPreferredSize().getWidth()) {
			largest = copyInstanc;
		}
		selectVersionButton.setPreferredSize(largest.getPreferredSize());
		selectVersionButton.setSize(largest.getPreferredSize());
		langButton.setPreferredSize(largest.getPreferredSize());
		langButton.setSize(largest.getPreferredSize());
		settingsButton.setPreferredSize(largest.getPreferredSize());
		settingsButton.setSize(largest.getPreferredSize());

		JButton copyTabChan = new JButton(tabchangelog.getText());
		JButton copyTabInst = new JButton(tabinstances.getText());
		JButton copyTabServ = new JButton(tabservers.getText());
		largest = copyTabServ;
		if (largest.getPreferredSize().getWidth() < copyTabInst.getPreferredSize().getWidth()) {
			largest = copyTabInst;
		}
		if (largest.getPreferredSize().getWidth() < copyTabChan.getPreferredSize().getWidth()) {
			largest = copyTabChan;
		}
		tabchangelog.setPreferredSize(largest.getPreferredSize());
		tabchangelog.setSize(largest.getPreferredSize());
		tabinstances.setPreferredSize(largest.getPreferredSize());
		tabinstances.setSize(largest.getPreferredSize());
		tabservers.setPreferredSize(largest.getPreferredSize());
		tabservers.setSize(largest.getPreferredSize());
	}

	public static void quit(boolean close) {
		if (mainWindow != null) mainWindow.setVisible(false);
		if (mainWindow != null && close) mainWindow.dispose();
		Util.saveAccounts();
		Util.setProperty(BC.SETTINGS, "tab", tab.name());
		if (close) {
			for (Thread t : Launcher.totalThreads) {
				while (t.isAlive());
			}
			System.exit(0);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		// Initialize other windows if needed
		if (source == selectVersionButton && Release.versions.size() > 0) {
			if (versionsList == null) new SelectVersion();
			else versionsList.setVisible(true);
			SelectVersion.list.requestFocus();
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
			Util.saveAccounts();
			playButton.setEnabled(false);
			try {
				new Thread() {
					public void run() {
						int last = Launcher.totalThreads.size();
						while (last > 0) {
							last = Launcher.totalThreads.size();
							try {
								// wtf is wrong with java
								Thread.sleep(1L);
							} catch (InterruptedException e) {}
						}
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
