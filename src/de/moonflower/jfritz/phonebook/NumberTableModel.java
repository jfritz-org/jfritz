package de.moonflower.jfritz.phonebook;

import java.util.Enumeration;

import javax.swing.table.AbstractTableModel;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.struct.PhoneType;

public class NumberTableModel extends AbstractTableModel {
	private final String columnNames[] = { Main.getMessage("standard_short"), Main.getMessage("type"), Main.getMessage("number") }; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$

	private static final long serialVersionUID = 1;

	private Person person;

	private PhoneTypeModel typeModel;

	public NumberTableModel(Person person, PhoneTypeModel typeModel) {
		this.person = person;
		this.typeModel = typeModel;
	}

	public int getRowCount() {
		return person.getNumbers().size();
	}

	public int getColumnCount() {
		return 3;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Boolean.valueOf(((PhoneNumber) person.getNumbers()
					.elementAt(rowIndex)).getType().equals(
					person.getStandard()));
		case 1:
			return new PhoneType(((PhoneNumber) person.getNumbers()
					.elementAt(rowIndex)).getType());
		case 2:
			return ((PhoneNumber) person.getNumbers().elementAt(rowIndex))
					.getIntNumber();
		default:
			return null;

		}
	}

	public void setValueAt(Object value, int row, int column) {
		if (row < person.getNumbers().size()) {
			PhoneNumber p = (PhoneNumber) person.getNumbers()
					.elementAt(row);
			switch (column) {
			case 0:
				if (!p.getType().equals("")) //$NON-NLS-1$
					person.setStandard(p.getType());
				break;
			case 1:
				if (isValidType((PhoneType) value, p.getType())) {
					if (person.getStandard().equals(p.getType())
							|| person.getStandard().equals("")) { //$NON-NLS-1$
						person.setStandard(((PhoneType) value).getType());
					}
					p.setType(((PhoneType) value).getType());
				}
				break;
			case 2:
				if (isValidNumber((String) value, p.getIntNumber())) {
					p.setNumber((String) value);
				}
			default:
			}
			fireTableDataChanged();
		}
	}

	protected boolean isValidNumber(String value, String oldvalue) {
		if (value.equals(oldvalue))
			return true;
		Enumeration en = person.getNumbers().elements();
		while (en.hasMoreElements()) {
			String nr = ((PhoneNumber) en.nextElement()).getIntNumber();
			if (value.equals(nr))
				return false;
		}
		return true;
	}

	protected boolean isValidType(PhoneType value, String oldvalue) {
		if (value == null)
			return false;
		if (value.getType().equals(oldvalue))
			return true;

		for (int i = 0; i < typeModel.getSize(); i++) {
			if (value.getType().equals(
					((PhoneType) typeModel.getElementAt(i)).getType())) {
				Enumeration en = person.getNumbers().elements();
				while (en.hasMoreElements()) {
					String type = ((PhoneNumber) en.nextElement())
							.getType();
					if (value.getType().equals(type))
						return false;
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * @return Returns the column names
	 */
	public String getColumnName(int column) {
		return columnNames[column];
	}

	/**
	 * Update person
	 * @param person
	 */
	public void setPerson(Person person) {
		this.person = person;
		this.fireTableDataChanged();
	}

	/**
	 * Update type model
	 * @param typeModel
	 */
	public void setTypeModel(PhoneTypeModel typeModel) {
		this.typeModel = typeModel;
		this.fireTableDataChanged();
	}
}