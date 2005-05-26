/*
 * Created on 26.05.2005
 *
 */
package de.moonflower.jfritz.vcard;

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

	public void addVCard(VCard vcard) {
		boolean found = false;
		Enumeration en = list.elements();
		while (en.hasMoreElements()) {
			VCard c = (VCard) en.nextElement();
			if (c.getFon().equals(vcard.getFon())) {
				found = true;
				break;
			}
		}
		if (!found)
			list.add(vcard);
	}

	public int getCount() {
		return list.size();
	}

	public VCard getVCard(int i) {
		return (VCard) list.get(i);
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
			pw.println(this.toString());
			pw.close();
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + file.getName() + "!");
		}
	}

	public String toString() {
		String str = "";
		Enumeration en = list.elements();
		while (en.hasMoreElements()) {
			VCard vcard = (VCard) en.nextElement();
			str += vcard.toString();
		}
		return str;
	}
}
