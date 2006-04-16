/**
 *
 * Created on 22.03.2006
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

/**
 * This deprecated class manages editing of the participant cell in the caller
 * table.
 *
 */
public class NumberCellEditor extends AbstractCellEditor
		implements
			TableCellEditor,
			KeyListener {

	private static final long serialVersionUID = 1;
	private PersonPanel personPanel;
	private JTextField textField = new JTextField();

	public NumberCellEditor(PersonPanel personPanel) {
		this.personPanel = personPanel;
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
		// textField.setBackground(new Color(127, 255, 255));
		// Configure the component with the specified value
		String strval = "";  //$NON-NLS-1$
		if (value != null)
			strval = value.toString();
		textField.addKeyListener(this);
		textField.setText(strval);
		// Return the configured component
		return textField;
	}

	/**
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		return textField.getText();
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

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void keyPressed(KeyEvent arg0) {
		char code = arg0.getKeyChar();
		if (Character.isLetterOrDigit(code) || code == KeyEvent.VK_BACK_SPACE
				|| code == KeyEvent.VK_DELETE) {
			personPanel.firePropertyChange(true);
		}
	}

	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

}
