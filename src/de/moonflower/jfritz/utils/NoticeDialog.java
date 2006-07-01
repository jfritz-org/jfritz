package de.moonflower.jfritz.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

import de.moonflower.jfritz.JFritz;

/**
 * @author Bastian Schaefer
 *
 */

public class NoticeDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1;

	private boolean accepted = false;

//	private static JFritz jfritz;

	private String infoText, property;

	JButton okButton, cancelButton;

	JCheckBox checkBox;

	public NoticeDialog(JFritz jfritz,String property, String infoText) throws HeadlessException {
		super(jfritz.getJframe());
//		this.jfritz = jfritz;
		this.infoText = infoText;
		this.property= property;
		//this.setLocation(jfritz.getJframe().getX() + 80, jfritz.getJframe().getY() + 100);

		if (JFritz.getProperty(property, "false").equals( //$NON-NLS-1$
				"true")) { //$NON-NLS-1$
			accepted = true;
		} else {
			drawDialog();
		}
		this.setLocationRelativeTo(jfritz.getJframe());
	}

	public void drawDialog() {
		super.dialogInit();
		setTitle(JFritz.getMessage("information")); //$NON-NLS-1$

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
		okButton = new JButton(JFritz.getMessage("okay")); //$NON-NLS-1$
		okButton.setActionCommand("ok"); //$NON-NLS-1$
		okButton.addActionListener(this);
		addComponent(c, gbl, okButton, 0, 3, 1, 1, 1.0, 0, 30, 5, 0, 0);

		cancelButton = new JButton(JFritz.getMessage("cancel")); //$NON-NLS-1$
		cancelButton.setActionCommand("cancel"); //$NON-NLS-1$
		cancelButton.addActionListener(this);
		addComponent(c, gbl, cancelButton, 1, 3, 1, 1, 1.0, 0, 5, 30, 0, 0);

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

		// Bottom
		checkBox = new JCheckBox(JFritz.getMessage("infoDialog_showAgain")); //$NON-NLS-1$
		checkBox.setActionCommand("call"); //$NON-NLS-1$
		checkBox.addActionListener(this);
		checkBox.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
				property, "false"))); //$NON-NLS-1$
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
		if (e.getActionCommand().equals("ok")) { //$NON-NLS-1$
			accepted = true;
			JFritz.setProperty(property, Boolean
					.toString(checkBox.isSelected()));
			setVisible(false);
		} else if (e.getActionCommand().equals("cancel")) { //$NON-NLS-1$
			accepted = false;
			setVisible(false);
		}
	}

}
