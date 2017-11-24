/*
 *
 * Created on 14.04.2005
 *
 */
package de.moonflower.jfritz.callerlist;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;

/**
 * This class manages editing of the person cell in the caller table.
 *
 * @author Arno Willig
 */
public class PersonCellEditor extends AbstractCellEditor implements
		TableCellEditor {
	private static final long serialVersionUID = 1;

	CallerList callerList;
	JComponent component;

	public PersonCellEditor(CallerList callerlist) {
		super();
		this.callerList = callerlist;
		component = new PersonEditorPanel(this, callerlist);
	}

	/**
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable,
	 *      java.lang.Object, boolean, int, int)
	 */
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		if (isSelected) {
			// cell (and perhaps other cells) are selected
		}

		PersonEditorPanel panel = (PersonEditorPanel) component;
		Person person = (Person) value;


		// panel.repaint();
		// Configure the component with the specified value
		String strval = ""; //$NON-NLS-1$
		if (value != null) {
			strval = person.getFullname();
		} else {
			person = new Person();
			Call c = (Call) callerList.getFilteredCall(row);
			c.getPhoneNumber().setType();
			person.addNumber(c.getPhoneNumber());
		}
		panel.setPerson(person);
		panel.setText(strval);

		// Return the configured component
		return component;
	}

	/**
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		Person p = ((PersonEditorPanel) component).getPerson();
		return p;
	}

	public boolean stopCellEditing() {
		//Person p = (Person) getCellEditorValue();
		// Not valid: return false;
		return super.stopCellEditing();
	}
}
