/*
 * Created on 03.06.2005
 *
 */
package de.moonflower.jfritz.callerlist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.PhoneNumber;

/**
 *
 * @author Robert Palmer
 *
 */
public class CallPanel extends JComponent {
	private static final long serialVersionUID = 1;

	private PhoneNumber number;

	private JFritz jfritz;

	private JLabel input;

	/**
	 *
	 */
	public CallPanel(CallCellEditor editor, JFritz jfritz) {
		super();
		this.jfritz = jfritz;
		drawPanel();
	}

	private void drawPanel() {
		setLayout(new BorderLayout());
		input = new JLabel("  ");
		input.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					CallDialog p = new CallDialog(jfritz, number);
					p.setVisible(true);
//					p.show();
					p.dispose();
				}
			}
		});
		ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/call.png")));

		input.setFocusable(false);
		input.setForeground(new Color(127,127,255));
		input.setIcon(icon);
		add(input, BorderLayout.CENTER);
	}

	public void setText(String text) {
		input.setText(text);
	}

	public String getText() {
		return input.getText();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

	}

	/**
	 * @return Returns the PhoneNumber.
	 */
	public final PhoneNumber getNumber() {
		return number;
	}

	/**
	 * @param number
	 *            The number to set.
	 */
	public final void setNumber(PhoneNumber number) {
		this.number = number;
	}
}
