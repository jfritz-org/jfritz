/*
 *
 * Created on 06.05.2005
 *
 */
package de.moonflower.jfritz.cellrenderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This renderer shows a route in the specified way.
 *
 * @author Arno Willig
 *
 */

public class PortCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1;
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			String portStr;
			String port = (String) value;

			if (port.equals("4")) //$NON-NLS-1$
				portStr = "ISDN"; //$NON-NLS-1$
			else if (port.equals("0")) //$NON-NLS-1$
				portStr = "FON 1"; //$NON-NLS-1$
			else if (port.equals("1")) //$NON-NLS-1$
				portStr = "FON 2"; //$NON-NLS-1$
			else if (port.equals("2")) //$NON-NLS-1$
				portStr = "FON 3"; //$NON-NLS-1$
			else if (port.equals("10")) //$NON-NLS-1$
				portStr = "DECT 1"; //$NON-NLS-1$
			else if (port.equals("11")) //$NON-NLS-1$
				portStr = "DECT 2"; //$NON-NLS-1$
			else if (port.equals("12")) //$NON-NLS-1$
				portStr = "DECT 3"; //$NON-NLS-1$
			else if (port.equals("13")) //$NON-NLS-1$
				portStr = "DECT 4"; //$NON-NLS-1$
			else if (port.equals("14")) //$NON-NLS-1$
				portStr = "DECT 5"; //$NON-NLS-1$
			else if (port.equals("15")) //$NON-NLS-1$
				portStr = "DECT 6"; //$NON-NLS-1$
		    else if (port.equals("3")) //$NON-NLS-1$
			    portStr = "Durchwahl"; //$NON-NLS-1$
            else if (port.equals("32")) //$NON-NLS-1$
                portStr = "Daten Fon 1";     //$NON-NLS-1$
            else if (port.equals("33")) //$NON-NLS-1$
                portStr = "Daten Fon 2";        //$NON-NLS-1$
            else if (port.equals("34")) //$NON-NLS-1$
                portStr = "Daten Fon 3";       //$NON-NLS-1$
            else if (port.equals("36")) //$NON-NLS-1$
                portStr = "Daten S0"; //$NON-NLS-1$
			else if (port.equals("")) //$NON-NLS-1$
				portStr = ""; //$NON-NLS-1$
			else
				portStr = port;

			if (!port.equals("")) //$NON-NLS-1$
				setToolTipText(port);

			label.setText(portStr);
			label.setHorizontalAlignment(JLabel.CENTER);
		}
		return label;
	}
}
