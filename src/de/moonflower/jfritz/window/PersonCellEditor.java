/*
 *
 * Created on 14.04.2005
 *
 */
package de.moonflower.jfritz.window;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import de.moonflower.jfritz.struct.Person;

/**
 * This class manages editing of the participant cell in the caller table.
 *
 * @author Arno Willig
 */
public class PersonCellEditor extends AbstractCellEditor implements
		TableCellEditor {

	JComponent component = new PersonEditorPanel(null);

	/**
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable,
	 *      java.lang.Object, boolean, int, int)
	 */
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {

		if (isSelected) {
			// cell (and perhaps other cells) are selected
		}
		// ((PersonEditorPanel) component).repaint();
		// Configure the component with the specified value

		String strval = "";
		if (value != null)
			strval = ((Person) value).getFullname();
		((PersonEditorPanel) component).setText(strval);

		// Return the configured component
		return component;
	}

	/**
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		return ((PersonEditorPanel) component).getText();
	}

	public boolean stopCellEditing() {
		String s = (String) getCellEditorValue();
		if (!isValid(s)) { // Should display an error message at this point
			return false;
		}
		return super.stopCellEditing();
	}

	public boolean isValid(String s) {
		return true;
	}

	/**
	 * @see javax.swing.AbstractCellEditor#fireEditingCanceled()
	 */
	protected void fireEditingCanceled() {
		super.fireEditingCanceled();
	}

	/**
	 * @see javax.swing.AbstractCellEditor#fireEditingStopped()
	 */
	protected void fireEditingStopped() {
		super.fireEditingStopped();

	}

}
