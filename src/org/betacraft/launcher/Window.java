package org.betacraft.launcher;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Window extends JFrame implements ActionListener {

	static String[] versions = new String[]{"1.0", "1.0_01", "1.0.2", "1.1_01", "1.1_02",
			"1.2", "1.2_01", "1.2_02", "1.3", "1.3_01", "1.4", "1.4_01", "1.5",
			"1.5_01", "1.6", "1.6.1", "1.6.2", "1.6.3", "1.6.4", "1.6.5", "1.6.6",
			"1.7", "1.7_01", "1.7.2", "1.7.3", "1.8", "1.8.1"};
	static String[] other_versions = new String[] {"1.6-build3", "1.8-pre1", "1.8-pre2",
			"1.9-pre1", "1.9-pre2", "1.9-pre3", "1.9-pre4", "1.9-pre5", "1.9-pre6"};
	public static String chosen_version = "1.6.6";

	JButton play, about, options;
	JLabel kazu, nicktext;
	static JTextField nick = new JTextField();

	public Window()
	{
		// TODO autoupdate, obrazki, przegladarka
		setSize(800, 450);
		setTitle("Betacraft Launcher " + Launcher.VERSION);
		setLayout(null);
		setLocationRelativeTo(null);
		setResizable(false);
		
		play = new JButton("Graj");
		about = new JButton("About");
		kazu = new JLabel("Launcher zosta³ napisany przez techników BetaCrafta");
		nicktext = new JLabel("Nick:");
		options = new JButton("Opcje");

		play.setBounds(300, 340, 195, 36);
		nick.setBounds(337, 310, 120, 23);
		kazu.setBounds(15, 380, 310, 30);
		about.setBounds(750, 380, 22, 19);
		nicktext.setBounds(300, 311, 35, 19);
		options.setBounds(50, 350, 70, 19);

		add(play);
		add(nick);
		add(kazu);
		add(about);
		add(nicktext);
		add(options);

		play.addActionListener(this); // this - sluchaczem zdarzen jest cala ramka
		about.addActionListener(this);

		kazu.setForeground(new Color(61, 60, 68));
		nicktext.setForeground(Color.BLACK);

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
					JOptionPane.showMessageDialog(null, "Maksymalna d³ugoœæ nicku to 16 znaków!", "UWAGA!", JOptionPane.WARNING_MESSAGE);
					Window.setTextInField(nick, "");
				}
			}
		});
	}
	
	public static void main(String[] args) {
		Window w = new Window();
		About ab = new About();
		w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ab.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		w.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		About ab = new About();
		
		if (source == play) {
			if (nick.getText().length() < 3) {
				JOptionPane.showMessageDialog(null, "Nick musi zawieraæ wiêcej ni¿ 3 znaki. Wyd³u¿ swój nick!", "UWAGA!", JOptionPane.WARNING_MESSAGE);
				return;
			}
			String nickk = nick.getText().replaceAll("[^\\x00-\\x7F]", "");
			Window.setTextInField(nick, nickk);
			if (nick.getText().contains(" ") || nick.getText().contains("&") || nick.getText().contains("#") || nick.getText().contains("@") || nick.getText().contains("!") || nick.getText().contains("$") || nick.getText().contains("%") || nick.getText().contains("^") || nick.getText().contains("*") || nick.getText().contains("(") || nick.getText().contains(")") || nick.getText().contains("+") || nick.getText().contains("=") || nick.getText().contains("'") || nick.getText().contains("\"") || nick.getText().contains(";") || nick.getText().contains(":") || nick.getText().contains(".") || nick.getText().contains(",") || nick.getText().contains(">") || nick.getText().contains("<") || nick.getText().contains("/") || nick.getText().contains("?") || nick.getText().contains("|") || nick.getText().contains("\\") || nick.getText().contains("]") || nick.getText().contains("[") || nick.getText().contains("{") || nick.getText().contains("}") || nick.getText().contains("~") || nick.getText().contains("`") || nick.getText().contains("€") /* precz z komun¹ */) {
				JOptionPane.showMessageDialog(null, "Nick nie mo¿e zawieraæ polskich znaków, spacji oraz znaków typu &, # i tym podobnych.", "UWAGA!", JOptionPane.WARNING_MESSAGE);
				return;
			}
			Launcher.Download("_" + chosen_version); // gracz
		} else if (source == about) {
			ab.setVisible(true);
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
