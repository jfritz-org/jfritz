/*
 *
 * Created on 10.04.2005
 *
 */
package de.moonflower.jfritz;

import java.awt.Component;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This is the renderer for the call type cell of the table, which shows a small icon.
 *
 * @author Arno Willig
 */
public class CallTypeCellRenderer extends DefaultTableCellRenderer {

	final ImageIcon imageCallInFailed, imageCallIn, imageCallOut;

	/**
	 * renders call type field in CallerTable
	 */
	public CallTypeCellRenderer() {
		super();
		imageCallIn = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/callin.png")));
		imageCallInFailed = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/callinfailed.png")));
		imageCallOut = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/callout.png")));
	}
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			CallType curType = (CallType) value;
			setToolTipText(curType.toDescription());

			label.setText("");
			if (curType.toInt()==CallType.CALLIN) {
				label.setIcon(imageCallIn);
			}
			if (curType.toInt()==CallType.CALLIN_FAILED) {
				label.setIcon(imageCallInFailed);
			}
			if (curType.toInt()==CallType.CALLOUT) {
				label.setIcon(imageCallOut);
			}
		}
		return label;
	}
}