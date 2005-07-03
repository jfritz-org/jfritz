/*
 *
 * Created on 07.05.2005
 *
 */
package de.moonflower.jfritz.callerlist;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.moonflower.jfritz.dialogs.phonebook.PhoneBookTable;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.VCardList;

/**
 * Listener class for copying phone numbers to clipboard
 *
 * @author Arno Willig
 *
 */
public class SelectionListener implements ListSelectionListener {

	CallerTable table;

	// It is necessary to keep the table since it is not possible
	// to determine the table from the event's source
	public SelectionListener(JTable table) {
		this.table = (CallerTable) table;
	}

	/**
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			String str = "";
			VCardList list = new VCardList();
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			Person person = null;
			int rows[] = table.getSelectedRows();
			for (int i = 0; i < rows.length; i++) {
				person = (Person) table.getModel().getValueAt(rows[i], 3);
				if (person != null && person.getFullname() != "") { // FIXME
					// person.getVCard
					list.addVCard(person);
				}
			}

			StringSelection cont = new StringSelection(list.toVCardList());
			clip.setContents(cont, null);

			if (rows.length == 1) {
				// table.getJfritz().getJframe().getPhoneBookPanel().getPersonPanel().setPerson(person);
				PhoneBookTable pt = table.getJfritz().getJframe()
						.getPhoneBookPanel().getPhoneBookTable();
				Vector persons = table.getJfritz().getPhonebook().getFilteredPersons();
				for (int i = 0; i < persons.size(); i++) {
					Person p = (Person) persons.get(i);
					if (p == person) {
						pt.getSelectionModel().setSelectionInterval(i, i);
						table.getJfritz().getJframe().getPhoneBookPanel()
								.showPersonPanel();
						break;
					}
				}
			}
		}
	}
}
