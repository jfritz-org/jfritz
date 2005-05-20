/*
 * Created on 20.05.2005
 *
 */
package de.moonflower.jfritz;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * Class for VCard (vcf) handling
 *
 * @author Arno Willig
 *
 */
public class VCard {
	private String fullname, fon;

	/**
	 * Constructs vcard
	 * @param fullname
	 * @param fon
	 */
	public VCard(String fullname, String fon) {
		this.fullname = fullname;
		this.fon = fon;
	}

	public String toString() {
		String vcard = "";
		vcard = "BEGIN:vCard\n" + "VERSION:3.0\n" + "FN: " + fullname + "\n"
				+ "TEL;TYPE=VOICE,MSG,WORK:" + fon + "\n" + "END:vCard";
		return vcard;
	}

	/**
	 * Saves vcard to file
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

}
