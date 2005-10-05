/*
 *
 * Created on 14.04.2005
 *
 */
package de.moonflower.jfritz.cellrenderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This is the renderer for the duration display of calls in the table.
 *
 * @author Arno Willig
 */
public class CostCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1;
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
		    String costs = value.toString();

			// setToolTipText(value.toString());

			label.setText(costs + " Ä");
			label.setHorizontalAlignment(JLabel.RIGHT);
		}
		return label;
	}
}
