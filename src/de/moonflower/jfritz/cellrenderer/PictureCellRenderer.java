package de.moonflower.jfritz.cellrenderer;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This is the renderer for the picture.
 *
 * @author Robert Palmer
 */
public class PictureCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1;
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);
		label.setText("");
		label.setHorizontalAlignment(JLabel.CENTER);
		ImageIcon icon = new ImageIcon("");
		if ( value instanceof ImageIcon )
		{
			icon = (ImageIcon)value;
		}
		label.setIcon(icon);
		if ( ( icon.getIconWidth() != -1) && ( icon.getIconHeight() != -1 ) )
		{
			if ( table.getRowHeight(row) != icon.getIconHeight())
				table.setRowHeight(row, icon.getIconHeight());
		}
		return label;
	}
}