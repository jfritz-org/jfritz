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
import javax.swing.JCheckBox;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * Config dialog for Syslog-Callmonitor
 * Lets enable/disable check for syslogd and telefond
 *
 * @author rob
 *
 */
public class SyslogConfigDialog extends JDialog {

	private JFritz jfritz;

	private JLabel userNameLabel;

	private JCheckBox checkSyslog;

	private JCheckBox checkTelefon;

	private JLabel passwordLabel;

	private JButton okButton, cancelButton;

	private int exitCode = 0;

	public static final int APPROVE_OPTION = 1;

	public static final int CANCEL_OPTION = 2;

	public SyslogConfigDialog(JFrame parent, JFritz jfritz) {
		super(parent, true);
		if (parent != null) {
			setLocationRelativeTo(parent);
		}
		this.jfritz = jfritz;
		initDialog();
	}

	public SyslogConfigDialog(JDialog parent, JFritz jfritz) {
		super(parent, true);
		if (parent != null) {
			setLocationRelativeTo(parent);
		}
		this.jfritz = jfritz;
		initDialog();
	}

	public void initDialog() {
		setTitle("Syslog - Einstellungen");
		setSize(250, 170);
		drawDialog();
		setProperties();
	}

	private void setProperties() {
		checkSyslog.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
				"syslog.checkSyslog", "true")));
		checkTelefon.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
				"syslog.checkTelefon", "true")));
	}

	private void storeProperties() {
		JFritz.setProperty("syslog.checkSyslog", Boolean.toString(checkSyslog
				.isSelected()));
		JFritz.setProperty("syslog.checkTelefon", Boolean.toString(checkTelefon
				.isSelected()));
	}

	public int showSyslogConfigDialog() {
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
				if (source == okButton) {
					//OK
					exitCode = APPROVE_OPTION;
					storeProperties();
				} else if (source == cancelButton) {
					exitCode = CANCEL_OPTION;
				}
				// Close Window
				if (source == okButton || source == cancelButton) {
					setVisible(false);
				}
			}
		};

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		c.gridwidth = 1;
		c.gridy = 0;
		checkSyslog = new JCheckBox("Überprüfe syslogd *");
		checkSyslog
				.setToolTipText("Überprüft mittels Telnet, ob der Syslog-Daemon"
						+ " auf der FritzBox richtig läuft"
						+ " und startet ihn gegebenenfalls neu.");
		panel.add(checkSyslog, c);

		c.gridy = 1;
		checkTelefon = new JCheckBox("Überprüfe telefond *");
		checkTelefon
		.setToolTipText("Überprüft mittels Telnet, ob der Telefon-Daemon"
				+ " auf der FritzBox richtig läuft"
				+ " und startet ihn gegebenenfalls neu.");
		panel.add(checkTelefon, c);


		c.gridy = 2;
		JLabel infoLabel = new JLabel("*: Benötigt Telnet auf der FritzBox");
		panel.add(infoLabel, c);

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

		getContentPane().add(panel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}
}
