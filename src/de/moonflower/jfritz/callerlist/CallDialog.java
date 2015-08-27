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
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.network.NetworkStateMonitor;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.Port;
import de.moonflower.jfritz.utils.CallPendingDialog;
import de.moonflower.jfritz.utils.ComplexJOptionPaneMessage;
import de.moonflower.jfritz.utils.Debug;

/**
 * @author Robert Palmer
 *
 */
public class CallDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1;

	private Vector<PhoneNumberOld> numbers;

	private JComboBox portComboBox;

	JButton okButton, cancelButton;

	private Object cboNumber;
	private PhoneNumberOld defaultNumber;

	private PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	/**
	 * This Constructor initializes the CallDialog with a set of numbers and a default number.
	 * If Vector contains more than one number CallDialog will present the numbers of the vector in an editable JComboBox with the defaultNumber selected as default.
	 * @param numbers
	 *            Vector of PhoneNumbers (e.g. from Person.getNumbers())
	 * @param defaultNumber
	 *            String containig the default number to select in JComboBox
	 * @throws HeadlessException
	 */
	public CallDialog(Vector<PhoneNumberOld> numbers, PhoneNumberOld defaultNumber)
			throws HeadlessException {
		super(JFritz.getJframe()); //sets icon to JFritz' one
		//this.setLocation(JFritz.getJframe().getX() + 80, JFritz.getJframe().getY() + 100);
		this.numbers = numbers;
		this.defaultNumber = defaultNumber;
		drawDialog();
		this.setLocationRelativeTo(JFritz.getJframe());
	}

	/**
	 * This Constructor initializes the CallDialog with a one number and sets this to the defaultNumber.
	 * Due to having only one number in Vector CallDialog will present this number in an editable JTextField.
	 * @param number
	 *            PhoneNumber object
	 * @throws HeadlessException
	 */
	public CallDialog(PhoneNumberOld number)
			throws HeadlessException {
		super(JFritz.getJframe()); //sets icon to JFritz' one
		// this.setLocationRelativeTo(JFritz.getJframe());
		//this.setLocation(JFritz.getJframe().getX() + 80, JFritz.getJframe().getY() + 100);
		Vector<PhoneNumberOld> v = new Vector<PhoneNumberOld>();
		v.addElement(number);
		this.numbers = v;
        this.defaultNumber = number; //does not really need, but to be complete
		drawDialog();
		this.setLocationRelativeTo(JFritz.getJframe());
	}

	private void drawDialog() {
		int answer = JOptionPane.YES_OPTION;
		ComplexJOptionPaneMessage msg = new ComplexJOptionPaneMessage(
                "legalInfo.telephoneCharges", //$NON-NLS-1$
				messages.getMessage("telefonCharges_Warning")); //$NON-NLS-1$

		if (msg.showDialogEnabled()) {
			answer = JOptionPane.showConfirmDialog(null,
					msg.getComponents(),
					messages.getMessage("information"), JOptionPane.YES_NO_OPTION);
			if (answer == JOptionPane.YES_OPTION) {
				msg.saveProperty();
				properties.saveStateProperties();
			}
		}

		if (answer == JOptionPane.YES_OPTION) {
			super.dialogInit();
			setTitle(messages.getMessage("call")); //$NON-NLS-1$
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

			JLabel label = new JLabel(messages.getMessage("number")+": "); //$NON-NLS-1$,  //$NON-NLS-2$
			topPane.add(label, c);

			//make the number editable
			if (this.numbers.size() == 1) { // if only one number -> use editable JTextField
				cboNumber = new JTextField((numbers.elementAt(0)).getAreaNumber());
				((JTextField)cboNumber).setPreferredSize(new Dimension(230, 32));
			} else {// if more then one number -> use editable JComboBox
				cboNumber = new JComboBox();
				for (int i = 0; i < this.numbers.size(); i++) {
					((JComboBox) cboNumber).addItem((numbers
							.elementAt(i)).getAreaNumber());
				}

				//choose defaultNumber as initial value of JComboBox
				((JComboBox) cboNumber).setSelectedItem(this.defaultNumber
						.getAreaNumber());
				((JComboBox) cboNumber).setEditable(true);
			}
			topPane.add((Component) cboNumber, c);
			c.gridy = 2;
			label = new JLabel(messages.getMessage("extension")+": "); //$NON-NLS-1$,  //$NON-NLS-2$
			topPane.add(label, c);

			portComboBox = new JComboBox();

			Vector<Port> ports = NetworkStateMonitor.getAvailablePorts();

			//make sure the firmware was correctly detected
			if(ports != null){
				for(int i=0; i < ports.size(); i++)
				{
					if ((!"".equals(ports.get(i).getDialPort()))
						&& (!"-1".equals(ports.get(i).getDialPort())))
					{
						portComboBox.addItem(ports.get(i));
					}
				}
			}

            portComboBox.setPreferredSize(new Dimension(230, 32));
			topPane.add(portComboBox, c);

			// Bottom Pane
			okButton = new JButton(messages.getMessage("call")); //$NON-NLS-1$
			okButton.setActionCommand("call"); //$NON-NLS-1$
			okButton.addActionListener(this);

			cancelButton = new JButton(messages.getMessage("cancel")); //$NON-NLS-1$
			cancelButton.setActionCommand("close"); //$NON-NLS-1$
			cancelButton.addActionListener(this);

			bottomPane.add(okButton);
			bottomPane.add(cancelButton);

			//set default confirm button (Enter)
			getRootPane().setDefaultButton(okButton);

			//set default close button (ESC)
			KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
			Action escapeAction = new AbstractAction()
			{
				private static final long serialVersionUID = 3L;

				public void actionPerformed(ActionEvent e)
				{
					 cancelButton.doClick();
				}
			};
			getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE"); //$NON-NLS-1$
			getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$

			if (portComboBox.getItemCount() > 0)
			{
				try {
					int lastPort = Integer.parseInt(properties.getStateProperty("calldialog.lastport"));
					if (portComboBox.getItemCount() > lastPort)
					{
						portComboBox.setSelectedIndex(lastPort);
					}
					else
					{
						portComboBox.setSelectedIndex(0);
					}
				} catch (NumberFormatException nfe)
				{
					portComboBox.setSelectedIndex(0);
				}
			}

			getContentPane().add(topPane, BorderLayout.NORTH);
			getContentPane().add(bottomPane, BorderLayout.SOUTH);
			setSize(new Dimension(400, 170));

		}

	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("call")) { //$NON-NLS-1$
			try {
				Port port = (Port) portComboBox.getSelectedItem();

				properties.setStateProperty("calldialog.lastport", Integer.toString(portComboBox.getSelectedIndex()));

				if (cboNumber.getClass().toString().equals(
						"class javax.swing.JTextField")) //$NON-NLS-1$
				{
					NetworkStateMonitor.doCall(((JTextField) cboNumber).getText(),
							(Port)portComboBox.getSelectedItem());
				}
				if (cboNumber.getClass().toString().equals(
						"class javax.swing.JComboBox")) //$NON-NLS-1$
				{
					NetworkStateMonitor.doCall(((JComboBox) cboNumber).getSelectedItem().toString(),
							(Port)portComboBox.getSelectedItem());
				}

				setVisible(false);

				//popup new dialog with the possibility to cancel this call
				String text = messages.getMessage("calldialog_pending_call");
				if (this.numbers.size() == 1) { // if only one number -> use editable JTextField
					text = text.replaceAll("%NUMBER", ((JTextField) cboNumber).getText());
				} else {// if more then one number -> use editable JComboBox
					text = text.replaceAll("%NUMBER", ((JComboBox) cboNumber).getSelectedItem().toString());
				}

				CallPendingDialog cancelDialog = new CallPendingDialog(text, port);
				cancelDialog.setVisible(true);
				cancelDialog.dispose();
			} catch (WrongPasswordException e1) {
				JFritz.errorMsg(messages.getMessage("box.wrong_password")); //$NON-NLS-1$
				Debug.errDlg(messages.getMessage("box.wrong_password"), e1); //$NON-NLS-1$
			} catch (IOException e1) {
				JFritz.errorMsg(messages.getMessage("box.not_found")); //$NON-NLS-1$
				Debug.errDlg(messages.getMessage("box.not_found"), e1); //$NON-NLS-1$
			}
		} else if (e.getActionCommand().equals("close")) { //$NON-NLS-1$
			properties.setStateProperty("calldialog.lastport", Integer.toString(portComboBox.getSelectedIndex()));
			setVisible(false);
		}
	}
}
