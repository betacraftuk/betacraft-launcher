package org.betacraft.launcher;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.betacraft.launcher.VersionSorter.Order;

public class Wersja extends JFrame implements ActionListener {

	static JList list;
	static DefaultListModel listModel;
	static JScrollPane listScroller;
	static JButton sort_button;
	static JButton OK;
	static JButton more;
	static JButton open_wiki;
	static boolean showinfo = false;
	static Order order = Order.FROM_OLDEST;
	static JLabel version_name = new JLabel("put_version_name_here_lol");
	static JLabel date = new JLabel("u_really_shouldn't_see_this");
	static JTextArea date2 = new JTextArea("");
	static JLabel information = new JLabel("please_report_it_@Moresteck#1688");
	static JTextArea information2 = new JTextArea("");

	static String also_known = "Also known as \"%s\"";
	static String release = "Release date:";
	static String info = "Information:";
	static String show_more = "Show more";
	static String mc_wiki = "Minecraft Wiki article"; 
	static String internal_err = "An internal error has occured.";

	public Wersja() {
		Logger.a("Otwarto okno wyboru wersji.");
		this.setIconImage(Window.img);
		setSize(282, 386);
		setLayout(null);
		setTitle("Version list");
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);

		String name = (Wersja.order == Order.FROM_OLDEST) ? Lang.get("sort_oldest") : Lang.get("sort_newest");
		if (name.equals("")) {
			name = Wersja.order == Order.FROM_OLDEST ? "Sort: from oldest" : "Sort: from newest";
		}
		sort_button = new JButton(name);
		sort_button.setBounds(10, 0, 262, 30);
		sort_button.setBackground(Color.LIGHT_GRAY);
		add(sort_button);
		sort_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Wersja.order == Order.FROM_OLDEST) {
					Wersja.order = Order.FROM_NEWEST;
					sort_button.setText(Lang.get("sort_newest"));
				} else {
					Wersja.order = Order.FROM_OLDEST;
					sort_button.setText(Lang.get("sort_oldest"));
				}
				updateList();
			}
		});
		updateList();
		Release release = ((Release)list.getSelectedValue());

		version_name = new JLabel(release.getName());
		version_name.setFont(new Font("Dialog", 22, 22));
		version_name.setBounds(300, 10, 200, 30);
		add(version_name);

		date = new JLabel(Wersja.release);
		StringBuilder datee = new StringBuilder();
		if (!release.getDate().equals("")) {
			datee.append(release.getDate());
		}
		if (!release.getTime().equals("")) {
			datee.append(", " + release.getTime() + " CEST");
		}
		date2 = new JTextArea(datee.toString());
		date2.setFont(Font.getFont("BOLD"));
		date.setBounds(300, 50, 230, 20);
		add(date);
		date2.setBounds(400, 50, 230, 20);
		date2.setEditable(false);
		date2.setBackground(new Color(238, 238, 238));
		add(date2);

		information = new JLabel(info);
		information.setBounds(300, 65, 230, 20);
		add(information);

		information2 = new JTextArea(release.getDescription());
		information2.setBounds(310, 82, 230, 200);
		information2.setFont(Font.getFont("BOLD"));
		information2.setEditable(false);
		information2.setLineWrap(true);
		information2.setWrapStyleWord(true);
		information2.setBackground(new Color(238, 238, 238));
		add(information2);
		if (release.hasSpecialName()) {
			information2.setText((release.hasSpecialName() ? String.format(also_known, release.getSpecialName()) : "") + release.getDescription());
		}

		OK = new JButton("OK");
		OK.setBounds(10, 320, 60, 20);
		OK.addActionListener(this);
		add(OK);

		OK.setBackground(Color.LIGHT_GRAY);

		more = new JButton(show_more + " >");
		more.setBounds(150, 320, 122, 20);
		more.addActionListener(this);
		add(more);

		more.setBackground(Color.LIGHT_GRAY);

		open_wiki = new JButton(mc_wiki);
		open_wiki.setBounds(356, 320, 185, 20);
		open_wiki.addActionListener(this);
		add(open_wiki);

		if (release.getWikiLink() == null) {
			open_wiki.setEnabled(false);
		}

		open_wiki.setBackground(Color.LIGHT_GRAY);

		this.getRootPane().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				System.out.println(arg0.getKeyCode());
			}
		});
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			public boolean dispatchKeyEvent(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					saveVersion();
				}
				return true;
			}
		});
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
		list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				Release release = (Release) list.getSelectedValue();
				version_name.setText(release.getName());
				StringBuilder datee = new StringBuilder();
				if (!release.getDate().equals("")) {
					datee.append(release.getDate());
				}
				if (!release.getTime().equals("")) {
					datee.append(", " + release.getTime() + " CEST");
				}
				date2.setText(datee.toString());
				information2.setText((release.hasSpecialName() ? String.format(also_known, release.getSpecialName()) : "") + release.getDescription());
				if (release.getWikiLink() == null) {
					open_wiki.setEnabled(false);
				} else {
					open_wiki.setEnabled(true);
				}
			}
		});
	}

	public void saveVersion() {
		Release ver = (Release) list.getSelectedValue();
		Launcher.chosen_version = ver.getName();
		Window.currentver.setText(ver.getName());
		Launcher.setProperty(Launcher.SETTINGS, "version", ver.getName());
		setVisible(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == OK) {
			saveVersion();
		}
		if (e.getSource() == more) {
			if (!showinfo) {
				this.setSize(564, 386);
				showinfo = true;
				more.setText(show_more + " <");
			} else {
				showinfo = false;
				more.setText(show_more + " >");
				this.setSize(282, 386);
			}
		}
		if (e.getSource() == open_wiki) {
			if (!open_wiki.isEnabled()) return;
			Release ver = (Release) list.getSelectedValue();
			try {
				Desktop.getDesktop().browse(ver.getWikiLink().toURI());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(null, internal_err, "Error", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}
}
