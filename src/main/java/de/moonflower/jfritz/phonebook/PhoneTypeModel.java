package de.moonflower.jfritz.phonebook;

import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.PhoneType;

public class PhoneTypeModel extends AbstractListModel implements
		ComboBoxModel {
	private static final long serialVersionUID = 1;

	private String[] basicTypes = { "home", "mobile", "homezone", //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
			"business", "other", "fax", "sip", "main" }; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$ , //$NON-NLS-4$ , //$NON-NLS-5$

	private transient PhoneType sel;

	private Vector<PhoneType> types;

	private Person person;

	public PhoneTypeModel(Person person) {
		super();
		types = new Vector<PhoneType>();
		this.person = person;
		setTypes();
	}

	public void setTypes() {
		types.clear();
		int[] typeCount = new int[basicTypes.length];
		for (int i = 0; i < typeCount.length; i++)
			typeCount[i] = 0;

		Enumeration<PhoneNumberOld> en = person.getNumbers().elements();
		while (en.hasMoreElements()) {
			String type = ((PhoneNumberOld) en.nextElement()).getType();
			Pattern p = Pattern.compile("([a-z]*)(\\d*)"); //$NON-NLS-1$
			Matcher m = p.matcher(type);
			if (m.find()) {
				for (int i = 0; i < typeCount.length; i++) {
					if (basicTypes[i].equals(m.group(1))) {
						if (m.group(2).equals("")) { //$NON-NLS-1$
							typeCount[i] = 1;
						} else if (typeCount[i] < Integer.parseInt(m.group(2))) {
							typeCount[i] = Integer.parseInt(m.group(2));
						}
						break;
					}
				}
			}
		}
		for (int i = 0; i < typeCount.length; i++) {
			if (typeCount[i] == 0) {
				types.add(new PhoneType(basicTypes[i]));
			} else {
				types.add(new PhoneType(basicTypes[i] + (typeCount[i] + 1)));
			}
		}
		fireContentsChanged(this, 0, types.size() - 1);
	}

	public Vector<PhoneType> getTypes() {
		return types;
	}

	public int getSize() {
		return types.size();
	}

	public Object getElementAt(int index) {
		return types.get(index);
	}

	public void setSelectedItem(Object anItem) {
		sel = (PhoneType) anItem;
	}

	public Object getSelectedItem() {
		return sel;
	}

	/**
	 * @author Bastian Schaefer
	 *
	 * @return all available types
	 */
	public String[] getBasicTypes() {
		return basicTypes;
	}
}