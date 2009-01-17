package de.moonflower.jfritz.dialogs.config;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import de.moonflower.jfritz.callerlist.JFritzTableColumn;

public class ColumnTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 5141550318674410834L;

	private Vector<JFritzTableColumn> columns = new Vector<JFritzTableColumn>();

	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return columns.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex)
		{
		case 0: return columns.get(rowIndex).isVisible();

		case 1: return columns.get(rowIndex).getI18NName();
		}
		return null;
	}

	public boolean isCellEditable(int row, int col)
    {
		if ( col == 0 )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public Class getColumnClass(int columnIndex) {
		Object o = getValueAt(0, columnIndex);
		if (o == null) {
			return Object.class;
		} else {
			return o.getClass();
		}
	}

	public void setValueAt(Object object, int rowIndex, int columnIndex) {

		if (columnIndex == 0)
		{
			columns.get(rowIndex).setVisible((Boolean)object);
		}

		fireTableCellUpdated(rowIndex, columnIndex);
	}

	public void refresh()
	{
		fireTableDataChanged();
	}

	public void addData(JFritzTableColumn col)
	{
		columns.add(col);
	}

	public int getDataSize()
	{
		return columns.size();
	}

	public JFritzTableColumn getData(int index)
	{
		return columns.get(index);
	}

	public void removeElementAt(int index)
	{
		columns.removeElementAt(index);
	}

	public void insertElementAt(JFritzTableColumn col, int index)
	{
		columns.insertElementAt(col, index);
	}
}
