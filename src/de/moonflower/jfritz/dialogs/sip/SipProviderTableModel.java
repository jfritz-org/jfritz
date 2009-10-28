/*
 *
 * Created on 18.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.sip;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import de.moonflower.jfritz.Main;

/**
 * @author Arno Willig
 *
 */
public class SipProviderTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 100;

    private final String columnNames[] = { Main.getMessage("id"), Main.getMessage("active"), //$NON-NLS-1$,  //$NON-NLS-2$
    		Main.getMessage("sip_numbers"), Main.getMessage("provider") }; //$NON-NLS-1$,  //$NON-NLS-2$

    private Vector<SipProvider> providerList;

    public SipProviderTableModel() {
        super();
        providerList = new Vector<SipProvider>();
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return providerList.size();
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
        SipProvider sip = providerList.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return Integer.toString(sip.getProviderID());
        case 1:
            if (sip.isActive())
                return Main.getMessage("yes"); //$NON-NLS-1$
            else
                return Main.getMessage("no"); //$NON-NLS-1$
        case 2:
            return sip.getNumber();
        case 3:
            return sip.getProvider();
        default:
            return "?"; //$NON-NLS-1$
        }
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     * Updates SIP-Provider list
     * @param newProviderList
     *            The new providerList to update.
     */
    public final void updateProviderList(Vector<SipProvider> newProviderList) {
    	providerList = newProviderList;
    }

    public void sortAllRowsBy(int col) {
	    Collections.sort(providerList, new ColumnSorter<SipProvider>(col, true));
		fireTableDataChanged();
	}

	/**
	 * Get phoneNumber and providerName to corresponding providerID
	 * @param sipID p.E. SIP0
	 * @param defaultReturn p.E. SIP0 or 123456
	 * @return Number of SipProvider (123@sipgate.de)
	 */
	public String getSipProvider(String sipID, String defaultReturn) {
	    if (sipID.startsWith("SIP")) { //$NON-NLS-1$
            Enumeration<SipProvider> en = providerList.elements();
            	while (en.hasMoreElements()) {
            	    SipProvider sipProvider = en.nextElement();
            	    if (sipProvider.getProviderID() == Integer.parseInt(sipID.substring(3)))
            	        return sipProvider.toString();
            	}
            	return defaultReturn; // If SipProvider not found
	    } else return defaultReturn;
	}

	/**
	 * This comparator is used to sort vectors of data
	 */
	public class ColumnSorter<T extends SipProvider> implements Comparator<SipProvider> {
		int colIndex;

		boolean ascending;

		ColumnSorter(int colIndex, boolean ascending) {
			this.colIndex = colIndex;
			this.ascending = ascending;
		}

		public int compare(SipProvider v1, SipProvider v2){
			Object o1, o2;
			if (colIndex == 0)
			{
			    if (v1.getProviderID() > v2.getProviderID())
			    {
			        return 1;
			    }
			    else
		    	{
			    	return 0;
		    	}
			}
			else
			{
			    o1 = null;
				o2 = null;
			}

			// Treat empty strings like nulls
			if (o1 instanceof String && ((String) o1).trim().length() == 0) {
				o1 = null;
			}
			if (o2 instanceof String && ((String) o2).trim().length() == 0) {
				o2 = null;
			}

			// Sort nulls so they appear last, regardless
			// of sort order
			if (o1 == null && o2 == null) {
				return 0;
			} else if (o1 == null) {
				return 1;
			} else if (o2 == null) {
				return -1;
			} else if (o1 instanceof Comparable) {
				if (ascending) {
					return ((Comparable) o1).compareTo(o2);
				} else {
					return ((Comparable) o2).compareTo(o1);
				}
			} else {
				if (ascending) {
					return o1.toString().compareTo(o2.toString());
				} else {
					return o2.toString().compareTo(o1.toString());
				}
			}
		}
	}
}
