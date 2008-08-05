/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.utils.reverselookup;

import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.StatusBarPanel;
import de.moonflower.jfritz.network.NetworkStateMonitor;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.struct.ReverseLookupSite;

import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupGermany;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupAustria;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupTurkey;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookupXMLHandler;

/**
 * Class for telephone number reverse lookups using various web sites
 *
 *
 *
 */
public class ReverseLookup {
	public static final String AUSTRIA_CODE = "+43", BELGIUM_CODE = "+32",
			CHINA_CODE = "+86", CZECH_CODE = "+420", DENMARK_CODE = "+45",
			FINLAND_CODE = "+358", FRANCE_CODE = "+33", GERMANY_CODE = "+49",
			GREATBRITAIN_CODE = "+44", HOLLAND_CODE = "+31",
			HUNGARY_CODE = "+36", IRELAND_CODE = "+353", ITALY_CODE = "+39",
			JAPAN_CODE = "+81", LUXEMBOURG_CODE = "+352", NORWAY_CODE = "+47",
			POLAND_CODE = "+48", PORTUGAL_CODE = "+351", RUSSIA_CODE = "+7",
			SLOVAKIA_CODE = "+421", SPAIN_CODE = "+34", SWEDEN_CODE = "+46",
			SWITZERLAND_CODE = "+41", TURKEY_CODE = "+90",
			UKRAINE_CODE = "+380", USA_CODE = "+1";

	public static LookupThread thread;

	static volatile PriorityBlockingQueue<LookupRequest> requests = new PriorityBlockingQueue<LookupRequest>();
	static volatile PriorityBlockingQueue<LookupRequest> requests_done = new PriorityBlockingQueue<LookupRequest>();

	public static HashMap<String, Vector<ReverseLookupSite>> rlsMap;

	private static LookupObserver observer;

	private static Vector<Person> results = new Vector<Person>();

	private static int count = 0, done;

	private static StatusBarPanel statusBar = null;

	private static JLabel label = null;

	private static JProgressBar progressBar = null;

	/**
	 * This Function does a lookup for a Vector of PhoneNumbers, the caller must
	 * give an observer, his method personsFound(Vector<Person>) will be called
	 *
	 * @param number
	 *            the number wich will be looked up
	 * @param obs
	 *            the observer wich will be will receive the Persons
	 */
	public static synchronized void lookup(PhoneNumber number,
			LookupObserver obs) {
		Vector<PhoneNumber> v = new Vector<PhoneNumber>();
		v.add(number);
		lookup(v, obs, false);
	}

	/**
	 * This Function does a lookup for a Vector of PhoneNumbers the caller must
	 * give an observer, this method personsFound(Vector<Person>) will be called
	 * either on stop or on completion
	 *
	 * @param number
	 *            the numbers wich will be looked up
	 * @param obs
	 *            the observer wich will be will receive the Persons
	 */
	public static synchronized void lookup(Vector<PhoneNumber> numbers,
			LookupObserver obs, boolean quitOnDone) {

		if(Main.getProperty("option.clientTelephoneBook").equals("true") &&
				NetworkStateMonitor.isConnectedToServer()){
			//if connected to server make server to the lookup
			Debug.msg("requesting reverse lookup from server");
			NetworkStateMonitor.requestLookupFromServer();
			return;
		}

		LookupRequest req;
		Enumeration<PhoneNumber> en = numbers.elements();
		observer = obs;

		//run through elements and add elements that aren't already in the queue
		while(en.hasMoreElements()){
			req = new LookupRequest(en.nextElement(), 5);
			if(!requests.contains(req) && !requests_done.contains(req))
				requests.put(req);

		}

		count = requests.size();
		done = 0;
		Debug.msg("ReverseLookup requests for "+count+" numbers");

		//if the thread isn't started yet, start it up
		if (thread == null) {
			Debug.msg("creating thread");
			thread = new LookupThread(quitOnDone);
			thread.setDaemon(true);
			thread.setName("lookup-thread");
			thread.start();
		}else{
			thread.resumeLookup();
		}
		createStatusBar();
	}


	/**
	 * This method adds a special lookup request for a specific site to the queue
	 *
	 * @param number
	 * @param siteName
	 * @param obs
	 */
	public static synchronized void specificLookup(PhoneNumber number, String siteName, LookupObserver obs){

		if(Main.getProperty("option.clientTelephoneBook").equals("true") &&
				NetworkStateMonitor.isConnectedToServer()){
			//if connected to server make server to the lookup
			Debug.msg("requesting specific reverse lookup for "+number+" using "+ siteName+" from server");
			NetworkStateMonitor.requestSpecificLookupFromServer(number, siteName);
			return;
		}


		Debug.msg("Creating Lookup request for "+number+" using "+siteName);
		observer = obs;

		LookupRequest request = new LookupRequest(number, 10, siteName);
		if(!requests.contains(request) && !requests_done.contains(request))
			requests.put(request);

		count = requests.size();
		done = 0;

		//if the thread isn't started yet, start it up
		if (thread == null) {
			Debug.msg("creating thread");
			thread = new LookupThread(false);
			thread.setDaemon(true);
			thread.setName("lookup-thread");
			thread.start();
		}else{
			thread.resumeLookup();
		}
		createStatusBar();
	}

	/**
	 * Function removes the next element from the request queue
	 *
	 * @return next element in the queue or null
	 */
	public static synchronized LookupRequest getNextRequest(){
			return requests.peek();
	}

	/**
	 * gives the current count of request still in the queue
	 *
	 * @return count
	 */
	public static synchronized int getRequestsCount(){
		return requests.size();
	}

	/**
	 * used to suspend the lookup thread, thread will hibernate once
	 * it has finished processing the current request
	 *
	 */
	public static synchronized void stopLookup(){
		thread.suspendLookup();
	}

	/**
	 * used to terminate lookup thread
	 */
	public static synchronized void terminate(){
		if ( thread != null )
			thread.terminate();
	}

	/**
	 * adds a result to the array of results
	 *
	 * @param person
	 */
	public static synchronized void personFound(Person person){
		done++;
		try {
			requests_done.add(requests.take());
		}catch(InterruptedException e){
        	Thread.currentThread().interrupt();
		}
		results.add(person);
		if ( results.size() % 20 == 0) {
			observer.saveFoundEntries(results);
			requests_done.clear();
			results.clear();
		}
		Debug.msg("Finished "+done+" from "+ count +" requests");
		updateStatusBar(false);
	}

	/**
	 * this is called once thread is done processing the last /
	 * current request
	 *
	 */
	public static synchronized void lookupDone(){
		observer.personsFound(results);
		results.clear();
		requests_done.clear();
		updateStatusBar(true);
	}

	/**
	 * This function loads the various number files on start
	 *
	 */
	public static void loadSettings() {
		ReverseLookupGermany.loadAreaCodes();
		ReverseLookupAustria.loadAreaCodes();
		ReverseLookupUnitedStates.loadAreaCodes();
		ReverseLookupTurkey.loadAreaCodes();
		loadrlsXMLFile();
	}

	/**
	 * This function does one lookup for a PhoneNumber. Good if you want a
	 * single lookup, if you need more numbers looked up better use
	 * <code>lookup(Vector<PhoneNumber> number, LookupObserver obs)</code>
	 * this will start an extra Thread
	 *
	 * @param callerPhoneNumber
	 *            the number wich will be looked up
	 * @return the Person this method found
	 */
	public static Person busyLookup(PhoneNumber callerPhoneNumber) {
		return LookupThread.lookup(callerPhoneNumber, "");
	}

	/** This function loads the file number/international/reverselookup.xml
	 * It starts by initializing the rlsMap and then proceeds to setup
	 * the sax parser
	 *
	 *
	 * @author brian
	 *
	 */
	public static void loadrlsXMLFile(){
		try {
			Debug.msg("Loading the reverse lookup xml file");
			rlsMap = new HashMap<String, Vector<ReverseLookupSite>>(8);

			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(false);
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();

			reader.setErrorHandler(new ErrorHandler() {
				public void error(SAXParseException x) throws SAXException {
					// Debug.err(x.toString());
					throw x;
				}

				public void fatalError(SAXParseException x) throws SAXException {
					// Debug.err(x.toString());
					throw x;
				}

				public void warning(SAXParseException x) throws SAXException {
					// Debug.err(x.toString());
					throw x;
				}
			});

			reader.setContentHandler(new ReverseLookupXMLHandler());
			reader.parse(new InputSource(new FileInputStream(
					JFritzUtils.getFullPath("/number") +"/international/reverselookup.xml")));

		} catch (ParserConfigurationException e) {
			Debug.err("Error with ParserConfiguration!"); //$NON-NLS-1$
		} catch (SAXException e) {
			Debug.err("Error on parsing number/internation/reverselookup.xml! No reverse lookup sites loaded"); //$NON-NLS-1$,  //$NON-NLS-2$
			Debug.err(e.toString());
			e.printStackTrace();

			if (e.getLocalizedMessage().startsWith("Relative URI") //$NON-NLS-1$
					|| e.getLocalizedMessage().startsWith(
							"Invalid system identifier")) { //$NON-NLS-1$
				Debug.err(e.getLocalizedMessage());
			}
		} catch (IOException e) {
			Debug.err("Could not read number/international/reverselookup.xml! No reverse lookup sites loaded!" + e.getMessage()); //$NON-NLS-1$,  //$NON-NLS-2$
		}
	}

	/**
	 * This function is used by the xml handler to add entries to the list
	 *
	 * @param countryCode
	 * @param rls_list
	 */
	public static void addReverseLookupSites(String countryCode, Vector<ReverseLookupSite> rls_list){
		rlsMap.put(countryCode, rls_list);
	}

	private static void createStatusBar()
	{
		if ( statusBar == null )
		{
			statusBar = new StatusBarPanel(2);
			label = new JLabel("Reverse lookup: ");
			progressBar = new JProgressBar();
			progressBar.setVisible(false);
			progressBar.setValue(0);
			progressBar.setStringPainted(true);

			statusBar.add(label);
			statusBar.add(progressBar);
			if (   (JFritz.getJframe() != null )
				&& (JFritz.getJframe().getStatusBar() != null ))
				{
					JFritz.getJframe().getStatusBar().registerDynamicStatusPanel(statusBar);
				}
			updateStatusBar(false);
		}
	}

	private static void updateStatusBar(boolean finished)
	{
		if ( finished )
		{
			label.setVisible(false);
			progressBar.setVisible(false);
			progressBar.setValue(0);
			statusBar.setVisible(false);
		}
		else
		{
			statusBar.setVisible(true);
			label.setVisible(true);
			progressBar.setVisible(true);
			progressBar.setMinimum(0);
			progressBar.setMaximum(count);
			Dimension dim = new Dimension(100, 20);
			progressBar.setMinimumSize(dim);
			progressBar.setMaximumSize(dim);
			progressBar.setPreferredSize(dim);
			progressBar.setValue(done);
			if ( done == 0 )
			{
				progressBar.setIndeterminate(true);
				progressBar.setStringPainted(false);
			}
			else
			{
				progressBar.setIndeterminate(false);
				progressBar.setStringPainted(true);
			}
		}
		JFritz.getJframe().getStatusBar().refresh();
	}
}
