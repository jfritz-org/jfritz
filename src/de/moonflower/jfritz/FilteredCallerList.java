/*
 * Created on 08.05.2005
 *
 */
package de.moonflower.jfritz;


/**
 * @author Arno Willig
 *
 */
public class FilteredCallerList extends FilteredTableModel {
	private int fieldHiddenColumn = 0;

	/** Returns a fewer columns than the real
	 *    TableModel actually has, as we hide one
	 */
	public int getColumnCount() {
		return getRealTableModel().getColumnCount();
	}

	/**
	 * Gets the hiddenColumn property (int) value.
	 * @return The hiddenColumn property value.
	 * @see #setHiddenColumn
	 */
	public int getHiddenColumn() {
		return fieldHiddenColumn;
	}

	/** Provides a mapping from a requested
	 *    column to the column in the real model
	 */
	protected int mapColumn(int col) {
		if (col >= getHiddenColumn())
			col++;
		return col;
	}

	/**
	 * Sets the hiddenColumn property (int) value.
	 * @param hiddenColumn The new value for the property.
	 * @see #getHiddenColumn
	 */
	public void setHiddenColumn(int hiddenColumn) {
		fieldHiddenColumn = hiddenColumn;
	}
}
