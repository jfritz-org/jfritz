/*
 *
 * Created on 10.04.2005
 *
 */
package de.moonflower.jfritz.cellrenderer;

import java.awt.Component;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.PhoneNumber;

/**
 * This is the renderer for the call type cell of the table, which shows a small
 * icon.
 *
 * @author Arno Willig
 */
public class NumberCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1;
	private final ImageIcon imagePhone, imageHandy, imageHome, imageWorld,
			imageFreeCall;

	private final ImageIcon imageD1, imageD2, imageO2, imageEplus,
			imageSipgate;

	private final static boolean showHandyLogos = true;

	/**
	 * renders the number field in the CallerTable
	 */
	public NumberCellRenderer() {
		super();
		imagePhone = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/phone.png"))); //$NON-NLS-1$
		imageHandy = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/handy.png"))); //$NON-NLS-1$
		imageHome = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/home.png"))); //$NON-NLS-1$
		imageWorld = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/world.png"))); //$NON-NLS-1$
		imageD1 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/d1.png"))); //$NON-NLS-1$
		imageD2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/d2.png"))); //$NON-NLS-1$
		imageO2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/o2.png"))); //$NON-NLS-1$
		imageEplus = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/eplus.png"))); //$NON-NLS-1$
		imageSipgate = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/sipgate.png"))); //$NON-NLS-1$
		imageFreeCall = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/freecall.png"))); //$NON-NLS-1$

	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			PhoneNumber number = (PhoneNumber) value;
			setToolTipText(number.toString());
			label.setText(number.getShortNumber());
			// Debug.msg("Number: "+number.getShortNumber());
			if (number.getIntNumber().length() > 6) {
				if (number.isMobile()) {
					String provider = number.getMobileProvider();
					if (provider.equals("")) //$NON-NLS-1$
						provider = JFritz.getMessage("unknown"); //$NON-NLS-1$

					setToolTipText(JFritz.getMessage("cellphone_network") //$NON-NLS-1$
							+ ": " + provider); //$NON-NLS-1$
					if (showHandyLogos) {
						if (provider.equals("D1")) { //$NON-NLS-1$
							label.setIcon(imageD1);
						} else if (provider.equals("D2")) { //$NON-NLS-1$
							label.setIcon(imageD2);
						} else if (provider.equals("O2")) { //$NON-NLS-1$
							label.setIcon(imageO2);
						} else if (provider.equals("E+")) { //$NON-NLS-1$
							label.setIcon(imageEplus);
						} else {
							label.setIcon(imageHandy);
						}
					} else {
						label.setIcon(imageHandy);
					}
				} else if ((number.getIntNumber().startsWith(JFritz
						.getProperty("area.prefix") //$NON-NLS-1$
						+ JFritz.getProperty("area.code") + "1988")) //$NON-NLS-1$,  //$NON-NLS-2$
						|| (number.getIntNumber().startsWith("01801777"))) { //$NON-NLS-1$
					label.setIcon(imageSipgate);
					setToolTipText(JFritz.getMessage("voip_call")); //$NON-NLS-1$
				} else if (number.isLocalCall()) {
					label.setIcon(imageHome);
					setToolTipText(JFritz.getMessage("local_call")); //$NON-NLS-1$
				} else if (!number.getIntNumber().startsWith(
						JFritz.getProperty("country.prefix"))) { //$NON-NLS-1$
					label.setIcon(imageWorld);
					setToolTipText(JFritz.getMessage("int_call")); //$NON-NLS-1$
				} else if (number.isFreeCall()) {
					label.setIcon(imageFreeCall);
					setToolTipText(JFritz.getMessage("freecall")); //$NON-NLS-1$
				} else {
					label.setIcon(imagePhone);
					setToolTipText(JFritz.getMessage("fixed_network")); //$NON-NLS-1$
				}
			} else {
				label.setIcon(null);
			}
		} else {
			label.setIcon(null);
		}
		return label;
	}
}