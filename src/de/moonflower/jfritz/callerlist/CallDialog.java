/*
 * Created on 03.06.2005
 *
 */
package de.moonflower.jfritz.callerlist;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.InfoDialog;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * @author Robert Palmer
 *
 */
public class CallDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1;

	private Vector numbers;

	private JFritz jfritz;

	private JComboBox port;

	private FritzBoxFirmware firmware = null;

	JButton okButton, cancelButton;

	private Object cboNumber;

	private PhoneNumber defaultNumber;

	/**
	 *
	 * @param jfritz
	 *            JFritz object
	 * @param number
	 *            PhoneNumber object
	 * @throws HeadlessException
	 */
	public CallDialog(JFritz jfritz, Vector numbers, PhoneNumber defaultNumber)
			throws HeadlessException {
		super();
		this.jfritz = jfritz;
		// this.setLocationRelativeTo(jfritz.getJframe());
		this.setLocation(jfritz.getJframe().getX() + 80, jfritz.getJframe()
				.getY() + 100);
		this.numbers = numbers;
		this.defaultNumber = defaultNumber;
		drawDialog();
	}

	/**
	 *
	 * @param jfritz
	 *            JFritz object
	 * @param number
	 *            PhoneNumber object
	 * @throws HeadlessException
	 */
	public CallDialog(JFritz jfritz, PhoneNumber number)
			throws HeadlessException {
		super();
		this.jfritz = jfritz;
		// this.setLocationRelativeTo(jfritz.getJframe());
		this.setLocation(jfritz.getJframe().getX() + 80, jfritz.getJframe()
				.getY() + 100);
		Vector v = new Vector();
		v.addElement(number);
		this.numbers = v;
		drawDialog();
	}

	private void drawDialog() {
		InfoDialog info = new InfoDialog(
				jfritz,"legalInfo.telephoneCharges",
				JFritz.getMessage("telefonCharges_Warning"));
		info.setVisible(true);
		info.dispose();
		if (info.isAccepted()) {
			super.dialogInit();
			setTitle(JFritz.getMessage("call"));
			// this.setAlwaysOnTop(true); //erst ab Java V.5.0 möglich
			setModal(true);
			getContentPane().setLayout(new BorderLayout());

			JPanel topPane = new JPanel();
			JPanel bottomPane = new JPanel();

			// Top Pane
			topPane.setLayout(new GridBagLayout());
			topPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
			GridBagConstraints c = new GridBagConstraints();
			c.insets.top = 5;
			c.insets.bottom = 5;
			c.insets.left = 5;
			c.anchor = GridBagConstraints.WEST;

			c.gridy = 1;
			JLabel label = new JLabel(JFritz.getMessage("number")+": ");
			topPane.add(label, c);
			if (this.numbers.size() == 1) {
				cboNumber = new JLabel(((PhoneNumber) numbers.elementAt(0))
						.getShortNumber());
			} else {
				cboNumber = new JComboBox();
				for (int i = 0; i < this.numbers.size(); i++) {
					((JComboBox) cboNumber).addItem(((PhoneNumber) numbers
							.elementAt(i)).getShortNumber());
				}
				((JComboBox) cboNumber).setSelectedItem(this.defaultNumber
						.getShortNumber());
			}
			topPane.add((Component) cboNumber, c);
			c.gridy = 2;
			label = new JLabel(JFritz.getMessage("extension")+": ");
			topPane.add(label, c);

			boolean isdone = false;
			int connectionFailures = 0;
			while (!isdone) {
				try {
					firmware = JFritzUtils.detectBoxType(JFritz
							.getProperty("box.firmware"), JFritz
							.getProperty("box.address"), Encryption
							.decrypt(JFritz.getProperty("box.password")));
					isdone = true;
				} catch (WrongPasswordException e) {
					jfritz.getJframe().setStatus(
							JFritz.getMessage("password_wrong"));
					String password = jfritz.getJframe().showPasswordDialog(
							Encryption.decrypt(JFritz.getProperty(
									"box.password", "")));
					if (password == null) { // Dialog canceled
						isdone = true;
					} else {
						JFritz.setProperty("box.password", Encryption
								.encrypt(password));
					}
				} catch (IOException e) {
					// Warten, falls wir von einem Standby aufwachen,
					// oder das Netzwerk temporär nicht erreichbar ist.
					if (connectionFailures < 5) {
						Debug.msg("Waiting for FritzBox, retrying ...");
						connectionFailures++;
					} else {
						Debug.msg("Callerlist Box not found");
						String box_address = jfritz.getJframe()
								.showAddressDialog(
										JFritz.getProperty("box.address",
												"fritz.box"));
						if (box_address == null) { // Dialog canceled
							isdone = true;
						} else {
							JFritz.setProperty("box.address", box_address);
						}
					}
				}
			}

			port = new JComboBox();
			port.addItem("Fon 1");
			port.addItem("Fon 2");
			if (firmware != null) {
				switch (firmware.getBoxType()) {
					case FritzBoxFirmware.BOXTYPE_FRITZBOX_FON :
						break;
					case FritzBoxFirmware.BOXTYPE_FRITZBOX_FON_WLAN :
						port.addItem("Analog Alle"); // ggf. kann dies auch
														// für die anderen Boxen
														// gelten?
						break;
					case FritzBoxFirmware.BOXTYPE_FRITZBOX_ATA :
						break;
					case FritzBoxFirmware.BOXTYPE_FRITZBOX_5050 : {
						port.addItem("ISDN Alle");
						port.addItem("ISDN 1");
						port.addItem("ISDN 2");
						port.addItem("ISDN 3");
						port.addItem("ISDN 4");
						port.addItem("ISDN 5");
						port.addItem("ISDN 6");
						port.addItem("ISDN 7");
						port.addItem("ISDN 8");
						port.addItem("ISDN 9");
						break;
					}
					case FritzBoxFirmware.BOXTYPE_FRITZBOX_7050 : {
						port.addItem("ISDN Alle");
						port.addItem("ISDN 1");
						port.addItem("ISDN 2");
						port.addItem("ISDN 3");
						port.addItem("ISDN 4");
						port.addItem("ISDN 5");
						port.addItem("ISDN 6");
						port.addItem("ISDN 7");
						port.addItem("ISDN 8");
						port.addItem("ISDN 9");
					}
				}
			}
			topPane.add(port, c);

			// Bottom Pane
			okButton = new JButton(JFritz.getMessage("call"));
			okButton.setActionCommand("call");
			okButton.addActionListener(this);

			cancelButton = new JButton(JFritz.getMessage("cancel"));
			cancelButton.setActionCommand("close");
			cancelButton.addActionListener(this);

			bottomPane.add(okButton);
			bottomPane.add(cancelButton);

			getContentPane().add(topPane, BorderLayout.NORTH);
			getContentPane().add(bottomPane, BorderLayout.SOUTH);
			setSize(new Dimension(300, 150));

		}

	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("call")) {
			if (cboNumber.getClass().toString().equals(
					"class javax.swing.JLabel"))
				JFritzUtils.doCall(((JLabel) cboNumber).getText(), port
						.getSelectedItem().toString(), firmware);
			if (cboNumber.getClass().toString().equals(
					"class javax.swing.JComboBox"))
				JFritzUtils.doCall(((JComboBox) cboNumber).getSelectedItem()
						.toString(), port.getSelectedItem().toString(),
						firmware);
			setVisible(false);
		} else if (e.getActionCommand().equals("close")) {
			setVisible(false);
		}
	}
}
