package de.moonflower.jfritz.utils.reverselookup;

import java.util.Vector;

import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.ReverseLookupSite;
import de.moonflower.jfritz.utils.Debug;

public class LookupThread extends Thread implements ParseSiteResultListener {
	private LookupRequest currentRequest;

	private boolean threadSuspended, quit, terminate, terminated;

	private Person result;

	private Vector<ReverseLookupSite> rls_list;
	private Vector<Person> foundPersons = new Vector<Person>(10);

	private int pendingSites = 0;

	private static LookupThread INSTANCE = new LookupThread(false);
	/**
	 * sets the default thread state to active
	 *
	 */
	public LookupThread(boolean quitOnDone) {
		threadSuspended = false;
		terminate = false;
		terminated = false;
		quit = quitOnDone;
	}

	public static LookupThread getInstance() {
		return INSTANCE;
	}
	/**
	 * This method iterates over all lookup requests and stops once
	 * no more are present, in which case it notifies ReverseLookup
	 * that it is done. If threadSuspended is true the thread while wait
	 * it is reactivated to continue doing lookups.
	 *
	 */
	public void run() {

		while (!terminate) {
			try {

			if(ReverseLookup.getRequestsCount() == 0 && quit)
			{
				Debug.debug("No more numbers to lookup!");
				break;
			}

			while((!terminate) && (threadSuspended || ReverseLookup.getRequestsCount() == 0)){
					Debug.info("Lookup done!");
					ReverseLookup.lookupDone();
					synchronized(this){
						wait();
					}
				}
			} catch (InterruptedException e) {
				Debug.debug("Lookup-Thread interrupted!");
	        	Thread.currentThread().interrupt();
			}

			if (!terminate)
			{
				currentRequest = ReverseLookup.getNextRequest();

				result = lookup(currentRequest.number, currentRequest.lookupSite);
				ReverseLookup.personFound(result);

				try{
					sleep(1000);
				}catch(Exception e){
					Debug.debug("Exception occourred: " + e.toString());
					break;
				}
			}
		}

		Debug.info("Lookup thread  has quit");
		terminated = true;
	}

	public synchronized void terminate(){
		terminate = true;
		resumeLookup();
	}

	public synchronized boolean isTerminated(){
		return terminated;
	}

	public synchronized void suspendLookup(){
		Debug.info("suspending lookup thread");
		threadSuspended = true;
	}

	public synchronized void resumeLookup(){
		threadSuspended = false;
		notify();
		Debug.info("resuming lookup thread again");
	}

	public synchronized boolean isSuspended(){
		return threadSuspended;
	}

	/**
	 * This function handles the actual "lookup" part. It uses the data loaded
	 * from number/international/revserlookup.xml to iterate over a series of
	 * websites and over series of patterns for processing each website
	 *
	 * @see de.moonflower.jfritz.struct.ReverseLookupSite
	 *
	 *
	 * @param number number to be looked up
	 * @return a new person object containing the results of the search
	 */
	public synchronized Person lookup(PhoneNumberOld number, String siteName) {
		foundPersons.clear();
		String nummer = number.getAreaNumber();
		if (number.isFreeCall()) {
			Person p = new Person("", "FreeCall"); //$NON-NLS-1$,  //$NON-NLS-2$
			p.addNumber(number);
			foundPersons.add(p);
			Debug.debug("FreeCall detected");
		} else if (number.isSIPNumber() || number.isQuickDial()) {
			Person p = new Person();
			p.addNumber(number);
			foundPersons.add(p);
			Debug.debug("SIP-Number or QuickDial detected");
		} else if (ReverseLookup.rlsMap.containsKey(number.getCountryCode())) {

			rls_list = ReverseLookup.rlsMap.get(number.getCountryCode());

			Debug.info("Begin reverselookup for: "+nummer);

			//cut off the country code if were doing a non local lookup
			if(nummer.startsWith(number.getCountryCode())) {
				if (nummer.length() > number.getCountryCode().length()) {
					nummer = nummer.substring(number.getCountryCode().length());
					Debug.debug("Number has been refactored: " + nummer);
				}
				else
				{
					Debug.debug("Number has no country code, creating new Person as response!");
					Person p = new Person();
					p.addNumber(number);
					return p;
				}
			}

			//Iterate over all the web sites loaded for the given country
			//Use own thread for each site
			pendingSites = 0;
			for(int i=0; i < rls_list.size(); i++){
				ReverseLookupSite rls = rls_list.get(i);
				ParseSite pSite = new ParseSite(siteName, rls, number, nummer);
				pSite.addListener(this);
				pendingSites++;
				pSite.start();
			} // done iterating over all the loaded web sites

			while (pendingSites > 0) {
				yield();
			}
		//no reverse lookup sites available for country
		} else {
			Debug.warning("No reverse lookup sites for: "+number.getCountryCode());
		}

		if (foundPersons.size() == 0) {
			// we have not found a entry, create a dummy entry
			Person p = new Person();
			foundPersons.add(p);
			p.addNumber(number.getAreaNumber(), "home"); //$NON-NLS-1$
		}

		// fix city for all found persons
		for (Person person: foundPersons) {
			fixCityIfNecessary(number, person, nummer);
		}

		// sorting person list by number of filled fields
		foundPersons = sortPersonList(foundPersons);

		return foundPersons.get(0);
	}

	private void fixCityIfNecessary(PhoneNumberOld number, Person person, final String nummer) {
		if ((person.getCity() == null) || ("".equals(person.getCity())))
		{
			if(number.getCountryCode().equals(ReverseLookup.GERMANY_CODE))
				person.setCity(ReverseLookupGermany.getCity(nummer));
			else if(number.getCountryCode().equals(ReverseLookup.AUSTRIA_CODE))
				person.setCity(ReverseLookupAustria.getCity(nummer));
			else if(number.getCountryCode().startsWith(ReverseLookup.USA_CODE))
				person.setCity(ReverseLookupUnitedStates.getCity(nummer));
			else if(number.getCountryCode().startsWith(ReverseLookup.TURKEY_CODE))
				person.setCity(ReverseLookupTurkey.getCity(nummer));
		}
	}

	private Vector<Person> sortPersonList(final Vector<Person> input) {
		Vector<Person> personList = new Vector<Person>();

		Debug.info("" + input.size());
		boolean notFound = false;
		while (!notFound) {
			notFound = true;
			FavouritePerson favPerson = new FavouritePerson(rls_list);

			for (Person p: input) {
				if (!personList.contains(p)) {
					notFound = !favPerson.addPerson(p);
				}
			}
			if (favPerson.getFavourite() != null) {
				personList.add(favPerson.getFavourite());
				Debug.debug("Next person: "
						+ " site: " + favPerson.getFavourite().getLookupSite()
						+ " fullName: " + favPerson.getFavourite().getFullname()
						+ " street:" + favPerson.getFavourite().getStreet()
						+ " plz:" + favPerson.getFavourite().getPostalCode()
						+ " city:" + favPerson.getFavourite().getCity());
			}
		}
		return personList;
	}

	@Override
	public void finished(Vector<Person> newPersons) {
		foundPersons.addAll(newPersons);
		pendingSites--;
	}
}


