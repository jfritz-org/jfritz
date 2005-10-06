/*
 *
 * Created on 16.04.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Listener for clicks on the caller table header columns.
 *
 * @author Arno Willig
 */
public class ColumnHeaderListener extends MouseAdapter {
	PhoneBook list;

	public ColumnHeaderListener(TableModel list) {
		super();
		this.list = (PhoneBook) list;
	}

	public void mouseClicked(MouseEvent evt) {
		JTable table = ((JTableHeader) evt.getSource()).getTable();
		TableColumnModel colModel = table.getColumnModel();

		// The index of the column whose header was clicked
		int vColIndex = colModel.getColumnIndexAtX(evt.getX());
		int mColIndex = table.convertColumnIndexToModel(vColIndex);

		// Return if not clicked on any column header
		if (vColIndex == -1) {
			return;
		}

		// Determine if mouse was clicked between column heads
		Rectangle headerRect = table.getTableHeader().getHeaderRect(vColIndex);
		if (vColIndex == 0) {
			headerRect.width -= 6; // Hard-coded constant
		} else {
			headerRect.grow(-6, 0); // Hard-coded constant
		}
		if (!headerRect.contains(evt.getX(), evt.getY())) {
			// Mouse was clicked between column heads
			// vColIndex is the column head closest to the click

			// vLeftColIndex is the column head to the left of the click
			int vLeftColIndex = vColIndex;
			if (evt.getX() < headerRect.x) {
				vLeftColIndex--;
			}

		} else {
			list.sortAllFilteredRowsBy(mColIndex);
		}
	}
}
