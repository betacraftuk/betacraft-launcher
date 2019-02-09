package org.betacraft.launcher;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.betacraft.launcher.VersionSorter.Order;

public class Wersja extends JFrame {

	ImageIcon image = new ImageIcon();
	static JList list;
	static DefaultListModel listModel;
	static JScrollPane listScroller;
	JButton OK;
	static Order order = Order.FROM_OLDEST;

	public Wersja() {
		Logger.a("Otwarto okno wyboru wersji.");
		setSize(282, 386);
		setLayout(null);
		setTitle("Version list");
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);

		final JButton order = new JButton((Wersja.order == Order.FROM_OLDEST) ? "Sort: from oldest" : "Sort: from newest");
		order.setBounds(10, 0, 262, 30);
		order.setBackground(Color.LIGHT_GRAY);
		add(order);
		order.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Wersja.order == Order.FROM_OLDEST) {
					Wersja.order = Order.FROM_NEWEST;
					order.setText("Sort: from newest");
				} else {
					Wersja.order = Order.FROM_OLDEST;
					order.setText("Sort: from oldest");
				}
				updateList();
			}
		});
		updateList();

		OK = new JButton("OK");
		OK.setBounds(10, 320, 60, 20);
		OK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Release ver = (Release) list.getSelectedValue();
				Launcher.chosen_version = ver.getName();
				Launcher.setProperty(Launcher.SETTINGS, "version", ver.getName());
				setVisible(false);
			}
		});
		add(OK);

		OK.setBackground(Color.LIGHT_GRAY);
	}

	protected void updateList() {
		int i = 0;
		int index = 0;
		listModel = null;
		listModel = new DefaultListModel();
		for (Release item : VersionSorter.sort(order)) {
			listModel.addElement(item);
			if (Launcher.chosen_version.equalsIgnoreCase(item.getName())) {
				index = i;
			}
			i++;
		}

		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setBounds(10, 30, 262, 290);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(3);
		list.setSelectedIndex(index);

		if (listScroller != null) this.remove(listScroller);

		listScroller = new JScrollPane(list);
		listScroller.setBounds(10, 30, 262, 290);
		listScroller.setWheelScrollingEnabled(true);
		getContentPane().add(listScroller);
	}
}
