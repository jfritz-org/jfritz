package de.moonflower.jfritz.monitoring;

import javax.swing.table.AbstractTableModel;

import de.moonflower.jfritz.Main;

public class CurrentCallsTable extends AbstractTableModel {

    private static final long serialVersionUID = 1;

    private final String columnNames[] = { Main.getMessage("type"),
    		Main.getMessage("port"), Main.getMessage("callbycall"),
    		Main.getMessage("number"), Main.getMessage("name"),
    		Main.getMessage("time"), Main.getMessage("comment")};

	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnNames.length;
	}

	public int getRowCount() {
		// TODO Auto-generated method stub
		return 4;
	}

	public Object getValueAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

    public String getColumnName(int column) {
        return columnNames[column];
    }

}
