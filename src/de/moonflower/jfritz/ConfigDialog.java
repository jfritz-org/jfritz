/*
 *
 * Password dialog box
 */

package de.moonflower.jfritz;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSlider;
import javax.swing.JTextField;

/**
 * JDialog for JFritz configuration. TODO: I18N
 *
 * @author Arno Willig
 */
public class ConfigDialog extends JDialog {

	protected JTextField address, areaCode, countryCode, areaPrefix,
			countryPrefix;

	protected JPasswordField pass;

	protected JSlider timerSlider;

	protected JButton okButton;

	protected JButton cancelButton;

	private boolean pressed_OK = false;

	public ConfigDialog(Frame parent, String title) {
	}

	public ConfigDialog(Frame parent) {
		super(parent, true);
		if (parent != null) {
			setLocationRelativeTo(parent);
		}
		drawDialog();
	}

	public String getPass() {
		return new String(pass.getPassword());
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

	public void setValues(Properties properties) {
		pass.setText(properties.getProperty("box.password"));
		address.setText(properties.getProperty("box.address"));
		areaCode.setText(properties.getProperty("area.code"));
		countryCode.setText(properties.getProperty("country.code"));
		areaPrefix.setText(properties.getProperty("area.prefix"));
		countryPrefix.setText(properties.getProperty("country.prefix"));
		timerSlider.setValue(Integer.parseInt(properties
				.getProperty("fetch.timer")));
	}

	public void storeValues(Properties properties) {
		if (areaCode.getText().startsWith(areaPrefix.getText()))
			areaCode.setText(areaCode.getText().substring(
					areaPrefix.getText().length()));
		properties.setProperty("box.password", new String(pass.getPassword()));
		properties.setProperty("box.address", address.getText());
		properties.setProperty("area.code", areaCode.getText());
		properties.setProperty("country.code", countryCode.getText());
		properties.setProperty("area.prefix", areaPrefix.getText());
		properties.setProperty("country.prefix", countryPrefix.getText());
		if (timerSlider.getValue() < 3)
			timerSlider.setValue(3);
		properties.setProperty("fetch.timer", Integer.toString(timerSlider
				.getValue()));
	}

	protected void dialogInit() {
	}

	protected void drawDialog() {
		pass = new JPasswordField("", 16);
		address = new JTextField("", 16);
		areaCode = new JTextField("", 6);
		countryCode = new JTextField("", 3);
		areaPrefix = new JTextField("", 3);
		countryPrefix = new JTextField("", 3);
		okButton = new JButton("Okay");
		cancelButton = new JButton("Abbruch");
		timerSlider = new JSlider(0, 120, 30);
		timerSlider.setPaintTicks(true);
		timerSlider.setMinorTickSpacing(10);
		timerSlider.setMajorTickSpacing(30);
		timerSlider.setPaintLabels(true);

		super.dialogInit();

		KeyListener keyListener = (new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE
						|| (e.getSource() == cancelButton && e.getKeyCode() == KeyEvent.VK_ENTER)) {
					pressed_OK = false;
					ConfigDialog.this.setVisible(false);
				}
				if (e.getSource() == okButton
						&& e.getKeyCode() == KeyEvent.VK_ENTER) {
					pressed_OK = true;
					ConfigDialog.this.setVisible(false);
				}
			}
		});
		addKeyListener(keyListener);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				pressed_OK = (source == pass || source == okButton);
				ConfigDialog.this.setVisible(false);
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
		label = new JLabel("Fritz!Box-Addresse: ");
		gridbag.setConstraints(label, c);
		pane.add(label);
		gridbag.setConstraints(address, c);
		address.addActionListener(actionListener);
		address.addKeyListener(keyListener);
		pane.add(address);

		c.gridy = 2;
		label = new JLabel("Fritz!Box-Passwort: ");
		gridbag.setConstraints(label, c);
		pane.add(label);
		gridbag.setConstraints(pass, c);
		pass.addActionListener(actionListener);
		pass.addKeyListener(keyListener);
		pane.add(pass);

		c.gridy = 3;
		label = new JLabel("Ortsvorwahl: ");
		gridbag.setConstraints(label, c);
		pane.add(label);
		gridbag.setConstraints(areaCode, c);
		areaCode.addActionListener(actionListener);
		areaCode.addKeyListener(keyListener);
		pane.add(areaCode);

		c.gridy = 4;
		label = new JLabel("Landesvorwahl: ");
		gridbag.setConstraints(label, c);
		pane.add(label);
		gridbag.setConstraints(countryCode, c);
		countryCode.addActionListener(actionListener);
		countryCode.addKeyListener(keyListener);
		pane.add(countryCode);

		c.gridy = 5;
		label = new JLabel("Orts-Prefix: ");
		gridbag.setConstraints(label, c);
		pane.add(label);
		gridbag.setConstraints(areaPrefix, c);
		areaPrefix.addActionListener(actionListener);
		areaPrefix.addKeyListener(keyListener);
		pane.add(areaPrefix);

		c.gridy = 6;
		label = new JLabel("Landes-Prefix: ");
		gridbag.setConstraints(label, c);
		pane.add(label);
		gridbag.setConstraints(countryPrefix, c);
		countryCode.addActionListener(actionListener);
		countryCode.addKeyListener(keyListener);
		pane.add(countryPrefix);

		c.gridy = 7;
		label = new JLabel("Timer (in min): ");
		gridbag.setConstraints(label, c);
		pane.add(label);
		gridbag.setConstraints(timerSlider, c);
		//		slider.addActionListener(actionListener);
		timerSlider.addKeyListener(keyListener);
		pane.add(timerSlider);

		/*
		 * c.gridy = 7; gridbag.setConstraints(deleteAfterFetchLabel, c);
		 * pane.add(deleteAfterFetchLabel);
		 * gridbag.setConstraints(countryPrefix, c);
		 * countryCode.addActionListener(actionListener);
		 * countryCode.addKeyListener(keyListener); pane.add(countryPrefix);
		 */

		c.gridy = 8;
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