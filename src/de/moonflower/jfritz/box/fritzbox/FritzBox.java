package de.moonflower.jfritz.box.fritzbox;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.box.BoxCallBackListener;
import de.moonflower.jfritz.box.BoxCallMonitorInterface;
import de.moonflower.jfritz.box.BoxClass;
import de.moonflower.jfritz.box.BoxStatusListener;
import de.moonflower.jfritz.callmonitor.CallMonitorInterface;
import de.moonflower.jfritz.callmonitor.CallMonitorStatusListener;
import de.moonflower.jfritz.callmonitor.CallmessageCallMonitor;
import de.moonflower.jfritz.callmonitor.FBoxCallMonitorV1;
import de.moonflower.jfritz.callmonitor.FBoxCallMonitorV3;
import de.moonflower.jfritz.callmonitor.YACCallMonitor;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.IProgressListener;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.Port;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.network.AddonInfosXMLHandler;
import de.moonflower.jfritz.utils.network.UPNPAddonInfosListener;
import de.moonflower.jfritz.utils.network.UPNPCommonLinkPropertiesListener;
import de.moonflower.jfritz.utils.network.UPNPExternalIpListener;
import de.moonflower.jfritz.utils.network.UPNPStatusInfoListener;
import de.moonflower.jfritz.utils.network.UPNPUtils;

public class FritzBox extends BoxClass {

	public final static byte QUERY_METHOD_UNKNOWN = 0;
	public final static byte QUERY_METHOD_OLD = 1;
	public final static byte QUERY_METHOD_NEW = 2;

	private final static String POSTDATA_QUERY = "getpage=../html/query.txt";

	private final static String PARSE_LOGIN_REASON = "var theReason = parseInt\\(\"([^\"]*)\",10\\)";
	// <UDN>uuid:75802409-bccb-40e7-8e6c-MACADDRESS</UDN>
	private final static String PARSE_MAC_ADDRESS = "<UDN>uuid:([^<]*)</UDN>";

	private final static String QUERY_GET_MAC_ADDRESS = "env:settings/macdsl";
	private final static String QUERY_GET_VERSION = "logic:status/nspver";
	private final static String QUERY_EXTERNAL_IP = "connection0:status/ip";

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

	private final static String QUERY_ANALOG_COUNT = "telcfg:settings/MSN/Port/count";
	private final static String QUERY_ANALOG_NAME = "telcfg:settings/MSN/Port%NUM%/Name";

	private final static String QUERY_ISDN_COUNT = "telcfg:settings/NTHotDialList/Name/count";
	private final static String QUERY_ISDN_NAME = "telcfg:settings/NTHotDialList/Name%NUM%";
	private final static String QUERY_ISDN_NUMBER = "telcfg:settings/NTHotDialList/Number%NUM%";

	private final static String QUERY_DECT_MINI_COUNT = "telcfg:settings/Foncontrol/User/count";
	private final static String QUERY_DECT_MINI_NAME = "telcfg:settings/Foncontrol/User%NUM%/Name";
	private final static String QUERY_DECT_MINI_ID = "telcfg:settings/Foncontrol/User%NUM%/Id";
	private final static String QUERY_DECT_MINI_INTERN = "telcfg:settings/Foncontrol/User%NUM%/Intern";
	private final static String QUERY_DECT_MINI_TYPE = "telcfg:settings/Foncontrol/User%NUM%/Type";

	private final static String QUERY_VOIP_ENABLED = "telcfg:settings/VoipExtension/enabled";
	private final static String QUERY_VOIP_COUNT = "telcfg:settings/VoipExtension/count";
	private final static String QUERY_VOIP_ACTIVATED = "telcfg:settings/VoipExtension%NUM%/enabled";
	private final static String QUERY_VOIP_NAME = "telcfg:settings/VoipExtension%NUM%/Name";

	private final static String QUERY_SIP_ID = "sip:settings/sip%NUM%/ID";
	private final static String QUERY_SIP_ACTIVATED = "sip:settings/sip%NUM%/activated";
	private final static String QUERY_SIP_REGISTRAR = "sip:settings/sip%NUM%/registrar";
	private final static String QUERY_SIP_MSN = "telcfg:settings/SIP%NUM%/MSN";
	private final static String QUERY_SIP_MAXCOUNT = "telcfg:settings/SIP/count";
//	private final static String QUERY_SIP_COUNT = "sip:settings/sip/count";
	private final static String QUERY_SIP_NAME = "sip:settings/sip%NUM%/Name";
	private final static String QUERY_SIP_NUMBER = "sip:settings/sip%NUM%/Number";
	private final static String QUERY_SIP_DISPLAYNAME = "sip:settings/sip%NUM%/displayname";
	private final static String QUERY_SIP_USERNAME = "sip:settings/sip%NUM%/username";
//	private final static String QUERY_SIP_REGISTRY_TYPE = "telcfg:settings/SIP%NUM%/RegistryType";


	//the following are strings used by the web services on the box
	//from XX.04.33 onwards
	private static String URL_SERVICE_ADDONINFOS = ":49000/upnp/control/WANCommonIFC1";  //$NON-NLS-1$
	private static String URN_SERVICE_ADDONINFOS = "urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1#GetAddonInfos"; //$NON-NLS-1$
	private static String URL_SERVICE_DSLLINK = ":49000/upnp/control/WANDSLLinkC1";
	private static String URN_SERVICE_DSLLINK = "urn:schemas-upnp-org:service:WANDSLLinkConfig:1#GetDSLLinkInfo";
	private static String URL_SERVICE_EXTERNALIP = ":49000/upnp/control/WANIPConn1";
	private static String URN_SERVICE_EXTERNALIP = "urn:schemas-upnp-org:service:WANIPConnection:1#GetExternalIPAddress";
	private static String URL_SERVICE_STATUSINFO = ":49000/upnp/control/WANIPConn1";
	private static String URN_SERVICE_STATUSINFO = "urn:schemas-upnp-org:service:WANIPConnection:1#GetStatusInfo";
	private static String URL_SERVICE_COMMONLINK= ":49000/upnp/control/WANCommonIFC1";
	private static String URN_SERVICE_COMMONLINK = "urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1#GetCommonLinkProperties";
	private static String URL_SERVICE_GETINFO = ":49000/upnp/control/any";
	private static String URN_SERVICE_GETINFO = "urn:schemas-any-com:service:Any:1#GetInfo";
	private static String URL_SERVICE_AUTOCONFIG = ":49000/upnp/control/WANDSLLinkC1";
	private static String URN_SERVICE_AUTOCONFIG = "urn:schemas-upnp-org:service:WANDSLLinkConfig:1#GetAutoConfig";
	private static String URL_SERVICE_CONNECTIONTYPEINFO = ":49000/upnp/control/WANIPConn1";
	private static String URN_SERVICE_CONNECTIONTYPEINFO = "urn:schemas-upnp-org:service:WANIPConnection:1#GetConnectionTypeInfo";
	private static String URL_SERVICE_GENERICPORTMAPPING = ":49000/upnp/control/WANIPConn1";
	private static String URN_SERVICE_GENERICPORTMAPPING = "urn:schemas-upnp-org:service:WANIPConnection:1#GetGenericPortMappingEntry";
	private static String URL_SERVICE_FORCETERMINATION = ":49000/upnp/control/WANIPConn1";
	private static String URN_SERVICE_FORCETERMINATION = "urn:schemas-upnp-org:service:WANIPConnection:1#ForceTermination";

//	private static String POSTDATA_REBOOT = "getpage=..%2Fhtml%2Freboot.html&errorpage=..%2Fhtml%2Fde%2Fmenus%2Fmenu2.html&var%3Apagename=reset&var%3Aerrorpagename=reset&var%3Amenu=system&var%3Apagemaster=&time%3Asettings%2Ftime=1250935088%2C-120&var%3AtabReset=0&logic%3Acommand%2Freboot=..%2Fgateway%2Fcommands%2Fsaveconfig.html";
	private static String POSTDATA_REBOOT = "getpage=..%2Fhtml%2Freboot.html&var%3Apagename=reset&var%3Amenu=system&var%3Apagemaster=&time%3Asettings%2Ftime=1250935088%2C-120&var%3AtabReset=0&logic%3Acommand%2Freboot=..%2Fgateway%2Fcommands%2Fsaveconfig.html";

	private static String POSTDATA_CALL = "&telcfg:settings/UseClickToDial=1&telcfg:settings/DialPort=$NEBENSTELLE&telcfg:command/Dial=$NUMMER"; //$NON-NLS-1$
	private static String POSTDATA_HANGUP = "&telcfg:settings/UseClickToDial=1&telcfg:command%2FHangup"; //$NON-NLS-1$

	private static String POSTDATA_CLEAR_JOURNAL = "&telcfg:settings/ClearJournal&telcfg:settings/UseJournal=1";

	private static int max_retry_count = 2;

	private FritzBoxFirmware firmware = null;

	private CallMonitorInterface callMonitor = null;

	private Vector<SipProvider> sipProvider;

	private HashMap<Integer, Port> configuredPorts;

	private byte queryMethod = QUERY_METHOD_UNKNOWN;

	private Vector<BoxStatusListener> boxListener;
	private Vector<BoxCallBackListener> callBackListener;

	public FritzBox(String name, String description,
					String protocol, String address, String port, String password,
					Exception exc)
	{
		this.name = name;
		this.description = description;

		this.protocol = protocol;
		this.address = address;
		this.port = port;
		this.password = password;

		sipProvider = new Vector<SipProvider>();
		configuredPorts = new HashMap<Integer, Port>();
		boxListener = new Vector<BoxStatusListener>(4);
		callBackListener = new Vector<BoxCallBackListener>(4);
		exc = null;
		try {
			setBoxConnected();
			updateSettings();
		} catch (WrongPasswordException e) {
			exc = e;
			Debug.error(Main.getMessage("box.wrong_password"));
			setBoxDisconnected();
		} catch (InvalidFirmwareException e) {
			exc = e;
			Debug.error(Main.getMessage("unknown_firmware"));
			setBoxDisconnected();
		} catch (IOException e) {
			exc = e;
			Debug.error(Main.getMessage("box.not_found"));
			setBoxDisconnected();
		}
	}

	public void updateSettings() throws WrongPasswordException, InvalidFirmwareException, IOException
	{
		long start = 0;
		long end = 0;

		start = JFritzUtils.getTimestamp();
			detectFirmware();
		end = JFritzUtils.getTimestamp();
		Debug.debug("UpdateSettings: detectFirmware " + (end - start) + "ms");
		start = end;
			detectQueryMethod();
		end = JFritzUtils.getTimestamp();
		Debug.debug("UpdateSettings: detectQueryMethod " + (end - start) + "ms");
		start = end;
			detectMacAddress();
		end = JFritzUtils.getTimestamp();
		Debug.debug("UpdateSettings: detectMacAddress " + (end - start) + "ms");
		//getSettings();
		start = end;
			detectSipProvider();
		end = JFritzUtils.getTimestamp();
		Debug.debug("UpdateSettings: detectSipProvider " + (end - start) + "ms");
		start = end;
			initializePorts();
		end = JFritzUtils.getTimestamp();
		Debug.debug("UpdateSettings: initializePorts " + (end - start) + "ms");
	}

	/**
	 * Detects firmware version
	 * @return
	 */
	private void detectFirmware() throws WrongPasswordException, InvalidFirmwareException, IOException {
		//avoid trying to access the box if running as a client
		if (Main.getProperty("network.type").equals("2")
				&& Boolean.parseBoolean(Main.getProperty("option.clientCallList")))
		{
			Debug.netMsg("JFritz is running as a client and using call list from server, canceling firmware detection");
		}
		else
		{
			setBoxConnected();
			firmware = null;
			firmware = FritzBoxFirmware.detectFirmwareVersion(name, protocol,
					address, password, port);
		}
	}

	private String getPostData(String pattern) throws UnsupportedEncodingException
	{
		pattern = pattern.replaceAll("\\$LANG", firmware.getLanguage());
		if (firmware.getSessionId() != "")
		{
			pattern = pattern + "&sid=" + firmware.getSessionId();
		}
		else
		{
			pattern = pattern + "&login%3Acommand%2Fpassword=" + URLEncoder.encode(password, "ISO-8859-1");
		}
		return firmware.getAccessMethod() + pattern;
	}

	private final void detectQueryMethod()
	{
		Vector<String> query = new Vector<String>();
		query.add(QUERY_GET_VERSION);
		Vector<String> response = new Vector<String>();
		if (((response = getQueryOld(query)).size() != 0)
			&& (!"".equals(response.get(0))))
		{
			queryMethod = QUERY_METHOD_OLD;
		}
		else if (((response = getQueryNew(query)).size() != 0)
				&& (!"".equals(response.get(0))))
		{
			queryMethod = QUERY_METHOD_NEW;
		}
		else
		{
			queryMethod = QUERY_METHOD_UNKNOWN;
		}
	}

	private final Vector<String> getQuery(Vector<String> queries)
	{
		Vector<String> result;
		if (queryMethod == QUERY_METHOD_OLD)
		{
			result = getQueryOld(queries);
		}
		else if (queryMethod == QUERY_METHOD_NEW)
		{
			result = getQueryNew(queries);
		}
		else
		{
			try {
				setBoxConnected();
				updateSettings();
				return getQuery(queries);
			} catch (WrongPasswordException wpe)
			{
				Debug.error("Wrong password");
				setBoxDisconnected();
			} catch (InvalidFirmwareException ife)
			{
				Debug.error("Invalid firmware");
				setBoxDisconnected();
			} catch (IOException ioe) {
				Debug.error("IO exception");
				setBoxDisconnected();
			}
			result = new Vector<String>();
		}

		Thread.yield();
		return result;
	}

	private final Vector<String> getFromFile(final String file)
	{
		Vector<String> result = new Vector<String>();
		BufferedReader br = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);

			String line = "";
			while ((line = br.readLine()) != null) {
				result.add(line);
			}
		} catch (FileNotFoundException fe) {
			System.err.println("Datei nicht gefunden: " + file);
		} catch (IOException e) {
			System.err.println("Kann aus Datei nicht lesen: " + file);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					System.err.println("Kann Datei nicht schließen: " + file);
				}
			}
		}
		return result;
	}

	private final String generatePostDataOld(Vector<String> queries)
	{
		String postdata = POSTDATA_QUERY + "&var:cnt=" + queries.size();
		for (int i=0; i<queries.size(); i++)
		{
			postdata = postdata + "&var:n" + i + "="+queries.get(i);
		}

		if (firmware.isSidLogin())
		{
			try {
			postdata = postdata + "&sid=" + URLEncoder.encode(firmware.getSessionId(), "ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				Debug.error("Encoding not supported");
				e.printStackTrace();
			}
		}
		else
		{
			try {
				postdata = postdata + "&login:command/password=" + URLEncoder.encode(this.password, "ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				Debug.error("Encoding not supported");
				e.printStackTrace();
			}
		}

		return postdata;
	}

	private final Vector<String> getQueryOld(Vector<String> queries)
	{
		Vector<String> response = new Vector<String>();
		if (firmware != null)
		{
			String postdata = generatePostDataOld(queries);

			final String urlstr = protocol + "://" + address +":" + port + "/cgi-bin/webcm"; //$NON-NLS-1$, //$NON-NLS-2$

			boolean finished = false;
			boolean password_wrong = false;
			int retry_count = 0;

			while (!finished && (retry_count < max_retry_count))
			{
				try {
					retry_count++;
					if (password_wrong)
					{
						password_wrong = false;
						Debug.debug("Detecting new firmware, getting new SID");
						this.detectFirmware();
						postdata = generatePostDataOld(queries);
					}
					response = JFritzUtils.fetchDataFromURLToVector(name, urlstr, postdata, true);
					finished = true;
				} catch (WrongPasswordException e) {
					password_wrong = true;
					Debug.debug("Wrong password, maybe SID is invalid.");
					setBoxDisconnected();
				} catch (SocketTimeoutException ste) {
					ste.printStackTrace();
					setBoxDisconnected();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					setBoxDisconnected();
				} catch (InvalidFirmwareException e) {
					password_wrong = true;
					setBoxDisconnected();
				}
			}

			if ((response.size() != 0)
			  && (response.size() > (queries.size()+1)))
			{
				Pattern p = Pattern.compile(PARSE_LOGIN_REASON);
				for (int i=0; i<response.size(); i++)
				{
					Matcher m = p.matcher(response.get(i));
					if (m.find())
					{
						try {
							int loginReason = Integer.parseInt(m.group(1));
							if (loginReason == 2) // SID-Timeout
							{
								try {
									Debug.debug("SessionID expired, getting new SessionId!");
									detectFirmware();
									response = getQueryNew(queries);
									response.add(""); // add empty line to be removed further down in this method
								} catch (WrongPasswordException e) {
									Debug.errDlg(Main.getMessage("box.wrong_password"));
									setBoxDisconnected();
								} catch (InvalidFirmwareException e) {
									Debug.errDlg(Main.getMessage("box.address_wrong"));
									setBoxDisconnected();
								} catch (IOException e) {
									Debug.errDlg("I/O Exception");
									setBoxDisconnected();
								}
							}
						} catch (NumberFormatException nfe)
						{
							Debug.errDlg("Could not login to FritzBox. Please check password and try it again!");
							setBoxDisconnected();
						}
					}
				}
			}

			if (response.size() != 0)
			{
				response.remove(response.size()-1); // letzte Zeile entfernen (leerzeile)
			}
		}

		Debug.debug("Query-Response: ");
		for (int i=0; i<response.size(); i++)
		{
			Debug.debug(response.get(i));
		}
		Debug.debug("---");
		return response;
	}

	private final String generatePostDataNew(Vector<String> queries)
	{
		String postdata = POSTDATA_QUERY;

		for (int i=0; i<queries.size(); i++)
		{
			postdata = postdata + "&var:n[" + i + "]="+queries.get(i);
		}

		if (firmware.isSidLogin())
		{
			try {
			postdata = postdata + "&sid=" + URLEncoder.encode(firmware.getSessionId(), "ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				Debug.error("Encoding not supported");
				e.printStackTrace();
			}
		}
		else
		{
			try {
				postdata = postdata + "&login:command/password=" + URLEncoder.encode(this.password, "ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				Debug.error("Encoding not supported");
				e.printStackTrace();
			}
		}

		return postdata;
	}

	private final Vector<String> getQueryNew(Vector<String> queries)
	{
		Vector<String> response = new Vector<String>();
		if (firmware != null)
		{
			String postData = generatePostDataNew(queries);

			final String urlstr = protocol + "://" + address +":" + port + "/cgi-bin/webcm"; //$NON-NLS-1$, //$NON-NLS-2$

			boolean finished = false;
			boolean password_wrong = false;
			int retry_count = 0;

			while (!finished && (retry_count < max_retry_count))
			{
				try {
					if (password_wrong)
					{
						password_wrong = false;
						Debug.debug("Detecting new firmware, getting new SID");
						this.detectFirmware();
						postData = generatePostDataNew(queries);
					}
					retry_count++;
					response = JFritzUtils.fetchDataFromURLToVector(name, urlstr, postData, true);
					finished = true;
				} catch (WrongPasswordException e) {
					password_wrong = true;
					Debug.debug("Wrong password, maybe SID is invalid.");
					setBoxDisconnected();
				} catch (SocketTimeoutException ste) {
					ste.printStackTrace();
					setBoxDisconnected();
				} catch (InvalidFirmwareException e) {
					password_wrong = true;
					setBoxDisconnected();
				} catch (IOException e) {
					e.printStackTrace();
					setBoxDisconnected();
				} catch (Exception e) {
					e.printStackTrace();
					setBoxDisconnected();
				}
			}

			if ((response.size() != 0)
			  && (response.size() > (queries.size()+1)))
			{
				Pattern p = Pattern.compile(PARSE_LOGIN_REASON);
				for (int i=0; i<response.size(); i++)
				{
					Matcher m = p.matcher(response.get(i));
					if (m.find())
					{
						try {
							int loginReason = Integer.parseInt(m.group(1));
							if (loginReason == 2) // SID-Timeout
							{
								try {
									Debug.debug("SessionID expired, getting new SessionId!");
									detectFirmware();
									response = getQueryNew(queries);
									response.add(""); // add empty line to be removed further down in this method
								} catch (WrongPasswordException e) {
									Debug.errDlg(Main.getMessage("box.wrong_password"));
									setBoxDisconnected();
								} catch (InvalidFirmwareException e) {
									Debug.errDlg(Main.getMessage("box.address_wrong"));
									setBoxDisconnected();
								} catch (IOException e) {
									Debug.errDlg("I/O Exception");
									setBoxDisconnected();
								}
							}
						} catch (NumberFormatException nfe)
						{
							Debug.errDlg("Could not login to FritzBox. Please check password and try it again!");
						}
					}
				}
			}

			if (response.size() != 0)
			{
				response.remove(response.size()-1); // letzte Zeile entfernen (leerzeile)
			}
		}

		Debug.debug("Query-Response: ");
		for (int i=0; i<response.size(); i++)
		{
			Debug.debug(response.get(i));
		}
		Debug.debug("---");
		return response;
	}

	public void detectMacAddress()
	{
		Vector<String> query = new Vector<String>();
		query.add(QUERY_GET_MAC_ADDRESS);

		Vector<String> response = getQuery(query);
		if (response.size() == 1)
		{
			macAddress = response.get(0);
		}
		else
		{
			setBoxConnected();
			macAddress = getMacFromUPnP();
			if ("".equals(macAddress))
			{
				macAddress = Main.getMessage("unknown");
			}
		}
	}

	public String getExternalIP() {
		Vector<String> query = new Vector<String>();
		query.add(QUERY_EXTERNAL_IP);

		Vector<String> response = getQuery(query);
		if (response.size() == 1) {
			return response.get(0);
		}
		else {
			return "No external IP";
		}
	}

	public FritzBoxFirmware getFirmware()
	{
		return firmware;
	}

	/**************************************************************************************
	 * Implementation of port detection
	 **************************************************************************************/
	public void initializePorts(){
		configuredPorts.clear();
		addAnalogPorts();
		addIsdnPorts();
		addDectMiniPorts();
		addVoIPPorts();
		addOtherPorts();
	}

	public Port getConfiguredPort(int id)
	{
		if (configuredPorts.get(id) != null)
		{
			return configuredPorts.get(id);
		}
		else
		{
			return null;
		}
	}

	public Vector<Port> getConfiguredPorts()
	{
		Vector<Port> ports = new Vector<Port>();

		Collection<Port> collection = configuredPorts.values();
		for (Port port: collection)
		{
			port.setBox(this);
			ports.add(port);
		}

		return ports;
	}

	private void addConfiguredPort(Port port)
	{
		configuredPorts.put(port.getId(), port);
	}

	private void addAnalogPorts()
	{
		// detect configured ports
		Vector<String> query = new Vector<String>();
		query.add(QUERY_ANALOG_COUNT);
		Vector<String> response = getQuery(query);

		addConfiguredPort(new Port(-1, Main.getMessage("analog_telephones_all"), "9", "9"));
		if (response.size() == 1)
		{
			try {
				int analogCount = Integer.parseInt(response.get(0));
				query.clear();
				for (int i=0; i<analogCount; i++)
				{
					query.add(QUERY_ANALOG_NAME.replaceAll("%NUM%", Integer.toString(i)));
				}
				response = getQuery(query);

				if (response.size() == 1*analogCount)
				{
					for (int i=0; i<analogCount; i++)
					{

						String analogName = response.get(i+0);
						if (!"".equals(analogName)
						 && !"er".equals(analogName))
						{
							addConfiguredPort(new Port(i, analogName, Integer.toString(i+1), Integer.toString(i+1)));
						}
					}
				}
			} catch (NumberFormatException nfe)
			{
				Debug.warning("No analog ports available.");
			}
		}
	}

	private void addIsdnPorts()
	{
		// detect configured ports
		Vector<String> query = new Vector<String>();
		query.add(QUERY_ISDN_COUNT);
		Vector<String> response = getQuery(query);

		if (response.size() == 1)
		{
			try {
				int isdnCount = Integer.parseInt(response.get(0));
				if (isdnCount > 0)
				{
					addConfiguredPort(new Port(50, Main.getMessage("isdn_telephones_all"), "50", "50"));

					query.clear();
					for (int i=0; i<isdnCount; i++)
					{
						query.add(QUERY_ISDN_NUMBER.replaceAll("%NUM%", Integer.toString(i+1)));
						query.add(QUERY_ISDN_NAME.replaceAll("%NUM%", Integer.toString(i+1)));
					}
					response = getQuery(query);

					if (response.size() == 2*isdnCount)
					{
						for (int i=0; i<isdnCount; i++)
						{
							String number = response.get((i*2) + 0);
							String name = response.get((i*2) + 1);

							if ((!"er".equals(number))
								&& (!"".equals(number))) // if number is set
							{
								if (!"er".equals(name))
								{
									if ("".equals(name)) // if name is empty:
									{
										name = "ISDN " + Integer.toString(i+1);
									}

									addConfiguredPort(new Port(50+(i+1),
											name,
											"5"+(i+1),
											"5"+(i+1)));
								}
							}
						}
					}

				}
			} catch (NumberFormatException nfe)
			{
				Debug.warning("No isdn devices available.");
			}
		}
	}

	private void addDectMiniPorts()
	{
		// detect configured ports
		Vector<String> query = new Vector<String>();
		query.add(QUERY_DECT_MINI_COUNT);
		Vector<String> response = getQuery(query);

		if (response.size() == 1)
		{
			try {
				int dectCount = Integer.parseInt(response.get(0));
				if (dectCount > 0)
				{
					query.clear();
					for (int i=0; i<dectCount; i++)
					{
						query.add(QUERY_DECT_MINI_ID.replaceAll("%NUM%", Integer.toString(i)));
						query.add(QUERY_DECT_MINI_NAME.replaceAll("%NUM%", Integer.toString(i)));
						query.add(QUERY_DECT_MINI_INTERN.replaceAll("%NUM%", Integer.toString(i)));
						query.add(QUERY_DECT_MINI_TYPE.replaceAll("%NUM%", Integer.toString(i)));
					}
					response = getQuery(query);

					if (response.size() == 4*dectCount)
					{
						for (int i=0; i<dectCount; i++)
						{
							String id = response.get((i*4) + 0);
							String name = response.get((i*4) + 1);
							String internal = response.get((i*4) + 2);
							String type = response.get((i*4) + 3);
							String num = "";
							if (internal.length() >= 3) {
								num = internal.substring(2);
							}
							Debug.debug("ID: " + id);
							Debug.debug("Name: " + name);
							Debug.debug("Internal: " + internal);
							Debug.debug("Num: " + num);
							Debug.debug("Type: " + type);

							if ("".equals(name))
							{
								name = "DECT " + i;
							}

							if (!"".equals(internal)) {
								addConfiguredPort(new Port(10+Integer.parseInt(num),
										name,
										"6"+num,
										internal));
							}
						}
					}
				}
			} catch (NumberFormatException nfe)
			{
				Debug.warning("No dect/mini devices available.");
			}
		}
	}

	private void addVoIPPorts()
	{
		// detect configured ports
		Vector<String> query = new Vector<String>();
		query.add(QUERY_VOIP_ENABLED);
		query.add(QUERY_VOIP_COUNT);
		Vector<String> response = getQuery(query);

		if (response.size() == 2)
		{
			boolean voipEnabled =  response.get(0).equals("1");
			try {
				int voipCount = Integer.parseInt(response.get(1));
//				if (voipEnabled)
				{
					query.clear();
					for (int i=0; i<voipCount; i++)
					{
						query.add(QUERY_VOIP_ACTIVATED.replaceAll("%NUM%", Integer.toString(i)));
						query.add(QUERY_VOIP_NAME.replaceAll("%NUM%", Integer.toString(i)));
					}
					response = getQuery(query);

					if (response.size()==2*voipCount)
					{
						for (int i=0; i<voipCount; i++)
						{
							boolean activated = "1".equals(response.get((i*2) + 0));
							if (activated)
							{
								String voipName = response.get((i*2) + 1);
								if ("".equals(voipName))
								{
									voipName = Main.getMessage("voip_extension")+ " " + Integer.toString(i+620);
								}
								addConfiguredPort(new Port(20+i,
										voipName,
										Integer.toString(20+i),
										"62"+Integer.toString(i)));
							}
						}
					}
				}
			} catch (NumberFormatException nfe)
			{
				Debug.warning("No VoIP extensions available.");
			}
		}
	}

	private void addOtherPorts()
	{
		// add static configured ports
		addConfiguredPort(new Port(3, Main.getMessage("call_through"), "-1", "-1"));
		addConfiguredPort(new Port(4, Main.getMessage("isdn"), "-1", "-1"));
		addConfiguredPort(new Port(5, Main.getMessage("fax_fon"), "-1", "-1"));
		addConfiguredPort(new Port(6, Main.getMessage("answering_machine"), "-1", "-1"));
		addConfiguredPort(new Port(32, Main.getMessage("data_fon_1"), "-1", "-1"));
		addConfiguredPort(new Port(33, Main.getMessage("data_fon_2"), "-1", "-1"));
		addConfiguredPort(new Port(34, Main.getMessage("data_fon_3"), "-1", "-1"));
		addConfiguredPort(new Port(36, Main.getMessage("data_fon_isdn"), "-1", "-1"));
	}

	/**************************************************************************************
	 * Implementation of the BoxCallMonitorInterface
	 **************************************************************************************/

	/**
	 * @see BoxCallMonitorInterface.startCallMonitor
	 */
	public int startCallMonitor(Vector<CallMonitorStatusListener> listener) {
		switch (Integer.parseInt(Main.getProperty("option.callMonitorType"))) //$NON-NLS-1$
		{
			case 1: {
				if ((firmware != null) && (firmware.getMajorFirmwareVersion() == 3)
						&& (firmware.getMinorFirmwareVersion() < 96)) {
					Debug.errDlg(Main
							.getMessage("callmonitor_error_wrong_firmware")); //$NON-NLS-1$

					for (int i=0; i<listener.size(); i++)
					{
						listener.get(i).setDisconnectedStatus(this.name);
					}
					return BoxCallMonitorInterface.CALLMONITOR_FIRMWARE_INCOMPATIBLE;
				} else {
					if ((firmware != null) && (firmware.getMajorFirmwareVersion() >= 4)
							&& (firmware.getMinorFirmwareVersion() >= 3)) {
						if (callMonitor != null)
						{
							Debug.errDlg(Main.getMessage("callmonitor_already_started"));
						} else {
							callMonitor = new FBoxCallMonitorV3(this, listener);
						}
					} else {
						if (callMonitor != null)
						{
							Debug.errDlg(Main.getMessage("callmonitor_already_started"));
						} else {
							callMonitor = new FBoxCallMonitorV1(this, listener);
						}
					}
					return BoxCallMonitorInterface.CALLMONITOR_STARTED;
				}
			}
			case 2: {
				callMonitor = new YACCallMonitor(name,
						Integer.parseInt(Main.getProperty("option.yacport")), //$NON-NLS-1$
						listener);
				return BoxCallMonitorInterface.CALLMONITOR_STARTED;
			}
			case 3: {
				callMonitor = new CallmessageCallMonitor(name,
						Integer.parseInt(Main.getProperty("option.callmessageport")), //$NON-NLS-1$
						listener);
				return BoxCallMonitorInterface.CALLMONITOR_STARTED;
			}
			default: {
				return BoxCallMonitorInterface.CALLMONITOR_NOT_CONFIGURED;
			}
		}
	}

	/**
	 * @see BoxCallMonitorInterface.stopCallMonitor
	 */
	public void stopCallMonitor(Vector<CallMonitorStatusListener> listener) {
		if (callMonitor != null)
		{
			callMonitor.stopCallMonitor();
			callMonitor = null;
			for (int i=0; i<listener.size(); i++)
			{
				listener.get(i).setDisconnectedStatus(this.name);
			}
		}
	}

	public boolean isCallMonitorConnected()
	{
		if (callMonitor != null)
		{
			return callMonitor.isConnected();
		}

		return false;
	}

	/**************************************************************************************
	 * Implementation of the BoxCallListInterface
	 **************************************************************************************/
	public Vector<Call> getCallerList(Vector<IProgressListener> progressListener)
	throws IOException, MalformedURLException {
		if (true) {
			return getCallerListFromBox(progressListener);
		} else {
			return getCallerListFromFile(progressListener);
		}
	}

	private Vector<Call> getCallerListFromBox(Vector<IProgressListener> progressListener)
			throws IOException, MalformedURLException {

		setBoxConnected();
		// getting number of entries
		Vector<String> query = new Vector<String>();
		query.add(QUERY_CALLS_REFRESH);
		query.add(QUERY_NUM_CALLS);

		Vector<String> response = getQuery(query);
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
					response = getQuery(query);

					if (response.size() == querySize*stepSize)
					{
						boolean result = createCallFromResponse(tmpCalls, response, querySize, stepSize);
						if (!result)
						{
							throw new IOException("Malformed data while receiving caller list!");
						}
						else
						{
							for (BoxCallBackListener listener: callBackListener)
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
			setBoxDisconnected();
		}

		return new Vector<Call>();
	}

	private Vector<Call> getCallerListFromFile(Vector<IProgressListener> progressListener)
	throws IOException, MalformedURLException {
		Vector<String> response = new Vector<String>();
		response = getFromFile("D:\\callerlist.dat");

		Vector<Call> newCalls = new Vector<Call>(response.size());
		Vector<Call> tmpCalls = new Vector<Call>(response.size());
		int querySize = 8;
		boolean result = createCallFromResponse(tmpCalls, response, querySize, response.size()/querySize);
		if (!result)
		{
			throw new IOException("Malformed data while receiving caller list!");
		}
		else
		{
			newCalls.addAll(tmpCalls);
		}
		for (IProgressListener listener: progressListener)
		{
			listener.setProgress(100);
		}

		return newCalls;
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
					calltype = new CallType("call_in");
				} else if ((response.get(newOffset+0).equals("2"))) {
					calltype = new CallType("call_in_failed");
				} else if ((response.get(newOffset+0).equals("3"))) {
					calltype = new CallType("call_out");
				} else {
					Debug.error("Invalid Call type while importing caller list!"); //$NON-NLS-1$
					return false;
				}

				Date calldate;
				// Call date and time
				if (response.get(newOffset+1) != null) {
					try {
						calldate = new SimpleDateFormat("dd.MM.yy HH:mm").parse(response.get(newOffset+1)); //$NON-NLS-1$
					} catch (ParseException e) {
						Debug.error("Invalid date format while importing caller list!"); //$NON-NLS-1$
						return false;
					}
				} else {
					Debug.error("Invalid date format while importing caller list!"); //$NON-NLS-1$
					return false;
				}

				// Phone number
				PhoneNumberOld number;
				if (!response.get(newOffset+2).equals("")) {
					number = new PhoneNumberOld(response.get(newOffset+2), Main.getProperty(
							"option.activateDialPrefix").toLowerCase().equals("true")
							&& (calltype.toInt() == CallType.CALLOUT)
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
					port = this.getConfiguredPort(portId);
					if (port == null) { // Fallback auf statisch konfigurierte Ports
						port = Port.getPort(portId);
					}
				} catch (NumberFormatException nfe)
				{
					Debug.warning("FritzBox: Could not parse portstr as number: " + portStr);
				}
				if (port == null)
				{
					port = new Port(0, portStr, "-1", "-1");
				}

				int routeType = Integer.parseInt(response.get(newOffset+6));
				String route = "";
				if (routeType == 0) // Festnetz
				{
					route = response.get(newOffset+5);
					if ("".equals(route))
					{
						route = Main.getMessage("fixed_network");
					}
				}
				else if (routeType == 1) // SIP
				{
					int id = Integer.parseInt(response.get(newOffset+5));
					for (SipProvider provider: sipProvider)
					{
						if (provider.isProviderID(id))
						{
							route = provider.toString();
							break;
						}
					}
				}
				else
				{
					route = "ERROR";
					Debug.error("Could not determine route type: " + routeType);
				}

				// make the call object and exit
				Call call = new Call(calltype, calldate, number, port, route,
						Integer.parseInt(time[0])* 3600 + Integer.parseInt(time[1]) * 60);

				calls.add(call);
			}
		}

		return true;
	}

	public void clearCallerList()
	{
		if ((firmware != null) && (firmware.getMajorFirmwareVersion() == 4)
						&& (firmware.getMinorFirmwareVersion() < 86)) {
			clearJournalOld();
		} else {
			clearJournalNew();
		}
	}

	public void clearJournalOld() {
		Vector<String> query = new Vector<String>();
		query.add("telcfg:settings/ClearJournal");
		getQuery(query);
	}

	public void clearJournalNew()
	{
        String postdata = POSTDATA_CLEAR_JOURNAL;

		try {
			postdata = this.getPostData(postdata);
		} catch (UnsupportedEncodingException e) {
			Debug.error("Encoding not supported! " + e.toString());
		}

		String urlstr = protocol+"://" //$NON-NLS-1$
			+ this.address + ":" + this.port
			+ "/cgi-bin/webcm"; //$NON-NLS-1$

		boolean finished = false;
		boolean password_wrong = false;
		int retry_count = 0;

		while (!finished && (retry_count < max_retry_count))
		{
			try {
				retry_count++;
				if (password_wrong)
				{
					password_wrong = false;
					Debug.debug("Detecting new firmware, getting new SID");
					this.detectFirmware();
			        postdata = POSTDATA_CLEAR_JOURNAL;

					try {
						postdata = this.getPostData(postdata);
					} catch (UnsupportedEncodingException e) {
						Debug.error("Encoding not supported! " + e.toString());
					}
				}
				JFritzUtils.fetchDataFromURLToVector(
						this.getName(), urlstr, postdata, true);
				finished = true;
			} catch (WrongPasswordException e) {
				password_wrong = true;
				Debug.debug("Wrong password, maybe SID is invalid.");
				setBoxDisconnected();
			} catch (SocketTimeoutException ste) {
				ste.printStackTrace();
				setBoxDisconnected();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setBoxDisconnected();
			} catch (InvalidFirmwareException e) {
				password_wrong = true;
				setBoxDisconnected();
			}
		}
	}


	/**************************************************************************************
	 * Implementation of the BoxSipProviderInterface
	 **************************************************************************************/
	public void detectSipProvider() {
		setBoxConnected();
		sipProvider.clear();

		Vector<String> query = new Vector<String>();
		query.add(QUERY_SIP_MAXCOUNT);
		Vector<String> response = getQuery(query);

		if (response.size() == 1)
		{
			int numQueries = 8;
			int sipCount = Integer.parseInt(response.get(0));
			Debug.debug("Number of SIP Providers: " + sipCount);

			query.clear();
			for (int i=0; i<sipCount; i++)
			{
				query.add(QUERY_SIP_ACTIVATED.replaceAll("%NUM%", Integer.toString(i)));
				query.add(QUERY_SIP_ID.replaceAll("%NUM%", Integer.toString(i)));
				query.add(QUERY_SIP_REGISTRAR.replaceAll("%NUM%", Integer.toString(i)));
				query.add(QUERY_SIP_MSN.replaceAll("%NUM%", Integer.toString(i)));
				query.add(QUERY_SIP_NAME.replaceAll("%NUM%", Integer.toString(i)));
				query.add(QUERY_SIP_NUMBER.replaceAll("%NUM%", Integer.toString(i)));
				query.add(QUERY_SIP_DISPLAYNAME.replaceAll("%NUM%", Integer.toString(i)));
				query.add(QUERY_SIP_USERNAME.replaceAll("%NUM%", Integer.toString(i)));
//				query.add(QUERY_SIP_REGISTRY_TYPE.replaceAll("%NUM%", Integer.toString(i)));
			}

			response = getQuery(query);
			if (response.size() == sipCount*numQueries)
			{
				for (int i=0; i<sipCount; i++)
				{
					int offset = i * numQueries;
					if (!"er".equals(response.get(offset+0)))
					{
						int id = Integer.parseInt(response.get(offset+1));
						String name = response.get(offset+2);
						int numberId = (id * numQueries) + 3;
						String number = response.get(numberId);
						Debug.debug("id= " + id + " NumberID=" +numberId + " Number=" + number + " Name="+name);
						if (!"".equals(number))
						{
							Debug.debug("SIP-Provider["+i+"]: id="+id+" Number="+number+ " Name="+name);
							SipProvider newSipProvider =
								new SipProvider(id,
												number,
												name);
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
		else
		{
			setBoxDisconnected();
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

	/**************************************************************************************
	 * Implementation of UPnP-Statistics Interface
	 **************************************************************************************/
	/**
	 * This function calls one of the upnp web services of the box and returns the raw data
	 * the data returned has the following format
	 *
	 * <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body>
	 * <u:GetAddonInfosResponse xmlns:u="urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1">
	 * <NewByteSendRate>0</NewByteSendRate>
	 * <NewByteReceiveRate>0</NewByteReceiveRate>
	 * <NewPacketSendRate>0</NewPacketSendRate>
	 * <NewPacketReceiveRate>0</NewPacketReceiveRate>
	 * <NewTotalBytesSent>0</NewTotalBytesSent>
	 * <NewTotalBytesReceived>0</NewTotalBytesReceived>
	 * <NewAutoDisconnectTime>300</NewAutoDisconnectTime>
	 * <NewIdleDisconnectTime>7</NewIdleDisconnectTime>
	 * <NewDNSServer1>X.X.X.X</NewDNSServer1>
	 * <NewDNSServer2>Y.Y.Y.Y</NewDNSServer2>
	 * <NewVoipDNSServer1>Z.Z.Z.Z</NewVoipDNSServer1>
	 * <NewVoipDNSServer2>0.0.0.0</NewVoipDNSServer2>
	 * <NewUpnpControlEnabled>0</NewUpnpControlEnabled>
	 * <NewRoutedBridgedModeBoth>0</NewRoutedBridgedModeBoth>
	 * </u:GetAddonInfosResponse>
	 * </s:Body> </s:Envelope>
	 *
	 * @return the raw xml from the web service of the box
	 */
	public void getInternetStats(UPNPAddonInfosListener listener){

		String xml =
	        "<?xml version=\"1.0\"?>\n" +
	        "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
	        +"s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
	        "<s:Body>" +
	        "<u:GetAddonInfos xmlns:u=\"urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1\"></u:GetAddonInfos>\n"	+
	        "</s:Body>\n" +
	        "</s:Envelope>";

		String result = UPNPUtils.getSOAPData(protocol + "://" + getAddress() +
				URL_SERVICE_ADDONINFOS, URN_SERVICE_ADDONINFOS, xml);

//		Debug.msg("Result of getAddonInfos: "+ result);

		if (!result.equals(""))
		{
			try {
				XMLReader reader = SAXParserFactory.newInstance().newSAXParser()
						.getXMLReader();
				reader.setContentHandler(new AddonInfosXMLHandler(listener));
				reader.parse(new InputSource(new StringReader(
						result)));

			} catch (ParserConfigurationException e1) {
				Debug.error(e1.toString());
			} catch (SAXException e1) {
				Debug.error(e1.toString());
			} catch (IOException e1) {
				Debug.error(e1.toString());
			}
		}
	}

	/**
	 * This functions contains various web service calls that are not currently
	 * used but may be used in the future
	 *
	 */
	private void getWebservice(){

		String xml =
	        "<?xml version=\"1.0\"?>\n" +
	        "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
	        +"s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
	        "<s:Body><u:GetDSLLinkInfo xmlns:u=\"urn:schemas-upnp-org:service:WANDSLLinkConfig:1\"></u:GetDSLLinkInfo>\n"	+
	        "</s:Body>\n" +
	        "</s:Envelope>";

		// String result =
		UPNPUtils.getSOAPData(protocol+"://" + getAddress() +
			URL_SERVICE_DSLLINK, URN_SERVICE_DSLLINK, xml);

		/*	This is the result of the web service
			<?xml version="1.0"?>
			<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body>
			<u:GetDSLLinkInfoResponse xmlns:u="urn:schemas-upnp-org:service:WANDSLLinkConfig:1">
			<NewLinkType>PPPoA</NewLinkType>
			<NewLinkStatus>Up</NewLinkStatus>
			</u:GetDSLLinkInfoResponse>
			</s:Body> </s:Envelope>
		 	*/

//		Debug.msg("Result of GetDSLLinkInfo: "+ result);

	}

	public void getStatusInfo(UPNPStatusInfoListener listener)
	{
		String xml =
	        "<?xml version=\"1.0\"?>\n" +
	        "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
	        +"s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
	        "<s:Body><u:GetDSLLinkInfo xmlns:u=\"urn:schemas-upnp-org:service:WANDSLLinkConfig:1\"></u:GetDSLLinkInfo>\n"	+
	        "</s:Body>\n" +
	        "</s:Envelope>";

		String result = UPNPUtils.getSOAPData(protocol+"://" + getAddress() +
				URL_SERVICE_STATUSINFO, URN_SERVICE_STATUSINFO, xml);

//		Debug.msg("Result of dsl getStatusInfo: "+ result);

		Pattern p = Pattern.compile("<NewUptime>([^<]*)</NewUptime>");
		Matcher m = p.matcher(result);
		if(m.find())
			listener.setUptime(m.group(1));
		else
			listener.setUptime("-");

		/*
		<?xml version="1.0"?>
		<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body>
		<u:GetStatusInfoResponse xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1">
		<NewConnectionStatus>Connected</NewConnectionStatus>
		<NewLastConnectionError>ERROR_NONE</NewLastConnectionError>
		<NewUptime>3574</NewUptime>
		</u:GetStatusInfoResponse>
		</s:Body> </s:Envelope>
		*/
	}

	/**
	 * function gets the external IP address from the box using the upnp web services
	 *
	 * @return
	 */
	public void getExternalIPAddress(UPNPExternalIpListener listener){

		String xml =
			"<?xml version=\"1.0\"?>\n" +
			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
			+"s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
			"<s:Body><u:GetExternalIPAddress xmlns:u=\"urn:schemas-upnp-org:service:WANIPConnection:1\"></u:GetExternalIPAddress>\n"	+
			"</s:Body>\n" +
			"</s:Envelope>";

		String result = UPNPUtils.getSOAPData(protocol+"://" + getAddress() +
				URL_SERVICE_EXTERNALIP, URN_SERVICE_EXTERNALIP, xml);

		/*
		<?xml version="1.0"?>
		<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body>
		<u:GetExternalIPAddressResponse xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1">
		<NewExternalIPAddress>93.216.135.71</NewExternalIPAddress>
		</u:GetExternalIPAddressResponse>
		</s:Body> </s:Envelope>
		 */

//		Debug.msg("External IP response: "+result);

		Pattern p = Pattern.compile("<NewExternalIPAddress>([^<]*)</NewExternalIPAddress>");
		Matcher m = p.matcher(result);
		if(m.find())
			listener.setExternalIp(m.group(1));
		else
			listener.setExternalIp("-");
	}

	public void getCommonLinkInfo(UPNPCommonLinkPropertiesListener listener){
		String xml =
			"<?xml version=\"1.0\"?>\n" +
			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
			+"s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
			"<s:Body><u:GetCommonLinkProperties xmlns:u=\"urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1\"></u:GetCommonLinkProperties>\n"	+
			"</s:Body>\n" +
			"</s:Envelope>";

		String result =  UPNPUtils.getSOAPData(protocol+"://" + getAddress() +
				URL_SERVICE_COMMONLINK, URN_SERVICE_COMMONLINK, xml);

//		Debug.debug("Result of getCommonLinkProperties: "+ result);

		Pattern p = Pattern.compile("<NewLayer1UpstreamMaxBitRate>([^<]*)</NewLayer1UpstreamMaxBitRate>");
		Matcher m = p.matcher(result);
		if(m.find())
			listener.setUpstreamMaxBitRate(m.group(1));
		else
			listener.setUpstreamMaxBitRate("-");

		p = Pattern.compile("<NewLayer1DownstreamMaxBitRate>([^<]*)</NewLayer1DownstreamMaxBitRate>");
		m = p.matcher(result);
		if(m.find())
			listener.setDownstreamMaxBitRate(m.group(1));
		else
			listener.setDownstreamMaxBitRate("-");

		/*  This is the response
		<?xml version="1.0"?>
		<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body>
		<u:GetCommonLinkPropertiesResponse xmlns:u="urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1">
		<NewWANAccessType>DSL</NewWANAccessType>
		<NewLayer1UpstreamMaxBitRate>10044000</NewLayer1UpstreamMaxBitRate>
		<NewLayer1DownstreamMaxBitRate>51384000</NewLayer1DownstreamMaxBitRate>
		<NewPhysicalLinkStatus>Up</NewPhysicalLinkStatus>
		</u:GetCommonLinkPropertiesResponse>
		</s:Body> </s:Envelope>
		*/
	}

	private void getInfo() {
		String xml =
			"<?xml version=\"1.0\"?>\n" +
			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
			"s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
			"<s:Body><u:GetInfo xmlns:u=\"urn:schemas-any-com:service:Any:1\"></u:GetInfo>\n" +
			"</s:Body>\n" +
			"</s:Envelope>";

//		String result =
			UPNPUtils.getSOAPData(protocol+"://" + getAddress() +
				URL_SERVICE_GETINFO, URN_SERVICE_GETINFO, xml);

//		Debug.msg("Result of getInfo: "+ result);

		/*
		<?xml version="1.0"?>
		<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body>
		<u:GetInfoResponse xmlns:u="urn:schemas-any-com:service:Any:1">
		<NewBoxid>123</NewBoxid>
		<NewMacaddress>456</NewMacaddress>
		<NewProductname>FRITZ!Box</NewProductname>
		<NewHostname></NewHostname>
		<NewLanguage></NewLanguage>
		<NewHardwarelist></NewHardwarelist>
		<NewUsbPluglist></NewUsbPluglist>
		<NewExtendedInfo></NewExtendedInfo>
		</u:GetInfoResponse>
		</s:Body> </s:Envelope>
		*/
	}
	private void getAutoConfig() {
		String xml =
			"<?xml version=\"1.0\"?>\n" +
			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
			"s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
			"<s:Body><u:GetAutoConfig xmlns:u=\"urn:schemas-upnp-org:service:WANDSLLinkConfig:1\"></u:GetAutoConfig>\n" +
			"</s:Body>\n" +
			"</s:Envelope>";

		String result =  UPNPUtils.getSOAPData(protocol+"://" + getAddress() +
				URL_SERVICE_AUTOCONFIG, URN_SERVICE_AUTOCONFIG, xml);

		Debug.info("Result of getAutoConfig: "+ result);

		/*
		<?xml version="1.0"?>
		<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body>
		<u:GetAutoConfigResponse xmlns:u="urn:schemas-upnp-org:service:WANDSLLinkConfig:1">
		<NewAutoConfig>0</NewAutoConfig>
		</u:GetAutoConfigResponse>
		</s:Body> </s:Envelope>
		 */
	}

	private void getConnectionTypeInfo() {
		String xml =
			"<?xml version=\"1.0\"?>\n" +
			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
			":encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
			"<s:Body><u:GetConnectionTypeInfo xmlns:u=\"urn:schemas-upnp-org:service:WANIPConnection:1\"></u:GetConnectionTypeInfo>\n" +
			"</s:Body>\n" +
			"</s:Envelope>";

//		String result =
			UPNPUtils.getSOAPData(protocol+"://" + getAddress() +
				URL_SERVICE_CONNECTIONTYPEINFO, URN_SERVICE_CONNECTIONTYPEINFO, xml);

//		Debug.msg("Result of getConnectionTypeInfo: "+ result);

		/*
		<?xml version="1.0"?>
		<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body>
		<u:GetConnectionTypeInfoResponse xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1">
		<NewConnectionType>IP_Routed</NewConnectionType>
		<NewPossibleConnectionTypes>IP_Routed</NewPossibleConnectionTypes>
		</u:GetConnectionTypeInfoResponse>
		</s:Body> </s:Envelope>
		 */
	}

	private void getGenericPortMappingEntry()
	{
		String xml =
			"<?xml version=\"1.0\"?>\n" +
			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
			"s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
			"<s:Body><u:GetGenericPortMappingEntry xmlns:u=\"urn:schemas-upnp-org:service:WANIPConnection:1\">\n" +
			"<NewPortMappingIndex></NewPortMappingIndex>\n" +
			"</u:GetGenericPortMappingEntry>\n" +
			"</s:Body>\n" +
			"</s:Envelope>";

//		String result =
			UPNPUtils.getSOAPData(protocol+"://" + getAddress() +
				URL_SERVICE_GENERICPORTMAPPING, URN_SERVICE_GENERICPORTMAPPING, xml);

//		Debug.msg("Result of getGenericPortMappingEntry: "+ result);
	}

	public void renewIPAddress() {
	String xml =
		"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
		"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
		"s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
		"<s:Body><u:ForceTermination xmlns:u=\"urn:schemas-upnp-org:service:WANIPConnection:1\" />\n" +
		"</s:Body>\n" +
		"</s:Envelope>";

		UPNPUtils.getSOAPData(protocol+"://" + getAddress() +
				URL_SERVICE_FORCETERMINATION, URN_SERVICE_FORCETERMINATION, xml);
	}

	public String getMacFromUPnP() {
		String mac = "";
		Vector<String> response = new Vector<String>();
		if (firmware != null)
		{
			final String urlstr = protocol+"://" + address +":49000/igddesc.xml"; //$NON-NLS-1$, //$NON-NLS-2$

			try {
				response = JFritzUtils.fetchDataFromURLToVector(name, urlstr, null, true);
			} catch (WrongPasswordException e) {
				Debug.debug("Wrong password, maybe SID is invalid.");
				setBoxDisconnected();
			} catch (SocketTimeoutException ste) {
				ste.printStackTrace();
				setBoxDisconnected();
			} catch (IOException e) {
				e.printStackTrace();
				setBoxDisconnected();
			} catch (Exception e) {
				e.printStackTrace();
				setBoxDisconnected();
			}

			if (response.size() != 0)
			{
				Pattern p = Pattern.compile(PARSE_MAC_ADDRESS);
				for (int i=0; i<response.size(); i++)
				{
					Matcher m = p.matcher(response.get(i));
					if (m.find())
					{
						String resp = m.group(1);
						int idx = resp.lastIndexOf("-");
						String macTmp = resp.substring(idx+1);
						for (int j=0; j<macTmp.length(); j++) {
							mac = mac + macTmp.charAt(j);
							if ((j != 0)
								&& (j != macTmp.length()-1)
								&& ((j-1)%2 == 0))
							{
								mac = mac.concat(":");
							}
						}
						break;
					}
				}
			}
		}

		return mac;
	}

	/**************************************************************************************
	 * Implementation of DoCall-Interface
	 **************************************************************************************/
	private String generateDoCallPostData(String currentNumber, Port port) {
		String postdata = POSTDATA_CALL;

		postdata = postdata.replaceAll("\\$NUMMER", currentNumber); //$NON-NLS-1$
		postdata = postdata.replaceAll("\\$NEBENSTELLE", port.getDialPort()); //$NON-NLS-1$

		try {
			postdata = this.getPostData(postdata);
		} catch (UnsupportedEncodingException e) {
			Debug.error("Encoding not supported! " + e.toString());
		}

		return postdata;
	}

	public void doCall(PhoneNumberOld number, Port port) {
		setBoxConnected();
		String currentNumber = number.getAreaNumber();
		currentNumber = currentNumber.replaceAll("\\+", "00"); //$NON-NLS-1$,  //$NON-NLS-2$

		String postdata = generateDoCallPostData(currentNumber, port);
		String urlstr = protocol+"://" //$NON-NLS-1$
						+ this.address + ":" + this.port
						+ "/cgi-bin/webcm"; //$NON-NLS-1$

		boolean finished = false;
		boolean password_wrong = false;
		int retry_count = 0;

		while (!finished && (retry_count < max_retry_count))
		{
			try {
				retry_count++;
				if (password_wrong)
				{
					password_wrong = false;
					Debug.debug("Detecting new firmware, getting new SID");
					this.detectFirmware();
					postdata = generateDoCallPostData(currentNumber, port);
				}
				JFritzUtils.fetchDataFromURLToVector(
						this.getName(), urlstr, postdata, true);
				finished = true;
			} catch (WrongPasswordException e) {
				password_wrong = true;
				Debug.debug("Wrong password, maybe SID is invalid.");
				setBoxDisconnected();
			} catch (SocketTimeoutException ste) {
				ste.printStackTrace();
				setBoxDisconnected();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setBoxDisconnected();
			} catch (InvalidFirmwareException e) {
				password_wrong = true;
				setBoxDisconnected();
			}
		}
	}

	public void hangup(Port port)
	{
		setBoxConnected();
        String postdata = POSTDATA_HANGUP;

		try {
			postdata = this.getPostData(postdata);
		} catch (UnsupportedEncodingException e) {
			Debug.error("Encoding not supported! " + e.toString());
		}

		String urlstr = protocol+"://" //$NON-NLS-1$
			+ this.address + ":" + this.port
			+ "/cgi-bin/webcm"; //$NON-NLS-1$

		boolean finished = false;
		boolean password_wrong = false;
		int retry_count = 0;

		while (!finished && (retry_count < max_retry_count))
		{
			try {
				retry_count++;
				if (password_wrong)
				{
					password_wrong = false;
					Debug.debug("Detecting new firmware, getting new SID");
					this.detectFirmware();
			        postdata = POSTDATA_HANGUP;

					try {
						postdata = this.getPostData(postdata);
					} catch (UnsupportedEncodingException e) {
						Debug.error("Encoding not supported! " + e.toString());
					}
				}
				JFritzUtils.fetchDataFromURLToVector(
						this.getName(), urlstr, postdata, true);
				finished = true;
			} catch (WrongPasswordException e) {
				password_wrong = true;
				Debug.debug("Wrong password, maybe SID is invalid.");
				setBoxDisconnected();
			} catch (SocketTimeoutException ste) {
				ste.printStackTrace();
				setBoxDisconnected();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setBoxDisconnected();
			} catch (InvalidFirmwareException e) {
				password_wrong = true;
				setBoxDisconnected();
			}
		}
	}

	public void addBoxStatusListener(BoxStatusListener listener)
	{
		if (!boxListener.contains(listener)) {
			boxListener.add(listener);
		}
	}

	public void removeBoxStatusListener(BoxStatusListener listener)
	{
		if (boxListener.contains(listener)) {
			boxListener.remove(listener);
		}
	}

	private void setBoxConnected() {
		for (BoxStatusListener listener: boxListener) {
			listener.setBoxConnected(name);
		}
	}

	private void setBoxDisconnected() {
		for (BoxStatusListener listener: boxListener) {
			listener.setBoxDisconnected(name);
		}
	}

	public void addBoxCallBackListener(BoxCallBackListener listener)
	{
		if (!callBackListener.contains(listener)) {
			callBackListener.add(listener);
		}
	}

	public void reboot() throws WrongPasswordException {
		final String urlstr = protocol+"://" + address + ":" + port + "/cgi-bin/webcm"; //$NON-NLS-1$, //$NON-NLS-2$
		boolean password_wrong = true;
		int retry_count = 0;
		int max_retry_count = 2;

		while ((password_wrong) && (retry_count < max_retry_count)) {
			String postdata = POSTDATA_REBOOT;
			Debug.debug("Retry count: " + retry_count);
			retry_count++;

			if (firmware.isSidLogin())
			{
				try {
				postdata = postdata + "&sid=" + URLEncoder.encode(firmware.getSessionId(), "ISO-8859-1");
				} catch (UnsupportedEncodingException e) {
					Debug.error("Encoding not supported");
					e.printStackTrace();
				}
			}
			else
			{
				try {
					postdata = postdata + "&login:command/password=" + URLEncoder.encode(this.password, "ISO-8859-1");
				} catch (UnsupportedEncodingException e) {
					Debug.error("Encoding not supported");
					e.printStackTrace();
				}
			}

			Vector<String> data = new Vector<String>(1000);
			try {
				data = JFritzUtils.fetchDataFromURLToVector(name,
						urlstr, postdata, true);
				password_wrong = false;
				for (int i=0; i<data.size(); i++) {
					Debug.debug(data.get(i));
				}
			} catch (WrongPasswordException wpe) {
				password_wrong = true;
				if (retry_count == max_retry_count) {
					throw wpe;
				}
				try {
					Thread.sleep(wpe.getRetryTime() * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (SocketTimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
