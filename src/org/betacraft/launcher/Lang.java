package org.betacraft.launcher;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class Lang extends JFrame implements LanguageElement {
	public static List<String> locales = new ArrayList<String>();

	static JList list;
	static DefaultListModel listModel;
	static JScrollPane listScroller;
	JButton OKButton;
	static JPanel panel;
	static GridBagConstraints constr;

	public Lang() {
		Logger.a("Language option window has been opened.");
		this.setIconImage(Window.img);
		this.setMinimumSize(new Dimension(282, 386));

		this.setTitle(Lang.LANG);
		this.setResizable(true);
		final boolean panelnull = panel == null;

		if (panelnull) {
			panel = new JPanel();
			panel.setLayout(new GridBagLayout());
		}

		constr = new GridBagConstraints();

		constr.gridx = 0;
		constr.fill = GridBagConstraints.BOTH;
		constr.insets = new Insets(5, 5, 0, 5);
		constr.gridwidth = GridBagConstraints.RELATIVE;
		constr.gridheight = GridBagConstraints.RELATIVE;
		constr.gridy = 0;
		constr.weightx = 1.0;
		constr.weighty = 1.0;
		try {
			if (locales.isEmpty()) initLang();
		} catch (IOException e1) {
			e1.printStackTrace();
			Logger.printException(e1);
		}

		constr.gridy++;
		constr.weighty = GridBagConstraints.RELATIVE;
		constr.gridheight = 1;
		constr.insets = new Insets(5, 5, 5, 5);
		if (panelnull) {
			OKButton = new JButton(Lang.OPTIONS_OK);
			OKButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setLang();
					Window.lang = null;
				}
			});
			panel.add(OKButton, constr);
		}
		this.add(panel);
		this.pack();
		this.setLocationRelativeTo(Window.mainWindow);
		this.setVisible(true);
	}

	public void update() {
		this.setTitle(Lang.LANG);
		OKButton.setText(Lang.OPTIONS_OK);
	}

	public void setLang() {
		String lang = (String) list.getSelectedValue();

		// Only return if we failed to download without a backup
		if (download(lang) == DownloadResult.FAILED_WITHOUT_BACKUP) {
			return;
		}
		Util.setProperty(BC.SETTINGS, "language", lang);
		setVisible(false);
		Launcher.restart();
	}

	public void initLang() throws IOException {
		URL url = new URL("https://betacraft.pl/lang/" + Launcher.VERSION + "/index.html");

		Scanner scanner = new Scanner(url.openStream(), "UTF-8");
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
		String lang = Util.getProperty(BC.SETTINGS, "language");
		for (String item : locales) {
			listModel.addElement(item);
			if (lang.equals(item)) {
				index = i;
			}
			i++;
		}


		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(3);
		list.setSelectedIndex(index);

		if (listScroller != null) panel.remove(listScroller);

		listScroller = new JScrollPane(list);
		listScroller.setWheelScrollingEnabled(true);
		panel.add(listScroller, constr);
	}

	public static boolean downloaded(String lang) {
		File file = new File(BC.get() + "launcher" + File.separator + "lang" + File.separator + lang + ".txt");
		if (file.exists()) {
			return true;
		}
		return false;
	}

	public static DownloadResult download(String lang) {
		DownloadResult done = Launcher.download("https://betacraft.pl/lang/" + Launcher.VERSION + "/" + lang + ".txt", new File(BC.get() + "launcher" + File.separator + "lang", lang + ".txt"));
		if (done != DownloadResult.OK) {
			JOptionPane.showMessageDialog(null, "No Internet connection", "Language file download failed!", JOptionPane.ERROR_MESSAGE);
		}
		return done;
	}

	public static void refresh(boolean download, boolean force) {
		String lang = Util.getProperty(BC.SETTINGS, "language");
		if (lang.equals("")) {
			lang = "English";
		}
		if (download && download(lang) == DownloadResult.FAILED_WITHOUT_BACKUP) return;
		File file = new File(BC.get() + "launcher" + File.separator + "lang" + File.separator +  lang + ".txt");
		String charset = "UTF-8";
		if (lang.equalsIgnoreCase("Russian")) charset = "Cp1251";

		WINDOW_SELECT_VERSION = Util.getProperty(file, "version_button", charset);
		WINDOW_PLAY = Util.getProperty(file, "play_button", charset);
		WINDOW_OPTIONS = Util.getProperty(file, "options_button", charset);
		WINDOW_TITLE = Util.getProperty(file, "launcher_title", charset) + Launcher.VERSION;
		WINDOW_CREDITS = Util.getProperty(file, "credits", charset);
		WINDOW_LANGUAGE = Util.getProperty(file, "language", charset);
		WINDOW_DOWNLOADING = Util.getProperty(file, "downloading", charset);
		WINDOW_USERNAME_FIELD_EMPTY = Util.getProperty(file, "username_field_empty", charset);

		LANG = Util.getProperty(file, "lang_title", charset);
		USERNAME = Util.getProperty(file, "username", charset);

		ERR_WARNING = Util.getProperty(file, "warning", charset);
		ERR_NO_CONNECTION = Util.getProperty(file, "no_connection", charset);
		ERR_DL_FAIL = Util.getProperty(file, "download_fail", charset);

		OPTIONS_UPDATE_HEADER = Util.getProperty(file, "update_check", charset);
		OPTIONS_UPDATE_NF = Util.getProperty(file, "update_not_found", charset);
		OPTIONS_PROXY = Util.getProperty(file, "use_betacraft", charset);
		OPTIONS_RPC = Util.getProperty(file, "discord_rpc", charset);
		OPTIONS_LAUNCH_ARGS = Util.getProperty(file, "launch_arguments", charset) + ":";
		OPTIONS_KEEP_OPEN = Util.getProperty(file, "keep_launcher_open", charset);
		OPTIONS_WIDTH = Util.getProperty(file, "width", charset);
		OPTIONS_OK = Util.getProperty(file, "ok", charset);
		OPTIONS_HEIGHT = Util.getProperty(file, "height", charset);
		OPTIONS_UPDATE = Util.getProperty(file, "check_update_button", charset);
		OPTIONS_TITLE = Util.getProperty(file, "options_title", charset);

		SORT_FROM_OLDEST = Util.getProperty(file, "sort_oldest", charset);
		SORT_FROM_NEWEST = Util.getProperty(file, "sort_newest", charset);
		VERSION_LIST_TITLE = Util.getProperty(file, "version_title", charset);

		ADDON_LIST_TITLE = Util.getProperty(file, "addon_list_title", charset);
		ADDON_NO_DESC = Util.getProperty(file, "addon_no_desc", charset);
		ADDON_SHOW_INFO = Util.getProperty(file, "addon_show_info", charset);

		LOGIN_TITLE = Util.getProperty(file, "login_title", charset);
		LOGIN_BUTTON = Util.getProperty(file, "log_in_button", charset);
		LOGOUT_BUTTON = Util.getProperty(file, "log_out_button", charset);
		LOGIN_EMAIL_NICKNAME = Util.getProperty(file, "login_email_nickname", charset);
		LOGIN_PASSWORD = Util.getProperty(file, "login_password", charset);
		LOGIN_REMEMBER_PASSWORD = Util.getProperty(file, "login_remember_password", charset);

		LOGIN_FAILED = Util.getProperty(file, "login_failed", charset);
		LOGIN_FAILED_METHOD = Util.getProperty(file, "login_failed_method", charset);
		LOGIN_FAILED_INVALID_CREDENTIALS = Util.getProperty(file, "login_failed_invalid_credentials", charset);

		INSTANCE_GAME_DIRECTORY = Util.getProperty(file, "instance_game_directory", charset);
		INSTANCE_GAME_DIRECTORY_TITLE = Util.getProperty(file, "instance_game_directory_title", charset);
		INSTANCE_REMOVE_QUESTION = Util.getProperty(file, "instance_remove_question", charset);
		INSTANCE_REMOVE_TITLE = Util.getProperty(file, "instance_remove_title", charset);
		INSTANCE_CHANGE_ICON_NAME = Util.getProperty(file, "instance_change_icon_name", charset);
		INSTANCE_CHANGE_ICON_TITLE = Util.getProperty(file, "instance_change_icon_title", charset);
		INSTANCE_CHANGE_ICON_UNSUPPORTED = Util.getProperty(file, "instance_change_icon_unsupported", charset);
		INSTANCE_CHANGE_ICON_UNSUPPORTED_TITLE = Util.getProperty(file, "instance_change_icon_unsupported_title", charset);
		INSTANCE_CHANGE_ICON_FAILED = Util.getProperty(file, "instance_change_icon_failed", charset);
		INSTANCE_CHANGE_ICON_FAILED_TITLE = Util.getProperty(file, "instance_change_icon_failed_title", charset);
		INSTANCE_NAME = Util.getProperty(file, "instance_name", charset);
		INSTANCE_SELECT_ADDONS = Util.getProperty(file, "instance_select_addons", charset);
		INSTANCE_MODS_REPOSITORY = Util.getProperty(file, "instance_mods_repository", charset);

		SELECT_INSTANCE_TITLE = Util.getProperty(file, "select_instance_title", charset);
		SELECT_INSTANCE_NEW = Util.getProperty(file, "select_instance_new", charset);

		UPDATE_FOUND = Util.getProperty(file, "new_version_found", charset);

		WRAP_USER = Util.getProperty(file, "nick", charset);
		WRAP_VERSION = Util.getProperty(file, "version", charset);
		WRAP_SERVER = Util.getProperty(file, "server", charset);
		WRAP_SERVER_TITLE = Util.getProperty(file, "server_title", charset);
		WRAP_CLASSIC_RESIZE = Util.getProperty(file, "classic_resize", charset);

		TAB_CHANGELOG =  Util.getProperty(file, "tab_changelog", charset);
		TAB_INSTANCES =  Util.getProperty(file, "tab_instances", charset);
		TAB_SERVERS =  Util.getProperty(file, "tab_servers", charset);

		VERSION_CUSTOM = Util.getProperty(file, "version_custom", charset);

		BROWSER_TITLE = Util.getProperty(file, "browser_title", charset);

		TAB_SRV_LOADING = Util.getProperty(file, "srv_loading", charset);
		TAB_SRV_FAILED = Util.getProperty(file, "srv_failed", charset);
		TAB_CL_LOADING = Util.getProperty(file, "cl_loading", charset);
		TAB_CL_FAILED = Util.getProperty(file, "cl_failed", charset);

		FORCE_UPDATE = Util.getProperty(file, "force_update", charset);

		if (force) {
			Window.mainWindow.update();
			if (Window.addonsList != null) Window.addonsList.update();
			if (Window.modsRepo != null) Window.modsRepo.update();
			if (Window.lang != null) Window.lang.update();
			if (Window.instanceList != null) Window.instanceList.update();
			if (Window.instanceSettings != null) Window.instanceSettings.update();
			if (Window.versionsList != null) Window.versionsList.update();
			if (Window.loginPanel != null) Window.loginPanel.update();
		}
	}

	static String UPDATE_FOUND = "There is a new version of the launcher available (%s). Would you like to update?";

	static String WINDOW_PLAY = "Play";
	static String WINDOW_SELECT_VERSION = "Select version";
	static String WINDOW_LANGUAGE = "Language";
	static String WINDOW_OPTIONS = "Edit instance";
	static String WINDOW_CREDITS = "BetaCraft Launcher made by Kazu & Moresteck";
	static String WINDOW_TITLE = "BetaCraft Launcher JE v" + Launcher.VERSION;
	static String WINDOW_DOWNLOADING = "Downloading ...";
	static String WINDOW_USERNAME_FIELD_EMPTY = "The username field is empty!";

	static String LANG = "Select language";
	static String USERNAME = "Username";

	static String OPTIONS_PROXY = "Use skin & sound proxy";
	static String OPTIONS_UPDATE_HEADER = "Update check";
	static String OPTIONS_UPDATE_NF = "Couldn't find any newer version of the launcher.";
	static String OPTIONS_KEEP_OPEN = "Keep the launcher open";
	static String OPTIONS_RPC = "Discord RPC";
	static String OPTIONS_LAUNCH_ARGS = "Launch arguments:";
	static String OPTIONS_OK = "OK";
	static String OPTIONS_UPDATE = "Check for update";
	static String OPTIONS_WIDTH = "width:";
	static String OPTIONS_HEIGHT = "height:";
	static String OPTIONS_TITLE = "Options";

	static String SORT_FROM_OLDEST = "Sort: from oldest";
	static String SORT_FROM_NEWEST = "Sort: from newest";
	static String VERSION_LIST_TITLE = "Versions list";

	static String ADDON_LIST_TITLE = "Addons list";
	static String ADDON_NO_DESC = "No description.";
	static String ADDON_SHOW_INFO = "Show info";

	static String LOGIN_TITLE = "Log in";
	static String LOGIN_BUTTON = "Log in";
	static String LOGOUT_BUTTON = "Log out";
	static String LOGIN_EMAIL_NICKNAME = "E-mail:";
	static String LOGIN_PASSWORD = "Password:";
	static String LOGIN_REMEMBER_PASSWORD = "Remember password";

	static String LOGIN_FAILED = "Failed to log in";
	static String LOGIN_FAILED_METHOD = "Login method changed on Mojang's side. Please report the bug or update the launcher.";
	static String LOGIN_FAILED_INVALID_CREDENTIALS = "Invalid e-mail or password.";

	static String INSTANCE_GAME_DIRECTORY = "Game directory";
	static String INSTANCE_GAME_DIRECTORY_TITLE = "Choose a directory for the instance";
	static String INSTANCE_REMOVE_QUESTION = "Are you sure you want to remove this instance?";
	static String INSTANCE_REMOVE_TITLE = "Remove instance";
	static String INSTANCE_CHANGE_ICON_NAME = "Change icon";
	static String INSTANCE_CHANGE_ICON_TITLE = "Choose a new icon for the instance";
	static String INSTANCE_CHANGE_ICON_UNSUPPORTED = "Your icon file format is not supported. Currently supported formats: %s";
	static String INSTANCE_CHANGE_ICON_UNSUPPORTED_TITLE = "Unsupported image format";
	static String INSTANCE_CHANGE_ICON_FAILED = "Something went wrong: %s";
	static String INSTANCE_CHANGE_ICON_FAILED_TITLE = "Failed to save icon";
	static String INSTANCE_NAME = "Name:";
	static String INSTANCE_SELECT_ADDONS = "Select addons";
	static String INSTANCE_MODS_REPOSITORY = "Mods repository";

	static String SELECT_INSTANCE_TITLE = "Select instance";
	static String SELECT_INSTANCE_NEW = "New instance";

	static String TAB_CHANGELOG = "Changelog";
	static String TAB_INSTANCES = "Instances";
	static String TAB_SERVERS = "Classic servers";

	static String VERSION_CUSTOM = " [Custom]";

	static String BROWSER_TITLE = "Webpage viewer";

	static String TAB_SRV_LOADING = "Loading servers list...";
	static String TAB_SRV_FAILED = "Failed to list classic servers!";
	static String TAB_CL_LOADING = "Loading update news...";
	static String TAB_CL_FAILED = "Failed to load update news!";

	static String FORCE_UPDATE = "Force update";

	public static String WRAP_USER = "User: %s";
	public static String WRAP_VERSION = "Version: %s";
	public static String WRAP_SERVER = "Server IP (leave blank if you don't want to play online):";
	public static String WRAP_SERVER_TITLE = "Server IP";
	public static String WRAP_CLASSIC_RESIZE = "<html><font size=5>Resize the window to the size you want to play on.<br />Click anywhere inside this window to start the game.</font></html>";

	static String ERR_WARNING = "Warning";
	static String ERR_NO_CONNECTION = "No stable internet connection.";
	static String ERR_DL_FAIL = "Download failed.";
}
