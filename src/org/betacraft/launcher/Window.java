package org.betacraft.launcher;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Window extends JFrame implements ActionListener {

	public static String chosen_version = "b1.6.6";

	JButton play, about, options;
	JLabel kazu, nicktext;
	static JTextField nick = null;
	static Wersja currentAbout = null;
	static Opcje currentOptions = null;
	public static Window window = null;

	public Window()
	{
		setSize(800, 450);
		setTitle("Betacraft Launcher version " + Launcher.VERSION);
		setLayout(null);
		setLocationRelativeTo(null);
		setResizable(false);

		nick = new JTextField(Launcher.getLastlogin());
		play = new JButton("Play");
		about = new JButton("Change version");
		kazu = new JLabel("Betacraft Launcher made by KazuGod & Moresteck");
		nicktext = new JLabel("Nick:");
		options = new JButton("Options");

		play.setBounds(300, 340, 195, 36);
		nick.setBounds(337, 310, 120, 23);
		kazu.setBounds(15, 380, 390, 30);
		about.setBounds(750, 380, 22, 19);
		nicktext.setBounds(300, 311, 35, 19);
		options.setBounds(50, 350, 120, 19);

		add(play);
		add(nick);
		add(kazu);
		add(about);
		add(nicktext);
		add(options);

		play.addActionListener(this); // this - sluchaczem zdarzen jest cala ramka
		about.addActionListener(this);
		options.addActionListener(this);

		kazu.setForeground(new Color(61, 60, 68));
		nicktext.setForeground(Color.BLACK);

		play.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Launcher.write(new File(BC.get(), "lastlogin"), new String[] {nick.getText()}, false);
			}
		});

		nick.getDocument().addDocumentListener(new DocumentListener() {

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
				if (nick.getText().length() > 16){
					JOptionPane.showMessageDialog(null, "The maxixum amount of characters in a nickname is 16.", "Warning", JOptionPane.WARNING_MESSAGE);
					Window.setTextInField(nick, nick.getText().substring(0, 16));
				}
			}
		});
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				Launcher.write(new File(BC.get(), "lastlogin"), new String[] {nick.getText()}, false);
				Logger.a("Dezaktywacja...");
				System.exit(0);
			}
		});
	}
	
	public static void main(String[] args) {
		new File(BC.get() + "versions/").mkdirs();
		new File(BC.get() + "bin/natives/").mkdirs();
		window = new Window();
		try {
			Release.initVersions();
		} catch (Exception ex) {
			Logger.a("FATALNY ERROR: ");
			Logger.a(ex.getMessage());
			ex.printStackTrace();
		}
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		if (Launcher.checkForUpdate()) {
			Launcher.downloadUpdate();
		}
		String ver = Launcher.getProperty(new File(BC.get(), "launcher.settings"), "version");
		if (ver.equals("")) {
			chosen_version = "c0.0.13a_03";
		} else {
			chosen_version = ver;
		}
	}

	public static void quit() {
		window.setVisible(false);
		window.dispose();
		System.exit(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == about && currentAbout == null) {
			currentAbout = new Wersja();
		}
		if (source == options && currentOptions == null) {
			currentOptions = new Opcje();
		}
		if (source == options) {
			currentOptions.setVisible(true);
		}
		if (source == play) {
			if (nick.getText().length() < 3) {
				JOptionPane.showMessageDialog(null, "Your nickname must have at least 3 characters.", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			String nickk = nick.getText().replaceAll("[^\\x00-\\x7F]", "");
			Window.setTextInField(nick, nickk);
			if (nick.getText().contains(" ") || nick.getText().contains("&") || nick.getText().contains("#") || nick.getText().contains("@") || nick.getText().contains("!") || nick.getText().contains("$") || nick.getText().contains("%") || nick.getText().contains("^") || nick.getText().contains("*") || nick.getText().contains("(") || nick.getText().contains(")") || nick.getText().contains("+") || nick.getText().contains("=") || nick.getText().contains("'") || nick.getText().contains("\"") || nick.getText().contains(";") || nick.getText().contains(":") || nick.getText().contains(".") || nick.getText().contains(",") || nick.getText().contains(">") || nick.getText().contains("<") || nick.getText().contains("/") || nick.getText().contains("?") || nick.getText().contains("|") || nick.getText().contains("\\") || nick.getText().contains("]") || nick.getText().contains("[") || nick.getText().contains("{") || nick.getText().contains("}") || nick.getText().contains("~") || nick.getText().contains("`") || nick.getText().contains("€") /* precz z komuną */) {
				JOptionPane.showMessageDialog(null, "Your nickname mustn't contain spaces and any characters like #, @, etc.", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			play.setText("Downloading ...");
			play.setEnabled(false);

			Timer timer = new Timer();
			try {
				timer.schedule(new TimerTask() {
					public void run() {
						if (!Launcher.getVerDownloaded(chosen_version)) {
							if (!Launcher.download(Launcher.getVerLink(chosen_version), new File(Launcher.getVerFolder(), chosen_version + ".jar"))) {
								JOptionPane.showMessageDialog(null, "No Internet connection. Couldn't download the version.", "No connection", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						if (!Launcher.nativesDownloaded(false)) {
							Launcher.nativesDownloaded(true);
						}
						
						play.setText("Play");
						play.setEnabled(true);
						new Launcher().LaunchGame(Launcher.getCustomParameters(), nick.getText());
					}
				}, 10);
			} catch (Exception ex) {
				
			}
		} else if (source == about) {
			currentAbout.setVisible(true);
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
}
