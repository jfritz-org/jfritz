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
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.NoticeDialog;
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

	private JTextField number;

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
		NoticeDialog info = new NoticeDialog(
				jfritz,"legalInfo.telephoneCharges", //$NON-NLS-1$
				JFritz.getMessage("telefonCharges_Warning")); //$NON-NLS-1$

		info.setVisible(true);
		info.dispose();
		if (info.isAccepted()) {
			super.dialogInit();
			setTitle(JFritz.getMessage("call")); //$NON-NLS-1$
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

			JLabel label = new JLabel(JFritz.getMessage("number")+": "); //$NON-NLS-1$,  //$NON-NLS-2$
			topPane.add(label, c);

			//make the number editable
			number = new JTextField( ((PhoneNumber) numbers.elementAt(0)).getShortNumber());
			c.fill = GridBagConstraints.REMAINDER;
			topPane.add(number, c);

			c.gridy = 2;
			label = new JLabel(JFritz.getMessage("extension")+": "); //$NON-NLS-1$,  //$NON-NLS-2$
			topPane.add(label, c);

			port = new JComboBox();
			port.addItem("Fon 1"); //$NON-NLS-1$
			if (firmware != null) {
				switch (firmware.getBoxType()) {
					case FritzBoxFirmware.BOXTYPE_FRITZBOX_FON :
						port.addItem("Fon 2"); //$NON-NLS-1$
						break;
					case FritzBoxFirmware.BOXTYPE_FRITZBOX_FON_WLAN :
						// ggf. kann dies auch für die anderen Boxen gelten?
						port.addItem("Fon 2"); //$NON-NLS-1$
						port.addItem(JFritz.getMessage("analog_telephones_all"));  //$NON-NLS-1$
						break;
					case FritzBoxFirmware.BOXTYPE_FRITZBOX_ATA :
						port.addItem("Fon 2"); //$NON-NLS-1$
						break;
					case FritzBoxFirmware.BOXTYPE_FRITZBOX_5010:
						// die 5010 hat nur einen analogen Anschluss
						break;
					case FritzBoxFirmware.BOXTYPE_FRITZBOX_5050:
					case FritzBoxFirmware.BOXTYPE_FRITZBOX_7050:
					case FritzBoxFirmware.BOXTYPE_FRITZBOX_7170:
						 {
							 port.addItem("Fon 2"); //$NON-NLS-1$
							 port.addItem("Fon 3"); //$NON-NLS-1$
						 }
					case FritzBoxFirmware.BOXTYPE_FRITZBOX_5012:
						 {
							 port.addItem("ISDN Alle"); //$NON-NLS-1$
							 port.addItem("ISDN 1"); //$NON-NLS-1$
							 port.addItem("ISDN 2"); //$NON-NLS-1$
							 port.addItem("ISDN 3"); //$NON-NLS-1$
							 port.addItem("ISDN 4"); //$NON-NLS-1$
							 port.addItem("ISDN 5"); //$NON-NLS-1$
							 port.addItem("ISDN 6"); //$NON-NLS-1$
							 port.addItem("ISDN 7"); //$NON-NLS-1$
							 port.addItem("ISDN 8"); //$NON-NLS-1$
							 port.addItem("ISDN 9"); //$NON-NLS-1$
							 break;
						 }
					}
			}
			topPane.add(port, c);

			// Bottom Pane
			okButton = new JButton(JFritz.getMessage("call")); //$NON-NLS-1$
			okButton.setActionCommand("call"); //$NON-NLS-1$
			okButton.addActionListener(this);

			cancelButton = new JButton(JFritz.getMessage("cancel")); //$NON-NLS-1$
			cancelButton.setActionCommand("close"); //$NON-NLS-1$
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
		if (e.getActionCommand().equals("call")) { //$NON-NLS-1$

			if(!number.getText().equals(""))
				jfritz.getFritzBox().doCall(number.getText(), port.getSelectedItem().toString());

			setVisible(false);
		} else if (e.getActionCommand().equals("close")) { //$NON-NLS-1$
			setVisible(false);
		}
	}
}
