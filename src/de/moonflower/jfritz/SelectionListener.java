/*
 * Created on 07.05.2005
 *
 */
package de.moonflower.jfritz;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Listener class for copying phone numbers to clipboard
 * @author Arno Willig
 *
 */
public class SelectionListener implements ListSelectionListener {

	JTable table;

	// It is necessary to keep the table since it is not possible
	// to determine the table from the event's source
	SelectionListener(JTable table) {
		this.table = table;
	}

	/**
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			int row = table.getSelectedRow();
			if (row >= 0) {
				String number = (String) table.getModel().getValueAt(row, 2);
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection cont = new StringSelection(number);
				clip.setContents(cont, null);
			}
		}
	}
}
