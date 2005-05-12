/*
 * Created on 06.05.2005
 *
 */
package de.moonflower.jfritz;

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This renderer shows a date in the specified way ("dd.MM.yy HH:mm").
 *
 * @author Arno Willig
 *
 */
public class DateCellRenderer extends DefaultTableCellRenderer {
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm");
			String datumStr = df.format((Date) value);

			label.setText(datumStr);
			// label.setIcon((ImageIcon) value);
		}
		return label;
	}
}
