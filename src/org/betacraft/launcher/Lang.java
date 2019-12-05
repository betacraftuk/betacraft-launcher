package org.betacraft.launcher;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.betacraft.launcher.VersionSorter.Order;

public class Lang extends JFrame {
	public static List<String> locales = new ArrayList<String>();

	static JList list;
	static DefaultListModel listModel;
	static JScrollPane listScroller;
	JButton OKButton;

	public Lang() {
		Logger.a("Language option window has been opened.");
		setSize(282, 386);
		setLayout(null);
		setTitle("Select language");
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);

		try {
			if (locales.isEmpty()) initLang();
		} catch (IOException e1) {
			e1.printStackTrace();
			Logger.a(e1);
		}

		OKButton = new JButton("OK");
		OKButton.setBounds(10, 320, 60, 20);
		OKButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setLang();
			}
		});
		add(OKButton);

		OKButton.setBackground(Color.LIGHT_GRAY);
	}

	public void setLang() {
		String lang = (String) list.getSelectedValue();

		// Only return if we failed to download without a backup
		if (download(lang) == DownloadResult.FAILED_WITHOUT_BACKUP) {
			return;
		}
		Launcher.setProperty(Launcher.SETTINGS, "language", lang);
		setVisible(false);
		apply(true);
	}

	public void initLang() throws IOException {
		URL url = new URL("https://betacraft.pl/lang/index.html");

		Scanner scanner = new Scanner(url.openStream());
		String now;
		while (scanner.hasNextLine()) {
			now = scanner.nextLine();
			if (now.equalsIgnoreCase("")) continue;
			locales.add(now);
		}
		scanner.close();

		int i = 0;
		int index = 0;
		listModel = new DefaultListModel();
		String lang = Launcher.getProperty(Launcher.SETTINGS, "language");
		for (String item : locales) {
			listModel.addElement(item);
			if (lang.equals(item)) {
				index = i;
			}
			i++;
		}

		if (list != null) this.remove(list);

		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(3);
		list.setSelectedIndex(index);

		if (listScroller != null) this.remove(listScroller);

		listScroller = new JScrollPane(list);
		listScroller.setBounds(10, 30, 262, 280);
		listScroller.setWheelScrollingEnabled(true);
		add(listScroller);
	}

	public static boolean downloaded(String lang) {
		File file = new File(BC.get() + "launcher/lang", lang);
		if (file.exists()) {
			return true;
		}
		return false;
	}

	public static DownloadResult download(String lang) {
		DownloadResult done = Launcher.download("https://betacraft.pl/lang/" + lang + ".txt", new File(BC.get() + "launcher/lang/", lang + ".txt"));
		if (done != DownloadResult.OK) {
			JOptionPane.showMessageDialog(null, "No Internet connection", "Language file download failed!", JOptionPane.ERROR_MESSAGE);
			resetChangelog(true);
			//Window.quit();
		}
		return done;
	}

	public static String get(String property) {
		return Launcher.getProperty(new File(BC.get() + "launcher/lang/", Launcher.getProperty(Launcher.SETTINGS, "language") + ".txt"), property);
	}

	public static void apply() {
		apply(false);
	}

	public static void resetChangelog(boolean connectionFail) {
		Window.infoPanel.setVisible(false);
		Window.mainWindow.remove(Window.infoPanel);
		Window.infoPanel = null;
		Window.infoPanel = new InfoPanel(!connectionFail);
		Window.mainWindow.add(Window.infoPanel);
	}

	public static void apply(boolean all) {
		String lang = Launcher.getProperty(Launcher.SETTINGS, "language");
		if (lang.equals("")) {
			download("English");
			lang = "English";
		}
		File file = new File(BC.get() + "launcher/lang/", lang + ".txt");

		// Reset language of the changelog
		if (all) {
			resetChangelog(false);
		}

		Window.selectVersionButton.setText(Launcher.getProperty(file, "version_button"));
		Window.playButton.setText(Launcher.getProperty(file, "play_button"));
		Window.optionsButton.setText(Launcher.getProperty(file, "options_button"));
		Window.mainWindow.setTitle(Launcher.getProperty(file, "launcher_title") + Launcher.VERSION);
		Window.credits.setText(Launcher.getProperty(file, "credits"));
		Window.nicktext.setText(Launcher.getProperty(file, "nick") + ":");

		Launcher.update = Launcher.getProperty(file, "new_version_found");
		Launcher.lang_version = Launcher.getProperty(file, "version");

		Window.play_lang = Launcher.getProperty(file, "play_button");
		Window.max_chars = Launcher.getProperty(file, "max_chars");
		Window.min_chars = Launcher.getProperty(file, "min_chars");
		Window.banned_chars = Launcher.getProperty(file, "banned_chars");
		Window.warning = Launcher.getProperty(file, "warning");
		Window.no_connection = Launcher.getProperty(file, "no_connection");
		Window.download_fail = Launcher.getProperty(file, "download_fail");
		Window.downloading = Launcher.getProperty(file, "downloading");
		Window.resizeObjects();

		Options.update = Launcher.getProperty(file, "update_check");
		Options.update_not_found = Launcher.getProperty(file, "update_not_found");
		if (Window.optionsWindow != null) {
			Options.proxyCheck.setText(Launcher.getProperty(file, "use_betacraft"));
			Options.parametersText.setText(Launcher.getProperty(file, "launch_arguments") + ":");
			Options.keepOpenCheck.setText(Launcher.getProperty(file, "keep_launcher_open"));
			Options.checkUpdateButton.setText(Launcher.getProperty(file, "check_update_button"));
			Window.optionsWindow.setTitle(Launcher.getProperty(file, "options_title"));
		}

		Version.also_known = Launcher.getProperty(file, "also_known_as");
		Version.release = Launcher.getProperty(file, "release_date");
		Version.info = Launcher.getProperty(file, "info");
		Version.show_more = Launcher.getProperty(file, "info_button");
		Version.mc_wiki = Launcher.getProperty(file, "minecraft_wiki");
		Version.internal_err = Launcher.getProperty(file, "internal_error");
		if (Window.versionListWindow != null) {
			Window.versionListWindow.setTitle(Launcher.getProperty(file, "version_title"));
			if (Version.showinfo) {
				Version.more.setText(Version.show_more + " <");
			} else {
				Version.more.setText(Version.show_more + " >");
			}
			Version.date.setText(Version.release);
			Version.information.setText(Version.info);
			Version.sort_button.setText(Version.order == Order.FROM_OLDEST ? Launcher.getProperty(file, "sort_oldest") : Launcher.getProperty(file, "sort_newest"));
			Version.open_wiki.setText(Version.mc_wiki);
		}
	}
}
