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

import de.moonflower.jfritz.struct.Port;

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
			Port port = (Port) value;

			if (!port.equals(""))
			{
				setToolTipText(port.getName());
			}

			label.setText(port.getName());
			label.setHorizontalAlignment(JLabel.CENTER);
		}
		return label;
	}
}
