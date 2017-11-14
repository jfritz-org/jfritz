package de.moonflower.jfritz.box.fritzbox.sipprovider;

import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.dialogs.sip.SipProvider;

public class FritzBoxSipProvider {
	private final static Logger log = Logger.getLogger(FritzBoxSipProvider.class);

	private final static int NUM_QUERIES = 4;
	private final static int MAX_SIP_COUNT = 20;
	
	private final static String QUERY_SIP_MAXCOUNT = "telcfg:settings/SIP/count";
	private final static String QUERY_SIP_COUNT = "sip:settings/sip/count";

	private final static String QUERY_SIP_ID = "sip:settings/sip%NUM%/ID";
	private final static String QUERY_SIP_ACTIVATED = "sip:settings/sip%NUM%/activated";
	private final static String QUERY_SIP_REGISTRAR = "sip:settings/sip%NUM%/registrar";
	private final static String QUERY_SIP_DISPLAYNAME = "sip:settings/sip%NUM%/displayname";

	private final static String QUERY_SIP_MSN = "telcfg:settings/SIP%NUM%/MSN";
	
	private Vector<SipProvider> sipProvider;
	private HashMap<String, String> msnMap;

	public FritzBoxSipProvider() {
		sipProvider = new Vector<SipProvider>();
		msnMap = new HashMap<String, String>();
	}
	
	public void detectSipProvider(FritzBox fritzBox) {
		fritzBox.setBoxConnected();

		Vector<String> queryNumSipProviders = new Vector<String>();
		if (fritzBox.getFirmware() != null && fritzBox.getFirmware().isLowerThan(5, 50)) {
			queryNumSipProviders.add(QUERY_SIP_MAXCOUNT);
		} else {
			queryNumSipProviders.add(QUERY_SIP_COUNT);
		}

		Vector<String> response = fritzBox.getQuery(queryNumSipProviders);
		if (response.size() == 1)
		{
			sipProvider.clear();
			msnMap.clear();

			int sipCount = Integer.parseInt(response.get(0));
			log.debug("Number of SIP Providers: " + sipCount);

			Vector<String> querySipMsns = generateMsnQuery();
//			log.debug("query for all sip MSN: " + querySipMsns);
			response = fritzBox.getQuery(querySipMsns);
//			log.debug("response for all sip MSN: " + response);
			parseMsnResponse(response);
			
			Vector<String> querySipProviders = generateQuery(sipCount);
//			log.debug("query for all sip providers: " + querySipProviders);
			response = fritzBox.getQuery(querySipProviders);
//			log.debug("response for all sip providers: " + response);
			parseResponse(response);
		}
		else
		{
			fritzBox.setBoxDisconnected();
		}
	}

	private Vector<String> generateMsnQuery() {
		Vector<String> response = new Vector<String>();
		for (int i=0; i<MAX_SIP_COUNT; i++)
		{
			response.add(QUERY_SIP_MSN.replaceAll("%NUM%", Integer.toString(i)));
		}
		return response;
	}
	
	protected void parseMsnResponse(Vector<String> response) {
		if (response != null && response.size() == MAX_SIP_COUNT)
		{
			for (int i=0; i<response.size(); i++)
			{
				String msn = response.get(i);
				msnMap.put("sip"+i, msn);
			}
		}
	}

	
	private Vector<String> generateQuery(int sipCount) {
		Vector<String> response = new Vector<String>();
		
		for (int i=0; i<sipCount; i++)
		{
			response.add(QUERY_SIP_ACTIVATED.replaceAll("%NUM%", Integer.toString(i)));
			response.add(QUERY_SIP_ID.replaceAll("%NUM%", Integer.toString(i)));
			response.add(QUERY_SIP_REGISTRAR.replaceAll("%NUM%", Integer.toString(i)));
			response.add(QUERY_SIP_DISPLAYNAME.replaceAll("%NUM%", Integer.toString(i)));
		}
		return response;
	}

	protected void parseResponse(Vector<String> response) {		
		if (response != null && response.size() % NUM_QUERIES == 0)
		{
			int numEntries = response.size() / NUM_QUERIES;
			int offset = 0;
			for (int i=0; i<numEntries; i++)
			{
				offset = i * NUM_QUERIES;
				if (!"er".equals(response.get(offset+1)) && !"".equals(response.get(offset+1)))
				{
					SipResponse sipResponse = new SipResponse();
					sipResponse.setActivated(Integer.parseInt(response.get(offset+0)) != 0);
					sipResponse.setId(Integer.parseInt(response.get(offset+1)));
					sipResponse.setRegistrar(response.get(offset+2));
					sipResponse.setDisplayname(response.get(offset+3));
					sipResponse.setMsn(msnMap.get("sip"+sipResponse.getId()));
					
					log.debug(sipResponse);
					if (!"".equals(sipResponse.getMsn()))
					{
//						log.debug("SIP-Provider["+i+"]:"
//								+" id=sip"+sipResponse.getId() + " Activated=" + sipResponse.isActivated() 
//								+" MSN="+sipResponse.getMsn() + " Registrar="+sipResponse.getRegistrar()
//								+ " name="+sipResponse.getDisplayname());
						
						SipProvider newSipProvider = new SipProvider(sipResponse.getId(), sipResponse.getMsn(), sipResponse.getRegistrar());	
						newSipProvider.setActive(sipResponse.isActivated());
						sipProvider.add(newSipProvider);
					}
				}
			}
		}
	}

	public SipProvider getSipProvider(int id)
	{
		for (int i=0; i<sipProvider.size(); i++)
		{
			if (sipProvider.get(i).getProviderID() == id)
			{
				return sipProvider.get(i);
			}
		}

		return null;
	}

	public Vector<SipProvider> getSipProvider() {
		return sipProvider;
	}

	public SipProvider getSipProviderByRoute(String route) {
		for (SipProvider p: sipProvider) {
			if (p.getNumber().equals(route)) {
				return p;
			}
		}

		// sip provider has not been found!
		if (route.contains("@")) {
			String[] splitted = route.split("@");
			String number = splitted[0];
			String name = splitted[1];
			return new SipProvider(SipProvider.UNKNOWN_SIP_PROVIDER_ID, number, name);
		}

		return null;
	}
}
