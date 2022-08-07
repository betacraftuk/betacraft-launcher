package org.betacraft.launcher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ConsoleLogFrame extends JFrame {
	static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
	BufferedImage iconImg;
	
	JButton copy, clear, pause;
	JTextArea logArea;

	boolean enabled;
	boolean paused = false;
	
	String buffer = "";
	String pauseBuffer = "";

	public ConsoleLogFrame(String instancename, boolean enabled) {
		this.enabled = enabled;
		if (!this.enabled) return;

		try {
			iconImg = ImageIO.read(Launcher.class.getResource("/icons/console.png"));
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		}
		
		this.setIconImage(this.iconImg);
		this.setTitle(String.format(Lang.CONSOLE_OUTPUT_FOR, instancename));
		this.setMinimumSize(new Dimension(640, 640));
		this.setPreferredSize(new Dimension(1080, 720));
		this.setResizable(true);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setLayout(new java.awt.FlowLayout(FlowLayout.LEFT, 5, 5));

		GridBagConstraints constr = new GridBagConstraints();

		constr.gridx = 0;
		constr.gridy = 0;
		constr.fill = GridBagConstraints.BOTH;
		constr.anchor = GridBagConstraints.WEST;
		constr.gridwidth = 1;
		constr.weightx = 0.0;
		constr.insets = new Insets(10, 5, 10, 5);
		
		copy = new JButton(Lang.COPY);
		copy.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(buffer);
				Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection, selection);
			}
			
		});
		panel.add(copy);
		
		constr.gridx++;
		pause = new JButton(Lang.PAUSE);
		pause.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				paused = !paused;
				if (paused) {
					pause.setText(Lang.UNPAUSE);
				} else {
					print(pauseBuffer);
					pauseBuffer = "";
					pause.setText(Lang.PAUSE);
				}
			}
			
		});
		panel.add(pause);
		
		constr.gridx++;
		clear = new JButton(Lang.CLEAR);
		clear.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				clear();
			}
			
		});
		panel.add(clear);
		
		panel.setBackground(new java.awt.Color(22, 22, 22));

		logArea = new JTextArea();
		logArea.setFont(new Font("Consolas", Font.PLAIN, 18));
		logArea.setForeground(java.awt.Color.LIGHT_GRAY);
		logArea.setBackground(new java.awt.Color(22, 22, 22));
		logArea.setLineWrap(true);
		logArea.setWrapStyleWord(true);
		logArea.setCaretColor(java.awt.Color.WHITE);

		JScrollPane scroll = new JScrollPane(logArea);
		scroll.setWheelScrollingEnabled(true);
		
		this.getContentPane().add(panel, BorderLayout.NORTH);
		this.getContentPane().add(scroll, BorderLayout.CENTER);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public void clear() {
		if (!this.enabled) return;

		this.buffer = "";
		this.pauseBuffer = "";
		logArea.setText("");
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}
	
	public void print(String text) {
		if (!this.enabled) return;

		logArea.append(text);
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	public void log(String text) {
		if (!this.enabled) return;

		String date = format.format(Long.valueOf(System.currentTimeMillis()));
		text = "(" + date + ") " + text;

		this.buffer += text;
		if (!this.paused) {
			print(text);
		} else {
			this.pauseBuffer += text;
		}
	}
}
