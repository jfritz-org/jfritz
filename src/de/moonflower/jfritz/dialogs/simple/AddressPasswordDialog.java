/*
 *
 * Password dialog box
 */

package de.moonflower.jfritz.dialogs.simple;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import de.moonflower.jfritz.Main;

/**
 * Simple dialog for Fritz!Box address and password input.
 *
 * @author Arno Willig
 */
public class AddressPasswordDialog extends JDialog {
	private static final long serialVersionUID = 1;

	protected JTextField address;

	protected JPasswordField pass;

	protected JButton okButton;

	protected JButton cancelButton;

	protected JLabel passLabel, addressLabel;

	private boolean pressed_OK = false;

	private boolean isPasswordDlg;

	public AddressPasswordDialog(Frame parent, String title) {
		super(parent, title);
	    addWindowListener(new WindowCloseHandle(this));
	}

	public AddressPasswordDialog(Frame parent, boolean isPasswordDlg) {
		super(parent, true);
	    addWindowListener(new WindowCloseHandle(this));
		this.isPasswordDlg = isPasswordDlg;
		drawDialog();
		if (parent != null) {
			setLocationRelativeTo(parent);
		} else {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int top = (screenSize.height - getHeight()) / 2;
			int left = (screenSize.width - getWidth()) / 2;
			setLocation(left, top);
		}
	}

	public String getPass() {
		return String.valueOf(pass.getPassword());
	}

	public String getAddress() {
		return address.getText();
	}

	public boolean okPressed() {
		return pressed_OK;
	}

	public void setPass(String pass) {
		this.pass.setText(pass);
	}

	public void setAddress(String address) {
		this.address.setText(address);
	}

	protected void dialogInit() {
	}

	protected void drawDialog() {
		pass = new JPasswordField("", 20); //$NON-NLS-1$
		address = new JTextField("", 20); //$NON-NLS-1$
		okButton = new JButton(Main.getMessage("okay")); //$NON-NLS-1$
		cancelButton = new JButton(Main.getMessage("cancel")); //$NON-NLS-1$
		passLabel = new JLabel(Main.getMessage("password") + ": "); //$NON-NLS-1$
		addressLabel = new JLabel(Main.getMessage("ip_address") + ": "); //$NON-NLS-1$

		super.dialogInit();

		KeyListener keyListener = (new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE
						|| (e.getSource() == cancelButton && e.getKeyCode() == KeyEvent.VK_ENTER)) {
					pressed_OK = false;
					AddressPasswordDialog.this.setVisible(false);
				}
				if (e.getSource() == okButton
						&& e.getKeyCode() == KeyEvent.VK_ENTER) {
					pressed_OK = true;
					AddressPasswordDialog.this.setVisible(false);
				}
			}
		});
		addKeyListener(keyListener);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				pressed_OK = (source == pass || source == okButton);
				AddressPasswordDialog.this.setVisible(false);
			}
		};

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		JPanel pane = new JPanel(gridbag);
		pane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

		c.anchor = GridBagConstraints.EAST;
		c.gridy = 1;
		if (isPasswordDlg) {
			setTitle(Main.getMessage("dialog_title_password_dialog")); //$NON-NLS-1$
			gridbag.setConstraints(passLabel, c);
			pane.add(passLabel);
			gridbag.setConstraints(pass, c);
			pass.addActionListener(actionListener);
			pass.addKeyListener(keyListener);
			pane.add(pass);
		} else {
			setTitle(Main.getMessage("dialog_title_ipaddress_dialog")); //$NON-NLS-1$
			gridbag.setConstraints(addressLabel, c);
			pane.add(addressLabel);
			gridbag.setConstraints(address, c);
			address.addActionListener(actionListener);
			address.addKeyListener(keyListener);
			pane.add(address);
		}

		c.gridy = 2;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		JPanel panel = new JPanel();
		okButton.addActionListener(actionListener);
		okButton.addKeyListener(keyListener);
		panel.add(okButton);
		cancelButton.addActionListener(actionListener);
		cancelButton.addKeyListener(keyListener);
		panel.add(cancelButton);

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

		gridbag.setConstraints(panel, c);
		pane.add(panel);

		getContentPane().add(pane);

		pack();
	}

	public boolean showDialog() {
		setVisible(true);
		return okPressed();
	}

	private class WindowCloseHandle extends java.awt.event.WindowAdapter
	{
		private AddressPasswordDialog dialog;
		public WindowCloseHandle(AddressPasswordDialog dlg)
		{
			this.dialog = dlg;
		}

		public void windowClosing(java.awt.event.WindowEvent evt)
        {
			dialog.dispose();
        }
	}
}