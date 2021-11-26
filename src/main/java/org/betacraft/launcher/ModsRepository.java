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
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.betacraft.launcher.Release.VersionRepository;

public class ModsRepository extends JFrame implements ActionListener, LanguageElement {

	public static ArrayList<String> mods = new ArrayList<String>();

	public static void loadMods() {
		try {
			final URL url = new URL("http://files.betacraft.uk/launcher/assets/mods/1.09_10/list.txt");

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
			}

			// If connection failed, return
			if (onlineListStream == null) {
				return;
			}

			Scanner onlineListScanner = new Scanner(onlineListStream, "UTF-8");
			for (String ver : scan(onlineListScanner)) {
				mods.add(ver);
			}

			// Close the connection
			onlineListScanner.close();
			onlineListStream.close();
		} catch (Exception ex) {
			Logger.a("A critical error occurred while initializing addons list!");
			ex.printStackTrace();
			Logger.printException(ex);
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
	static JButton more_button;
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

		more_button = new JButton(Lang.ADDON_SHOW_INFO);
		more_button.addActionListener(this);

		panel.add(more_button, constr);
		this.add(panel, BorderLayout.NORTH);

		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		constr.gridy = 2;
		constr.weighty = GridBagConstraints.RELATIVE;
		constr.gridheight = 1;
		constr.insets = new Insets(0, 5, 5, 5);
		OK = new JButton(Lang.OPTIONS_OK);
		OK.addActionListener(this);
		panel.add(OK, constr);

		this.add(panel, BorderLayout.SOUTH);

		panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		/*panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		constr = new GridBagConstraints();
		constr.fill = GridBagConstraints.BOTH;
		constr.insets = new Insets(5, 5, 0, 5);
		constr.gridwidth = GridBagConstraints.RELATIVE;
		constr.weightx = 1.0;*/

		updateList();

		/*constr.gridy = 2;
		constr.weighty = GridBagConstraints.RELATIVE;
		constr.gridheight = 1;
		constr.insets = new Insets(0, 5, 5, 5);
		OK = new JButton(Lang.OPTIONS_OK);
		OK.addActionListener(this);
		panel.add(OK, constr);*/

		this.getContentPane().add(panel, BorderLayout.CENTER);
		this.pack();
		setLocationRelativeTo(Window.mainWindow);
		setVisible(true);
	}

	public void update() {
		this.setTitle(Lang.INSTANCE_MODS_REPOSITORY);
		OK.setText(Lang.OPTIONS_OK);
		this.pack();
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
		list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				String mod = (String) list.getSelectedValue();
			}
		});
	}

	public void saveVersions() {
		if (list.getSelectedValuesList().size() != 0) {
			for (Object o : list.getSelectedValuesList()) {
				String s = (String) o;
				new ReleaseJson(s).downloadJson();
			}
			try {
				Release.loadVersions(VersionRepository.BETACRAFT);
			} catch (Exception ex) {
				ex.printStackTrace();
				Logger.printException(ex);
			}
			if (list.getSelectedValuesList().size() == 1) {
				Launcher.currentInstance.version = (String) list.getSelectedValuesList().get(0);
				Launcher.setInstance(Launcher.currentInstance);
				Launcher.currentInstance.saveInstance();
			}
		}
		setVisible(false);
	}

	public JScrollPane getInfo(String name) {
		JEditorPane pane = new JEditorPane();
		pane.setEditable(false);
		pane.setOpaque(false);
		pane.setContentType("text/html;charset=UTF-8");
		pane.addHyperlinkListener(WebsitePanel.EXTERNAL_HYPERLINK_LISTENER);
		try {
			pane.setPage(new URL("http://files.betacraft.uk/launcher/assets/mods/" + name + ".html"));
		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
			pane.setText(Lang.ADDON_NO_DESC);
		}
		JScrollPane scrlPane = new JScrollPane(pane);
		scrlPane.setBorder(null);
		scrlPane.setWheelScrollingEnabled(true);
		return scrlPane;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == OK) {
			saveVersions();
			Window.modsRepo = null;
		} else if (e.getSource() == more_button) {
			for (Object l : list.getSelectedValuesList()) {
				String mod = (String) l;
				new BrowserWindow(getInfo(mod));
			}
		}
	}
}
