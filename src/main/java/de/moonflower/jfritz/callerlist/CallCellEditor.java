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

import de.moonflower.jfritz.struct.PhoneNumberOld;


/**
 * This is responsible for dealing with the wahlhilfe
 * This class is used by both phonebooktable and callerlisttable.java
 * This class works by using Callpanel (it isnt really a panel!?!?) to then call
 * Calldialog which in turn calls NoticeDialog.java
 * @see CallDialog.java, CallPanel.java, NoticeDialog.java
 * Brian Jensen
 *
 * @author Arno Willig
 * @author Brian Jensen
 *
 */
public class CallCellEditor extends AbstractCellEditor implements
		TableCellEditor {
	private static final long serialVersionUID = 1;
	JComponent component;

	public CallCellEditor() {
		super();
		component = new CallPanel(this);
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
		// Configure the component with the specified value
		PhoneNumberOld number = (PhoneNumberOld) value;
		CallPanel panel = (CallPanel) component;
		if (value == null) {
		    return null;
		}

		panel.setNumber(number);
		panel.setText(number.getShortNumber());
		// Return the configured component
		return component;
	}

	/**
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		PhoneNumberOld number = ((CallPanel) component).getNumber();
		return number;
	}

	public boolean stopCellEditing() {
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
