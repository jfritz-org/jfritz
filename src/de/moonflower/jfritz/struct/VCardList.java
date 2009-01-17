/*
 * Created on 26.05.2005
 *
 */
package de.moonflower.jfritz.struct;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import de.moonflower.jfritz.utils.Debug;

/**
 * Support for VCard-List. Save list to file.
 *
 * @author Arno Willig
 *
 */


public class VCardList {

	Vector<Person> list;

	public VCardList() {
		list = new Vector<Person>();
	}

	/**
	 * Add person to VCard-List
	 * @param person
	 */
	public void addVCard(Person person) {
		boolean found = false;
		Enumeration<Person> en = list.elements();
		while (en.hasMoreElements()) {
			Person p = (Person) en.nextElement();
			if (p.getStandardTelephoneNumber().getIntNumber().equals(
					person.getStandardTelephoneNumber().getIntNumber())
					|| p.getLastName().equals("")) {  //$NON-NLS-1$
				found = true;
				break;
			}
		}
		if (!found) {
			list.add(person);
		}
	}

	public int getCount() {
		return list.size();
	}

	public Person getPerson(int i) {
		return list.get(i);
	}

	/**
	 * Saves vcard list to file
	 *
	 * @param file
	 */
	public void saveToFile(File file) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			PrintWriter pw = new PrintWriter(fos);
			pw.println(toVCardList());
			pw.close();
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + file.getName() + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		}
	}

	public String toVCardList() {
		String str = ""; //$NON-NLS-1$
		Enumeration<Person> en = list.elements();
		while (en.hasMoreElements()) {
			Person p = en.nextElement();
			str += p.toVCard();
		}
		return str;
	}
}
