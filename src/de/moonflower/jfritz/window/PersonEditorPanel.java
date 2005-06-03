/*
 * Created on 03.06.2005
 *
 */
package de.moonflower.jfritz.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import de.moonflower.jfritz.JFritz;

/**
 * @author Arno Willig
 *
 */
public class PersonEditorPanel extends JComponent implements ActionListener {

	private JFritz jfritz;

	private JTextField input;

	private JButton button;

	/**
	 *
	 */
	public PersonEditorPanel(JFritz jfritz) {
		super();
		this.jfritz = jfritz;
		drawPanel();
	}

	private void drawPanel() {
		ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/modify.png")));

		setLayout(new BorderLayout());
		input = new JTextField("  ");
		button = new JButton();
		button.setIcon(icon);
		button.setFocusable(false);
		button.addActionListener(this);
		input.setBackground(new Color(127, 255, 255));
		input.setFocusable(true);
		add(button, BorderLayout.WEST);
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
		JOptionPane.showMessageDialog(this, JFritz.PROGRAM_NAME + " v"
				+ JFritz.PROGRAM_VERSION + "\n"
				+ "This is not yet implemented!");
	}
}
