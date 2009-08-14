package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.utils.CopyFile;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;

public class ConfigPanelOther extends JPanel implements ConfigPanel {

	private static final long serialVersionUID = -5765034036707188670L;

	private JLabel timerLabel;

	private JCheckBox checkNewVersionAfterStart, passwordAfterStartButton,
			timerAfterStartButton, startMinimizedButton, confirmOnExitButton,
			searchWithSSDP, minimizeInsteadOfClose, createBackup,
			createBackupAfterFetch;

	private JTextField save_location;

	private JPasswordField passwordField;

	private JSlider timerSlider;

	private ConfigPanelFritzBox fritzBoxPanel;

	public ConfigPanelOther(ConfigPanelFritzBox fritzBoxPanel) {
		this.fritzBoxPanel = fritzBoxPanel;

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

		JPanel cPane = new JPanel();
//		cPane.setLayout(new BoxLayout(cPane, BoxLayout.Y_AXIS));
		cPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 0;
		c.insets.bottom = 0;
		c.insets.left = 5;
		c.anchor = GridBagConstraints.WEST;

		JPanel timerPanel = new JPanel();
		timerLabel = new JLabel(Main.getMessage("timer_in") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		timerPanel.add(timerLabel);

		timerSlider = new JSlider(0, 120, 30);
		timerSlider.setPaintTicks(true);
		timerSlider.setMinorTickSpacing(10);
		timerSlider.setMajorTickSpacing(30);
		timerSlider.setPaintLabels(true);
		timerSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (timerSlider.getValue() < 3)
					timerSlider.setValue(3);
				timerLabel
						.setText(Main.getMessage("timer") + ": " + timerSlider.getValue() + " " + Main.getMessage("abbreviation_minutes")); //$NON-NLS-1$,  //$NON-NLS-2$
			}

		});
		timerPanel.add(timerSlider);
		c.gridy = 0;
		cPane.add(timerPanel, c);

		checkNewVersionAfterStart = new JCheckBox(Main
				.getMessage("check_for_new_version_after_start")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(checkNewVersionAfterStart, c);

		passwordAfterStartButton = new JCheckBox(Main
				.getMessage("ask_for_password_before_start")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(passwordAfterStartButton, c);

		JPanel passwordPane = new JPanel();
		JLabel passwordLabel = new JLabel(Main.getMessage("password") + ": ");
		passwordPane.add(passwordLabel);
		passwordField = new JPasswordField("", 16);
		passwordPane.add(passwordField);
		passwordPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		c.gridy++;
		cPane.add(passwordPane, c);

		timerAfterStartButton = new JCheckBox(Main
				.getMessage("get_timer_after")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(timerAfterStartButton, c);

		startMinimizedButton = new JCheckBox(Main.getMessage("start_minimized")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(startMinimizedButton, c);

		confirmOnExitButton = new JCheckBox(Main.getMessage("confirm_on_exit")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(confirmOnExitButton, c);

		searchWithSSDP = new JCheckBox(Main.getMessage("search_with_SSDP")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(searchWithSSDP, c);

		minimizeInsteadOfClose = new JCheckBox(Main
				.getMessage("minimize_instead_close")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(minimizeInsteadOfClose, c);

		createBackup = new JCheckBox(Main.getMessage("create_backup_start")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(createBackup, c);

		createBackupAfterFetch = new JCheckBox(Main
				.getMessage("create_backup_fetch")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(createBackupAfterFetch, c);

		JPanel panel = new JPanel();

		JLabel label = new JLabel(Main.getMessage("save_directory"));
		panel.add(label);

		save_location = new JTextField(Main.SAVE_DIR);
		save_location.setPreferredSize(new Dimension(200, 20));
		panel.add(save_location);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(Main.SAVE_DIR);
				fc.setDialogTitle(Main.getMessage("save_directory")); //$NON-NLS-1$
				fc.setDialogType(JFileChooser.SAVE_DIALOG);

				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if (!file.exists()) {
						JOptionPane
								.showMessageDialog(
										null,
										Main.getMessage("file_not_found"), //$NON-NLS-1$
										Main
												.getMessage("dialog_title_file_not_found"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					} else {
						save_location.setText(file.getAbsolutePath());
					}
				}
			}
		};

		JButton browseButton = new JButton(Main.getMessage("browse"));
		browseButton.addActionListener(actionListener);

		panel.add(browseButton);
		c.gridy++;
		cPane.add(panel, c);

		add(new JScrollPane(cPane), BorderLayout.CENTER);

	}

	public void loadSettings() {
		checkNewVersionAfterStart.setSelected(JFritzUtils.parseBoolean(Main
				.getProperty("option.checkNewVersionAfterStart")));//$NON-NLS-1$, //ßNON-NLS-2$
		timerAfterStartButton.setSelected(JFritzUtils.parseBoolean(Main
				.getProperty("option.timerAfterStart"))); //$NON-NLS-1$
		confirmOnExitButton.setSelected(JFritzUtils.parseBoolean(Main
				.getProperty("option.confirmOnExit"))); //$NON-NLS-1$,  //$NON-NLS-2$
		startMinimizedButton.setSelected(JFritzUtils.parseBoolean(Main
				.getProperty("option.startMinimized"))); //$NON-NLS-1$,  //$NON-NLS-2$
		minimizeInsteadOfClose.setSelected(JFritzUtils.parseBoolean(Main
				.getProperty("option.minimize"))); //$NON-NLS-1$,  //$NON-NLS-2$
		createBackup.setSelected(JFritzUtils.parseBoolean(Main.getProperty(
				"option.createBackup"))); //$NON-NLS-1$,  //$NON-NLS-2$
		createBackupAfterFetch.setSelected(JFritzUtils.parseBoolean(Main
				.getProperty("option.createBackupAfterFetch"))); //$NON-NLS-1$,  //$NON-NLS-2$

		String decrypted_pwd = Encryption.decrypt(Main.getProperty("jfritz.seed"));
		if ((decrypted_pwd != null)
			&& (decrypted_pwd.length() > Main.PROGRAM_SEED.length()))
		{
			String pwd = decrypted_pwd.substring(Main.PROGRAM_SEED.length());
			if (Main.PROGRAM_SECRET.equals(pwd))
			{
				pwd = "";
			}
			passwordField.setText(pwd);
		}
		else
		{
			passwordField.setText("");
		}

		String pwd = new String(passwordField.getPassword());
		if ("".equals(pwd))
		{
			passwordAfterStartButton.setSelected(false);
		}
		else
		{
			String a = Encryption.decrypt(
					Main.getProperty("jfritz.pwd"));
			String b = Main.PROGRAM_SECRET + pwd;
			boolean pwAfterStart = !a.equals(b); //$NON-NLS-1$,  //$NON-NLS-2$

			passwordAfterStartButton.setSelected(pwAfterStart);
		}

		timerSlider.setValue(Integer.parseInt(Main.getProperty("fetch.timer"))); //$NON-NLS-1$

		searchWithSSDP.setSelected(JFritzUtils.parseBoolean(Main.getProperty(
				"option.useSSDP"))); //$NON-NLS-1$,  //$NON-NLS-2$

		if(Main.getProperty("network.type").equals("2")
				&& Boolean.parseBoolean(Main.getProperty("option.clientCallList"))){

			Debug.netMsg("JFritz is running as a client and using call list from server, disabeling some options");
			searchWithSSDP.setSelected(false);
			searchWithSSDP.setEnabled(false);
		}

	}

	public void saveSettings() {
		// only write the save dir to disk if the user changed something
		if (!save_location.getText().equals(Main.SAVE_DIR)) {
			File f = new File(save_location.getText());
			if (f.isDirectory())
			{
				CopyFile backup = new CopyFile();
				try {
					backup.copy(Main.SAVE_DIR, "xml", save_location.getText()); //$NON-NLS-1$,  //$NON-NLS-2$
				} catch (NullPointerException e) {
					Debug.error("No directory choosen for backup!"); //$NON-NLS-1$
				}
				Main.removeLock();
				Main.SAVE_DIR = save_location.getText();
				Main.createLock();
				Main.writeSaveDir();
			}
		}
		Main.setProperty("option.useSSDP", Boolean.toString(searchWithSSDP //$NON-NLS-1$
				.isSelected()));
		Main.setProperty("option.timerAfterStart", Boolean //$NON-NLS-1$
				.toString(timerAfterStartButton.isSelected()));
		Main.setProperty("option.confirmOnExit", Boolean //$NON-NLS-1$
				.toString(confirmOnExitButton.isSelected()));
		Main.setProperty("option.startMinimized", Boolean //$NON-NLS-1$
				.toString(startMinimizedButton.isSelected()));
		Main.setProperty("option.minimize", Boolean //$NON-NLS-1$
				.toString(minimizeInsteadOfClose.isSelected()));
		Main
				.setProperty(
						"option.createBackup", Boolean.toString(createBackup.isSelected())); //$NON-NLS-1$
		Main
				.setProperty(
						"option.createBackupAfterFetch", Boolean.toString(createBackupAfterFetch.isSelected())); //$NON-NLS-1$
		Main
				.setProperty(
						"option.checkNewVersionAfterStart", Boolean.toString(checkNewVersionAfterStart.isSelected())); //$NON-NLS-1$

		String passwd = new String(passwordField.getPassword());
		if ("".equals(passwd))
		{
			// if password is empty, set it to program secret
			passwd = Main.PROGRAM_SECRET;
			passwordAfterStartButton.setSelected(false);
		}
		if (!passwordAfterStartButton.isSelected()) {
			Main.setProperty("jfritz.seed", Encryption
					.encrypt(Main.PROGRAM_SEED + passwd));
			Main.setProperty("jfritz.pwd", Encryption //$NON-NLS-1$
					.encrypt(Main.PROGRAM_SECRET + passwd));
		} else {
			Main.setProperty("jfritz.seed", Encryption
					.encrypt(Main.PROGRAM_SEED + passwd));
			Main.removeProperty("jfritz.pwd"); //$NON-NLS-1$
		}
		if (timerSlider.getValue() < 3)
			timerSlider.setValue(3);
		Main.setProperty("fetch.timer", Integer.toString(timerSlider //$NON-NLS-1$
				.getValue()));

	}

	public String getPath()
	{
		return Main.getMessage("other");
	}

	public JPanel getPanel() {
		return this;
	}

	public String getHelpUrl() {
		return "http://jfritz.org/wiki/JFritz_Handbuch:Deutsch#Weiteres";
	}

	public void cancel() {
		// TODO Auto-generated method stub

	}
}
