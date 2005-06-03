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

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.struct.VCardList;

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
				Person person = (Person) table.getModel()
						.getValueAt(rows[i], 3);
				String number = ((PhoneNumber) table.getModel().getValueAt(
						rows[i], 2)).getNumber();
				if (person != null) { // FIXME person.getVCard
					list.addVCard(new Person("", "", person.getFullname(), "",
							"", "", "", "", "", "", number, "", ""));
				}
			}

			StringSelection cont = new StringSelection(list.toVCardList());
			clip.setContents(cont, null);
		}
	}
}
