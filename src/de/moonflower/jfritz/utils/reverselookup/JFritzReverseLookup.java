/*
 *
 * Created on 05.05.2005
 *
 */
package de.moonflower.jfritz.utils.reverselookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.phonebook.PhoneBook;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.JFritzUtils;
import org.jfritz.reverseLookup.api.IReverseLookupFinishedListener;
import org.jfritz.reverseLookup.api.IReverseLookupResponseListener;
import org.jfritz.reverseLookup.api.ReverseLookupFacade;
import org.jfritz.reverseLookup.api.ReverseLookupRequest;
import org.jfritz.reverseLookup.api.ReverseLookupResponse;
import org.jfritz.reverseLookup.exceptions.ReverseLookupException;

/**
 * Class for telephone number reverse lookups using various web sites
 *
 *
 *
 */
public class JFritzReverseLookup {
	private final static Logger log = Logger.getLogger(JFritzReverseLookup.class);

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

	/**
	 * This function loads the various number files on start
	 *
	 */
	public static void loadSettings() {
		try {
			ReverseLookupFacade.getReverseLookupService().loadConfigFile(
					JFritzUtils.getFullPath(JFritzUtils.FILESEP + "number") +JFritzUtils.FILESEP + "international" + JFritzUtils.FILESEP + "reverselookup.xml");
		} catch (ReverseLookupException e) {
			log.error("Could not load reverselookup.xml");
		}
		ReverseLookupGermany.loadAreaCodes();
		ReverseLookupAustria.loadAreaCodes();
		ReverseLookupUnitedStates.loadAreaCodes();
		ReverseLookupTurkey.loadAreaCodes();
	}

	public static void stopAsyncLookup() {
		ReverseLookupFacade.getReverseLookupService().stopAsyncLookup();
	}

	public static void terminateAsyncLookup() {
		ReverseLookupFacade.getReverseLookupService().terminateAsyncLookup();
	}


	public static void doAsyncLookup(final PhoneNumberOld number, final int priority,
			final int progressCount, final IReverseLookupProgressListener progressListener,
			final IReverseLookupFinishedWithResultListener finishedListener) {
		Vector<PhoneNumberOld> numbers = new Vector<PhoneNumberOld>();
		numbers.add(number);
		doAsyncLookup(numbers, priority, progressCount, progressListener, finishedListener);
	}

	public static void doAsyncLookup(final Vector<PhoneNumberOld> numbers, final int priority,
			 final int progressCount, final IReverseLookupProgressListener progressListener,
			 final IReverseLookupFinishedWithResultListener finishedListener) {
		final Vector<Person> personList = new Vector<Person>();
		final List<ReverseLookupRequest> requestList = new ArrayList<ReverseLookupRequest>();

		for (final PhoneNumberOld p: numbers) {
			ReverseLookupRequest request = new ReverseLookupRequest(p.getIntNumber(), priority, new IReverseLookupResponseListener() {

				@Override
				public void lookupResponse(List<ReverseLookupResponse> responseList, int percent) {
					Person person = null;
					if (responseList.size() > 0) {
						person = convertReverseLookupResponse(responseList.get(0));
						person.addNumber(p);
						person.setStandard(p.getIntNumber());
					} else {
						person = PhoneBook.createDummyPerson(p);
					}
					personList.add(person);
					if (progressListener != null) {
						if (personList.size() % progressCount == 0) {
							progressListener.progress(percent, personList);
							personList.clear();
						}
					}
				}
			});
			requestList.add(request);
		}
        try {
			ReverseLookupFacade.getReverseLookupService().asynchronousLookup(requestList, new IReverseLookupFinishedListener() {

				@Override
				public void finished() {
					if (finishedListener != null) {
						finishedListener.finished(personList);
					}
					if (progressListener != null) {
						progressListener.progress(100, personList);
						personList.clear();
					}
				}
			});
		} catch (ReverseLookupException e) {
			log.error("Exception while doing reverse lookup: " + e.getMessage());
		}
	}

	public static Vector<Person> doBlockingLookup(final Vector<PhoneNumberOld> numbers) {
		Vector<Person> result = new Vector<Person>();
		for (PhoneNumberOld n: numbers) {
			result.add(doBlockingLookup(n));
		}
		return result;
	}

	public static Person doBlockingLookup(final PhoneNumberOld number) {
		final ReverseLookupRequest req = new ReverseLookupRequest(number.getIntNumber());
		try {
			List<ReverseLookupResponse> response = ReverseLookupFacade.getReverseLookupService().blockingLookup(req);
			if (response.size() > 0) {
				final Person p = convertReverseLookupResponse(response.get(0));
				p.addNumber(number);
				p.setStandard(number.getIntNumber());
				if (p.getCity() == null || p.getCity().equals("")) {
					fixCity(number, p, number.getAreaNumber());
				}
				return p;
			}
		} catch (ReverseLookupException e) {
			log.error("Exception while looking up number " + number + ": " + e.getMessage());
		}
		return PhoneBook.createDummyPerson(number);
	}

	private static Person convertReverseLookupResponse(final ReverseLookupResponse r) {
		final Person p = new Person();
		p.setLookupSite(r.getFoundBy());
		p.setCompany(r.getCompany());
		p.setFirstName(r.getFirstName());
		p.setLastName(r.getLastName());
		p.setPostalCode(r.getZipCode());
		if (r.getHouseNumber() != null && !r.getHouseNumber().equals("")) {
			p.setStreet(r.getStreet() + " " + r.getHouseNumber());
		} else {
			p.setStreet(r.getStreet());
		}

		p.setCity(r.getCity());
		return p;
	}

	private static void fixCity(PhoneNumberOld number, Person person, final String nummer) {
		if ((person.getCity() == null) || ("".equals(person.getCity())))
		{
			if(number.getCountryCode().equals(JFritzReverseLookup.GERMANY_CODE))
				person.setCity(ReverseLookupGermany.getCity(nummer));
			else if(number.getCountryCode().equals(JFritzReverseLookup.AUSTRIA_CODE))
				person.setCity(ReverseLookupAustria.getCity(nummer));
			else if(number.getCountryCode().startsWith(JFritzReverseLookup.USA_CODE))
				person.setCity(ReverseLookupUnitedStates.getCity(nummer));
			else if(number.getCountryCode().startsWith(JFritzReverseLookup.TURKEY_CODE))
				person.setCity(ReverseLookupTurkey.getCity(nummer));
		}
	}
}
