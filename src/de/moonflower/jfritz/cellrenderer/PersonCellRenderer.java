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

import de.moonflower.jfritz.struct.Person;

/**
 * This renderer shows a person in the specified way.
 *
 * @author Arno Willig
 *
 */

public class PersonCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1;
	private final ImageIcon imagePerson;

	public PersonCellRenderer() {
		super();
		imagePerson = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/person.png")));

	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			Person person = (Person) value;
			//setToolTipText(person.getFullname());
			label.setText(person.getFullname());
			setToolTipText(person.getFullname()+" | "+person.getStreet()+" | " + person.getPostalCode() + " " + person.getCity());
			label.setIcon(imagePerson);
			label.setHorizontalAlignment(JLabel.LEFT);
		} else {
			label.setIcon(null);
			label.setText("");
		}
		return label;
	}
}
