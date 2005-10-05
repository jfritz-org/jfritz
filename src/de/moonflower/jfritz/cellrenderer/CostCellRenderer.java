/*
 *
 * Created on 14.04.2005
 *
 */
package de.moonflower.jfritz.cellrenderer;

import java.awt.Component;
import java.text.DecimalFormat;

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
		    DecimalFormat df = new DecimalFormat( "0.00" );
		    double costs = Double.parseDouble(value.toString());
		    if (costs == -1) {
		        label.setText("Unbekannt");
		    }
		    else if (costs == -2){
		        label.setText("Freiminuten");
		    }
		    else {
			    String costString = df.format(costs );
				label.setText(costString + " ct");
		    }

			label.setHorizontalAlignment(JLabel.RIGHT);
		}
		return label;
	}
}
