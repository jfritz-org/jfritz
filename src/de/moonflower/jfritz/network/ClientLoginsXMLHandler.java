package de.moonflower.jfritz.network;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.moonflower.jfritz.callerlist.filter.AnonymFilter;
import de.moonflower.jfritz.callerlist.filter.CallByCallFilter;
import de.moonflower.jfritz.callerlist.filter.CallFilter;
import de.moonflower.jfritz.callerlist.filter.CallInFailedFilter;
import de.moonflower.jfritz.callerlist.filter.CallInFilter;
import de.moonflower.jfritz.callerlist.filter.CallOutFilter;
import de.moonflower.jfritz.callerlist.filter.CommentFilter;
import de.moonflower.jfritz.callerlist.filter.DateFilter;
import de.moonflower.jfritz.callerlist.filter.FixedFilter;
import de.moonflower.jfritz.callerlist.filter.HandyFilter;
import de.moonflower.jfritz.callerlist.filter.SearchFilter;
import de.moonflower.jfritz.callerlist.filter.SipFilter;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;

public class ClientLoginsXMLHandler extends DefaultHandler{
	private final static Logger log = Logger.getLogger(ClientLoginsXMLHandler.class);

	String chars, username, password, type, cbcProviders, sip, dateSpecial,
			start, end, text;

	boolean allowCallListAdd, allowCallListUpdate, allowCallListRemove, allowPhoneBookAdd,
			allowPhoneBookUpdate, allowPhoneBookRemove, allowLookup, allowGetCallList,
			enabled, inverted, allowCallList, allowPhoneBook, allowCallMonitor,
			allowDeleteList, allowDoCall;

	Vector<CallFilter> callFilters;

	public ClientLoginsXMLHandler()  {
		super();
	}

	public void startDocument() throws SAXException {
	}

	public void endDocument() throws SAXException {
	}

	public void startElement(String namespaceURI, String lName, String qName,
			Attributes attrs) throws SAXException {

		String eName = lName;

		if ("".equals(eName)) //$NON-NLS-1$
			eName = qName;

		//	Important to clear buffer :)
		chars = "";  //$NON-NLS-1$


		//if we have a beginning country tag create a new reverselookupsite list
		if (eName.equals("client")) { //$NON-NLS-1$
			username = "";
			password = "";
			allowCallList = false;
			allowCallListAdd = false;
			allowCallListUpdate = false;
			allowCallListRemove = false;
			allowPhoneBook = false;
			allowPhoneBookAdd = false;
			allowPhoneBookUpdate = false;
			allowPhoneBookRemove = false;
			allowCallMonitor = false;
			allowLookup = false;
			allowGetCallList = false;
			allowDeleteList = false;
			allowDoCall = false;
			callFilters = new Vector<CallFilter>();

		}else if(eName.equals("callfilter")){
			type = "";
			enabled = false;
			inverted = false;
		}

		//process all attributes
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				String aName = attrs.getLocalName(i); // Attr name
				if ("".equals(aName)) //$NON-NLS-1$
					aName = attrs.getQName(i);

				//store the type of attribute used
				if(aName.equals("type")){
					type = attrs.getValue(i);

						//clear out old settings
					if(type.equals(CallFilter.FILTER_CALLBYCALL))
						cbcProviders = "";
					else if(type.equals(CallFilter.FILTER_DATE)){
						dateSpecial = "";
						start = "";
						end = "";
					}else if(type.equals(CallFilter.FILTER_SEARCH))
						text = "";
					else if(type.equals(CallFilter.FILTER_SIP))
						sip = "";
				}
			}
		}
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {

		if (qName.equals("user")) { //$NON-NLS-1$
			username = chars;
		} else if (qName.equals("password")) { //$NON-NLS-1$
			password = Encryption.decrypt(chars);
		}else if(qName.equals("allowCallList")){
			allowCallList = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowCallListAdd")){
			allowCallListAdd = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowCallListUpdate")){
			allowCallListUpdate = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowCallListRemove")){
			allowCallListRemove = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowPhoneBook")){
			allowPhoneBook = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowPhoneBookAdd")){
			allowPhoneBookAdd = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowPhoneBookUpdate")){
			allowPhoneBookUpdate = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowPhoneBookRemove")){
			allowPhoneBookRemove = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowCallMonitor")){
			allowCallMonitor = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowDoLookup")){
			allowLookup = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowGetCallList")){
			allowGetCallList = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowDeleteList")){
			allowDeleteList = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowDoCall")){
			allowDoCall = Boolean.parseBoolean(chars);

			//individual filter settings
		}else if(qName.equals("callbycall")){
			cbcProviders = chars;

		}else if(qName.equals("start")){
			start = chars;

		}else if(qName.equals("end")){
			end = chars;

		}else if(qName.equals("special")){
			dateSpecial = chars;

		}else if(qName.equals("enabled")){
			enabled = Boolean.parseBoolean(chars);

		}else if(qName.equals("inverted")){
			inverted = Boolean.parseBoolean(chars);

		}else if(qName.equals("text")){
			text = chars;

		}else if(qName.equals("providers")){
			sip = chars;

			//process the call filter
		}else if(qName.equals("callfilter")){

			//process all other filters first
			if(!type.equals(CallFilter.FILTER_DATE)){

				if(type.equals(CallFilter.FILTER_ANONYM)){
					AnonymFilter af = new AnonymFilter();
					af.setEnabled(enabled);
					af.setInvert(inverted);
					callFilters.add(af);

				}else if(type.equals(CallFilter.FILTER_CALLBYCALL)){
					String[] parts = cbcProviders.trim().split(" ");
					Vector<String> cbc = new Vector<String>();

					for(String part: parts)
						cbc.add(part);

					CallByCallFilter cbcf = new CallByCallFilter(cbc);
					cbcf.setEnabled(enabled);
					cbcf.setInvert(inverted);
					callFilters.add(cbcf);

				}else if(type.equals(CallFilter.FILTER_CALLIN_NOTHING)){
					CallInFailedFilter cinf = new CallInFailedFilter();;
					cinf.setEnabled(enabled);
					cinf.setInvert(inverted);
					callFilters.add(cinf);

				}else if(type.equals(CallFilter.FILTER_CALLINFAILED)){
					CallInFilter cf = new CallInFilter();
					cf.setEnabled(enabled);
					cf.setInvert(inverted);
					callFilters.add(cf);

				}else if(type.equals(CallFilter.FILTER_CALLOUT)){
					CallOutFilter cof = new CallOutFilter();
					cof.setEnabled(enabled);
					cof.setInvert(inverted);
					callFilters.add(cof);

				}else if(type.equals(CallFilter.FILTER_COMMENT)){
					CommentFilter cf = new CommentFilter();
					cf.setEnabled(enabled);
					cf.setInvert(inverted);
					callFilters.add(cf);

				}else if(type.equals(CallFilter.FILTER_DATE)){

					if(dateSpecial.trim().equals(CallFilter.THIS_DAY)){

						Date s = Calendar.getInstance().getTime();
						Date e = Calendar.getInstance().getTime();
						JFritzUtils.setStartOfDay(s);
						JFritzUtils.setEndOfDay(e);

						DateFilter df = new DateFilter(s, e);
						df.specialType = type;
						callFilters.add(df);

					}else if(dateSpecial.trim().equals(CallFilter.LAST_DAY)){

						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
						Date e = cal.getTime();
						Date s = cal.getTime();
						JFritzUtils.setStartOfDay(s);
						JFritzUtils.setEndOfDay(e);

						DateFilter df = new DateFilter(s, e);
						df.specialType = type;
						callFilters.add(df);

					}else if(dateSpecial.trim().equals(CallFilter.THIS_WEEK)){

						Calendar cal = Calendar.getInstance();
						int daysPastMonday = (Calendar.DAY_OF_WEEK + (7 - Calendar.MONDAY)) % 7; //
						cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)
								- daysPastMonday);
						Date s = cal.getTime();
						cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 7);
						Date e = cal.getTime();
						JFritzUtils.setStartOfDay(s);
						JFritzUtils.setEndOfDay(e);

						DateFilter df = new DateFilter(s, e);
						df.specialType = type;
						callFilters.add(df);

					}else if(dateSpecial.trim().equals(CallFilter.LAST_WEEK)){

						Calendar cal = Calendar.getInstance();
						int daysPastMonday = (Calendar.DAY_OF_WEEK + (7 - Calendar.MONDAY)) % 7; //
						cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)
								- daysPastMonday);
						Date e = cal.getTime();
						cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 7);
						Date s = cal.getTime();
						JFritzUtils.setStartOfDay(s);
						JFritzUtils.setEndOfDay(e);

						DateFilter df = new DateFilter(s, e);
						df.specialType = type;
						callFilters.add(df);

					}else if(dateSpecial.trim().equals(CallFilter.THIS_MONTH)){

						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.DAY_OF_MONTH, 1);
						Date s = cal.getTime();
						cal.set(Calendar.DAY_OF_MONTH, cal
								.getActualMaximum(Calendar.DAY_OF_MONTH));
						Date e = cal.getTime();
						JFritzUtils.setStartOfDay(s);
						JFritzUtils.setEndOfDay(e);

						DateFilter df = new DateFilter(s, e);
						df.specialType = type;
						callFilters.add(df);

					}else if(dateSpecial.trim().equals(CallFilter.LAST_MONTH)){

						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1); // last
						cal.set(Calendar.DAY_OF_MONTH, 1);
						Date s = cal.getTime();
						cal.set(Calendar.DAY_OF_MONTH, cal
								.getActualMaximum(Calendar.DAY_OF_MONTH));
						Date e = cal.getTime();
						JFritzUtils.setStartOfDay(s);
						JFritzUtils.setEndOfDay(e);

						DateFilter df = new DateFilter(s, e);
						df.specialType = type;
						callFilters.add(df);

						//no special date type
					}else{
						DateFormat dform = new SimpleDateFormat("dd.MM.yy HH:mm");
						Date s = new Date();
						Date e = new Date();

						try {
							s = dform.parse(start);
							e = dform.parse(end);

							DateFilter df = new DateFilter(s, e);
							df.setEnabled(enabled);
							df.setInvert(inverted);
							callFilters.add(df);

						} catch (ParseException error) {
							log.error("error parsing date while loading dates from client settings "
											+ error.toString());
						}

					}

				}else if(type.equals(CallFilter.FILTER_FIXED)){
					FixedFilter ff = new FixedFilter();
					ff.setEnabled(enabled);
					ff.setInvert(inverted);
					callFilters.add(ff);

				}else if(type.equals(CallFilter.FILTER_HANDY)){
					HandyFilter hf = new HandyFilter();
					hf.setEnabled(enabled);
					hf.setInvert(inverted);
					callFilters.add(hf);

				}else if(type.equals(CallFilter.FILTER_SEARCH)){
					SearchFilter sf = new SearchFilter(text);
					sf.setEnabled(enabled);
					sf.setInvert(inverted);
					callFilters.add(sf);

				}else if(type.equals(CallFilter.FILTER_SIP)){
					Vector<String> sips = new Vector<String>();
					String[] parts = sip.trim().split(" ");
					for(String part: parts)
						sips.add(part);

					SipFilter sf = new SipFilter();
					sf.setProvider(sips);
					sf.setEnabled(enabled);
					sf.setInvert(inverted);
					callFilters.add(sf);
				}
			}

		}else if (qName.equals("client")) { //$NON-NLS-1$

			//make sure the user didn't add any invalid settings by hand editing the xml file
			if(!allowCallList){
				allowCallListAdd = false;
				allowCallListRemove = false;
				allowCallListUpdate = false;
				allowGetCallList = false;
			}
			if(!allowPhoneBook){
				allowPhoneBookAdd = false;
				allowPhoneBookRemove = false;
				allowPhoneBookUpdate = false;
				allowLookup = false;
			}

			//add login settings to the list
			Login login = new Login(username, password, allowCallList, allowCallListAdd, allowCallListUpdate,
					allowCallListRemove, allowPhoneBook, allowPhoneBookAdd, allowPhoneBookUpdate,
					allowPhoneBookRemove, allowCallMonitor, allowLookup, allowGetCallList,
					allowDeleteList, allowDoCall, callFilters, "");
			ClientLoginsTableModel.addLogin(login);
			log.info("NETWORKING: Adding client login: "+username+" with "+ callFilters.size()+" Filters");
		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}
}
