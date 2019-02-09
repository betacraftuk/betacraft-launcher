package org.betacraft.launcher;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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

	static JButton play, about, options;
	static JLabel kazu, nicktext;
	static JTextField nick = null;
	static Wersja currentAbout = null;
	static Opcje currentOptions = null;
	static InfoPanel infopanel = null;
	static LoginPanel loginpanel = null;
	public static Window window = null;

	public Window() {
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

		nick = new JTextField(Launcher.getLastlogin());
		play = new JButton("Play");
		about = new JButton("Change version");
		kazu = new JLabel("Betacraft Launcher made by KazuGod & Moresteck");
		nicktext = new JLabel("Nick:");
		options = new JButton("Options");

		resizeObjects();

		// buttony sa dodawane w Background.paintComponent() aby nie byly pod tlem

		play.addActionListener(this); // this - sluchaczem zdarzen jest cala ramka
		about.addActionListener(this);
		options.addActionListener(this);

		kazu.setForeground(Color.LIGHT_GRAY);
		nicktext.setForeground(Color.WHITE);

		options.setBackground(Color.LIGHT_GRAY);
		about.setBackground(Color.LIGHT_GRAY);
		play.setBackground(Color.LIGHT_GRAY);

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

		this.addComponentListener(new ComponentAdapter() {
		    public void componentResized(ComponentEvent componentEvent) {
		    	if (componentEvent.getComponent().getSize().width < 800 || componentEvent.getComponent().getSize().height < 450) {
		    		componentEvent.getComponent().setSize(800, 450);
		    	}
		        resizeObjects();
		    }
		});
	}

	public static void resizeObjects() {
		int width = window.getSize().width / 2; // 400
		int height = window.getSize().height / 2; // 450

		infopanel.setBounds(0, 0, 800, height + 25 + 40); // 290, 800
		play.setBounds(((Double)(width * 0.75)).intValue(), 50, 195, 36);
		nick.setBounds(((Double)(width * 0.84)).intValue(), 20, 120, 24); // 337
		kazu.setBounds(15, 90, 390, 30);
		about.setBounds(((Double)(width * 0.13)).intValue(), 39, 150, 20); // 50
		nicktext.setBounds(((Double)(width * 0.75)).intValue(), 21, 35, 20);
		options.setBounds(((Double)(width * 0.13)).intValue(), 60, 150, 20);
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
						if (!Launcher.getVerDownloaded(Launcher.chosen_version)) {
							if (!Launcher.download(Launcher.getVerLink(Launcher.chosen_version), new File(Launcher.getVerFolder(), Launcher.chosen_version + ".jar"))) {
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
