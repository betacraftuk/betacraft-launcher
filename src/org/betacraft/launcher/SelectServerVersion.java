package org.betacraft.launcher;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class SelectServerVersion extends JFrame {
	static JList list;
	static DefaultListModel listModel;
	static JScrollPane listScroller;

	static JButton OKButton;
	static JPanel panel;
	static GridBagConstraints constr;

	public SelectServerVersion(ArrayList<Release> thelist, final String mppass, final String address) {
		this.setIconImage(Window.img);
		this.setMinimumSize(new Dimension(282, 169));

		this.setTitle(Lang.WINDOW_SELECT_VERSION);
		this.setResizable(true);

		panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		constr = new GridBagConstraints();

		constr.fill = GridBagConstraints.BOTH;
		constr.insets = new Insets(5, 5, 0, 5);
		constr.gridwidth = GridBagConstraints.RELATIVE;
		constr.weightx = 1.0;

		makeList(thelist);

		constr.gridy++;
		constr.weighty = GridBagConstraints.RELATIVE;
		constr.gridheight = 1;
		constr.insets = new Insets(5, 5, 5, 5);
		OKButton = new JButton(Lang.OPTIONS_OK);

		OKButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				Release ver = (Release) list.getSelectedValue();
				Launcher.currentInstance.version = ver.getName();
				Launcher.setInstance(Launcher.currentInstance);
				Launcher.currentInstance.saveInstance();
				Launcher.saveLastLogin();
				Window.mainWindow.playButton.setEnabled(false);
				new Thread() {
					public void run() {
						Window.mainWindow.setStatus(Window.mainWindow.playButton, Lang.WINDOW_DOWNLOADING);
						Launcher.initStartup();

						// Update the button state
						Window.mainWindow.setStatus(Window.mainWindow.playButton, Lang.WINDOW_PLAY);
						Window.mainWindow.playButton.setEnabled(true);
						new Launcher().launchGame(Launcher.currentInstance, address, mppass, null);
					}
				}.start();
			}
		});
		panel.add(OKButton, constr);
		this.add(panel);
		this.pack();
		this.setLocationRelativeTo(Window.mainWindow);
		this.setVisible(true);
	}

	public void makeList(ArrayList<Release> thelist) {
		int index = 0;
		listModel = new DefaultListModel();
		for (int i = 0; i < thelist.size(); i++) {
			Release item = thelist.get(i);
			listModel.addElement(item);
			if (i == thelist.size()-1) {
				index = i;
			}
		}

		constr.weighty = 1.0;
		constr.gridheight = GridBagConstraints.RELATIVE;
		constr.gridy = 1;

		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(5);
		list.setSelectedIndex(index);

		if (listScroller != null) panel.remove(listScroller);

		listScroller = new JScrollPane(list);
		listScroller.setWheelScrollingEnabled(true);
		panel.add(listScroller, constr);
	}
}