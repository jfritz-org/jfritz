package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.utils.JFritzUtils;

public class ConfigPanelPhone extends JPanel implements ConfigPanel{

	/**
	 *
	 */
	private static final long serialVersionUID = -282450814821033801L;

	private JTextField areaPrefix, areaCode, countryPrefix, countryCode, dialPrefix;

	private JCheckBox activateDialPrefix;

	public ConfigPanelPhone() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

		JPanel cPane = new JPanel();
		cPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 1;
		JLabel label = new JLabel(Main.getMessage("area_code")); //$NON-NLS-1$
		cPane.add(label, c);
		areaCode = new JTextField("", 6); //$NON-NLS-1$
		cPane.add(areaCode, c);

		c.gridy = 2;
		label = new JLabel(Main.getMessage("country_code")); //$NON-NLS-1$
		cPane.add(label, c);
		countryCode = new JTextField("", 6); //$NON-NLS-1$
		cPane.add(countryCode, c);

		c.gridy = 3;
		label = new JLabel(Main.getMessage("area_prefix")); //$NON-NLS-1$
		cPane.add(label, c);
		areaPrefix = new JTextField("", 6); //$NON-NLS-1$
		cPane.add(areaPrefix, c);

		c.gridy = 4;
		label = new JLabel(Main.getMessage("country_prefix")); //$NON-NLS-1$
		cPane.add(label, c);
		countryPrefix = new JTextField("", 6); //$NON-NLS-1$
		cPane.add(countryPrefix, c);

        c.gridy = 5;
        activateDialPrefix = new JCheckBox(Main.getMessage("dial_prefix")); //$NON-NLS-1$
        cPane.add(activateDialPrefix, c);
        dialPrefix = new JTextField("", 6); //$NON-NLS-1$
        cPane.add(dialPrefix, c);

        add(new JScrollPane(cPane), BorderLayout.CENTER);
	}

	public void loadSettings() {
		activateDialPrefix.setSelected(JFritzUtils.parseBoolean(Main.getProperty(
                "option.activateDialPrefix"))); //$NON-NLS-1$,  //$NON-NLS-2$
		areaCode.setText(Main.getProperty("area.code")); //$NON-NLS-1$
		countryCode.setText(Main.getProperty("country.code")); //$NON-NLS-1$
        areaPrefix.setText(Main.getProperty("area.prefix")); //$NON-NLS-1$
        dialPrefix.setText(Main.getProperty("dial.prefix")); //$NON-NLS-1$
		countryPrefix.setText(Main.getProperty("country.prefix")); //$NON-NLS-1$
	}

	public void saveSettings() {
		//		 Remove leading "0" from areaCode
		if (areaCode.getText().startsWith(areaPrefix.getText()))
			areaCode.setText(areaCode.getText().substring(
					areaPrefix.getText().length()));
		Main.setProperty(
                "option.activateDialPrefix", Boolean.toString(activateDialPrefix.isSelected())); //$NON-NLS-1$

		Main.setProperty("area.code", areaCode.getText()); //$NON-NLS-1$

		//Phone stuff here
		//make sure country code has a plus on it
		if(!countryCode.getText().startsWith("+"))
			countryCode.setText("+"+countryCode.getText());

		Main.setProperty("country.code", countryCode.getText()); //$NON-NLS-1$
		Main.setProperty("area.prefix", areaPrefix.getText()); //$NON-NLS-1$
        Main.setProperty("dial.prefix", dialPrefix.getText()); //$NON-NLS-1$
		Main.setProperty("country.prefix", countryPrefix.getText()); //$NON-NLS-1$

	}

	public String getPath()
	{
		return Main.getMessage("telephone");
	}

	public JPanel getPanel() {
		return this;
	}

	public String getHelpUrl() {
		return "http://jfritz.org/wiki/JFritz_Handbuch:Deutsch#Telefon";
	}
}
