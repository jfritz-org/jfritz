package de.moonflower.jfritz.dialogs.configwizard;

import java.awt.*;

import javax.swing.*;

/**
 *
 * @author Brian Jensen
 *
 * This is the first panel, containing only an informational message
 *
 * @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanel1 extends JPanel{

	    private static final long serialVersionUID = 1;

	    private JPanel configPanel1;

	    public ConfigPanel1() {

			configPanel1 = new JPanel();
			configPanel1.setLayout(new GridBagLayout());
			configPanel1.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
			GridBagConstraints c = new GridBagConstraints();
			c.insets.top = 5;
			c.insets.bottom = 5;
			c.insets.left = 5;
			c.anchor = GridBagConstraints.WEST;

			c.gridy = 0;
			c.gridx = 0;
			c.anchor = GridBagConstraints.CENTER;
			JLabel label = new JLabel("Welcome to JFritz!");
			configPanel1.add(label, c);

			c.gridy = 2;
			c.anchor = GridBagConstraints.LINE_START;
			label = new JLabel("This wizard will assist you in configuring the most important");
			configPanel1.add(label, c);

			c.gridy = 3;
			label = new JLabel("settings in JFritz. More options are available in the");
			configPanel1.add(label, c);

			c.gridy = 4;
			label = new JLabel("configuration dialog. Make sure to fill out all fields");
			configPanel1.add(label, c);

			c.gridy = 5;
			label = new JLabel("correctly or JFritz will not function properly.");
			configPanel1.add(label, c);

			add(configPanel1);

 	    }

}