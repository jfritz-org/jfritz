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

import de.moonflower.jfritz.struct.Person;

/**
 * This renderer shows a person in the specified way.
 *
 * @author Arno Willig
 *
 */

public class CityCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1;

	public CityCellRenderer() {
		super();
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			String city = (String) value;

			label.setText(city);
			label.setHorizontalAlignment(JLabel.LEFT);
		} else {
			label.setText(""); //$NON-NLS-1$
		}
		return label;
	}
}
