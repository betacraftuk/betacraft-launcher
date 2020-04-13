package org.betacraft.launcher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class ModsRepository extends JFrame implements ActionListener {

	public static ArrayList<String> mods = new ArrayList<String>();

	public static void loadMods() {
		try {
			final URL url = new URL("https://betacraft.pl/launcher/assets/mods/list.txt");

			InputStream onlineListStream = null;
			try {
				onlineListStream = url.openStream();
			} catch (UnknownHostException ex) {
				Logger.a(null);
			} catch (SocketTimeoutException ex) {
				Logger.a(null);
			} catch (SocketException ex) {
				Logger.a(null);
			} catch (Exception ex) {
				Logger.a("A critical error has occurred while attempting to get the online addons list!");
				ex.printStackTrace();
				Logger.printException(ex);

				// Every networking bug has been catched before, so this one must be serious
				JOptionPane.showMessageDialog(null, "An error occurred while loading mods list! Report this to: @Moresteck#1688", "Critical error!", JOptionPane.ERROR_MESSAGE);
			}

			// If connection failed, return
			if (onlineListStream == null) {
				return;
			}

			Scanner onlineListScanner = new Scanner(onlineListStream, "UTF-8");
			for (String ver : scan(onlineListScanner)) {
				mods.add(ver);
			}
			setModsOnline();

			// Close the connection
			onlineListScanner.close();
			onlineListStream.close();
		} catch (Exception ex) {
			Logger.a("A critical error occurred while initializing addons list!");
			ex.printStackTrace();
			Logger.printException(ex);

			JOptionPane.showMessageDialog(null, "An error occurred while loading mods list! Report this to: @Moresteck#1688", "Critical error!", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected static void setModsOnline() {
		for (String s : mods) {
			for (Release r : Release.versions) {
				if (r.getName().equals(s)) {
					if (Release.versions.get(0).getJson().online) {
						r.getJson().online = true;
					}
				}
			}
		}
	}

	protected static List<String> scan(Scanner scanner) {
		List<String> results = new ArrayList<String>();

		String currentLine = null;
		while (scanner.hasNextLine()) {
			currentLine = scanner.nextLine();

			// If the line is empty, ignore it
			if (currentLine.equalsIgnoreCase("")) continue;

			results.add(currentLine);
		}
		return results;
	}

	static JList list;
	static DefaultListModel listModel;
	static JScrollPane listScroller;
	static JButton OK;
	static JPanel panel;
	static GridBagConstraints constr;

	public ModsRepository() {
		Logger.a("Mods repository window has been opened.");
		this.setIconImage(Window.img);
		setMinimumSize(new Dimension(282, 386));
		setTitle(Lang.INSTANCE_MODS_REPOSITORY);
		setResizable(true);

		panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		constr = new GridBagConstraints();
		constr.fill = GridBagConstraints.BOTH;
		constr.insets = new Insets(5, 5, 0, 5);
		constr.gridwidth = GridBagConstraints.RELATIVE;
		constr.weightx = 1.0;

		updateList();

		constr.gridy = 2;
		constr.weighty = GridBagConstraints.RELATIVE;
		constr.gridheight = 1;
		constr.insets = new Insets(0, 5, 5, 5);
		OK = new JButton(Lang.OPTIONS_OK);
		OK.addActionListener(this);
		panel.add(OK, constr);

		this.getContentPane().add(panel, BorderLayout.CENTER);
		this.pack();
		setLocationRelativeTo(Window.mainWindow);
		setVisible(true);
	}

	protected void updateList() {
		listModel = null;
		listModel = new DefaultListModel();
		for (String s : mods) {
			listModel.addElement(s);
		}

		constr.weighty = 1.0;
		constr.gridheight = GridBagConstraints.RELATIVE;
		constr.gridy = 1;
		
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(3);

		if (listScroller != null) panel.remove(listScroller);

		listScroller = new JScrollPane(list);
		listScroller.setWheelScrollingEnabled(true);
		panel.add(listScroller, constr);
	}

	public void saveVersions() {
		if (list.getSelectedValuesList().size() == 0) return;
		for (Object o : list.getSelectedValuesList()) {
			String s = (String) o;
			new ReleaseJson(s, true, true);
		}
		try {
			Release.initVersions();
			setModsOnline();
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
		if (list.getSelectedValuesList().size() == 1) {
			Launcher.currentInstance.version = (String) list.getSelectedValuesList().get(0);
			Launcher.setInstance(Launcher.currentInstance);
			Launcher.currentInstance.saveInstance();
		}
		setVisible(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == OK) {
			saveVersions();
			Window.modsRepo = null;
		}
	}
}
