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
import java.io.File;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Window extends JFrame implements ActionListener {

	static JLabel selectedInstanceDisplay = null;
	static JButton playButton, selectVersionButton, optionsButton, langButton;
	static JButton tabchangelog, tabservers, tabinstances;
	static JLabel credits, nicktext;
	static JButton nicknameButton = null;
	static InfoPanel infoPanel = null;
	static BottomPanel bottomPanel = null;
	static Component centerPanel = null;
	public static Window mainWindow = null;

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
		setTitle(Lang.WINDOW_TITLE);
		setLayout(new BorderLayout());
		setLocationRelativeTo(null);

		// Initialize components
		nicknameButton = new JButton(String.format(Lang.WINDOW_USER, Launcher.getNickname()));
		playButton = new JButton(Lang.WINDOW_PLAY);
		selectedInstanceDisplay = new JLabel(Launcher.currentInstance.name + " [" + Launcher.currentInstance.version + "]");
		selectVersionButton = new JButton(Lang.WINDOW_SELECT_VERSION);
		credits = new JLabel(Lang.WINDOW_CREDITS);
		nicktext = new JLabel(Lang.WINDOW_USER);
		optionsButton = new JButton(Lang.WINDOW_OPTIONS);
		langButton = new JButton(Lang.WINDOW_LANGUAGE);

		nicknameButton.setContentAreaFilled(false);
		//login.setBorderPainted(true);
		//Border b = new BevelBorder(JFrame.DO_NOTHING_ON_CLOSE, Color.GRAY, Color.DARK_GRAY);
		//login.setBorder(b);
		nicknameButton.setOpaque(false);
		nicknameButton.setForeground(Color.WHITE);

		nicknameButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new LoginPanel();
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
		tabchangelog = new JButton("Changelog");
		tabinstances = new JButton("Instances");
		tabservers = new JButton("Classic servers");
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

					new InstanceList();
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
		nicktext.setForeground(Color.WHITE);
		selectedInstanceDisplay.setForeground(Color.WHITE);

		// Add listeners for components
		playButton.addActionListener(this);
		selectVersionButton.addActionListener(this);
		optionsButton.addActionListener(this);
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
		if (largest.getPreferredSize().getWidth() < optionsButton.getPreferredSize().getWidth()) {
			largest = optionsButton;
		}
		selectVersionButton.setPreferredSize(largest.getPreferredSize());
		langButton.setPreferredSize(largest.getPreferredSize());
		optionsButton.setPreferredSize(largest.getPreferredSize());

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
		Launcher.setProperty(Launcher.SETTINGS, "tab", tab.name());
		if (close) System.exit(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		// Initialize other windows if needed
		if (source == selectVersionButton) {
			new SelectVersion();
		}
		if (source == optionsButton) {
			new InstanceSettings();
		}
		if (source == langButton) {
			new Lang();
		}

		if (source == playButton) {
			playButton.setEnabled(false);
			try {
				new Thread() {
					public void run() {
						setStatus(playButton, Lang.WINDOW_DOWNLOADING);
						File wrapper = new File(BC.get() + "launcher", "betacraft_wrapper.jar");
						try {
							Files.copy(Launcher.currentPath.toPath(), wrapper.toPath(), StandardCopyOption.REPLACE_EXISTING);
						} catch (FileSystemException ex) {
							// There is another instance of the game running, we are going to ignore it
						} catch (Exception ex) {
							ex.printStackTrace();
							Logger.printException(ex);
							JOptionPane.showMessageDialog(null, "The file could not be copied! Try running with Administrator rights. If that won't help, contact me: @Moresteck#1688", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}

						// Download Discord RPC if the checkbox is selected
						if (Launcher.currentInstance.RPC) {
							File rpc = new File(BC.get() + "launcher", "discord_rpc.jar");
							if (!rpc.exists()) {
								Launcher.download("https://betacraft.pl/launcher/assets/discord_rpc.jar", rpc);
							}
						}

						// Download the game if not done already
						if (!Launcher.isReadyToPlay(Launcher.currentInstance.version)) {
							if (DownloadResult.OK != Launcher.download(Release.getReleaseByName(Launcher.currentInstance.version).getJson().getDownloadURL(), new File(Launcher.getVerFolder(), Launcher.currentInstance.version + ".jar"))) {
								JOptionPane.showMessageDialog(null, Lang.ERR_NO_CONNECTION, Lang.ERR_DL_FAIL, JOptionPane.ERROR_MESSAGE);
							}
						}

						// Download the latest libs and natives
						if (!Launcher.checkDepends()) Launcher.downloadDepends();

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
