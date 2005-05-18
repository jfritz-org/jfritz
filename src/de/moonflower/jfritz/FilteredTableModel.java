/*
 * $Id
 *
 */
package de.moonflower.jfritz;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public abstract class FilteredTableModel implements TableModel {
	private TableModel fieldRealTableModel = null;

	/**
	 * addTableModelListener method comment.
	 */
	public void addTableModelListener(TableModelListener l) {
		getRealTableModel().addTableModelListener(l);
	}

	/**
	 * getColumnClass method comment.
	 */
	public Class getColumnClass(int columnIndex) {
		return getRealTableModel().getColumnClass(mapColumn(columnIndex));
	}

	/**
	 * getColumnCount method comment.
	 */
	public int getColumnCount() {
		return getRealTableModel().getColumnCount();
	}

	/**
	 * getColumnName method comment.
	 */
	public String getColumnName(int columnIndex) {
		return getRealTableModel().getColumnName(mapColumn(columnIndex));
	}

	/**
	 * Gets the realTableModel property (javax.swing.table.TableModel) value.
	 *
	 * @return The realTableModel property value.
	 * @see #setRealTableModel
	 */
	public TableModel getRealTableModel() {
		return fieldRealTableModel;
	}

	/**
	 * getRowCount method comment.
	 */
	public int getRowCount() {
		return getRealTableModel().getRowCount();
	}

	/**
	 * getValueAt method comment.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		return getRealTableModel().getValueAt(mapRow(rowIndex),
				mapColumn(columnIndex));

	}

	/**
	 * isCellEditable method comment.
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return getRealTableModel().isCellEditable(mapRow(rowIndex),
				mapColumn(columnIndex));

	}

	/**
	 * Provides a mapping from a requested column to the column in the real
	 * model Subclasses can override this to define special mappings
	 */
	protected int mapColumn(int column) {
		return column;
	}

	/**
	 * Provides a mapping from a requested row to the row in the real model
	 * Subclasses can override this to define special mappings
	 */
	protected int mapRow(int row) {
		return row;
	}

	/**
	 * removeTableModelListener method comment.
	 */
	public void removeTableModelListener(TableModelListener l) {
		getRealTableModel().removeTableModelListener(l);
	}

	/**
	 * Sets the realTableModel property (javax.swing.table.TableModel) value.
	 *
	 * @param realTableModel
	 *            The new value for the property.
	 * @see #getRealTableModel
	 */
	public void setRealTableModel(TableModel realTableModel) {
		fieldRealTableModel = realTableModel;
	}

	/**
	 * setValueAt method comment.
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		getRealTableModel().setValueAt(aValue, mapRow(rowIndex),
				mapColumn(columnIndex));
	}
}
