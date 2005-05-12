/*
 * Created on 06.05.2005
 *
 */
package de.moonflower.jfritz;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This renderer shows a callport in the specified way.
 *
 * @author Arno Willig
 *
 */

public class PortCellRenderer extends DefaultTableCellRenderer {
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			String portStr;
			String port = (String) value;

			if (port.equals("4"))
				portStr = "ISDN";
			else if (port.equals("0"))
				portStr = "FON 1";
			else if (port.equals("1"))
				portStr = "FON 2";
			else if (port.equals("2"))
				portStr = "FON 3";
			else if (port.equals(""))
				portStr = "";
			else
				portStr = "Port " + port;

			if (!port.equals("")) setToolTipText("Port " + port);

			label.setText(portStr);
			label.setHorizontalAlignment(JLabel.CENTER);
			//label.setIcon(curType.getImageIcon());
		}
		return label;
	}
}
