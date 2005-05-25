/*
 *
 * Created on 18.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.sip;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * @author Arno Willig
 *
 * TODO: I18N
 */
public class SipProviderTableModel extends AbstractTableModel {

	private final String columnNames[] = { "ID", "Aktiv", "SIP-Nummer",
			"Provider" };

	private Vector data;

	public SipProviderTableModel() {
		super();
		data = new Vector();
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return data.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		SipProvider sip = (SipProvider) data.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return Integer.toString(sip.getProviderID());
		case 2:
			return sip.getNumber();
		case 3:
			return sip.getProvider();
		default:
			return "?";
		}
	}

	public String getColumnName(int column) {
		return columnNames[column];
	}

	/**
	 * @return Returns the data.
	 */
	public final Vector getData() {
		return data;
	}

	/**
	 * @param data
	 *            The data to set.
	 */
	public final void setData(Vector data) {
		this.data = data;
	}

	public final void addProvider(SipProvider sip) {
		data.add(sip);
	}
}
