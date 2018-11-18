package me.kazu.betacraftlaunucher;

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

import com.google.common.base.CharMatcher;

public class Window extends JFrame implements ActionListener{

	JButton play, about, admin;
	JLabel kazu, nicktext;
	static JTextField nick = new JTextField();

	public Window()
	{
		// TODO obrazki, przegladarka
		setSize(800, 450);
		setTitle("Betacraft Launcher Java 1.4");
		setLayout(null);
		setLocationRelativeTo(null);
		setResizable(false);

		play = new JButton("Graj");
		about = new JButton("About");
		admin = new JButton("Admin");
		kazu = new JLabel("Launcher zosta³ zrobiony przez Kazu");
		nicktext = new JLabel("Nick");

		play.setBounds(300, 340, 195, 36);
		nick.setBounds(337, 310, 120, 23);
		kazu.setBounds(15, 370, 210, 30);
		about.setBounds(750, 380, 22, 19);
		nicktext.setBounds(300, 311, 25, 19);
		admin.setBounds(15, 10, 21, 79);

		add(play);
		add(nick);
		add(kazu);
		add(about);
		add(nicktext);
		add(admin);

		play.addActionListener(this); // this - sluchaczem zdarzen jest cala ramka
		about.addActionListener(this);
		admin.addActionListener(this);

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
				if (nick.getText().length() >= 16){
					JOptionPane.showMessageDialog(null, "Maksymalna d³ugoœæ nicku to 15 znaków!", "UWAGA!", JOptionPane.WARNING_MESSAGE);
					Runnable setPassword = new Runnable() {
						public void run() {
							nick.setText("");
						}

					};
					SwingUtilities.invokeLater(setPassword);
				}
			}
		});
	}
	
	public static void main(String[] args) {
		Window w = new Window();
		About ab = new About();
		Password pw = new Password();
		w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ab.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pw.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		w.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		About ab = new About();
		Password pw = new Password();
		
		if (source == play) {
			if (nick.getText().length() < 3) {
				JOptionPane.showMessageDialog(null, "Nick musi zawieraæ wiêcej ni¿ 3 znaki. Wyd³u¿ swój nick!", "UWAGA!", JOptionPane.WARNING_MESSAGE);
				return;
			}
			boolean ASCII = CharMatcher.ASCII.matchesAllOf(nick.getText());
			if (!ASCII || nick.getText().contains(" ") || nick.getText().contains("&") || nick.getText().contains("#") || nick.getText().contains("@") || nick.getText().contains("!") || nick.getText().contains("$") || nick.getText().contains("%") || nick.getText().contains("^") || nick.getText().contains("*") || nick.getText().contains("(") || nick.getText().contains(")") || nick.getText().contains("+") || nick.getText().contains("=") || nick.getText().contains("'") || nick.getText().contains("\"") || nick.getText().contains(";") || nick.getText().contains(":") || nick.getText().contains(".") || nick.getText().contains(",") || nick.getText().contains(">") || nick.getText().contains("<") || nick.getText().contains("/") || nick.getText().contains("?") || nick.getText().contains("|") || nick.getText().contains("\\") || nick.getText().contains("]") || nick.getText().contains("[") || nick.getText().contains("{") || nick.getText().contains("}") || nick.getText().contains("~") || nick.getText().contains("`") || nick.getText().contains("€") /* precz z komun¹ */) {
				JOptionPane.showMessageDialog(null, "Nick nie mo¿e zawieraæ polskich znaków, spacji oraz znaków typu &, # i tym podobnych.", "UWAGA!", JOptionPane.WARNING_MESSAGE);
				return;
			}
			Launcher.Download("_"); // gracz
		} else if (source == about) {
			ab.setVisible(true);
			
		} else if (source == admin) {
			pw.setVisible(true);
		}
	}
}
