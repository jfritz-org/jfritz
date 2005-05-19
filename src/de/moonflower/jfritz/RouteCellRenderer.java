/*
 *
 * Created on 06.05.2005
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
 * This renderer shows a callport in the specified way.
 *
 * @author Arno Willig
 *
 * TODO: I18N
 */

public class RouteCellRenderer extends DefaultTableCellRenderer {

	final ImageIcon imageSIP, imagePhone;

	public RouteCellRenderer() {
		super();
		imageSIP = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/world.png")));
		imagePhone = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/phone.png")));

	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			String routeStr;
			String route = (String) value;

			if (route.contains("@")) {
				// SIP Call and we know the provider
				String[] parts = route.split("@");
				routeStr = parts[0];
				setToolTipText("Internet-Telefonat: " + route);
				setIcon(imageSIP);
			} else if (route.contains("SIP")) {
				// SIP Call but we don't know the provider
				routeStr = route;
				setToolTipText("Internet-Telefonat: " + route);
				setIcon(imageSIP);
			} else {
				// regular call
				routeStr = route;
				setIcon(null);
				setToolTipText("Festnetz-Telefonat: " + route);
				setIcon(imagePhone);
			}

			label.setText(routeStr);
			label.setHorizontalAlignment(JLabel.LEFT);
		}
		return label;
	}
}
