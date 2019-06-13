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

    static JLabel currentver = null;
	static JButton play, select_version, options, lang;
	static JLabel credits, nicktext;
	static JTextField nick_input = null;
	static Wersja currentAbout = null;
	static Opcje currentOptions = null;
	static Lang currentLang = null;
	static InfoPanel infopanel = null;
	static LoginPanel loginpanel = null;
	public static Window window = null;

	public static String max_chars = "The maxixum amount of characters in a nickname is 16.";
	public static String min_chars = "Your nickname must have at least 3 characters.";
	public static String banned_chars = "Your nickname mustn't contain spaces and any characters like #, @, etc.";
	public static String warning = "Warning";
	public static String no_connection = "No internet connection.";
	public static String download_fail = "Download failed.";
	public static String downloading = "Downloading ...";
	public static String play_lang = "Play";

	static BufferedImage img;

	public Window() {
		try {
			InputStream imstream = this.getClass().getClassLoader().getResourceAsStream("icons/icon.png");
			img = ImageIO.read(imstream);
			this.setIconImage(img);
		} catch (Exception ex) {
			System.out.println("Error podczas wczytywania pliku ikony! (niewazne)");
			ex.printStackTrace();
		}
		// 1697,21 + 
		window = this;
		setSize(800, 450);
		setTitle("Betacraft Launcher version " + Launcher.VERSION);
		setLayout(null);
		setLocationRelativeTo(null);
		setResizable(false);

		infopanel = new InfoPanel();
		add(infopanel);

		loginpanel = new LoginPanel();
		add(loginpanel);

		nick_input = new JTextField(Launcher.getLastlogin());
		play = new JButton(play_lang);
		currentver = new JLabel("");
		select_version = new JButton("Select version");
		credits = new JLabel("Betacraft Launcher made by KazuGod & Moresteck");
		nicktext = new JLabel("Nick:");
		options = new JButton("Options");
		lang = new JButton("Language");

		resizeObjects();

		// buttony sa dodawane w Background.paintComponent() aby nie byly pod tlem

		play.addActionListener(this); // this - sluchaczem zdarzen jest cala ramka
		select_version.addActionListener(this);
		options.addActionListener(this);
		lang.addActionListener(this);

		credits.setForeground(Color.LIGHT_GRAY);
		nicktext.setForeground(Color.WHITE);
		currentver.setForeground(Color.WHITE);

		options.setBackground(Color.LIGHT_GRAY);
		select_version.setBackground(Color.LIGHT_GRAY);
		play.setBackground(Color.LIGHT_GRAY);
		lang.setBackground(Color.LIGHT_GRAY);

		play.addActionListener(new ActionListener() {
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
				}
			}
		});
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				Launcher.write(new File(BC.get(), "lastlogin"), new String[] {nick_input.getText()}, false);
				Logger.a("Dezaktywacja...");
				Window.quit();
			}
		});

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
		int width = window.getSize().width / 2; // 400
		//int height = window.getSize().height / 2; // 450

		infopanel.setBounds(0, 0, 800, 290); // 290, 800
		play.setBounds(((Double)(width * 0.75)).intValue(), 50, 195, 36);
		nick_input.setBounds(((Double)(width * 0.84)).intValue(), 20, 120, 24); // 337
		credits.setBounds(15, 90, 390, 30);
		select_version.setBounds(((Double)(width * 0.13)).intValue(), 39, 150, 20); // 50
		nicktext.setBounds(((Double)(width * 0.75)).intValue(), 21, 35, 20);
		options.setBounds(((Double)(width * 0.13)).intValue(), 60, 150, 20);
		lang.setBounds(600, 60, 150, 20);
		currentver.setBounds(55, 21, 100, 20);
	}

	public static void quit() {
		window.setVisible(false);
		window.dispose();
		System.exit(1);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == select_version && currentAbout == null) {
			currentAbout = new Wersja();
		}
		if (source == options && currentOptions == null) {
			currentOptions = new Opcje();
		}
		if (source == lang && currentLang == null) {
			currentLang = new Lang();
		}
		if (source == options) {
			currentOptions.setVisible(true);
		}
		if (source == lang) {
			currentLang.setVisible(true);
		}
		if (source == play) {
			if (nick_input.getText().length() < 3) {
				JOptionPane.showMessageDialog(null, min_chars, warning, JOptionPane.WARNING_MESSAGE);
				return;
			}
			String nickk = nick_input.getText().replaceAll("[^\\x00-\\x7F]", "");
			Window.setTextInField(nick_input, nickk);
			if (nick_input.getText().contains(" ") || nick_input.getText().contains("&") || nick_input.getText().contains("#") || nick_input.getText().contains("@") || nick_input.getText().contains("!") || nick_input.getText().contains("$") || nick_input.getText().contains("%") || nick_input.getText().contains("^") || nick_input.getText().contains("*") || nick_input.getText().contains("(") || nick_input.getText().contains(")") || nick_input.getText().contains("+") || nick_input.getText().contains("=") || nick_input.getText().contains("'") || nick_input.getText().contains("\"") || nick_input.getText().contains(";") || nick_input.getText().contains(":") || nick_input.getText().contains(".") || nick_input.getText().contains(",") || nick_input.getText().contains(">") || nick_input.getText().contains("<") || nick_input.getText().contains("/") || nick_input.getText().contains("?") || nick_input.getText().contains("|") || nick_input.getText().contains("\\") || nick_input.getText().contains("]") || nick_input.getText().contains("[") || nick_input.getText().contains("{") || nick_input.getText().contains("}") || nick_input.getText().contains("~") || nick_input.getText().contains("`") || nick_input.getText().contains("€") /* precz z komuną */) {
				JOptionPane.showMessageDialog(null, banned_chars, warning, JOptionPane.WARNING_MESSAGE);
				return;
			}
			play.setEnabled(false);

			//Timer timer = new Timer();
			try {
				new Thread() {
					public void run() {
						setStatus(play, downloading);
						File wrapper = new File(BC.get() + "launcher/", "betacraft_wrapper.jar");
						try {
							Files.copy(Launcher.currentPath.toPath(), wrapper.toPath(), StandardCopyOption.REPLACE_EXISTING);
						} catch (FileSystemException ex) { // there is another instance of the game running
						} catch (Exception ex) {
							ex.printStackTrace();
							JOptionPane.showMessageDialog(null, "File cannot be copied! Try running with Administrator rights. If that won't help, contact me: @Moresteck#1688", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						if (Launcher.getProperty(Launcher.SETTINGS, "RPC").equalsIgnoreCase("true")) {
							File rpc = new File(BC.get() + "launcher/", "discord_rpc.jar");
							if (!rpc.exists()) {
								Launcher.download("https://betacraft.ovh/versions/discord_rpc.jar", rpc);
							}
						}
						if (!Launcher.getVerDownloaded(Launcher.chosen_version)) {
							for (Release r : Release.versions) {
							    if (r.getName().equals(Launcher.chosen_version)) {
							        if (!Launcher.download(Launcher.getVerLink(Launcher.chosen_version), new File(Launcher.getVerFolder(), Launcher.chosen_version + ".jar"))) {
		                                JOptionPane.showMessageDialog(null, no_connection, download_fail, JOptionPane.ERROR_MESSAGE);
							        }
							    }
							}
						}
						if (!Launcher.nativesDownloaded(false)) {
							Launcher.nativesDownloaded(true);
						}

						setStatus(play, play_lang);
						play.setEnabled(true);
						new Launcher().LaunchGame(Launcher.getCustomParameters(), nick_input.getText());
					}
				}.start();
			} catch (Exception ex) {
				
			}
		} else if (source == select_version) {
			currentAbout.setVisible(true);
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
