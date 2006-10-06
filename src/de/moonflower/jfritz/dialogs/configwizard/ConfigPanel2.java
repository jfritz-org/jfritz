package de.moonflower.jfritz.dialogs.configwizard;

import java.awt.*;

import javax.swing.*;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * @author Brian Jensen
 *
 * This panel configures the telephone network settings
 *
 * @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanel2 extends JPanel{

    private static final long serialVersionUID = 1;

    public JTextField areaCode;

    public JTextField countryCode;

    public JTextField areaPrefix;

    public JTextField countryPrefix;

    public JCheckBox activateDialPrefix;

    public JTextField dialPrefix;

	public ConfigPanel2(){

		//draw the panel
		JPanel phonepane = new JPanel();
		phonepane.setLayout(new GridBagLayout());
		phonepane.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.LINE_START;

		c.gridy = 2;
		JLabel label = new JLabel(Main.getMessage("area_code")); //$NON-NLS-1$
		phonepane.add(label, c);
		areaCode = new JTextField("", 6); //$NON-NLS-1$
		phonepane.add(areaCode, c);

		c.gridy = 1;
		label = new JLabel(Main.getMessage("country_code")); //$NON-NLS-1$
		phonepane.add(label, c);
		countryCode = new JTextField("", 3); //$NON-NLS-1$
		phonepane.add(countryCode, c);

		c.gridy = 3;
		label = new JLabel(Main.getMessage("area_prefix")); //$NON-NLS-1$
		phonepane.add(label, c);
		areaPrefix = new JTextField("", 3); //$NON-NLS-1$
		phonepane.add(areaPrefix, c);

		c.gridy = 0;
		label = new JLabel(Main.getMessage("country_prefix")); //$NON-NLS-1$
		phonepane.add(label, c);
		countryPrefix = new JTextField("", 3); //$NON-NLS-1$
		phonepane.add(countryPrefix, c);

        c.gridy = 4;
        activateDialPrefix = new JCheckBox(Main.getMessage("dial_prefix")); //$NON-NLS-1$
        phonepane.add(activateDialPrefix, c);
        dialPrefix = new JTextField("", 3); //$NON-NLS-1$
        phonepane.add(dialPrefix, c);

		//initialize the panel to correct values
    	areaCode.setText(Main.getProperty("area.code")); //$NON-NLS-1$
		countryCode.setText(Main.getProperty("country.code")); //$NON-NLS-1$
		areaPrefix.setText(Main.getProperty("area.prefix")); //$NON-NLS-1$
		countryPrefix.setText(Main.getProperty("country.prefix")); //$NON-NLS-1$
		dialPrefix.setText(Main.getProperty("dial.prefix")); //$NON-NLS-1$
		activateDialPrefix.setSelected(JFritzUtils.parseBoolean(Main.getProperty(
                "option.activateDialPrefix", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$

		add(phonepane);

	}

}
