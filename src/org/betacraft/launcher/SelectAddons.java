package org.betacraft.launcher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.betacraft.launcher.VersionSorter.Order;

public class SelectAddons extends JFrame implements ActionListener {

	static JList list;
	static DefaultListModel listModel;
	static JScrollPane listScroller;
	static JButton more_button;
	static JButton OK;
	static Order order = Order.FROM_OLDEST;
	static JPanel panel;
	static GridBagConstraints constr;

	public SelectAddons() {
		Logger.a("Addons list window has been opened.");
		this.setIconImage(Window.img);
		this.setMinimumSize(new Dimension(282, 386));
		this.setPreferredSize(new Dimension(282, 386));
		this.setTitle(Lang.ADDON_LIST_TITLE);
		this.setResizable(true);
		this.setLayout(new BorderLayout());

		panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		constr = new GridBagConstraints();

		constr.fill = GridBagConstraints.BOTH;
		constr.insets = new Insets(5, 5, 0, 5);
		constr.gridwidth = GridBagConstraints.RELATIVE;
		constr.weightx = 1.0;

		more_button = new JButton(Lang.ADDON_SHOW_INFO);
		more_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// todo
			}
		});
		more_button.setEnabled(false);
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
		updateList();
		this.add(panel, BorderLayout.CENTER);
		this.pack();
		this.setLocationRelativeTo(Window.mainWindow);
		this.setVisible(true);
	}

	protected static ArrayList<JCheckBox> checkboxes = new ArrayList<JCheckBox>();

	protected void updateList() {
		checkboxes.clear();
		JPanel listpanel = new JPanel();
		listpanel.setLayout(new GridBagLayout());
		listpanel.setMaximumSize(new Dimension(282, 300));

		GridBagConstraints constr1 = new GridBagConstraints();

		constr1.gridx = 0;
		constr1.gridy = 0;
		constr1.fill = GridBagConstraints.HORIZONTAL;
		constr1.weightx = 1.0;
		//constr1.insets = new Insets(10, 10, 0, 10);
		for (Addon item : Addon.addons) {
			JCheckBox checkbox = new JCheckBox(item.name);
			for (String addon : Launcher.currentInstance.addons) {
				if (addon.equals(item.name)) checkbox.setSelected(true);
			}

			listpanel.add(checkbox, constr1);
			checkboxes.add(checkbox);
			constr1.gridy++;
		}
		//listpanel.validate();

		constr.weighty = 1.0;
		constr.gridheight = GridBagConstraints.RELATIVE;
		constr.gridy = 1;

		if (listScroller != null) panel.remove(listScroller);

		listScroller = new JScrollPane(listpanel);
		listScroller.setWheelScrollingEnabled(true);
		panel.add(listScroller, constr);
	}

	public void saveAddons() {
		ArrayList<String> elist = new ArrayList<String>();
		for (int i = 0; i < checkboxes.size(); i++) {
			JCheckBox checkbox = checkboxes.get(i);
			if (checkbox.isSelected()) {
				elist.add(checkbox.getText());
			}
		}
		Launcher.currentInstance.setAddons(elist);
		setVisible(false);
		Launcher.currentInstance.saveInstance();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == OK) {
			saveAddons();
			Window.addonsList = null;
		}
	}
}
