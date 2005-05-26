/*
 *
 * Created on 07.05.2005
 *
 */
package de.moonflower.jfritz.window;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.moonflower.jfritz.vcard.VCard;
import de.moonflower.jfritz.vcard.VCardList;

/**
 * Listener class for copying phone numbers to clipboard
 *
 * @author Arno Willig
 *
 */
public class SelectionListener implements ListSelectionListener {

	JTable table;

	// It is necessary to keep the table since it is not possible
	// to determine the table from the event's source
	public SelectionListener(JTable table) {
		this.table = table;
	}

	/**
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			String str = "";
			VCardList list = new VCardList();
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();

			int rows[] = table.getSelectedRows();
			for (int i = 0; i < rows.length; i++) {
				String name = (String) table.getModel().getValueAt(rows[i], 3);
				String number = (String) table.getModel()
						.getValueAt(rows[i], 2);
				if ( !name.startsWith("?") &&  !number.equals("") ) {
					list.addVCard(new VCard(name, number));
				}
			}

			/*
			 * int row = table.getSelectedRow(); if (row >= 0) { String number =
			 * (String) table.getModel().getValueAt(row, 2); String name =
			 * (String) table.getModel().getValueAt(row, 3); // VCard vcard =
			 * new VCard((String) // table.getModel().getValueAt(row,
			 * 3),number);
			 */
			StringSelection cont = new StringSelection(list.toString());
			clip.setContents(cont, null);
		}
	}
}
