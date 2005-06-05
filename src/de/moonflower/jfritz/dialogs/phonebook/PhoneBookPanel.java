/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import de.moonflower.jfritz.JFritz;

/**
 * @author Arno Willig
 *
 */
public class PhoneBookPanel extends JPanel {

	public PhoneBookPanel(JFritz jfritz) {
		setLayout(new BorderLayout());
		add(createPhoneBookToolBar(), BorderLayout.NORTH);
		add(createPhoneBookTable(), BorderLayout.CENTER);
	}

	public JToolBar createPhoneBookToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(true);

		return toolBar;
	}


	public JScrollPane createPhoneBookTable() {
		JTable quickdialtable = new JTable();
		return new JScrollPane(quickdialtable);
	}
}
