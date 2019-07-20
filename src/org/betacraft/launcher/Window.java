package org.betacraft.launcher;

import java.awt.Color;
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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Window extends JFrame implements ActionListener {

	static JLabel selectedVersionDisplay = null;
	static JButton playButton, selectVersionButton, optionsButton, langButton;
	static JLabel credits, nicktext;
	static JTextField nick_input = null;
	static Version versionListWindow = null;
	static Options optionsWindow = null;
	static Lang languageWindow = null;
	static InfoPanel infoPanel = null;
	static LoginPanel bottomPanel = null;
	public static Window mainWindow = null;

	public static String max_chars = "The maximum amount of characters in a nickname is 16.";
	public static String min_chars = "Your nickname must have at least 3 characters.";
	public static String banned_chars = "Your nickname mustn't contain spaces and any special characters.";
	public static String warning = "Warning";
	public static String no_connection = "No stable internet connection.";
	public static String download_fail = "Download failed.";
	public static String downloading = "Downloading ...";
	public static String play_lang = "Play";

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
		}
		// 1697,21 + 
		mainWindow = this;
		setSize(800, 450);
		setTitle("BetaCraft Launcher v" + Launcher.VERSION);
		setLayout(null);
		setLocationRelativeTo(null);
		setResizable(false);

		// Initialize components
		nick_input = new JTextField(Launcher.getLastlogin());
		playButton = new JButton(play_lang);
		selectedVersionDisplay = new JLabel(Launcher.chosen_version);
		selectVersionButton = new JButton("Select version");
		credits = new JLabel("BetaCraft Launcher made by Kazu & Moresteck");
		nicktext = new JLabel("Nick:");
		optionsButton = new JButton("Options");
		langButton = new JButton("Language");

		infoPanel = new InfoPanel(true);
		Window.mainWindow.add(infoPanel);

		bottomPanel = new LoginPanel();
		add(bottomPanel);

		resizeObjects();

		// Buttons are being added in LoginPanel.paintComponent()
		// because it will put them above the bottom panel's background

		// Add some texture to the components
		credits.setForeground(Color.LIGHT_GRAY);
		nicktext.setForeground(Color.WHITE);
		selectedVersionDisplay.setForeground(Color.WHITE);

		optionsButton.setBackground(Color.LIGHT_GRAY);
		selectVersionButton.setBackground(Color.LIGHT_GRAY);
		playButton.setBackground(Color.LIGHT_GRAY);
		langButton.setBackground(Color.LIGHT_GRAY);

		// Add listeners for components
		playButton.addActionListener(this);
		selectVersionButton.addActionListener(this);
		optionsButton.addActionListener(this);
		langButton.addActionListener(this);
		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Launcher.write(new File(BC.get(), "lastlogin"), new String[] {nick_input.getText()}, false);
			}
		});
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
				if (nick_input.getText().length() > 16) {
					JOptionPane.showMessageDialog(null, max_chars, warning, JOptionPane.WARNING_MESSAGE);
					Window.setTextInField(nick_input, nick_input.getText().substring(0, 16));

					// Save the lastlogin to fix "" player issue
					Launcher.write(new File(BC.get(), "lastlogin"), new String[] {nick_input.getText()}, false);
				}
			}
		});

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				Launcher.write(new File(BC.get(), "lastlogin"), new String[] {nick_input.getText()}, false);
				Logger.a("Deactivation...");
				Window.quit();
			}
		});

		// TODO: Make the window resizeable for 1.09
		/*this.addComponentListener(new ComponentAdapter() {
		    public void componentResized(ComponentEvent componentEvent) {
		    	if (componentEvent.getComponent().getSize().width < 800 || componentEvent.getComponent().getSize().height < 450) {
		    		componentEvent.getComponent().setSize(800, 450);
		    	}
		        resizeObjects();
		    }
		});*/
	}

	public static void resizeObjects() {
		int width = mainWindow.getSize().width / 2; // 400
		//int height = window.getSize().height / 2; // 450

		infoPanel.setBounds(0, 0, 800, 290);
		playButton.setBounds(((Double)(width * 0.75)).intValue(), 50, 195, 36);
		nick_input.setBounds(((Double)(width * 0.84)).intValue(), 20, 120, 24); // 337
		credits.setBounds(15, 90, 390, 30);
		selectVersionButton.setBounds(((Double)(width * 0.13)).intValue(), 39, 150, 20); // 50
		nicktext.setBounds(((Double)(width * 0.75)).intValue(), 21, 35, 20);
		optionsButton.setBounds(((Double)(width * 0.13)).intValue(), 60, 150, 20);
		langButton.setBounds(600, 60, 150, 20);
		selectedVersionDisplay.setBounds(55, 21, 100, 20);
	}

	public static void quit() {
		mainWindow.setVisible(false);
		mainWindow.dispose();
		System.exit(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		// Initialize other windows if not initialized already
		if (source == selectVersionButton && versionListWindow == null) {
			versionListWindow = new Version();
		}
		if (source == optionsButton && optionsWindow == null) {
			optionsWindow = new Options();
		}
		if (source == langButton && languageWindow == null) {
			languageWindow = new Lang();
		}

		// Respond to actions
		if (source == optionsButton) {
			optionsWindow.setVisible(true);
		}
		if (source == langButton) {
			languageWindow.setVisible(true);
		}
		if (source == playButton) {
			if (nick_input.getText().length() < 3) {
				JOptionPane.showMessageDialog(null, min_chars, warning, JOptionPane.WARNING_MESSAGE);
				return;
			}

			// Check for banned characters
			for (int i = 0; i < nick_input.getText().length(); i++) {
				if ("-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz".indexOf(nick_input.getText().charAt(i)) < 0) {
					JOptionPane.showMessageDialog(null, banned_chars, warning, JOptionPane.WARNING_MESSAGE);
					return;
				}
			}

			playButton.setEnabled(false);
			try {
				new Thread() {
					public void run() {
						setStatus(playButton, downloading);
						File wrapper = new File(BC.get() + "launcher/", "betacraft_wrapper.jar");
						try {
							Files.copy(Launcher.currentPath.toPath(), wrapper.toPath(), StandardCopyOption.REPLACE_EXISTING);
						} catch (FileSystemException ex) {
							// There is another instance of the game running, we are going to ignore it
						} catch (Exception ex) {
							ex.printStackTrace();
							JOptionPane.showMessageDialog(null, "The file could not be copied! Try running with Administrator rights. If that won't help, contact me: @Moresteck#1688", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}

						// Download Discord RPC if the checkbox is selected
						if (Launcher.getProperty(Launcher.SETTINGS, "RPC").equalsIgnoreCase("true")) {
							File rpc = new File(BC.get() + "launcher/", "discord_rpc.jar");
							if (!rpc.exists()) {
								Launcher.download("https://betacraft.ovh/versions/discord_rpc.jar", rpc);
							}
						}

						// Download the game if not done already
						if (!Launcher.isReadyToPlay(Launcher.chosen_version)) {
							if (DownloadResult.OK != Launcher.download(Launcher.getVerLink(Launcher.chosen_version), new File(Launcher.getVerFolder(), Launcher.chosen_version + ".jar"))) {
								JOptionPane.showMessageDialog(null, no_connection, download_fail, JOptionPane.ERROR_MESSAGE);
							}
						}

						// Download the natives if not done already
						if (!Launcher.nativesDownloaded(false)) {
							Launcher.nativesDownloaded(true);
						}

						// Update the button state
						setStatus(playButton, play_lang);
						playButton.setEnabled(true);

						// Start the wrapper
						new Launcher().launchGame(Launcher.getCustomParameters(), Launcher.getLastlogin());
					}
				}.start();
			} catch (Exception ex) {

			}
		} else if (source == selectVersionButton) {
			versionListWindow.setVisible(true);
		}
		Lang.apply();
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
}
