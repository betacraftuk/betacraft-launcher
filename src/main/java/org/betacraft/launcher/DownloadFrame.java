package org.betacraft.launcher;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

// TODO redo this before next major release
public class DownloadFrame extends JFrame {
	public JProgressBar bar;
	public int size;

	public DownloadFrame(String update) {
		setSize(360, 80);
		setLayout(null);
		setTitle("Updating launcher");
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints constr = new GridBagConstraints();

		constr.gridx = 0;
		constr.gridy = 0;
		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.insets = new Insets(10, 10, 10, 10);

		JLabel label = new JLabel("Downloading Launcher v" + update + " ...");
		bar = new JProgressBar();
		panel.add(bar, constr);

		label.setBounds(30, 10, 340, 20);
		this.getContentPane().add(label, BorderLayout.CENTER);
		setVisible(true);
	}

	public void download(final String link, final File folder) {
		new Thread() {
			int downloadSize = 0;
			public void run() {
				System.out.println("Update started from: " + link);

				try {
					// Start download
					URL url = new URL(link);

					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.connect();
					size = con.getContentLength();

					bar.setStringPainted(true);
					bar.setMaximum(size);
					bar.setValue(0);

					BufferedInputStream inputst = new BufferedInputStream(con.getInputStream());
					FileOutputStream outputst = new FileOutputStream(folder);
					byte[] buffer = new byte[1024];
					int count = 0;
					while((count = inputst.read(buffer, 0, 1024)) != -1) {
						downloadSize += count;
						outputst.write(buffer, 0, count);
						bar.setValue(downloadSize);
					}
					outputst.close();
					inputst.close();
					downloadSize = 0;
					System.out.println("Update done.");
					return;
				} catch (Exception ex) {
					ex.printStackTrace();

					// Delete the failed download
					folder.delete();
				}
			}
		}.start();
	}
}
