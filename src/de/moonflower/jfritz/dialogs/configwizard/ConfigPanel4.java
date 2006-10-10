package de.moonflower.jfritz.dialogs.configwizard;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import de.moonflower.jfritz.Main;
/**
 * @author Brian Jensen
 *
 * This is the panel for the message settings
 *
 * @see http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/index.html
 *
 */
public class ConfigPanel4 extends JPanel {

    private static final long serialVersionUID = 1;

    public JRadioButton popupNoButton, popupDialogButton, popupTrayButton;

	public ConfigPanel4(){

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 0;
		JLabel text = new JLabel(Main.getMessage("popup_for_information")); //$NON-NLS-1$
		panel.add(text, c);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (popupNoButton.isSelected()) {
					Main.setProperty("option.popuptype", "0"); //$NON-NLS-1$,  //$NON-NLS-2$
				} else if (popupDialogButton.isSelected()) {
					Main.setProperty("option.popuptype", "1"); //$NON-NLS-1$,  //$NON-NLS-2$
				} else {
					Main.setProperty("option.popuptype", "2"); //$NON-NLS-1$,  //$NON-NLS-2$
				}
			}
		};

		ButtonGroup popupGroup = new ButtonGroup();
		c.gridy = 1;
		popupNoButton = new JRadioButton(Main.getMessage("no_popups")); //$NON-NLS-1$
		popupNoButton.addActionListener(actionListener);
		popupGroup.add(popupNoButton);
		panel.add(popupNoButton, c);

		c.gridy = 2;
		popupDialogButton = new JRadioButton(Main.getMessage("popup_windows")); //$NON-NLS-1$
		popupDialogButton.addActionListener(actionListener);
		popupGroup.add(popupDialogButton);
		panel.add(popupDialogButton, c);

		c.gridy = 3;
		popupTrayButton = new JRadioButton(Main.getMessage("tray_messages")); //$NON-NLS-1$
		popupTrayButton.addActionListener(actionListener);
		popupGroup.add(popupTrayButton);
		panel.add(popupTrayButton, c);

		if (!Main.SYSTRAY_SUPPORT) {
			popupTrayButton.setVisible(false);
		}
		switch (Integer.parseInt(Main.getProperty("option.popuptype", "1"))) { //$NON-NLS-1$,  //$NON-NLS-2$
			case 0 : {
				popupNoButton.setSelected(true);
				break;
			}
			case 1 : {
				popupDialogButton.setSelected(true);
				break;
			}
			case 2 : {
				popupTrayButton.setSelected(true);
				break;
			}
		}

		add(panel, BorderLayout.CENTER);

	}
}
