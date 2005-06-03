/*
 * Created on 03.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.Frame;
import java.awt.HeadlessException;

import javax.swing.JDialog;

/**
 * @author Arno Willig
 *
 */
public class PersonDialog extends JDialog {

	/**
	 * @throws java.awt.HeadlessException
	 */
	public PersonDialog() throws HeadlessException {
		super();
	}

	/**
	 * @param owner
	 * @throws java.awt.HeadlessException
	 */
	public PersonDialog(Frame owner) throws HeadlessException {
		super(owner);
	}

	/**
	 * @param owner
	 * @param modal
	 * @throws java.awt.HeadlessException
	 */
	public PersonDialog(Frame owner, boolean modal) throws HeadlessException {
		super(owner, modal);
	}


}
