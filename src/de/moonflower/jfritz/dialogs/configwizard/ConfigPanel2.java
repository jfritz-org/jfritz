package de.moonflower.jfritz.dialogs.configwizard;

import java.awt.*;

import javax.swing.*;

import de.moonflower.jfritz.JFritz;

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

	public ConfigPanel2(){

		//draw the panel
		JPanel phonepane = new JPanel();
		phonepane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.LINE_START;

		c.gridy = 1;
		JLabel label = new JLabel(JFritz.getMessage("area_code")); //$NON-NLS-1$
		phonepane.add(label, c);
		areaCode = new JTextField("", 6); //$NON-NLS-1$
		phonepane.add(areaCode, c);

		c.gridy = 2;
		label = new JLabel(JFritz.getMessage("country_code")); //$NON-NLS-1$
		phonepane.add(label, c);
		countryCode = new JTextField("", 3); //$NON-NLS-1$
		phonepane.add(countryCode, c);

		c.gridy = 3;
		label = new JLabel(JFritz.getMessage("area_prefix")); //$NON-NLS-1$
		phonepane.add(label, c);
		areaPrefix = new JTextField("", 3); //$NON-NLS-1$
		phonepane.add(areaPrefix, c);

		c.gridy = GridBagConstraints.REMAINDER;
		label = new JLabel(JFritz.getMessage("country_prefix")); //$NON-NLS-1$
		phonepane.add(label, c);
		countryPrefix = new JTextField("", 3); //$NON-NLS-1$
		phonepane.add(countryPrefix, c);

		//initialize the panel to correct values
    	areaCode.setText(JFritz.getProperty("area.code")); //$NON-NLS-1$
		countryCode.setText(JFritz.getProperty("country.code")); //$NON-NLS-1$
		areaPrefix.setText(JFritz.getProperty("area.prefix")); //$NON-NLS-1$
		countryPrefix.setText(JFritz.getProperty("country.prefix")); //$NON-NLS-1$

		add(phonepane);

	}

}
