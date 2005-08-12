/*
 * Created on 05.08.2005
 *
 */
package de.moonflower.jfritz.dialogs.config;

import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPasswordField;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Encryption;


/**
 * @author rob
 *
 * TODO: passwort immer 5 Zeichen lang
 *
 */
public class TelnetConfigDialog extends JDialog {

	private JFritz jfritz;

	private JLabel userNameLabel;

	private JPasswordField passwordTextfield;

	private JTextField userNameTextfield;

	private JLabel passwordLabel;

	private JButton okButton, cancelButton;

	private int exitCode = 0;

	public static final int APPROVE_OPTION = 1;
	public static final int CANCEL_OPTION = 2;

	public TelnetConfigDialog(JFrame parent, JFritz jfritz) {
		super(parent, true);
		if (parent != null) {
			setLocationRelativeTo(parent);
		}
		this.jfritz = jfritz;
		initDialog();
	}

	public TelnetConfigDialog(JDialog parent, JFritz jfritz) {
		super(parent, true);
		if (parent != null) {
			setLocationRelativeTo(parent);
		}
		this.jfritz = jfritz;
		initDialog();
	}
	public void initDialog() {
		setTitle("Telnet - Einstellungen");
		setSize(270, 140);
		drawDialog();
		setProperties();
	}

	private void setProperties() {
		userNameTextfield.setText(JFritz.getProperty("telnet.user", ""));
		if (JFritz.getProperty("telnet.password").equals("")) {
			passwordTextfield.setText("");
		}
		else {
			passwordTextfield.setText(Encryption.decrypt(JFritz.getProperty("telnet.password","")));
		}
	}

	private void storeProperties() {
		JFritz.setProperty("telnet.user",userNameTextfield.getText());
		JFritz.setProperty("telnet.password",Encryption.encrypt(new String(passwordTextfield.getPassword())));
	}

	public int showTelnetConfigDialog() {
		super.show();
		return exitCode;
	}

	private void drawDialog() {
		KeyListener keyListener = (new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				// Cancel
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE
						|| (e.getSource() == cancelButton && e.getKeyCode() == KeyEvent.VK_ENTER)) {
					exitCode = CANCEL_OPTION;
					setVisible(false);
				}
				// OK
				if (e.getSource() == okButton
						&& e.getKeyCode() == KeyEvent.VK_ENTER) {
					storeProperties();
					exitCode = APPROVE_OPTION;
					setVisible(false);
				}
			}
		});
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				if (source == passwordTextfield || source == okButton) {
					//OK
					exitCode = APPROVE_OPTION;
					storeProperties();
				}
				else if (source == cancelButton) {
					exitCode = CANCEL_OPTION;
				}
				// Close Window
				if (source == passwordTextfield || source == okButton
						|| source == cancelButton) {
					setVisible(false);
				}
			}
		};

		JPanel telnetPanel = new JPanel();
		telnetPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		c.gridwidth = 1;
		c.gridy = 0;
		JLabel telnetUserLabel = new JLabel("Telnet user: ");
		telnetPanel.add(telnetUserLabel, c);
		userNameTextfield = new JTextField("", 12);
		userNameTextfield.addKeyListener(keyListener);
		telnetPanel.add(userNameTextfield, c);

		c.gridy = 1;
		JLabel telnetPasswordLabel = new JLabel("Telnet password: ");
		telnetPanel.add(telnetPasswordLabel, c);
		passwordTextfield = new JPasswordField("", 12);
		passwordTextfield.addKeyListener(keyListener);
		telnetPanel.add(passwordTextfield, c);

		JPanel buttonPanel = new JPanel();
		okButton = new JButton(JFritz.getMessage("okay"));
		okButton.setActionCommand("ok_pressed");
		okButton.addActionListener(actionListener);
		okButton.addKeyListener(keyListener);

		cancelButton = new JButton(JFritz.getMessage("cancel"));
		cancelButton.setActionCommand("cancel_pressed");
		cancelButton.addActionListener(actionListener);
		cancelButton.addKeyListener(keyListener);

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		getContentPane().add(telnetPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}
}
