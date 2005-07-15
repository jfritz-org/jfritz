/*
 *
 * Password dialog box
 */

package de.moonflower.jfritz.dialogs.simple;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import de.moonflower.jfritz.utils.Debug;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
/**
 * Simple dialog for Fritz!Box address and password input.
 * TODO: I18N
 *
 * @author Arno Willig
 */
public class AddressPasswordDialog extends JDialog {

	protected JTextField address;

	protected JPasswordField pass;

	protected JButton okButton;

	protected JButton cancelButton;

	protected JLabel passLabel, addressLabel;

	private boolean pressed_OK = false;

	private boolean isPasswordDlg;

	public AddressPasswordDialog(Frame parent, String title) {
	}

	public AddressPasswordDialog(Frame parent, boolean isPasswordDlg) {
		super(parent, true);
		this.isPasswordDlg = isPasswordDlg;
		if (parent != null) {
			setLocationRelativeTo(parent);
		}
		drawDialog();
	}

	public String getPass() {
		try {
			return URLEncoder.encode(new String(pass.getPassword()),"UTF-8");
			}
			catch (UnsupportedEncodingException e){
				Debug.msg("Exception (ConfigDialog:setValues): UnsupportedEncodungException");
				return "";
			}
	}

	public String getAddress() {
		return new String(address.getText());
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
		pass = new JPasswordField("", 20);
		address = new JTextField("", 20);
		okButton = new JButton("Okay");
		cancelButton = new JButton("Abbruch");
		passLabel = new JLabel("Passwort: ");
		addressLabel = new JLabel("Fritz!Box-Addresse: ");

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
		JLabel label;

		c.anchor = GridBagConstraints.EAST;
		c.gridy = 1;
		if (isPasswordDlg) {
			setTitle("Passwort eingeben");
			gridbag.setConstraints(passLabel, c);
			pane.add(passLabel);
			gridbag.setConstraints(pass, c);
			pass.addActionListener(actionListener);
			pass.addKeyListener(keyListener);
			pane.add(pass);
		} else {
			setTitle("Adresse eingeben");
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
		gridbag.setConstraints(panel, c);
		pane.add(panel);

		getContentPane().add(pane);

		pack();
	}

	public boolean showDialog() {
		setVisible(true);
		return okPressed();
	}
}