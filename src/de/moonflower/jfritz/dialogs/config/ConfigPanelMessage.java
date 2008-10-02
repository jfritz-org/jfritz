package de.moonflower.jfritz.dialogs.config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.moonflower.jfritz.Main;

public class ConfigPanelMessage extends JPanel implements ConfigPanel {

	private static final long serialVersionUID = -630145657490186844L;

	private AbstractButton popupNoButton;

	private AbstractButton popupDialogButton;

	private JRadioButton popupTrayButton;

	private JTextField popupDelay;

	private JLabel delayLbl;

	public ConfigPanelMessage() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 0;
		JLabel text = new JLabel(Main.getMessage("popup_for_information")); //$NON-NLS-1$
		add(text, c);

		delayLbl = new JLabel(Main.getMessage("popup_delay"));

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (popupNoButton.isSelected()) {
					Main.setProperty("option.popuptype", "0"); //$NON-NLS-1$,  //$NON-NLS-2$
					delayLbl.setVisible(false);
					popupDelay.setVisible(false);
				} else if (popupDialogButton.isSelected()) {
					Main.setProperty("option.popuptype", "1"); //$NON-NLS-1$,  //$NON-NLS-2$
					delayLbl.setVisible(true);
					popupDelay.setVisible(true);
				} else {
					Main.setProperty("option.popuptype", "2"); //$NON-NLS-1$,  //$NON-NLS-2$
					delayLbl.setVisible(false);
					popupDelay.setVisible(false);
				}
			}
		};

		ButtonGroup popupGroup = new ButtonGroup();
		c.gridy = 1;
		popupNoButton = new JRadioButton(Main.getMessage("no_popups")); //$NON-NLS-1$
		popupNoButton.addActionListener(actionListener);
		popupGroup.add(popupNoButton);
		add(popupNoButton, c);

		c.gridy = 2;
		popupDialogButton = new JRadioButton(Main.getMessage("popup_windows")); //$NON-NLS-1$
		popupDialogButton.addActionListener(actionListener);
		popupGroup.add(popupDialogButton);
		add(popupDialogButton, c);

		c.gridy = 3;
		popupTrayButton = new JRadioButton(Main.getMessage("tray_messages")); //$NON-NLS-1$
		popupTrayButton.addActionListener(actionListener);
		popupGroup.add(popupTrayButton);
		add(popupTrayButton, c);

		c.gridy = 4;
		c.insets.top = 10;
		add(delayLbl, c);

		popupDelay = new JTextField("", 3);
		c.gridx = 1;
		c.insets.left = 15;
		add(popupDelay, c);

		if (!Main.SYSTRAY_SUPPORT) {
			popupTrayButton.setVisible(false);
		}
	}

	public void loadSettings() {
		if (!Main.SYSTRAY_SUPPORT) {
			popupTrayButton.setVisible(false);
		}
		switch (Integer.parseInt(Main.getProperty("option.popuptype"))) { //$NON-NLS-1$,  //$NON-NLS-2$
		case 0: {
			popupNoButton.setSelected(true);
			delayLbl.setVisible(false);
			popupDelay.setVisible(false);
			break;
		}
		case 1: {
			popupDialogButton.setSelected(true);
			delayLbl.setVisible(true);
			popupDelay.setVisible(true);
			break;
		}
		case 2: {
			popupTrayButton.setSelected(true);
			delayLbl.setVisible(false);
			popupDelay.setVisible(false);
			break;
		}
		}

		popupDelay.setText(Main.getProperty("option.popupDelay"));

	}

	public void saveSettings() {
		// Set Popup Messages Type
		if (popupNoButton.isSelected()) {
			Main.setProperty("option.popuptype", "0"); //$NON-NLS-1$, //$NON-NLS-2$
		} else if (popupDialogButton.isSelected()) {
			Main.setProperty("option.popuptype", "1"); //$NON-NLS-1$, //$NON-NLS-2$
		} else {
			Main.setProperty("option.popuptype", "2"); //$NON-NLS-1$, //$NON-NLS-2$
		}

		Main.setProperty("option.popupDelay", popupDelay.getText());

	}

}
