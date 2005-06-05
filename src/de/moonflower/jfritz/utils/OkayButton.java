/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.utils;

import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import de.moonflower.jfritz.JFritz;

/**
 * @author Arno Willig
 *
 */
public class OkayButton extends JButton {

	/**
	 *
	 */
	public OkayButton(JFritz jfritz) {
		setText("Okay");
		setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/okay.png"))));
	}

}
