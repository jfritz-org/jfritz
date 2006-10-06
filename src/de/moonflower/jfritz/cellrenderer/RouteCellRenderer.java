/*
 *
 * Created on 06.05.2005
 *
 */
package de.moonflower.jfritz.cellrenderer;

import java.awt.Component;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.moonflower.jfritz.Main;

/**
 * This renderer shows a callport in the specified way.
 *
 * @author Arno Willig
 *
 */

public class RouteCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1;
	final ImageIcon imageSIP, imagePhone;

	public RouteCellRenderer() {
		super();
		imageSIP = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/world.png"))); //$NON-NLS-1$
		imagePhone = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/phone.png"))); //$NON-NLS-1$

	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			String routeStr;
			String route = (String) value;

			if (route.indexOf("@")>=0) { //$NON-NLS-1$
				// SIP Call and we know the provider
				String[] parts = route.split("@"); //$NON-NLS-1$
				routeStr = parts[0];
				setToolTipText(Main.getMessage("internet_call")+" " + route); //$NON-NLS-1$
				setIcon(imageSIP);
			} else if (route.indexOf("SIP")>=0) { //$NON-NLS-1$
				// SIP Call but we don't know the provider
				routeStr = route;
				setToolTipText(Main.getMessage("internet_call")+" " + route); //$NON-NLS-1$
				setIcon(imageSIP);
			} else {
				// regular call
				routeStr = route;
				setIcon(null);
				setToolTipText(Main.getMessage("fixed_line_network_call")+" " + route); //$NON-NLS-1$
				setIcon(imagePhone);
			}

			label.setText(routeStr);
			label.setHorizontalAlignment(JLabel.LEFT);
		}
		return label;
	}
}
