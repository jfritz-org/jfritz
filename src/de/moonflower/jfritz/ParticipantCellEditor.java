/*
 *
 * Created on 14.04.2005
 *
 */
package de.moonflower.jfritz;

import java.awt.Color;
import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

/**
 * This class manages editing of the participant cell in the caller table.
 *
 * @author Arno Willig
 */
public class ParticipantCellEditor extends AbstractCellEditor implements
		TableCellEditor {

	JComponent component = new JTextField();

	/**
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable,
	 *      java.lang.Object, boolean, int, int)
	 */
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		if (isSelected) {
			// cell (and perhaps other cells) are selected
		}
		((JTextField) component).setBackground(new Color(127, 255, 255));
		// Configure the component with the specified value
		((JTextField) component).setText((String) value);

		// Return the configured component
		return component;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		return ((JTextField) component).getText();
	}

	/*
	 * public boolean isCellEditable(EventObject evt) { if (evt instanceof
	 * MouseEvent) { int clickCount;
	 *
	 * clickCount = 2; return ((MouseEvent)evt).getClickCount() >= clickCount; }
	 * return true; }
	 */
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

	/*
	 * @see javax.swing.AbstractCellEditor#fireEditingStopped()
	 */
	protected void fireEditingStopped() {
		super.fireEditingStopped();

	}

}
