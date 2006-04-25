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
		setTitle(JFritz.getMessage("dialog_title_syslog_options")); //$NON-NLS-1$
		setSize(270, 300);
		drawDialog();
		setProperties();
	}

	public void setIP(String ip) {
	}

	private void setProperties() {
		checkSyslog.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
				"syslog.checkSyslog", "true")));//$NON-NLS-1$,  //$NON-NLS-2$
		checkTelefon.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
				"syslog.checkTelefon", "true")));//$NON-NLS-1$,  //$NON-NLS-2$
		ipAddressComboBox.setSelectedItem(JFritz.getProperty(
				"option.syslogclientip", "192.168.178.21"));//$NON-NLS-1$,  //$NON-NLS-2$
		syslogPassthroughCheckBox.setSelected(JFritzUtils.parseBoolean(JFritz
				.getProperty("option.syslogpassthrough", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		// Anhand von Problemen mit dem Passthrough ist diese Checkbox erst
		// einmal deaktiviert
		syslogPassthroughCheckBox.setEnabled(false);
	}

	private void storeProperties() {
		JFritz.setProperty("syslog.checkSyslog", Boolean.toString(checkSyslog //$NON-NLS-1$
				.isSelected()));
		JFritz.setProperty("syslog.checkTelefon", Boolean.toString(checkTelefon //$NON-NLS-1$
				.isSelected()));
		JFritz.setProperty("option.syslogclientip", ipAddressComboBox //$NON-NLS-1$
				.getSelectedItem().toString());
		JFritz.setProperty("option.syslogpassthrough", Boolean //$NON-NLS-1$
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
				if (e.getActionCommand().equals("restartSyslog")) { //$NON-NLS-1$
					Telnet telnet = new Telnet(jfritz);
					telnet.connect();
					if (telnet.isConnected()) {
						SyslogListener.restartSyslogOnFritzBox(telnet, JFritz
								.getProperty("option.syslogclientip", //$NON-NLS-1$
										"192.168.178.21")); //$NON-NLS-1$
						telnet.disconnect();
						JFritz.infoMsg("Syslogd erfolgreich gestartet"); //$NON-NLS-1$
						Debug.msg("Syslogd restarted successfully"); //$NON-NLS-1$
					}
					else {
						JFritz.infoMsg("Fehler beim Verbinden mit Telnet"); //$NON-NLS-1$
						Debug.msg("Fehler beim Verbinden mit Telnet"); //$NON-NLS-1$
					}
				}
				if (e.getActionCommand().equals("restartTelefon")) { //$NON-NLS-1$
					Telnet telnet = new Telnet(jfritz);
					telnet.connect();
					if (telnet.isConnected()) {
						if (SyslogListener.restartTelefonOnFritzBox(telnet, jfritz) == JOptionPane.YES_OPTION) {
							JFritz.infoMsg(JFritz.getMessage("telefond_restart_successfully")); //$NON-NLS-1$
							Debug.msg("Telefond restarted successfully"); //$NON-NLS-1$
						} else {
							JFritz.infoMsg(JFritz.getMessage("telefond_restart_failed")); //$NON-NLS-1$
							Debug.msg("Telefond not restarted"); //$NON-NLS-1$
						}
						telnet.disconnect();
					}
					else {
						JFritz.infoMsg(JFritz.getMessage("telnet_connection_error")); //$NON-NLS-1$
						Debug.msg("Connection failure"); //$NON-NLS-1$
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
		JLabel ipAddressLabel = new JLabel(JFritz.getMessage("local_ip")+": "); //$NON-NLS-1$
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
				JFritz.setProperty("option.syslogclientip", ipAddressComboBox //$NON-NLS-1$
						.getSelectedItem().toString());
			}
		});
		panel.add(ipAddressComboBox, c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		syslogPassthroughCheckBox = new JCheckBox(JFritz.getMessage("syslog_passthrough"+"?")); //$NON-NLS-1$
		panel.add(syslogPassthroughCheckBox, c);

		c.gridy = 3;
		checkSyslog = new JCheckBox(JFritz.getMessage("check_syslogd")); //$NON-NLS-1$
		checkSyslog
				.setToolTipText(JFritz.getMessage("check_syslogd_desc")); //$NON-NLS-1$
		panel.add(checkSyslog, c);

		c.gridy = 4;
		checkTelefon = new JCheckBox(JFritz.getMessage("check_telefond")); //$NON-NLS-1$
		checkTelefon
		.setToolTipText(JFritz.getMessage("check_telefond_desc")); //$NON-NLS-1$
		panel.add(checkTelefon, c);

		c.gridy = 5;
		JButton restartSyslog = new JButton(JFritz.getMessage("restart_syslogd")); //$NON-NLS-1$
		restartSyslog.setActionCommand("restartSyslog"); //$NON-NLS-1$
		restartSyslog.addActionListener(actionListener);
		restartSyslog
		.setToolTipText(JFritz.getMessage("restart_syslogd_desc")); //$NON-NLS-1$
		panel.add(restartSyslog, c);

		c.gridy = 6;
		JButton restartTelefon = new JButton(JFritz.getMessage("restart_telefond")); //$NON-NLS-1$
		restartTelefon.setActionCommand("restartTelefon"); //$NON-NLS-1$
		restartTelefon.addActionListener(actionListener);
		restartTelefon
		.setToolTipText(JFritz.getMessage("restart_telefond_desc")); //$NON-NLS-1$
		panel.add(restartTelefon, c);

		JPanel buttonPanel = new JPanel();
		okButton = new JButton(JFritz.getMessage("okay")); //$NON-NLS-1$
		okButton.setActionCommand("ok_pressed"); //$NON-NLS-1$
		okButton.addActionListener(actionListener);
		okButton.addKeyListener(keyListener);

		cancelButton = new JButton(JFritz.getMessage("cancel")); //$NON-NLS-1$
		cancelButton.setActionCommand("cancel_pressed"); //$NON-NLS-1$
		cancelButton.addActionListener(actionListener);
		cancelButton.addKeyListener(keyListener);

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		getContentPane().add(panel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}
}
