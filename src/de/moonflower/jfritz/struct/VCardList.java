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
 * @author Arno Willig
 *
 */
public class VCardList {

	Vector list;

	/**
	 *
	 */
	public VCardList() {
		list = new Vector();
	}

	public void addVCard(Person person) {
		boolean found = false;
		Enumeration en = list.elements();
		while (en.hasMoreElements()) {
			Person p = (Person) en.nextElement();
			if (p.getStandardTelephoneNumber().getNumber().equals(
					person.getStandardTelephoneNumber().getNumber())
					|| p.getLastName().equals("")) {
				found = true;
				break;
			}
		}
		if (!found) {
			Debug.msg("Adding person: "+person.getFullname());
			list.add(person);
		}
	}

	public int getCount() {
		return list.size();
	}

	public Person getPerson(int i) {
		return (Person) list.get(i);
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
			Debug.err("Could not write " + file.getName() + "!");
		}
	}

	public String toVCardList() {
		String str = "";
		Enumeration en = list.elements();
		while (en.hasMoreElements()) {
			Person p = (Person) en.nextElement();
			str += p.toVCard();
		}
		return str;
	}
}
