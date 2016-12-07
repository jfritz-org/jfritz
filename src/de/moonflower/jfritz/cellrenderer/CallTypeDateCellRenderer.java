/*
 *
 * Created on 10.04.2005
 *
 */
package de.moonflower.jfritz.cellrenderer;

import java.awt.Component;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;

/**
 * This is the renderer for the call type cell of the table, which shows a small
 * icon.
 *
 * @author Arno Willig
 */
public class CallTypeDateCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1;
	final ImageIcon imageCallInFailed, imageCallInBlocked, imageCallIn, imageCallOut;

	/**
	 * renders call type field in CallerTable
	 */
	public CallTypeDateCellRenderer() {
		super();
		imageCallIn = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/callin.png"))); //$NON-NLS-1$
		imageCallInFailed = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/callinfailed.png"))); //$NON-NLS-1$
		imageCallInBlocked = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/callinblocked.png"))); //$NON-NLS-1$
		imageCallOut = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/callout.png"))); //$NON-NLS-1$
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			CallType curType = ((Call) value).getCalltype();
			Date curDate = ((Call) value).getCalldate();
			setToolTipText(curType.toDescription());
			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm"); //$NON-NLS-1$

			label.setText(df.format(curDate));
			if (curType == CallType.CALLIN) {
				label.setIcon(imageCallIn);
			} else if (curType == CallType.CALLIN_FAILED) {
				label.setIcon(imageCallInFailed);
			} else if (curType == CallType.CALLIN_BLOCKED) {
				label.setIcon(imageCallInBlocked);
			} else if (curType == CallType.CALLOUT) {
				label.setIcon(imageCallOut);
			}
		} else {
			label.setIcon(null);
			label.setText(null);
		}
		return label;
	}
}