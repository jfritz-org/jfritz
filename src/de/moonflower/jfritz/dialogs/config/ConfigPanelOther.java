package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
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
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;

public class ConfigPanelOther extends JPanel implements ConfigPanel {

	private static final long serialVersionUID = -5765034036707188670L;

	private JLabel timerLabel;

	private JCheckBox checkNewVersionAfterStart, passwordAfterStartButton,
			timerAfterStartButton, startMinimizedButton, confirmOnExitButton,
			searchWithSSDP, minimizeInsteadOfClose, createBackup,
			createBackupAfterFetch, keepImportantBackupsOnly, useDecorations;

	private JTextField save_location;

	private JPasswordField passwordField;

	private JSlider timerSlider;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	public ConfigPanelOther(ConfigPanelFritzBox fritzBoxPanel) {
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
		timerLabel = new JLabel(messages.getMessage("timer_in") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
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
						.setText(messages.getMessage("timer") + ": " + timerSlider.getValue() + " " + messages.getMessage("abbreviation_minutes")); //$NON-NLS-1$,  //$NON-NLS-2$
			}

		});
		timerPanel.add(timerSlider);
		c.gridy = 0;
		cPane.add(timerPanel, c);

		checkNewVersionAfterStart = new JCheckBox(messages.getMessage("check_for_new_version_after_start")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(checkNewVersionAfterStart, c);
		checkNewVersionAfterStart.setEnabled(false);

		passwordAfterStartButton = new JCheckBox(messages.getMessage("ask_for_password_before_start")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(passwordAfterStartButton, c);

		JPanel passwordPane = new JPanel();
		JLabel passwordLabel = new JLabel(messages.getMessage("password") + ": ");
		passwordPane.add(passwordLabel);
		passwordField = new JPasswordField("", 16);
		passwordPane.add(passwordField);
		passwordPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		c.gridy++;
		cPane.add(passwordPane, c);

		timerAfterStartButton = new JCheckBox(messages.getMessage("get_timer_after")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(timerAfterStartButton, c);

		startMinimizedButton = new JCheckBox(messages.getMessage("start_minimized")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(startMinimizedButton, c);

		confirmOnExitButton = new JCheckBox(messages.getMessage("confirm_on_exit")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(confirmOnExitButton, c);

		searchWithSSDP = new JCheckBox(messages.getMessage("search_with_SSDP")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(searchWithSSDP, c);

		minimizeInsteadOfClose = new JCheckBox(messages.getMessage("minimize_instead_close")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(minimizeInsteadOfClose, c);

		useDecorations = new JCheckBox(messages.getMessage("use_decorations"));
		c.gridy++;
		cPane.add(useDecorations, c);

		createBackup = new JCheckBox(messages.getMessage("create_backup_start")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(createBackup, c);

		createBackupAfterFetch = new JCheckBox(messages.getMessage("create_backup_fetch")); //$NON-NLS-1$
		c.gridy++;
		cPane.add(createBackupAfterFetch, c);

		keepImportantBackupsOnly = new JCheckBox(messages.getMessage("keep_important_backups_only"));
		c.gridy++;
		cPane.add(keepImportantBackupsOnly, c);

		JPanel panel = new JPanel();

		JLabel label = new JLabel(messages.getMessage("save_directory"));
		panel.add(label);

		save_location = new JTextField(Main.SAVE_DIR, 16);
		panel.add(save_location);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(Main.SAVE_DIR);
				fc.setDialogTitle(messages.getMessage("save_directory")); //$NON-NLS-1$
				fc.setDialogType(JFileChooser.SAVE_DIALOG);

				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if (!file.exists()) {
						JOptionPane
								.showMessageDialog(
										null,
										messages.getMessage("file_not_found"), //$NON-NLS-1$
										messages.getMessage("dialog_title_file_not_found"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					} else {
						save_location.setText(file.getAbsolutePath());
					}
				}
			}
		};

		JButton browseButton = new JButton(messages.getMessage("browse"));
		browseButton.addActionListener(actionListener);

		panel.add(browseButton);
		c.gridy++;
		cPane.add(panel, c);

		add(new JScrollPane(cPane), BorderLayout.CENTER);

	}

	public void loadSettings() {
		checkNewVersionAfterStart.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("option.checkNewVersionAfterStart")));//$NON-NLS-1$
		timerAfterStartButton.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("option.timerAfterStart"))); //$NON-NLS-1$
		confirmOnExitButton.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("option.confirmOnExit"))); //$NON-NLS-1$
		startMinimizedButton.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("option.startMinimized"))); //$NON-NLS-1$
		minimizeInsteadOfClose.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("option.minimize"))); //$NON-NLS-1$
		useDecorations.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("window.useDecorations"))); //$NON-NLS-1$
		createBackup.setSelected(JFritzUtils.parseBoolean(properties.getProperty(
				"option.createBackup"))); //$NON-NLS-1$
		createBackupAfterFetch.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("option.createBackupAfterFetch"))); //$NON-NLS-1$
		keepImportantBackupsOnly.setSelected(JFritzUtils.parseBoolean(properties
				.getProperty("option.keepImportantBackupsOnly"))); //$NON-NLS-1$

		String decrypted_pwd = Encryption.decrypt(properties.getProperty("jfritz.seed"));
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
					properties.getProperty("jfritz.pwd"));
			String b = Main.PROGRAM_SECRET + pwd;
			boolean pwAfterStart = !a.equals(b); //$NON-NLS-1$,  //$NON-NLS-2$

			passwordAfterStartButton.setSelected(pwAfterStart);
		}

		timerSlider.setValue(Integer.parseInt(properties.getProperty("fetch.timer"))); //$NON-NLS-1$

		searchWithSSDP.setSelected(JFritzUtils.parseBoolean(properties.getProperty(
				"option.useSSDP"))); //$NON-NLS-1$,  //$NON-NLS-2$

		if(properties.getProperty("network.type").equals("2")
				&& Boolean.parseBoolean(properties.getProperty("option.clientCallList"))){

			Debug.netMsg("JFritz is running as a client and using call list from server, disabeling some options");
			searchWithSSDP.setSelected(false);
			searchWithSSDP.setEnabled(false);
		}

	}

	public void saveSettings() {
		// only write the save dir to disk if the user changed something
		if (!save_location.getText().equals(Main.SAVE_DIR)) {
			Main.changeSaveDir(save_location.getText());
		}

		properties.setProperty("option.useSSDP", Boolean.toString(searchWithSSDP //$NON-NLS-1$
				.isSelected()));
		properties.setProperty("option.timerAfterStart", Boolean //$NON-NLS-1$
				.toString(timerAfterStartButton.isSelected()));
		properties.setProperty("option.confirmOnExit", Boolean //$NON-NLS-1$
				.toString(confirmOnExitButton.isSelected()));
		properties.setProperty("option.startMinimized", Boolean //$NON-NLS-1$
				.toString(startMinimizedButton.isSelected()));
		properties.setProperty("option.minimize", Boolean //$NON-NLS-1$
				.toString(minimizeInsteadOfClose.isSelected()));
		properties.setProperty("window.useDecorations", Boolean //$NON-NLS-1$
				.toString(useDecorations.isSelected()));
		properties.setProperty("option.createBackup", Boolean //$NON-NLS-1$
				.toString(createBackup.isSelected()));
		properties.setProperty("option.createBackupAfterFetch", //$NON-NLS-1$
				Boolean.toString(createBackupAfterFetch.isSelected()));
		properties.setProperty("option.keepImportantBackupsOnly",
				Boolean.toString(keepImportantBackupsOnly.isSelected()));
		properties.setProperty("option.checkNewVersionAfterStart", //$NON-NLS-1$
				Boolean.toString(checkNewVersionAfterStart.isSelected()));

		String passwd = new String(passwordField.getPassword());
		if ("".equals(passwd))
		{
			// if password is empty, set it to program secret
			passwd = Main.PROGRAM_SECRET;
			passwordAfterStartButton.setSelected(false);
		}
		if (!passwordAfterStartButton.isSelected()) {
			properties.setProperty("jfritz.seed", Encryption
					.encrypt(Main.PROGRAM_SEED + passwd));
			properties.setProperty("jfritz.pwd", Encryption //$NON-NLS-1$
					.encrypt(Main.PROGRAM_SECRET + passwd));
		} else {
			properties.setProperty("jfritz.seed", Encryption
					.encrypt(Main.PROGRAM_SEED + passwd));
			properties.removeProperty("jfritz.pwd"); //$NON-NLS-1$
		}
		if (timerSlider.getValue() < 3)
			timerSlider.setValue(3);
		properties.setProperty("fetch.timer", Integer.toString(timerSlider //$NON-NLS-1$
				.getValue()));

	}

	public String getPath()
	{
		return messages.getMessage("other");
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

	public boolean shouldRefreshJFritzWindow() {
		return false;
	}

	public boolean shouldRefreshTrayMenu() {
		return false;
	}
}
