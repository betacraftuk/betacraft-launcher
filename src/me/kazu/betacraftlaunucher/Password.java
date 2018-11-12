package me.kazu.betacraftlaunucher;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Password extends JFrame implements ActionListener{
	
	JButton next, exit;
	JLabel text;
	static JPasswordField pass;
	
	public Password()
	{
		setSize(311, 116);
		setTitle("Betacraft Launcher Java 1.4");
		setLayout(null);
		setLocationRelativeTo(null);
		setResizable(false);
		this.setUndecorated(true);
		Color color = UIManager.getColor("activeCaptionBorder");
		this.getRootPane().setBorder(BorderFactory.createLineBorder(color, 4));
		
		next = new JButton("Dalej");
		exit = new JButton();
		pass = new JPasswordField();
		text = new JLabel("Has³o");
		
		next.setBounds(120, 70, 75, 20);
		exit.setBounds(260, 10, 32, 31);
		pass.setBounds(93, 50, 131, 20);
		text.setBounds(55, 45, 45, 30);
		
		add(next);
		add(exit);
		add(pass);
		add(text);
		
		next.addActionListener(this);
		exit.addActionListener(this);
		
		pass.getDocument().addDocumentListener(new DocumentListener() {
			
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
			     if (pass.getText().length() >= 15){
			     JOptionPane.showMessageDialog(null, "Za d³ugie has³o!", "UWAGA!", JOptionPane.WARNING_MESSAGE);
			     pass.setText(null); //error do naprawienia
			     }
			  }
		});
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		String peanus = "tytusgrubyhui";
		if(source == next)
		{
			if(!pass.getText().equalsIgnoreCase(peanus))
			{
				JOptionPane.showMessageDialog(null, "Niepoprawne has³o!", "UWAGA!", JOptionPane.WARNING_MESSAGE);
			}
			else if(pass.getText().equalsIgnoreCase(peanus))
			{
				Launcher.Download(); //admin
			}
		}
		if(source == exit)
		{
			this.setVisible(false);
		}
		
	}
}
