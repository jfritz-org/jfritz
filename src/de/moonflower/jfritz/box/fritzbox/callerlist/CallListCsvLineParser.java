package de.moonflower.jfritz.box.fritzbox.callerlist;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.exceptions.FeatureNotSupportedByFirmware;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.Port;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class CallListCsvLineParser {

	protected MessageProvider messages = MessageProvider.getInstance();
	protected PropertyProvider properties = PropertyProvider.getInstance();

	private String separator = ";";

	public CallListCsvLineParser(final String separator) {
		this.separator = separator;
	}

	public Call parseLine(final FritzBox fritzBox, final String line)
			throws FeatureNotSupportedByFirmware {
		if (fritzBox == null) {
			Debug.error("CallListCsvLineParser: FritzBox is null!");
			throw new FeatureNotSupportedByFirmware("Get caller list", messages.getMessage("box.no_caller_list"));
		}

		if (line == null || "".equals(line)) {
			Debug.error("CallListCsvLineParser: Could not parse CSV line because it is null or empty");
			throw new FeatureNotSupportedByFirmware("Get caller list", messages.getMessage("box.no_caller_list"));
		}

		String[] splitted = line.split(separator);
		if (splitted.length != 7) {
			// Typ;Datum;Name;Rufnummer;Nebenstelle;Eigene Rufnummer;Dauer
			Debug.error("CallListCsvLineParser: Expected 7 columns but got: " + splitted.length + " for line: " + line);
			throw new FeatureNotSupportedByFirmware("Get caller list", messages.getMessage("box.no_caller_list"));
		}

		return parse(fritzBox, splitted);
	}

	private Call parse(final FritzBox fritzBox, final String[] splitted)
			throws FeatureNotSupportedByFirmware {
		CallType calltype = parseCallType(splitted[0]);
		Date calldate = parseCallDate(splitted[1]);
		PhoneNumberOld number = parsePhoneNumber(calltype, splitted[3]);
		Port port = parsePort(fritzBox, splitted[4]);
		String route = parseRoute(fritzBox, splitted[5]);
		String[] time = parseTime(splitted[6]);

		return new Call(calltype, calldate, number, port, route,
				Integer.parseInt(time[0]) * 3600 + Integer.parseInt(time[1])
						* 60);
	}

	private String parseRoute(final FritzBox fritzBox, final String routeInput) {
		String route = "";

		int routeType = parseRouteType(routeInput);

		if (routeType == Call.ROUTE_FIXED_NETWORK) {
			route = routeInput;
			if ("".equals(route)) {
				route = messages.getMessage("fixed_network");
			}
		} else if (routeType == Call.ROUTE_SIP) {
			route = removeInternetPrefixIfNecessary(routeInput);

			SipProvider provider = fritzBox.getSipProviderByRoute(route);
			if (provider != null) {
				route = provider.toString();
			} else {
				route = route + "@" + messages.getMessage("unknown_sip_provider");
			}
		} else {
			route = "ERROR";
			Debug.error("Could not determine route type: " + routeType);
		}
		return route;
	}

	private String removeInternetPrefixIfNecessary(final String routeInput) {
		String route;
		if (routeInput.startsWith("Internet: ")) {
			route = routeInput.substring("Internet: ".length());
		} else {
			route = routeInput;
		}
		return route;
	}

	private int parseRouteType(final String routeInput) {
		int routeType = Call.ROUTE_FIXED_NETWORK;

		if (routeInput.contains("@")) {
			routeType = Call.ROUTE_SIP;
		} else if (routeInput.startsWith("Internet: ")) {
			routeType = Call.ROUTE_SIP;
		} else {
			routeType = Call.ROUTE_FIXED_NETWORK;
		}
		return routeType;
	}

	private CallType parseCallType(final String calltypestr)
			throws FeatureNotSupportedByFirmware {
		CallType calltype;
		if ("1".equals(calltypestr)) {
			calltype = CallType.CALLIN;
		} else if ("2".equals(calltypestr)) {
			calltype = CallType.CALLIN_FAILED;
		} else if ("3".equals(calltypestr) || "4".equals(calltypestr)) {
			calltype = CallType.CALLOUT;
		} else {
			Debug.error("CallListCsvLineParser: Invalid Call type while importing caller list!"); //$NON-NLS-1$
			throw new FeatureNotSupportedByFirmware("Get caller list", messages.getMessage("box.no_caller_list"));
		}
		return calltype;
	}

	private Date parseCallDate(final String datestr)
			throws FeatureNotSupportedByFirmware {
		Date calldate;
		if (datestr != null) {
			try {
				calldate = new SimpleDateFormat("dd.MM.yy HH:mm").parse(datestr); //$NON-NLS-1$
			} catch (ParseException e) {
				Debug.error("CallListCsvLineParser: Invalid date format while importing caller list!"); //$NON-NLS-1$
				throw new FeatureNotSupportedByFirmware("Get caller list", messages.getMessage("box.no_caller_list"));
			}
		} else {
			Debug.error("CallListCsvLineParser: Invalid date format while importing caller list!"); //$NON-NLS-1$
			throw new FeatureNotSupportedByFirmware("Get caller list", messages.getMessage("box.no_caller_list"));
		}
		return calldate;
	}

	private PhoneNumberOld parsePhoneNumber(final CallType calltype,
			final String phoneNumberStr) {
		PhoneNumberOld number;
		if (!"".equals(phoneNumberStr)) {
			number = new PhoneNumberOld(phoneNumberStr,
					JFritzUtils.parseBoolean(properties.getProperty("option.activateDialPrefix"))
					&& (calltype == CallType.CALLOUT));
		} else {
			number = null;
		}
		return number;
	}

	private Port parsePort(final FritzBox fritzBox, final String portStr) {
		Port port = null;
		try {
			int portId = Integer.parseInt(portStr);
			port = fritzBox.getConfiguredPort(portId);
			if (port == null) { // Fallback auf statisch konfigurierte Ports
				port = Port.getPort(portId);
			}
		} catch (NumberFormatException nfe) {
			// nothing to do, just proceed
		}

		if (port == null) {
			port = new Port(0, portStr, "-1", "-1");
		}

		return port;
	}

	private String[] parseTime(final String timeStr) throws FeatureNotSupportedByFirmware {
		String[] splitted = timeStr.split(":");
		if (splitted.length != 2) {
			throw new FeatureNotSupportedByFirmware("Get caller list", messages.getMessage("box.no_caller_list"));
		}

		try {
			Integer.parseInt(splitted[0]);
			Integer.parseInt(splitted[1]);
		} catch (NumberFormatException nfe) {
			throw new FeatureNotSupportedByFirmware("Get caller list", messages.getMessage("box.no_caller_list"));
		}
		return splitted;
	}
}
