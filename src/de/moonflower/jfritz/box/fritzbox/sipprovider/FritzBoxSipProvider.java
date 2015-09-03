package de.moonflower.jfritz.box.fritzbox.sipprovider;

import java.util.Vector;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.dialogs.sip.SipProvider;

public class FritzBoxSipProvider {
	private final static Logger log = Logger.getLogger(FritzBoxSipProvider.class);

	private final static int NUM_QUERIES = 8;

	private final static String QUERY_SIP_ID = "sip:settings/sip%NUM%/ID";
	private final static String QUERY_SIP_ACTIVATED = "sip:settings/sip%NUM%/activated";
	private final static String QUERY_SIP_REGISTRAR = "sip:settings/sip%NUM%/registrar";
	private final static String QUERY_SIP_MSN = "telcfg:settings/SIP%NUM%/MSN";
	private final static String QUERY_SIP_MAXCOUNT = "telcfg:settings/SIP/count";
	private final static String QUERY_SIP_COUNT = "sip:settings/sip/count";
	private final static String QUERY_SIP_NAME = "sip:settings/sip%NUM%/Name";
	private final static String QUERY_SIP_NUMBER = "sip:settings/sip%NUM%/Number";
	private final static String QUERY_SIP_DISPLAYNAME = "sip:settings/sip%NUM%/displayname";
	private final static String QUERY_SIP_USERNAME = "sip:settings/sip%NUM%/username";
//	private final static String QUERY_SIP_REGISTRY_TYPE = "telcfg:settings/SIP%NUM%/RegistryType";

	private Vector<SipProvider> sipProvider;

	public FritzBoxSipProvider() {
		sipProvider = new Vector<SipProvider>();
	}
	
	public void detectSipProvider(FritzBox fritzBox) {
		fritzBox.setBoxConnected();

		Vector<String> query = new Vector<String>();
		if (fritzBox.getFirmware() != null && fritzBox.getFirmware().isLowerThan(5, 50)) {
			query.add(QUERY_SIP_MAXCOUNT);
		} else {
			query.add(QUERY_SIP_COUNT);
		}

		Vector<String> response = fritzBox.getQuery(query);
		if (response.size() == 1)
		{
			int sipCount = Integer.parseInt(response.get(0));
			log.debug("Number of SIP Providers: " + sipCount);

			response = fritzBox.getQuery(generateQuery(sipCount));
			parseResponse(response);
		}
		else
		{
			fritzBox.setBoxDisconnected();
		}
	}

	private Vector<String> generateQuery(int sipCount) {
		Vector<String> response = new Vector<String>();
		
		for (int i=0; i<sipCount; i++)
		{
			response.add(QUERY_SIP_ACTIVATED.replaceAll("%NUM%", Integer.toString(i)));
			response.add(QUERY_SIP_ID.replaceAll("%NUM%", Integer.toString(i)));
			response.add(QUERY_SIP_REGISTRAR.replaceAll("%NUM%", Integer.toString(i)));
			response.add(QUERY_SIP_MSN.replaceAll("%NUM%", Integer.toString(i)));
			response.add(QUERY_SIP_NAME.replaceAll("%NUM%", Integer.toString(i)));
			response.add(QUERY_SIP_NUMBER.replaceAll("%NUM%", Integer.toString(i)));
			response.add(QUERY_SIP_DISPLAYNAME.replaceAll("%NUM%", Integer.toString(i)));
			response.add(QUERY_SIP_USERNAME.replaceAll("%NUM%", Integer.toString(i)));
//				query.add(QUERY_SIP_REGISTRY_TYPE.replaceAll("%NUM%", Integer.toString(i)));
		}
		return response;
	}

	protected void parseResponse(Vector<String> response) {
		sipProvider.clear();
		
		if (response != null && response.size() % NUM_QUERIES == 0)
		{
			int numEntries = response.size() / NUM_QUERIES;
			int offset = 0;
			for (int i=0; i<numEntries; i++)
			{
				offset = i * NUM_QUERIES;
				if (!"er".equals(response.get(offset+0)) && !"".equals(response.get(offset+0)))
				{
					int id = Integer.parseInt(response.get(offset+1));
					String name = response.get(offset+2);
					int numberId = offset + 3;
					String number = response.get(offset + 3);
					log.debug("id= " + id + " NumberID=" +numberId + " Number=" + number + " Name="+name);
					if (!"".equals(number))
					{
						log.debug("SIP-Provider["+i+"]: id="+id+" Number="+number+ " Name="+name);
						SipProvider newSipProvider = new SipProvider(id, number, name);
						if (Integer.parseInt(response.get(offset+0)) == 0)
						{
							newSipProvider.setActive(false);
						}
						else
						{
							newSipProvider.setActive(true);
						}
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
