package me.kazu.betacraftlaunucher;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class Window extends JFrame implements ActionListener{
	
	JButton play, about;
	JLabel kazu, nicktext;
	JTextField nick = new JTextField();
	
	public Window()
	{
		setSize(800, 450);
		setTitle("Betacraft Launcher Java 1.4");
		setLayout(null);
		
		play = new JButton("Graj");
		about = new JButton("About");
		kazu = new JLabel("Launcher zosta³ zrobiony przez Kazu");
		nicktext = new JLabel("Nick");
		
		play.setBounds(300, 340, 195, 36);
		nick.setBounds(337, 310, 120, 23);
		kazu.setBounds(15, 370, 210, 30);
		about.setBounds(750, 380, 22, 19);
		nicktext.setBounds(300, 311, 25, 19);
		
		add(play);
		add(nick);
		add(kazu);
		add(about);
		add(nicktext);
		
		play.addActionListener(this); //this - s³uchaczem zdarzeñ jest ca³a ramka
		about.addActionListener(this);
		
		//nick.setText("hui");
		kazu.setForeground(new Color(61, 60, 68));
		nicktext.setForeground(Color.BLACK);
	}
	
	public static void main(String[] args)
	{
		Window w = new Window();
		w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		w.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if(source == play)
		{
			
		}
		
		else if(source == about)
		{
			
		}
	}
	
	
}
