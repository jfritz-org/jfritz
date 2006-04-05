package de.moonflower.jfritz.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import de.moonflower.jfritz.JFritz;

/**
 * @author Bastian Schaefer
 *
 */

public class InfoDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1;

	private boolean accepted = false;

//	private static JFritz jfritz;

	private String infoText, property;

	JButton okButton, cancelButton;

	JCheckBox checkBox;

	public InfoDialog(JFritz jfritz,String property, String infoText) throws HeadlessException {
//		this.jfritz = jfritz;
		this.infoText = infoText;
		this.property= property;
		this.setLocation(jfritz.getJframe().getX() + 80, jfritz.getJframe()
				.getY() + 100);
		if (JFritz.getProperty(property, "false").equals(
				"true")) {
			accepted = true;
		} else {
			drawDialog();
		}

	}

	public void drawDialog() {
		super.dialogInit();
		setTitle("Info");

		Container c = getContentPane();
		GridBagLayout gbl = new GridBagLayout();
		c.setLayout(gbl);

		// this.setAlwaysOnTop(true); //erst ab Java V.5.0 möglich
		setModal(true);

		// Top
		JLabel label = new JLabel();
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setText(infoText);
		addComponent(c, gbl, label, 0, 0, 2, 3, 1.0, 1.0, 0, 0, 0, 0);

		// Center
		okButton = new JButton("OK");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		addComponent(c, gbl, okButton, 0, 3, 1, 1, 1.0, 0, 30, 5, 0, 0);

		cancelButton = new JButton(JFritz.getMessage("cancel"));
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		addComponent(c, gbl, cancelButton, 1, 3, 1, 1, 1.0, 0, 5, 30, 0, 0);

		// Bottom
		checkBox = new JCheckBox(JFritz.getMessage("InfoDialog_showAgain"));
		checkBox.setActionCommand("call");
		checkBox.addActionListener(this);
		checkBox.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
				property, "false")));
		addComponent(c, gbl, checkBox, 0, 4, 2, 1, 1.0, 0, 0, 0, 5, 0);

		setSize(new Dimension(300, 150));
		setResizable(false);
	}

	private void addComponent(Container cont, GridBagLayout gbl, Component c,
			int x, int y, int width, int height, double weightx,
			double weighty, int insetsLeft, int insetsRight, int insetsTop,
			int insetsBottom) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.insets.left = insetsLeft;
		gbc.insets.right = insetsRight;
		gbc.insets.top = insetsTop;
		gbc.insets.bottom = insetsBottom;
		gbl.setConstraints(c, gbc);
		cont.add(c);
	}

	public boolean isAccepted() {
		return accepted;
	}

	public boolean isChecked(){
		return checkBox.isSelected();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ok")) {
			accepted = true;
			JFritz.setProperty(property, Boolean
					.toString(checkBox.isSelected()));
			setVisible(false);
		} else if (e.getActionCommand().equals("cancel")) {
			accepted = false;
			setVisible(false);
		}
	}

}
