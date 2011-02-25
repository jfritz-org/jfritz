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

import de.moonflower.jfritz.messages.MessageProvider;

/**
 * This is the renderer for the duration display of calls in the table.
 */
public class CostCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1;
	protected MessageProvider messages = MessageProvider.getInstance();

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
		    DecimalFormat df = new DecimalFormat( "0.00" ); //$NON-NLS-1$
		    double costs = Double.parseDouble(value.toString());
		    if (costs == -1) {
		        label.setText(messages.getMessage("unknown")); //$NON-NLS-1$
		    }
		    else if (costs == -2){
		        label.setText("Freiminuten"); //$NON-NLS-1$
		    }
		    else {
			    String costString = df.format(costs );
				label.setText(costString + " ct"); //$NON-NLS-1$
		    }

			label.setHorizontalAlignment(JLabel.RIGHT);
		}
		return label;
	}
}
