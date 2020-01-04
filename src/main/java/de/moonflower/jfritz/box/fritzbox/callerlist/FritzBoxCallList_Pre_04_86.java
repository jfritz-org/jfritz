package de.moonflower.jfritz.box.fritzbox.callerlist;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import de.moonflower.jfritz.struct.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import de.moonflower.jfritz.box.BoxCallBackListener;
import de.moonflower.jfritz.box.BoxCallListInterface;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.exceptions.FeatureNotSupportedByFirmware;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import org.jfritz.fboxlib.exceptions.InvalidCredentialsException;
import org.jfritz.fboxlib.exceptions.LoginBlockedException;
import org.jfritz.fboxlib.exceptions.PageNotFoundException;

public class FritzBoxCallList_Pre_04_86 implements BoxCallListInterface {
	private final static Logger log = Logger.getLogger(FritzBoxCallList_Pre_04_86.class);

	private final static String QUERY_CALLS_REFRESH = "telcfg:settings/RefreshJournal";
	private final static String QUERY_NUM_CALLS = "telcfg:settings/Journal/count";
	private final static String QUERY_CALL_X_TYPE = "telcfg:settings/Journal%NUM%/Type";
	private final static String QUERY_CALL_X_DATE = "telcfg:settings/Journal%NUM%/Date";
	private final static String QUERY_CALL_X_NUMBER = "telcfg:settings/Journal%NUM%/Number";
	private final static String QUERY_CALL_X_PORT = "telcfg:settings/Journal%NUM%/Port";
	private final static String QUERY_CALL_X_DURATION = "telcfg:settings/Journal%NUM%/Duration";
	private final static String QUERY_CALL_X_ROUTE = "telcfg:settings/Journal%NUM%/Route";
	private final static String QUERY_CALL_X_ROUTETYPE = "telcfg:settings/Journal%NUM%/RouteType";
	private final static String QUERY_CALL_X_NAME = "telcfg:settings/Journal%NUM%/Name";

	protected FritzBox fritzBox;
	protected Vector<BoxCallBackListener> callbackListener;
	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	public FritzBoxCallList_Pre_04_86(FritzBox fritzBox, Vector<BoxCallBackListener> callbackListener) {
		this.fritzBox = fritzBox;
		this.callbackListener = callbackListener;
	}

	@Override
	public Vector<Call> getCallerList(Vector<IProgressListener> progressListener) throws FeatureNotSupportedByFirmware, ClientProtocolException, IOException, LoginBlockedException, InvalidCredentialsException, PageNotFoundException	{
		// getting number of entries
		Vector<String> query = new Vector<String>();
		query.add(QUERY_CALLS_REFRESH);
		query.add(QUERY_NUM_CALLS);

		Vector<String> response = fritzBox.getQuery(query);
		if (response.size() == 2)
		{
			int stepSize = 10;
			int querySize = 8;
			int numCalls = Integer.parseInt(response.get(1));
			Vector<Call> newCalls = new Vector<Call>(numCalls);

			int numIterations = numCalls / stepSize;
			int remaining = numCalls % stepSize;
			if (remaining > 0)
			{
				numIterations++;
			}

			for (IProgressListener listener: progressListener)
			{
				listener.setMin(0);
				listener.setMax(numIterations * stepSize);
			}

			boolean finish = false;
			for (int j=0; j<numIterations; j++)
			{
				Vector<Call> tmpCalls = new Vector<Call>(stepSize);
				query.clear();
				int offset = j * stepSize;
				if (!finish)
				{
					for (int i=0; i<stepSize; i++)
					{
						query.add(QUERY_CALL_X_TYPE.replaceAll("%NUM%", Integer.toString(offset+i)));
						query.add(QUERY_CALL_X_DATE.replaceAll("%NUM%", Integer.toString(offset+i)));
						query.add(QUERY_CALL_X_NUMBER.replaceAll("%NUM%", Integer.toString(offset+i)));
						query.add(QUERY_CALL_X_PORT.replaceAll("%NUM%", Integer.toString(offset+i)));
						query.add(QUERY_CALL_X_DURATION.replaceAll("%NUM%", Integer.toString(offset+i)));
						query.add(QUERY_CALL_X_ROUTE.replaceAll("%NUM%", Integer.toString(offset+i)));
						query.add(QUERY_CALL_X_ROUTETYPE.replaceAll("%NUM%", Integer.toString(offset+i)));
						query.add(QUERY_CALL_X_NAME.replaceAll("%NUM%", Integer.toString(offset+i)));
					}
					response = fritzBox.getQuery(query);

					if (response.size() == querySize*stepSize)
					{
						boolean result = createCallFromResponse(tmpCalls, response, querySize, stepSize);
						if (!result)
						{
							throw new IOException("Malformed data while receiving caller list!");
						}
						else
						{
							for (BoxCallBackListener listener: callbackListener)
							{
								finish = listener.finishGetCallerList(tmpCalls);
							}
							if (!finish)
							{
								newCalls.addAll(tmpCalls);
							}
						}
					}
				}

				for (IProgressListener listener: progressListener)
				{
					listener.setProgress(j * stepSize);
				}
			}

			return newCalls;
		}
		else
		{
			// response wrong, set disconnected status
			fritzBox.setBoxDisconnected();
		}

		return new Vector<Call>();
	}

	private boolean createCallFromResponse(Vector<Call> calls, Vector<String> response,
			int querySize, int stepSize)
	{
		for (int i=0; i<stepSize; i++)
		{
			int newOffset = i*querySize;
			if (!"er".equals(response.get(newOffset+0)))
			{
				CallType calltype;
				// Call type
				if ((response.get(newOffset+0).equals("1"))) {
					calltype = CallType.CALLIN;
				} else if ((response.get(newOffset+0).equals("2"))) {
					calltype = CallType.CALLIN_FAILED;
				} else if ((response.get(newOffset+0).equals("3"))) {
					calltype = CallType.CALLOUT;
				} else {
					log.error("Invalid Call type while importing caller list!"); //$NON-NLS-1$
					return false;
				}

				Date calldate;
				// Call date and time
				if (response.get(newOffset+1) != null) {
					try {
						calldate = new SimpleDateFormat("dd.MM.yy HH:mm").parse(response.get(newOffset+1)); //$NON-NLS-1$
					} catch (ParseException e) {
						log.error("Invalid date format while importing caller list!"); //$NON-NLS-1$
						return false;
					}
				} else {
					log.error("Invalid date format while importing caller list!"); //$NON-NLS-1$
					return false;
				}

				// Phone number
				PhoneNumberOld number;
				if (!response.get(newOffset+2).equals("")) {
					number = new PhoneNumberOld(this.properties, response.get(newOffset+2), properties.getProperty(
							"option.activateDialPrefix").toLowerCase().equals("true")
							&& (calltype == CallType.CALLOUT)
							&& !response.get(newOffset+6).startsWith("Internet"));
				} else {
					number = null;
				}

				// split the duration into two stings, hours:minutes
				String[] time = response.get(newOffset+4).split(":");

				String portStr = response.get(newOffset+3);
				Port port = null;
				try {
					int portId = Integer.parseInt(portStr);
					port = fritzBox.getConfiguredPort(portId);
					if (port == null) { // Fallback auf statisch konfigurierte Ports
						port = Port.getPort(portId);
					}
				} catch (NumberFormatException nfe)
				{
					// nothing to do, just proceed
				}
				
				if (port == null)
				{
					port = new Port(0, PortType.GENERIC, portStr, "-1", "-1");
				}

				int routeType = Integer.parseInt(response.get(newOffset+6));
				String route = "";
				if (routeType == 0) // Festnetz
				{
					route = response.get(newOffset+5);
					if ("".equals(route))
					{
						route = messages.getMessage("fixed_network");
					}
				}
				else if (routeType == 1) // SIP
				{
					try {
						int id = Integer.parseInt(response.get(newOffset+5));
						for (SipProvider provider: fritzBox.getSipProvider())
						{
							if (provider.isProviderID(id))
							{
								route = provider.toString();
								break;
							}
						}
					} catch (NumberFormatException nfe) {
						route = response.get(newOffset+5);
					}
				}
				else
				{
					route = "ERROR";
					log.error("Could not determine route type: " + routeType);
				}

				// make the call object and exit
				Call call = new Call(calltype, calldate, number, port, route,
						Integer.parseInt(time[0])* 3600 + Integer.parseInt(time[1]) * 60);

				calls.add(call);
			}
		}

		return true;
	}

	@Override
	public void clearCallerList() throws ClientProtocolException, IOException, LoginBlockedException, InvalidCredentialsException, PageNotFoundException {
		Vector<String> query = new Vector<String>();
		query.add("telcfg:settings/ClearJournal");
		fritzBox.getQuery(query);
	}



}
