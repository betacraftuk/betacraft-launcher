package org.betacraft.launcher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.betacraft.launcher.VersionSorter.Order;

public class SelectVersion extends JFrame implements ActionListener, LanguageElement {

	static JList list;
	static DefaultListModel listModel;
	public static JScrollPane listScroller;
	static JButton sort_button;
	static JButton OK;
	static Order order = Order.FROM_OLDEST;
	static JPanel panel;
	static GridBagConstraints constr;

	public SelectVersion() {
		Logger.a("Version list window opened.");
		this.setIconImage(Window.img);
		this.setMinimumSize(new Dimension(282, 386));
		this.setTitle(Lang.VERSION_LIST_TITLE);
		this.setResizable(true);

		panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		constr = new GridBagConstraints();
		constr.fill = GridBagConstraints.BOTH;
		constr.insets = new Insets(5, 5, 0, 5);
		constr.gridwidth = GridBagConstraints.RELATIVE;
		constr.weightx = 1.0;

		String name = (SelectVersion.order == Order.FROM_OLDEST) ? Lang.SORT_FROM_OLDEST : Lang.SORT_FROM_NEWEST;
		sort_button = new JButton(name);
		sort_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (SelectVersion.order == Order.FROM_OLDEST) {
					SelectVersion.order = Order.FROM_NEWEST;
					sort_button.setText(Lang.SORT_FROM_NEWEST);
				} else {
					SelectVersion.order = Order.FROM_OLDEST;
					sort_button.setText(Lang.SORT_FROM_OLDEST);
				}
				updateList();
			}
		});
		panel.add(sort_button, constr);
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
		this.setLocationRelativeTo(Window.mainWindow);
		this.setVisible(true);
	}

	public void update() {
		this.setTitle(Lang.VERSION_LIST_TITLE);
		String name = (SelectVersion.order == Order.FROM_OLDEST) ? Lang.SORT_FROM_OLDEST : Lang.SORT_FROM_NEWEST;
		sort_button.setText(name);
		OK.setText(Lang.OPTIONS_OK);
		this.pack();
	}

	protected void updateList() {
		int i = 0;
		int index = 0;
		listModel = null;
		listModel = new DefaultListModel();
		for (Release item : VersionSorter.sort(order)) {
			listModel.addElement(item);
			if (Launcher.currentInstance.version.equalsIgnoreCase(item.getName())) {
				index = i;
			}
			i++;
		}

		constr.weighty = 1.0;
		constr.gridheight = GridBagConstraints.RELATIVE;
		constr.gridy = 1;

		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(10);
		list.setSelectedIndex(index);

		if (listScroller != null) panel.remove(listScroller);

		listScroller = new JScrollPane(list);
		listScroller.setWheelScrollingEnabled(true);
		listScroller.requestFocus();
		panel.add(listScroller, constr);
	}

	public void saveVersion() {
		Release ver = (Release) list.getSelectedValue();
		if (ver != null) {
			Launcher.currentInstance.version = ver.getName();
			Launcher.setInstance(Launcher.currentInstance);
			Launcher.currentInstance.saveInstance();
		}
		dispose();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == OK) {
			saveVersion();
			Window.versionsList = null;
		}
	}
}
