/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.utils;

import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import de.moonflower.jfritz.Main;

/**
 * @author Arno Willig
 *
 */
public class OkayButton extends JButton {

	/**
	 *
	 */
	private static final long serialVersionUID = 1;

	public OkayButton() {
		setText(Main.getMessage("okay")); //$NON-NLS-1$
		setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/okay.png")))); //$NON-NLS-1$
	}

}
