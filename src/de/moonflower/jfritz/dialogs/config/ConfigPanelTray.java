package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import de.moonflower.jfritz.Main;

public class ConfigPanelTray extends JPanel implements ConfigPanel {

	private static final long serialVersionUID = -4475730980270094525L;

	private JRadioButton singleClickButton;
	private JRadioButton doubleClickButton;
	private boolean settingsChanged = false;

	public ConfigPanelTray() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

		JPanel trayPane = new JPanel();
		trayPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		JPanel clickPane = new JPanel();
		singleClickButton = new JRadioButton(Main.getMessage("single_click"));
		singleClickButton.setActionCommand("singleClick");

		doubleClickButton = new JRadioButton(Main.getMessage("double_click"));
		doubleClickButton.setActionCommand("doubleClick");

		ButtonGroup clickGroup = new ButtonGroup();
		clickGroup.add(singleClickButton);
		clickGroup.add(doubleClickButton);

		clickPane.add(singleClickButton);
		clickPane.add(doubleClickButton);

		clickPane.setBorder(BorderFactory.createTitledBorder(
		           BorderFactory.createEtchedBorder(), Main.getMessage("tray_click")));

		c.gridy++;
		trayPane.add(clickPane, c);

		add(new JScrollPane(trayPane), BorderLayout.CENTER);
	}

	public void loadSettings() {
		String clickCount = Main.getProperty("tray.clickCount");
		if ("1".equals(clickCount)) {
			singleClickButton.setSelected(true);
		} else {
			doubleClickButton.setSelected(true);
		}
		settingsChanged = false;
	}

	public void saveSettings() {
		if (singleClickButton.isSelected()) {
			if (!Main.getProperty("tray.clickCount").equals("1"))
			{
				settingsChanged = true;
			}
			Main.setProperty("tray.clickCount", "1");
		} else {
			if (Main.getProperty("tray.clickCount").equals("1"))
			{
				settingsChanged = true;
			}
			Main.setProperty("tray.clickCount", "2");
		}
	}

	public String getPath()
	{
		return Main.getMessage("other")+"::"+Main.getMessage("tray");
	}

	public JPanel getPanel() {
		return this;
	}

	public String getHelpUrl() {
		return "http://jfritz.org/wiki/JFritz_Handbuch:Deutsch#Tray";
	}

	public void cancel() {
		// TODO Auto-generated method stub

	}

	public boolean shouldRefreshJFritzWindow() {
		return false;
	}

	public boolean shouldRefreshTrayMenu() {
		return settingsChanged;
	}
}
