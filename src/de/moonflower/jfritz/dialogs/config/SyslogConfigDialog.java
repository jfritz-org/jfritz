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
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.network.SyslogListener;
import de.moonflower.jfritz.utils.network.Telnet;

/**
 * Config dialog for Syslog-Callmonitor
 * Lets enable/disable check for syslogd and telefond
 *
 * @author rob
 *
 */
public class SyslogConfigDialog extends JDialog implements CallMonitorConfigDialog {

	/**
	 * This avoid compiler warnings
	 * I don't know what it's for yet
	 */
	private static final long serialVersionUID = 1262373999715869093L;

	private JFritz jfritz;

	private JCheckBox checkSyslog;

	private JCheckBox checkTelefon;

	private JButton okButton, cancelButton;

	private int exitCode = 0;

	private JComboBox ipAddressComboBox;

	private JCheckBox syslogPassthroughCheckBox;

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
		setSize(270, 300);
		drawDialog();
		setProperties();
	}

	public void setIP(String ip) {
	}

	private void setProperties() {
		checkSyslog.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
				"syslog.checkSyslog", "true")));
		checkTelefon.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
				"syslog.checkTelefon", "true")));
		ipAddressComboBox.setSelectedItem(JFritz.getProperty(
				"option.syslogclientip", "192.168.178.21"));
		syslogPassthroughCheckBox.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.syslogpassthrough", "false")));
		// Anhand von Problemen mit dem Passthrough ist diese Checkbox erst
		// einmal deaktiviert
		syslogPassthroughCheckBox.setEnabled(false);
	}

	private void storeProperties() {
		JFritz.setProperty("syslog.checkSyslog", Boolean.toString(checkSyslog
				.isSelected()));
		JFritz.setProperty("syslog.checkTelefon", Boolean.toString(checkTelefon
				.isSelected()));
		JFritz.setProperty("option.syslogclientip", ipAddressComboBox
				.getSelectedItem().toString());
		JFritz.setProperty("option.syslogpassthrough", Boolean
				.toString(syslogPassthroughCheckBox.isSelected()));
	}

	public int showConfigDialog() {
//		super.show();
		super.setVisible(true);
		return exitCode;
	}

	private void drawDialog() {
	    this.setModal(true);
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
				if (e.getActionCommand().equals("restartSyslog")) {
					Telnet telnet = new Telnet(jfritz);
					telnet.connect();
					if (telnet.isConnected()) {
						SyslogListener.restartSyslogOnFritzBox(telnet, JFritz
								.getProperty("option.syslogclientip",
										"192.168.178.21"));
						telnet.disconnect();
						JFritz.infoMsg("Syslogd erfolgreich gestartet");
						Debug.msg("Syslogd restarted successfully");
					}
					else {
						JFritz.infoMsg("Fehler beim Verbinden mit Telnet");
						Debug.msg("Fehler beim Verbinden mit Telnet");
					}
				}
				if (e.getActionCommand().equals("restartTelefon")) {
					Telnet telnet = new Telnet(jfritz);
					telnet.connect();
					if (telnet.isConnected()) {
						if (SyslogListener.restartTelefonOnFritzBox(telnet, jfritz) == JOptionPane.YES_OPTION) {
							JFritz.infoMsg("Telefond erfolgreich gestartet");
							Debug.msg("Telefond restarted successfully");
						} else {
							JFritz.infoMsg("Telefond nicht gestartet");
							Debug.msg("Telefond not restarted");
						}
						telnet.disconnect();
					}
					else {
						JFritz.infoMsg("Fehler beim Verbinden mit Telnet");
						Debug.msg("Fehler beim Verbinden mit Telnet");
					}
				}
			}
		};

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		c.gridx = 0;
		c.gridy = 1;
		JLabel ipAddressLabel = new JLabel("Lokale IP-Adresse: ");
		panel.add(ipAddressLabel, c);

		c.gridx = 1;
		c.gridy = 1;
		ipAddressComboBox = new JComboBox();
		Vector ipAddresses = new Vector();
		ipAddresses = SyslogListener.getIP();
		Enumeration en = ipAddresses.elements();
		while (en.hasMoreElements()) {
			InetAddress ad = (InetAddress) en.nextElement();
			ipAddressComboBox.addItem(ad.toString().substring(1,
					ad.toString().length()));

		}

		ipAddressComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Debug.msg(ipAddressComboBox.getSelectedItem().toString());
				JFritz.setProperty("option.syslogclientip", ipAddressComboBox
						.getSelectedItem().toString());
			}
		});
		panel.add(ipAddressComboBox, c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		syslogPassthroughCheckBox = new JCheckBox("Syslog-passthrough?");
		panel.add(syslogPassthroughCheckBox, c);

		c.gridy = 3;
		checkSyslog = new JCheckBox("Überprüfe syslogd");
		checkSyslog
				.setToolTipText("Überprüft mittels Telnet, ob der Syslog-Daemon"
						+ " auf der FritzBox richtig läuft"
						+ " und startet ihn gegebenenfalls neu.");
		panel.add(checkSyslog, c);

		c.gridy = 4;
		checkTelefon = new JCheckBox("Überprüfe telefond");
		checkTelefon
		.setToolTipText("Überprüft mittels Telnet, ob der Telefon-Daemon"
				+ " auf der FritzBox richtig läuft"
				+ " und startet ihn gegebenenfalls neu.");
		panel.add(checkTelefon, c);

		c.gridy = 5;
		JButton restartSyslog = new JButton("Restart Syslogd on FritzBox");
		restartSyslog.setActionCommand("restartSyslog");
		restartSyslog.addActionListener(actionListener);
		restartSyslog
		.setToolTipText("Startet den sylsog-Dienst auf der FritzBox neu.");
		panel.add(restartSyslog, c);

		c.gridy = 6;
		JButton restartTelefon = new JButton("Restart Telefond on FritzBox");
		restartTelefon.setActionCommand("restartTelefon");
		restartTelefon.addActionListener(actionListener);
		restartTelefon
		.setToolTipText("Startet den telefon-Dienst auf der FritzBox neu.");
		panel.add(restartTelefon, c);

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
