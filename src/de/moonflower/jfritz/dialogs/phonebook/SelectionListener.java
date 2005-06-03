package de.moonflower.jfritz.dialogs.phonebook;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.moonflower.jfritz.struct.Person;

public class SelectionListener implements ListSelectionListener {
	PhoneBookDialog phbd;

	SelectionListener(PhoneBookDialog phbd) {
		this.phbd = phbd;
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == phbd.getTable().getSelectionModel()
				&& phbd.getTable().getRowSelectionAllowed()) {
			int first = e.getFirstIndex();
			int last = e.getLastIndex();
			int rows[] = phbd.getTable().getSelectedRows();
			if (rows.length != 0) {
				PhoneBook model = (PhoneBook) phbd.getTable().getModel();
				Person editPerson;
				// Bei mehrerer Zeilenauswahl wird nur das erste Element zum
				// Bearbeiten angezeigt
				editPerson = model.getPersonAt(rows[0]);
				phbd.setTextFieldFirstName(editPerson.getFirstName());
				phbd.setTextFieldMiddleName(editPerson.getMiddleName());
				phbd.setTextFieldLastName(editPerson.getLastName());
				phbd.setTextFieldStreet(editPerson.getStreet());
				phbd.setTextFieldPostalCode(editPerson.getPostalCode());
				phbd.setTextFieldCity(editPerson.getCity());
				phbd.setTextFieldHomeNumber(editPerson.getHomeTelNumber().getNumber());
				phbd.setTextFieldMobileNumber(editPerson.getMobileTelNumber().getNumber());
				phbd.setTextFieldBusinessNumber(editPerson
						.getBusinessTelNumber().getNumber());
				phbd.setTextFieldOtherNumber(editPerson.getOtherTelNumber().getNumber());
				phbd.setRadioButtonHome(false);
				phbd.setRadioButtonMobile(false);
				phbd.setRadioButtonBusiness(false);
				phbd.setRadioButtonOther(false);
				if (editPerson.getStandardTelephoneNumber().equals(
						editPerson.getHomeTelNumber())) {
					phbd.setRadioButtonHome(true);
				} else if (editPerson.getStandardTelephoneNumber().equals(
						editPerson.getMobileTelNumber())) {
					phbd.setRadioButtonMobile(true);
				} else if (editPerson.getStandardTelephoneNumber().equals(
						editPerson.getBusinessTelNumber())) {
					phbd.setRadioButtonBusiness(true);
				} else if (editPerson.getStandardTelephoneNumber().equals(
						editPerson.getOtherTelNumber())) {
					phbd.setRadioButtonOther(true);
				}
			}
		}
		if (e.getValueIsAdjusting()) {
			// The mouse button has not yet been released
		}
	}
}