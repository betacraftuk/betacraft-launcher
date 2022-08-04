package org.betacraft.launcher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.betacraft.launcher.Release.VersionRepository;

import uk.betacraft.auth.CustomResponse;
import uk.betacraft.auth.Request;
import uk.betacraft.auth.RequestUtil;
import uk.betacraft.json.lib.ModObject;

public class ModsRepository extends JFrame implements ActionListener, LanguageElement {

	public static ArrayList<ModObject> mods = new ArrayList<ModObject>();

	public static void loadMods() {
		try {
			String modlistjson = (new Request() {

				@Override
				public CustomResponse perform() {
					this.REQUEST_URL = "http://files.betacraft.uk/launcher/assets/mods/1.09_16/list.json";
					return new CustomResponse(RequestUtil.performGETRequest(this));
				}

			}).perform().response;

			ModObject[] ml = Util.gson.fromJson(modlistjson, ModObject[].class);
			
			for (ModObject obj : ml) {
				mods.add(obj);
			}
		} catch (Exception ex) {
			Logger.a("A critical error occurred while loading mod list!");
			ex.printStackTrace();
			Logger.printException(ex);
		}
	}

	public static ModObject getMod(String name) {
		for (ModObject obj : mods) {
			if (obj.toString().equalsIgnoreCase(name)) {
				return obj;
			}
		}
		return null;
	}

	static JList list;
	static DefaultListModel listModel;
	static JScrollPane listScroller;
	static JButton more_button;
	static JButton LoadButton;
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
		LoadButton = new JButton(Lang.LOAD);
		LoadButton.addActionListener(this);
		panel.add(LoadButton, constr);

		this.add(panel, BorderLayout.SOUTH);

		panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		updateList();

		this.getContentPane().add(panel, BorderLayout.CENTER);
		this.pack();
		setLocationRelativeTo(Window.mainWindow);
		setVisible(true);
	}

	public void update() {
		this.setTitle(Lang.INSTANCE_MODS_REPOSITORY);
		LoadButton.setText(Lang.OPTIONS_OK);
		this.pack();
	}

	protected void updateList() {
		listModel = null;
		listModel = new DefaultListModel();
		for (ModObject obj : mods) {
			listModel.addElement(obj);
		}

		constr.weighty = 1.0;
		constr.gridheight = GridBagConstraints.RELATIVE;
		constr.gridy = 1;

		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(10);

		if (listScroller != null) panel.remove(listScroller);

		listScroller = new JScrollPane(list);
		listScroller.setWheelScrollingEnabled(true);
		panel.add(listScroller, constr);
	}

	public void saveVersions() {
		if (list.getSelectedValues().length != 0) {
			for (Object o : list.getSelectedValues()) {
				ModObject obj = (ModObject) o;
				new ReleaseJson(obj.name, obj.infoFileURL).downloadJson();
			}
			try {
				Release.loadVersions(VersionRepository.BETACRAFT);
			} catch (Exception ex) {
				ex.printStackTrace();
				Logger.printException(ex);
			}
			Launcher.currentInstance.version = ((ModObject) list.getSelectedValues()[0]).name;
			Launcher.setInstance(Launcher.currentInstance);
			Launcher.currentInstance.saveInstance();
		}
		setVisible(false);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == LoadButton) {
			saveVersions();
			Window.modsRepo = null;
		} else if (e.getSource() == more_button) {
			for (Object l : list.getSelectedValues()) {
				ModObject mod = (ModObject) l;

				Util.openURL(mod.website);
			}
		}
	}
}
