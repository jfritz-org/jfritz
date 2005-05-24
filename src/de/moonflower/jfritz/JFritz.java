/**
 *
 * JFritz!
 * http://jfritz.sourceforge.net
 *
 *
 * (c) Arno Willig <akw@thinkwiki.org>
 *
 * Created on 08.04.2005
 *
 * Authors working on the project:
 * 		akw			Arno Willig <akw@thinkwiki.org>
 * 		robotniko	Robert Palmer <robotniko@gmx.de>
 *
 *
 * This tool is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This piece of software is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this driver; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * TODO:
 * Optionen-Dialog: box.clear_after_fetch=true/false
 * Optionen-Dialog: Bei Programmstart automatisch abrufen
 *
 * SIP-Provider: SIP-Provider Handling
 * QuickDial: Kurzwahlverwaltung
 * Phonebook: Telefonbuch für Participants
 *
 * CallerList: Einzelne Einträge löschen
 * CallerList: Einträge löschen älter als Datum
 *
 * Statistik: Top-Caller (Name/Nummer, Wie oft, Wie lange)
 *
 * JAR: Signing, Deploying, Website jfritz.moonflower.de oder Sourceforge
 *
 * CHANGELOG:
 * JFritz! 0.3.3
 * - New search filter feature
 * - Sorting of columns by clicking on column headers
 * - VOIP numbers starting with 49 are now rewritten correctly
 * - Bugfix: Config-Dialog now saves all values correctly
 * - Bugfix: No empty SIP provider after detection
 * - Bugfix: Save-Dialog on export functions
 *
 *
 * JFritz! 0.3.2:
 * - Saves and restores window position/size
 * - Saves and restores width of table columns
 * - CallTypeFilter works now (thanks to robotniko)
 * - Filter option is saved
 * - Added filter for calls without displayed number
 * - Total duration of calls now displayed in status bar
 *
 * JFritz! 0.3.0: Major release
 * - Compatibility for JRE 1.4.2
 * - Severel bugfixes
 *
 * JFritz! 0.2.8:
 * - Bugfix: Firmware detection had nasty bug
 * - Bugfix: Firmware detection detects modded firmware properly
 * - Bugfix: RegExps adapted for modded firmware
 * - Support for SIP-Provider for fritzbox fon wlan
 * - Notify users whenn calls have been retrieved
 * - CSV Export
 *
 * JFritz! 0.2.6:
 * - Several bugfixes
 * - Support for Fritz!Boxes with modified firmware
 * - Improved config dialog
 * - Improved firmware detection
 * - Initial support für SIP-Provider
 * - Firmware/SIP-Provider are saved in config file
 *
 * JFritz! 0.2.4:
 * - Several bugfixes
 * - Improventsment on number resolution
 * - Optimized Reverse Lookup
 *
 * JFritz! 0.2.2:
 * - FRITZ!Box FON WLAN works again and is detected automatically.
 * - Target MSN is displayed
 * - Bugfixes for Reverse Lookup (Mobile phone numbers are filtered now)
 * - Nice icons for calltypes (Regular call, Area call, Mobile call)
 * - Several small bugfixes
 *
 * JFritz! 0.2.0: Major release
 * - Improved GUI, arranged colours for win32 platform
 * - New ToolBar with nice icons
 * - Bugfix: Not all calls had been retrieved from box
 * - Improved reverse lookup
 * - Automatic box detection (does not yet work perfectly)
 * - Internal class restructuring
 *
 * JFritz! 0.1.6:
 * - Calls are now saved in XML format
 * - Support for Fritz!Box 7050
 *
 * JFritz! 0.1.0:
 * - Initial version
 */

package de.moonflower.jfritz;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.table.TableColumn;

import de.moonflower.jfritz.upnp.SSDPdiscoverThread;

/**
 * @author Arno Willig
 *
 */
public class JFritz {

	public final static String PROGRAM_NAME = "JFritz!";

	public final static String PROGRAM_VERSION = "0.3.3";

	public final static String CVS_TAG = "$Id: JFritz.java,v 1.32 2005/05/24 13:19:41 akw Exp $";

	public final static String PROGRAM_AUTHOR = "Arno Willig <akw@thinkwiki.org>";

	public final static String PROPERTIES_FILE = "jfritz.properties.xml";

	public final static String PARTICIPANTS_FILE = "jfritz.participants.xml";

	public final static String CALLS_FILE = "jfritz.calls.xml";

	public final static String CALLS_CSV_FILE = "calls.csv";

	public final static int SSDP_TIMEOUT = 3000;

	public final static boolean DEVEL_VERSION = Integer
			.parseInt(PROGRAM_VERSION.substring(PROGRAM_VERSION
					.lastIndexOf(".") + 1)) % 2 == 1;

	JFritzWindow jframe;

	ResourceBundle messages;

	JFritzProperties defaultProperties, properties, participants;

	private CallerList callerlist;

	private Vector devices;

	private SSDPdiscoverThread ssdpthread;

	/**
	 *
	 */
	public JFritz() {
		loadProperties();
		new ReverseLookup();
		loadMessages(new Locale("de", "DE"));
		callerlist = new CallerList(this);
		jframe = new JFritzWindow(this);

		ssdpthread = new SSDPdiscoverThread(this, SSDP_TIMEOUT);
		ssdpthread.start();

		javax.swing.SwingUtilities.invokeLater(jframe);
	}

	/**
	 *
	 * @param locale
	 */
	private void loadMessages(Locale locale) {
		try {
			messages = ResourceBundle.getBundle(
					"de.moonflower.jfritz.resources.jfritz", locale);
		} catch (MissingResourceException e) {
			Debug.err("Can't find i18n resource!");
			JOptionPane.showMessageDialog(null, JFritz.PROGRAM_NAME + " v"
					+ JFritz.PROGRAM_VERSION
					+ "\n\nCannot start if there is an '!' in path!");
			System.exit(0);
		}
	}

	/**
	 * Loads properties from xml files
	 */
	public void loadProperties() {
		participants = new JFritzProperties();
		defaultProperties = new JFritzProperties();
		properties = new JFritzProperties(defaultProperties);

		// Default properties
		defaultProperties.setProperty("box.address", "192.168.178.1");
		defaultProperties.setProperty("box.password", "");
		defaultProperties.setProperty("country.prefix", "00");
		defaultProperties.setProperty("area.prefix", "0");
		defaultProperties.setProperty("country.code", "49");
		defaultProperties.setProperty("area.code", "441");
		defaultProperties.setProperty("fetch.timer", "5");

		try {
			FileInputStream fis = new FileInputStream(JFritz.PROPERTIES_FILE);
			properties.loadFromXML(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			Debug.err("File " + JFritz.PROPERTIES_FILE
					+ " not found, using default values");
		} catch (Exception e) {
		}

		try {
			FileInputStream fis = new FileInputStream(JFritz.PARTICIPANTS_FILE);
			participants.loadFromXML(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			Debug.err("File " + JFritz.PARTICIPANTS_FILE
					+ " not found, using default values");
		} catch (Exception e) {
		}
	}

	/**
	 * Saves properties to xml files
	 */
	public void saveProperties() {

		properties.setProperty("position.left", Integer.toString(jframe
				.getLocation().x));
		properties.setProperty("position.top", Integer.toString(jframe
				.getLocation().y));
		properties.setProperty("position.width", Integer.toString(jframe
				.getSize().width));
		properties.setProperty("position.height", Integer.toString(jframe
				.getSize().height));

		Enumeration en = jframe.callertable.getColumnModel().getColumns();
		int i = 0;
		while (en.hasMoreElements()) {
			int width = ((TableColumn) en.nextElement()).getWidth();
			properties.setProperty("column" + i + ".width", Integer
					.toString(width));
			i++;
		}

		try {
			FileOutputStream fos = new FileOutputStream(JFritz.PROPERTIES_FILE);
			properties.storeToXML(fos, "Properties for " + JFritz.PROGRAM_NAME
					+ " v" + JFritz.PROGRAM_VERSION);
			fos.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		try {
			FileOutputStream fos = new FileOutputStream(
					JFritz.PARTICIPANTS_FILE);
			participants.storeToXML(fos, "Participants for "
					+ JFritz.PROGRAM_NAME + " v" + JFritz.PROGRAM_VERSION);
			fos.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	/**
	 * Main method for starting JFritz!
	 *
	 * @param args
	 *            Program arguments (-h -v)
	 */
	public static void main(String[] args) {
		System.out.println(PROGRAM_NAME + " v" + PROGRAM_VERSION
				+ " (c) 2005 by " + PROGRAM_AUTHOR);

		if (DEVEL_VERSION)
			Debug.on();

		for (int n = 0; n < args.length; n++) {
			String opt = args[n];
			if (opt.equals("-h") || opt.equals("--help")) {
				System.out.println("Arguments:");
				System.out.println(" -h or --help		This short description");
				System.out
						.println(" -v or --verbose	Turn on debug information");
				System.exit(0);
			} else if (opt.equals("-v") || opt.equals("--verbose")
					|| opt.equals("--debug")) {
				Debug.on();
			}
		}

		new JFritz();
	}

	/**
	 * @return Returns the callerlist.
	 */
	public final CallerList getCallerlist() {
		return callerlist;
	}

	/**
	 * @return Returns the messages.
	 */
	public final ResourceBundle getMessages() {
		return messages;
	}

	/**
	 * @return Returns the participants.
	 */
	public final JFritzProperties getParticipants() {
		return participants;
	}

	/**
	 * @return Returns the properties.
	 */
	public final JFritzProperties getProperties() {
		return properties;
	}

	/**
	 * @return Returns the jframe.
	 */
	public final JFritzWindow getJframe() {
		return jframe;
	}

	/**
	 * @return Returns the fritzbox devices.
	 */
	public final Vector getDevices() {
		try {
			ssdpthread.join();
		} catch (InterruptedException e) {
		}
		return ssdpthread.getDevices();
	}
}
