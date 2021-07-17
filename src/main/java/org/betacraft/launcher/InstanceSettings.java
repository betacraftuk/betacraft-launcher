package org.betacraft.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class InstanceSettings extends JFrame implements LanguageElement {

	public JCheckBox proxyCheck;
	public JCheckBox keepOpenCheck;
	public JCheckBox RPCCheck;
	public JCheckBox forceUpdate = null;

	public JLabel parametersText;
	public JTextField parameters;

	public JLabel dimensions1Text;
	public JLabel dimensions2Text;
	public JTextField dimensions1;
	public JTextField dimensions2;

	public JButton OKButton;

	public JButton dirChooser;
	public JTextField instanceName;
	public JLabel instanceIcon;
	public JButton chooseIcon;
	public JLabel removelabel;
	public JLabel instanceNameText;
	public JButton addons;
	public JButton modrepo;
	static Image img = null;
	static Image image = null;

	protected static class OptionsPanel extends JPanel {
		public OptionsPanel() {
			try {
				image = ImageIO.read(Launcher.class.getResource("/icons/stone.png")).getScaledInstance(32, 32, 16);
			} catch (IOException e2) {
				e2.printStackTrace();
				Logger.printException(e2);
				return;
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			//super.paintComponent(g);
			final int w = this.getWidth() / 2;
			final int h = this.getHeight() / 2;
			if (w <= 0 || h <= 0) return;
			img = this.createImage(w, h);
			final Graphics graphics2 = img.getGraphics();
			for (int i = 0; i <= w / 32; ++i) {
				for (int j = 0; j <= h / 32; ++j) {
					graphics2.drawImage(image, i * 32, j * 32, null);
				}
			}
			graphics2.dispose();
			g.drawImage(img, 0, 0, w * 2, h * 2, null);
		}
	}

	public InstanceSettings() {
		Logger.a("Options window has been opened.");

		this.setIconImage(Window.img);
		setTitle(Lang.OPTIONS_TITLE);
		setResizable(true);



		JPanel panel = new OptionsPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints constr = new GridBagConstraints();

		constr.gridx = 0;
		constr.gridy = 0;
		constr.fill = GridBagConstraints.BOTH;
		constr.gridwidth = 4;
		constr.weightx = 1.0;
		constr.insets = new Insets(10, 10, 0, 10);

		proxyCheck = new JCheckBox(Lang.OPTIONS_PROXY);
		proxyCheck.setForeground(Color.LIGHT_GRAY);
		proxyCheck.setOpaque(false);
		proxyCheck.setSelected(Launcher.currentInstance.proxy);
		panel.add(proxyCheck, constr);

		constr.insets = new Insets(2, 10, 0, 10);
		constr.gridy++;
		keepOpenCheck = new JCheckBox(Lang.OPTIONS_KEEP_OPEN);
		keepOpenCheck.setForeground(Color.LIGHT_GRAY);
		keepOpenCheck.setOpaque(false);
		keepOpenCheck.setSelected(Launcher.currentInstance.keepopen);
		panel.add(keepOpenCheck, constr);

		constr.gridy++;
		RPCCheck = new JCheckBox(Lang.OPTIONS_RPC);
		RPCCheck.setForeground(Color.LIGHT_GRAY);
		RPCCheck.setOpaque(false);
		RPCCheck.setSelected(Launcher.currentInstance.RPC);
		panel.add(RPCCheck, constr);

		constr.gridy++;
		parametersText = new JLabel(Lang.OPTIONS_LAUNCH_ARGS);
		parametersText.setForeground(Color.LIGHT_GRAY);
		panel.add(parametersText, constr);

		constr.gridy++;
		constr.insets = new Insets(2, 20, 0, 10);
		parameters = new JTextField(Launcher.currentInstance.launchArgs, 30);
		panel.add(parameters, constr);

		constr.gridy++;
		constr.insets = new Insets(2, 10, 10, 10);
		dirChooser = new JButton(Lang.INSTANCE_GAME_DIRECTORY);
		dirChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser dirChooser = new JFileChooser();
				dirChooser.setCurrentDirectory(new java.io.File(BC.path()));
				dirChooser.setDialogTitle(Lang.INSTANCE_GAME_DIRECTORY_TITLE);
				dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				dirChooser.setAcceptAllFileFilterUsed(false);
				if (dirChooser.showOpenDialog(Window.mainWindow) == JFileChooser.APPROVE_OPTION) { 
					System.out.println("getCurrentDirectory(): " 
							+  dirChooser.getCurrentDirectory());
					System.out.println("getSelectedFile() : " 
							+  dirChooser.getSelectedFile());
					File gameDir = null;
					if (!dirChooser.getSelectedFile().equals(dirChooser.getCurrentDirectory())) {
						if (!dirChooser.getSelectedFile().isDirectory()) {
							gameDir = dirChooser.getCurrentDirectory();
						} else {
							gameDir = dirChooser.getSelectedFile();
						}
					}
					Launcher.currentInstance.gameDir = gameDir.toPath().toString();
				}
			}
		});
		panel.add(dirChooser, constr);

		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.gridwidth = 1;
		constr.gridx = 0;
		constr.weightx = 0.0;

		constr.gridy++;
		dimensions1Text = new JLabel(Lang.OPTIONS_WIDTH);
		dimensions1Text.setForeground(Color.LIGHT_GRAY);
		panel.add(dimensions1Text, constr);

		constr.gridx = 1;
		dimensions1 = new JTextField(Integer.toString(Launcher.currentInstance.width), 4);
		panel.add(dimensions1, constr);

		constr.gridx = 0;
		constr.gridy++;
		dimensions2Text = new JLabel(Lang.OPTIONS_HEIGHT);
		dimensions2Text.setForeground(Color.LIGHT_GRAY);
		panel.add(dimensions2Text, constr);

		constr.gridx = 1;
		dimensions2 = new JTextField(Integer.toString(Launcher.currentInstance.height), 4);
		panel.add(dimensions2, constr);

		JPanel instanceSettings = new OptionsPanel();
		instanceSettings.setLayout(new GridBagLayout());
		JButton remove = new JButton("x");
		int height = (int) remove.getPreferredSize().getWidth();

		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(InstanceSettings.this, Lang.INSTANCE_REMOVE_QUESTION, Lang.INSTANCE_REMOVE_TITLE, JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					Launcher.currentInstance.removeInstance();
					setVisible(false);
					if (Instance.getInstances().size() > 0) {
						Launcher.setInstance(Instance.loadInstance(Instance.getInstances().get(0)));
					} else {
						Instance in = Instance.newInstance("(default instance)");
						in.saveInstance();
						Launcher.setInstance(in);
					}
				}
			}
		});
		removelabel = new JLabel(Lang.INSTANCE_REMOVE_TITLE);
		instanceNameText = new JLabel(Lang.INSTANCE_NAME);
		instanceIcon = new JLabel(new ImageIcon(Launcher.currentInstance.getIcon()));
		chooseIcon = new JButton(Lang.INSTANCE_CHANGE_ICON_NAME);
		chooseIcon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser dirChooser = new JFileChooser();
				dirChooser.setCurrentDirectory(new java.io.File(System.getProperty("user.home")));
				dirChooser.setDialogTitle(Lang.INSTANCE_CHANGE_ICON_TITLE);
				dirChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				dirChooser.setAcceptAllFileFilterUsed(false);
				if (dirChooser.showOpenDialog(Window.mainWindow) == JFileChooser.APPROVE_OPTION) { 
					File selected = dirChooser.getSelectedFile();
					try {
						Launcher.currentInstance.setIcon(selected);
						instanceIcon.setIcon(new ImageIcon(Launcher.currentInstance.getIcon()));
					} catch (Exception ex) {
						Launcher.currentInstance.setIcon(null);
						JOptionPane.showMessageDialog(Window.mainWindow, String.format(Lang.INSTANCE_CHANGE_ICON_FAILED, ex.getMessage()), Lang.INSTANCE_CHANGE_ICON_FAILED_TITLE, JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		addons = new JButton(Lang.INSTANCE_SELECT_ADDONS);
		addons.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Window.addonsList == null) new SelectAddons();
				else Window.addonsList.setVisible(true);
			}
		});
		modrepo = new JButton(Lang.INSTANCE_MODS_REPOSITORY);
		modrepo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Window.modsRepo == null) new ModsRepository();
				else Window.modsRepo.setVisible(true);
			}
		});

		instanceName = new JTextField(Launcher.currentInstance.name, 15);
		instanceNameText.setForeground(Color.LIGHT_GRAY);
		instanceNameText.setOpaque(false);
		removelabel.setForeground(Color.LIGHT_GRAY);
		removelabel.setOpaque(false);
		instanceName.setOpaque(true);
		GridBagConstraints constr1 = new GridBagConstraints();
		//constr1.fill = GridBagConstraints.NORTHEAST;
		constr1.fill = GridBagConstraints.HORIZONTAL;
		constr1.insets = new Insets(2, 2, 10, 10);
		constr1.gridy = 0;
		constr1.gridx = 0;
		instanceSettings.add(remove, constr1);
		constr1.gridx = 1;
		instanceSettings.add(removelabel, constr1);
		constr1.fill = GridBagConstraints.NORTHEAST;
		constr1.gridx = 0;
		constr1.gridy = 1;
		instanceSettings.add(instanceNameText, constr1);
		constr1.gridx = 1;
		instanceSettings.add(instanceName, constr1);
		constr1.insets = new Insets(10, 2, 2, 2);
		constr1.gridx = 0;
		constr1.gridy = 2;
		instanceSettings.add(instanceIcon, constr1);
		constr1.gridx = 1;
		constr1.gridy = 2;
		constr1.gridwidth = 3;
		constr1.ipadx = 50;
		instanceSettings.add(chooseIcon, constr1);
		constr1.gridx = 0;
		constr1.gridy = 3;
		constr1.weightx = 1.0;
		constr1.ipadx = 0;
		constr1.insets = new Insets(25, 2, 2, 2);
		constr1.fill = GridBagConstraints.HORIZONTAL;
		forceUpdate = new JCheckBox(Lang.FORCE_UPDATE);
		forceUpdate.setForeground(Color.LIGHT_GRAY);
		forceUpdate.setOpaque(false);
		forceUpdate.setSelected(Launcher.forceUpdate);
		instanceSettings.add(forceUpdate, constr1);
		constr1.gridy = 4;
		constr1.insets = new Insets(2, 2, 2, 2);
		//constr1.gridwidth = 2;
		instanceSettings.add(addons, constr1);
		constr1.ipady = 0;
		constr1.gridy = 5;
		instanceSettings.add(modrepo, constr1);

		JPanel okPanel = new OptionsPanel();
		okPanel.setLayout(new GridBagLayout());

		constr1.fill = GridBagConstraints.HORIZONTAL;
		constr1.gridx = 0;
		constr1.gridy = constr.gridy + 1;
		constr1.weighty = 1.0;
		constr1.insets = new Insets(2, 2, 2, 2);
		constr1.gridwidth = GridBagConstraints.RELATIVE;
		constr1.gridheight = 1;
		constr1.weightx = 1.0;
		OKButton = new JButton(Lang.OPTIONS_OK);
		OKButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveOptions();
				setVisible(false);
				Window.instanceSettings = null;
			}
		});
		okPanel.add(OKButton, constr1);
		okPanel.setBackground(Color.WHITE);

		this.getContentPane().add(panel, BorderLayout.CENTER);
		this.getContentPane().add(instanceSettings, BorderLayout.LINE_START);
		this.getContentPane().add(okPanel, BorderLayout.PAGE_END);
		this.pack();
		setLocationRelativeTo(Window.mainWindow);
		setVisible(true);

		DocumentListener doc = new DocumentListener() {

			public void changedUpdate(DocumentEvent e) {
				change();
			}
			public void removeUpdate(DocumentEvent e) {
				change();
			}
			public void insertUpdate(DocumentEvent e) {
				change();
			}

			public void change() {
				if (dimensions1.getText().length() > 9) {
					Window.setTextInField(dimensions1, "");
				}
				if (dimensions2.getText().length() > 9) {
					Window.setTextInField(dimensions2, "");
				}
				for (int i = 0; i < dimensions1.getText().length(); i++) {
					if ("0123456789".indexOf(dimensions1.getText().charAt(i)) < 0) {
						Window.setTextInField(dimensions1, "");
					}
				}
				for (int i = 0; i < dimensions2.getText().length(); i++) {
					if ("0123456789".indexOf(dimensions2.getText().charAt(i)) < 0) {
						Window.setTextInField(dimensions2, "");
					}
				}
			}
		};
		dimensions1.getDocument().addDocumentListener(doc);
		dimensions2.getDocument().addDocumentListener(doc);

		this.setMinimumSize(this.getPreferredSize().getSize());
		OKButton.requestFocus();
	}

	public void update() {
		this.setTitle(Lang.OPTIONS_TITLE);
		proxyCheck.setText(Lang.OPTIONS_PROXY);
		keepOpenCheck.setText(Lang.OPTIONS_KEEP_OPEN);
		RPCCheck.setText(Lang.OPTIONS_RPC);
		parametersText.setText(Lang.OPTIONS_LAUNCH_ARGS);
		dirChooser.setText(Lang.INSTANCE_GAME_DIRECTORY);
		dimensions1Text.setText(Lang.OPTIONS_WIDTH);
		dimensions2Text.setText(Lang.OPTIONS_HEIGHT);
		removelabel.setText(Lang.INSTANCE_REMOVE_TITLE);
		instanceNameText.setText(Lang.INSTANCE_NAME);
		chooseIcon.setText(Lang.INSTANCE_CHANGE_ICON_NAME);
		addons.setText(Lang.INSTANCE_SELECT_ADDONS);
		modrepo.setText(Lang.INSTANCE_MODS_REPOSITORY);
		forceUpdate.setText(Lang.FORCE_UPDATE);
		OKButton.setText(Lang.OPTIONS_OK);
		this.setMinimumSize(this.getPreferredSize().getSize());
		this.pack();
	}

	/*public void updateInfo() {
		proxyCheck.setSelected(Launcher.currentInstance.proxy);
		keepOpenCheck.setSelected(Launcher.currentInstance.keepopen);
		RPCCheck.setSelected(Launcher.currentInstance.RPC);
		parameters.setText(Launcher.currentInstance.launchArgs);
		dimensions1.setText(Integer.toString(Launcher.currentInstance.width));
		dimensions2.setText(Integer.toString(Launcher.currentInstance.height));
		Window.selectedInstanceDisplay.setText(Launcher.currentInstance.name);
	}*/

	public void saveOptions() {
		Launcher.forceUpdate = forceUpdate.isSelected();
		try {
			Launcher.currentInstance.width = Integer.parseInt(dimensions1.getText());
		} catch (Exception ex) {
			Launcher.currentInstance.width = 854;
		}
		try {
			Launcher.currentInstance.height = Integer.parseInt(dimensions2.getText());
		} catch (Exception ex) {
			Launcher.currentInstance.height = 480;
		}
		Launcher.currentInstance.keepopen = keepOpenCheck.isSelected();
		Launcher.currentInstance.proxy = proxyCheck.isSelected();
		Launcher.currentInstance.RPC = RPCCheck.isSelected();
		Launcher.currentInstance.launchArgs = parameters.getText();
		if (!instanceName.getText().equals(Launcher.currentInstance.name) && !instanceName.getText().equals("")) {
			Launcher.setInstance(Launcher.currentInstance.renameInstance(instanceName.getText()));
		}
		Launcher.currentInstance.saveInstance();
		Util.setProperty(BC.SETTINGS, "lastInstance", Launcher.currentInstance.name);
	}
}
