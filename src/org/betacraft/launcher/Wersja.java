package org.betacraft.launcher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Wersja extends JFrame {

	ImageIcon image = new ImageIcon();
	JList list;
	DefaultListModel listModel;
	JButton OK;
	int before = 0;

	public Wersja() {
		Logger.a("Otwarto okno wyboru wersji.");
		setSize(282, 386);
		setLayout(null);
		setTitle("Wybór wersji");
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);

		int i = 0;
		int multiplier = 1;
		int index = 0;
		String lastItemVer = "c0.";
        listModel = new DefaultListModel();
        listModel.addElement("CLASSIC");
        for (Release item : Release.versions) {
        	if (item.getName().startsWith("in-") && !lastItemVer.startsWith(item.getName().substring(0, 3))) {
        		lastItemVer = "in-";
        		listModel.addElement(" ");
        		listModel.addElement("INDEV");
        		multiplier += 2;
        	}
        	if (item.getName().startsWith("inf") && !lastItemVer.startsWith(item.getName().substring(0, 3))) {
        		lastItemVer = "inf";
        		listModel.addElement(" ");
        		listModel.addElement("INFDEV");
        		multiplier += 2;
        	}
        	if (item.getName().startsWith("a1.") && !lastItemVer.startsWith(item.getName().substring(0, 3))) {
        		lastItemVer = "a1.";
        		listModel.addElement(" ");
        		listModel.addElement("ALPHA");
        		multiplier += 2;
        	}
        	if (item.getName().startsWith("b1.") && !lastItemVer.startsWith(item.getName().substring(0, 3))) {
        		lastItemVer = "b1.";
        		listModel.addElement(" ");
        		listModel.addElement("BETA");
        		multiplier += 2;
        	}
        	listModel.addElement(item);
        	if (Window.chosen_version.equalsIgnoreCase(item.getName())) {
        		index = i + multiplier;
        	}
        	i++;
        }

        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBounds(30, 10, 180, 300);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(3);
        list.setSelectedIndex(index);
        
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setBounds(10, 10, 262, 300);
        listScroller.setWheelScrollingEnabled(true);
        getContentPane().add(listScroller);

        OK = new JButton("OK");
        OK.setBounds(10, 320, 60, 20);
        add(OK);
        before = list.getSelectedIndex();
        ListSelectionListener listSelectionListener = new ListSelectionListener() {
        	public void valueChanged(ListSelectionEvent listSelectionEvent) {
        		JList list = (JList) listSelectionEvent.getSource();
        		int index = list.getSelectedIndex();
        		Object obj = list.getSelectedValue();
        		if (obj instanceof Release) {
        			before = index;
        			return;
        		}
        		String name = (String) obj;
        		if (name.equalsIgnoreCase("Classic") || name.equalsIgnoreCase("Indev") || name.equalsIgnoreCase("Infdev") || name.equalsIgnoreCase("Alpha") ||
        				name.equalsIgnoreCase("Beta")) {
        			list.setSelectedIndex(before);
        			return;
        		}
        	}
        };
        list.addListSelectionListener(listSelectionListener);
        OK.addActionListener(new ActionListener() {
        	@Override
			public void actionPerformed(ActionEvent e) {
        		Release ver = (Release) list.getSelectedValue();
				Window.chosen_version = ver.getName();
				Launcher.write(Launcher.getBetacraft() + "launcher.settings", new String[] {"version:" + ver.getName()}, false);
				setVisible(false);
			}
        });
	}
}
