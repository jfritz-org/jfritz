/*
 *
 * Created on 10.04.2005
 *
 */
package de.moonflower.jfritz.cellrenderer;

import java.awt.Component;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.moonflower.jfritz.struct.CallType;


/**
 * This is the renderer for the call type cell of the table, which shows a small icon.
 *
 * @author Arno Willig
 */
public class CallTypeCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1;
	final ImageIcon imageCallInFailed, imageCallInBlocked, imageCallIn, imageCallOut;

	/**
	 * renders call type field in CallerTable
	 */
	public CallTypeCellRenderer() {
		super();
		imageCallIn = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				loadResource("images/callin.png"))); //$NON-NLS-1$
		imageCallInFailed = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				loadResource("images/callinfailed.png"))); //$NON-NLS-1$
		imageCallInBlocked = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				loadResource("images/callinblocked.png"))); //$NON-NLS-1$
		imageCallOut = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				loadResource("images/callout.png"))); //$NON-NLS-1$
	}
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			CallType curType = (CallType) value;
			setToolTipText(curType.toDescription());

			label.setText(""); //$NON-NLS-1$
			if (curType==CallType.CALLIN) {
				label.setIcon(imageCallIn);
			} else if (curType==CallType.CALLIN_FAILED) {
				label.setIcon(imageCallInFailed);
			} else if (curType==CallType.CALLIN_BLOCKED) {
				label.setIcon(imageCallInBlocked);
			} else if (curType==CallType.CALLOUT) {
				label.setIcon(imageCallOut);
			}
		}
		return label;
	}

	private URL loadResource(String resourcePath) {
		return getClass().getClassLoader().getResource(resourcePath);
	}
}