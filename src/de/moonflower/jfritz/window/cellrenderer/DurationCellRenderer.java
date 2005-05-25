/*
 *
 * Created on 14.04.2005
 *
 */
package de.moonflower.jfritz.window.cellrenderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This is the renderer for the duration display of calls in the table.
 *
 * @author Arno Willig
 */
public class DurationCellRenderer extends DefaultTableCellRenderer {
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			int min = Integer.parseInt(value.toString()) / 60;
			String durationStr;
			if (min>0) durationStr=min + " min"; else durationStr="";

			// setToolTipText(value.toString());

			label.setText(durationStr);
			label.setHorizontalAlignment(JLabel.RIGHT);
		}
		return label;
	}
}
