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

import de.moonflower.jfritz.JFritz;
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
						"/de/moonflower/jfritz/resources/images/person.png"))); //$NON-NLS-1$

	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			Person person = (Person) value;

			if(person.getFullname().equals("")){
				if(!person.getCity().equals(""))
					label.setText("["+person.getCity()+"]");
				else
					label.setText("");
			}else
				label.setText(person.getFullname());

			String tooltip = ""; //$NON-NLS-1$
			if (!person.getFullname().equals("")) //$NON-NLS-1$
				tooltip = person.getFullname();
			if (!person.getStreet().equals("")) //$NON-NLS-1$
				tooltip += " | " + person.getStreet(); //$NON-NLS-1$
			if (!person.getCity().equals("")) //$NON-NLS-1$
				tooltip += " | " + person.getCity(); //$NON-NLS-1$
			if (tooltip.equals("")) tooltip = JFritz.getMessage("no_information"); //$NON-NLS-1$,  //$NON-NLS-2$
			setToolTipText(tooltip);

			label.setIcon(imagePerson);
			label.setHorizontalAlignment(JLabel.LEFT);
		} else {
			label.setIcon(null);
			label.setText(""); //$NON-NLS-1$
		}
		return label;
	}
}
