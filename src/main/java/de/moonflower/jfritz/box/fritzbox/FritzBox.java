package de.moonflower.jfritz.box.fritzbox;

import java.io.IOException;
import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.box.BoxCallBackListener;
import de.moonflower.jfritz.box.BoxCallListInterface;
import de.moonflower.jfritz.box.BoxCallMonitorInterface;
import de.moonflower.jfritz.box.BoxClass;
import de.moonflower.jfritz.box.fritzbox.callerlist.FritzBoxCallerListFactory;
import de.moonflower.jfritz.box.fritzbox.sipprovider.FritzBoxSipProvider;
import de.moonflower.jfritz.callmonitor.CallMonitorInterface;
import de.moonflower.jfritz.callmonitor.CallMonitorStatusListener;
import de.moonflower.jfritz.callmonitor.CallmessageCallMonitor;
import de.moonflower.jfritz.callmonitor.FBoxCallMonitorV1;
import de.moonflower.jfritz.callmonitor.FBoxCallMonitorV3;
import de.moonflower.jfritz.callmonitor.YACCallMonitor;
import de.moonflower.jfritz.dialogs.simple.LoginDialog;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.exceptions.FeatureNotSupportedByFirmware;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.IProgressListener;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.Port;
import de.moonflower.jfritz.utils.ComplexJOptionPaneMessage;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.network.AddonInfosXMLHandler;
import de.moonflower.jfritz.utils.network.UPNPAddonInfosListener;
import de.moonflower.jfritz.utils.network.UPNPCommonLinkPropertiesListener;
import de.moonflower.jfritz.utils.network.UPNPExternalIpListener;
import de.moonflower.jfritz.utils.network.UPNPStatusInfoListener;
import de.moonflower.jfritz.utils.network.UPNPUtils;
import org.jfritz.fboxlib.enums.LoginMode;
import org.jfritz.fboxlib.exceptions.FirmwareNotDetectedException;
import org.jfritz.fboxlib.exceptions.InvalidCredentialsException;
import org.jfritz.fboxlib.exceptions.InvalidSessionIdException;
import org.jfritz.fboxlib.exceptions.LoginBlockedException;
import org.jfritz.fboxlib.exceptions.PageNotFoundException;
import org.jfritz.fboxlib.fritzbox.FirmwareVersion;
import org.jfritz.fboxlib.fritzbox.FritzBoxCommunication;

public class FritzBox extends BoxClass {
	private final static Logger log = Logger.getLogger(FritzBox.class);

	// <UDN>uuid:75802409-bccb-40e7-8e6c-MACADDRESS</UDN>
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

// 31.07.2015 Wahl per Lua
	private final static String URL_DIAL_FONBOOK_LUA = "/fon_num/dial_fonbook.lua";
	private final static String URL_FONBOOK_LIST_LUA = "/fon_num/fonbook_list.lua";
	private final static String QUERY_DialPort = "telcfg:settings/DialPort";

	private FritzBoxCommunication fbc;

	private String igdupnp = "upnp";  // 01.08.2015 // fbc.getNetworkMethods().getUPNPFromIgddesc();

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

	private static String URL_SERVICE_REBOOT = ":49000/upnp/control/deviceconfig"; // 01.08.2015
	private static String URN_SERVICE_REBOOT = "urn:dslforum-org:service:DeviceConfig:1#Reboot"; // 01.08.2015
	private static String URL_SERVICE_CREATEURLSID = ":49000/upnp/control/deviceconfig"; // 01.08.2015
	private static String URN_SERVICE_CREATEURLSID = "urn:dslforum-org:service:DeviceConfig:1#X_AVM-DE_CreateUrlSID"; // 01.08.2015

	private static int max_retry_count = 2;

	private FirmwareVersion firmware = null;

	private CallMonitorInterface callMonitor = null;

	private FritzBoxSipProvider sipProvider;

	private HashMap<Integer, Port> configuredPorts;

	private Vector<BoxCallBackListener> callBackListener;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	private BoxCallListInterface callList;
	private boolean shouldPopupLoginCredentials = true;
	
	public FritzBox(String name, String description,
					String protocol, String address, String port, boolean useUsername, String username, String password)
	{
		this.name = name;
		this.description = description;

		this.protocol = protocol;
		this.port = port;

		this.useUsername = useUsername;
		this.username = username;
		this.password = password;

		sipProvider = new FritzBoxSipProvider();
		configuredPorts = new HashMap<Integer, Port>();
		callBackListener = new Vector<BoxCallBackListener>(4);

		if ("".equals(address)) {
			this.address = "fritz.box";
		} else {
			this.address = address;
		}
	}

	public void init(boolean shouldPopupLoginCredentials) {
		this.shouldPopupLoginCredentials = shouldPopupLoginCredentials;
		try {
			setBoxConnected();
			updateSettings();
		} catch (WrongPasswordException e) {
			log.error(messages.getMessage("box.wrong_password"));
			setBoxDisconnected();
		} catch (InvalidFirmwareException e) {
			log.error(messages.getMessage("unknown_firmware"));
			setBoxDisconnected();
		} catch (IOException e) {
			log.error(messages.getMessage("box.not_found"));
			setBoxDisconnected();
		}
	}

	public void detectFirmware() throws IOException, FirmwareNotDetectedException, PageNotFoundException {
		fbc = new FritzBoxCommunication(this.protocol, this.address, this.port);
		
		try {
			firmware = fbc.getFirmwareVersion();
			fbc.detectLoginMethod();
		} catch (ClientProtocolException e1) {
			log.error(e1.getMessage());
			setBoxDisconnected();
			throw e1;
		} catch (IOException e1) {
			log.error(messages.getMessage("box.not_found"));
			setBoxDisconnected();
			throw e1;
		} catch (PageNotFoundException e1) {
			setBoxDisconnected();
			handlePageNotFoundException(e1);
			throw e1;
		} catch (FirmwareNotDetectedException e1) {
			setBoxDisconnected();
			handleFirmwareNotDetectedException(e1);
			throw e1;
		}
	}

	public void detectFirmwareAndLogin() throws InvalidCredentialsException, LoginBlockedException, IOException, PageNotFoundException, FirmwareNotDetectedException {
		fbc = new FritzBoxCommunication(this.protocol, this.address, this.port);
		if (this.useUsername) {
			fbc.setUserName(this.username);
		} else {
			fbc.setUserName("");
		}
		fbc.setPassword(this.password);

		firmware = fbc.getFirmwareVersion();
		if (firmware == null) {
			log.warn("Could not detect firmware. SystemStatus: " + fbc.getSystemStatus());
			throw new FirmwareNotDetectedException("Could not detect firmware, do not try to login");
		} else {
			log.debug(firmware.toString());
		}
		fbc.login();
	}

	public Exception updateSettings() throws WrongPasswordException, InvalidFirmwareException, IOException
	{
		Exception exc = null;
		long start = 0;
		long end = 0;

		end = JFritzUtils.getTimestamp();
		start = end;

		log.debug("UpdateSettings: start of detectFirmwareAndLogin");
		try {
			detectFirmwareAndLogin();
		} catch (ClientProtocolException e) {
			log.error(e.getMessage());
			setBoxDisconnected();
		} catch (InvalidCredentialsException e) {
			setBoxDisconnected();
			if (shouldPopupLoginCredentials) {
				shouldPopupLoginCredentials = false;
				handleInvalidCredentialsException(e);
				if (showLoginDialog(e)) {
					properties.saveConfigProperties();
					updateSettings();
				}
			}
		} catch (LoginBlockedException e) {
			setBoxDisconnected();
			if (shouldPopupLoginCredentials) {
				shouldPopupLoginCredentials = false;
				handleLoginBlockedException(e);
				if (showLoginDialog(e)) {
					properties.saveConfigProperties();
					updateSettings();
				}
			}
		} catch (IOException e) {
			log.error(messages.getMessage("box.not_found"));
			setBoxDisconnected();
		} catch (PageNotFoundException e) {
			setBoxDisconnected();
			handlePageNotFoundException(e);
		} catch (FirmwareNotDetectedException e) {
			setBoxDisconnected();
			handleFirmwareNotDetectedException(e);
		}
		
		end = JFritzUtils.getTimestamp();
		log.debug("UpdateSettings: detectFirmwareAndLogin " + (end - start) + "ms");
		start = end;

		// 01.08.2015
		log.debug("UpdateSettings: start of getUPNPFromIgddesc");
		String rep = "";
		try {
			rep = fbc.getNetworkMethods().getUPNPFromIgddesc(); //getUPNPFromIgddesc();
			setIgdupnp(rep);
		} catch (InvalidSessionIdException e) {
			setBoxDisconnected();
			handleInvalidSessionIdException(e);
		} catch (InvalidCredentialsException e) {
			setBoxDisconnected();
			handleInvalidCredentialsException(e);
		} catch (LoginBlockedException e) {
			setBoxDisconnected();
			handleLoginBlockedException(e);
		} catch (ClientProtocolException e) {
			// nothing to do, IgdUPNP will be set correctly
		} catch (IOException e) {
			// nothing to do, IgdUPNP will be set correctly
		} catch (PageNotFoundException e) {
			// nothing to do, IgdUPNP will be set correctly
		}

		end = JFritzUtils.getTimestamp();
		log.debug("UpdateSettings: getUPNPFromIgddesc " + (end - start) + "ms");

		start = end;
		detectMacAddress();
		end = JFritzUtils.getTimestamp();
		log.debug("UpdateSettings: detectMacAddress " + (end - start) + "ms");

		//getSettings();
		start = end;
			detectSipProvider();
		end = JFritzUtils.getTimestamp();
		log.debug("UpdateSettings: detectSipProvider " + (end - start) + "ms");
		start = end;
			initializePorts();
		end = JFritzUtils.getTimestamp();
		log.debug("UpdateSettings: initializePorts " + (end - start) + "ms");
			callList = FritzBoxCallerListFactory.createFritzBoxCallListFromFirmware(firmware, this, callBackListener);

		return exc;
	}

	public LoginMode getLoginMode() {
		if (fbc == null) {
			return LoginMode.NONE;
		}
		return fbc.getLoginMode();
	}

	public String getLastLoginUserName() {
		return fbc.getLastUserName();
	}

	public String getPageAsString(final String url) throws ClientProtocolException, IOException, LoginBlockedException, InvalidCredentialsException, PageNotFoundException {
		if (fbc.isLoggedIn()) {
			try {
				return fbc.getPageAsString(url);
			} catch (InvalidSessionIdException e) {
				setBoxDisconnected();
				handleInvalidSessionIdException(e);
				return "";
			}
		} else {
			return "";
		}
	}

	public String postToPageAndGetAsString(final String url, List<NameValuePair> params) throws ClientProtocolException, IOException, LoginBlockedException, InvalidCredentialsException, PageNotFoundException {
		if (fbc.isLoggedIn()) {
			try {
				return fbc.postToPageAndGetAsString(url, params);
			} catch (InvalidSessionIdException e) {
				setBoxDisconnected();
				handleInvalidSessionIdException(e);
				return "";
			}
		} else {
			return "";
		}
	}

	public Vector<String> postToPageAndGetAsVector(final String url, List<NameValuePair> params) throws ClientProtocolException, IOException, LoginBlockedException, InvalidCredentialsException, PageNotFoundException {
		if (fbc.isLoggedIn()) {
			try {
				return fbc.postToPageAndGetAsVector(url, params);
			} catch (InvalidSessionIdException e) {
				setBoxDisconnected();
				handleInvalidSessionIdException(e);
				return new Vector<String>();
			}
		} else {
			return new Vector<String>();
		}
	}

	public final Vector<String> getQuery(Vector<String> queries) //throws ClientProtocolException, IOException, LoginBlockedException, InvalidCredentialsException, PageNotFoundException
	{
		// FIXME throw exceptions!!!! 
		Vector<String> result = new Vector<String>();
		
		if (fbc.isLoggedIn()) {
			try {
				result = fbc.getQuery(queries);
			} catch (InvalidSessionIdException e) {
				handleInvalidSessionIdException(e);
			} catch (ClientProtocolException e) {
				log.error(e.getMessage());
			} catch (IOException e) {
				log.error(e.getMessage());
			} catch (LoginBlockedException e) {
				handleLoginBlockedException(e);
			} catch (InvalidCredentialsException e) {
				handleInvalidCredentialsException(e);
			} catch (PageNotFoundException e) {
				handlePageNotFoundException(e);
			}
			Thread.yield();
		}
		
		return result;
	}
	
	public String getUrlPrefix() {
		return protocol + "://" + address + ":" + port;
	}

	public String getWebcmUrl() {
		return getUrlPrefix() + "/cgi-bin/webcm"; //$NON-NLS-1$, //$NON-NLS-2$, //$NON-NLS-3$
	}

	public void detectMacAddress()
	{
		if (fbc.isLoggedIn()) {
			macAddress = messages.getMessage("unknown");

			try {
				macAddress = fbc.getNetworkMethods().getMacAddress();
			} catch (Exception e) {
				macAddress = messages.getMessage("unknown");
			}
			
			if ("".equals(macAddress))
			{
				macAddress = messages.getMessage("unknown");
			}
		} else {
			macAddress = properties.getProperty("box.mac");
		}
	}

	public String getExternalIP() {
		try {
			if (fbc.isLoggedIn()) {
				try {
					return fbc.getNetworkMethods().getExternalIP();
				} catch (Exception e) {
					return "No external IP detected";
				}
			} else {
				return "No external IP detected";
			}
		} catch (Exception e) {
			return "No external IP detected";
		}
	}

	public FirmwareVersion getFirmware()
	{
		return firmware;
	}

	/**************************************************************************************
	 * Implementation of port detection
	 **************************************************************************************/
	public void initializePorts() {
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
			Port cloned = new Port(port.getId(), port.getName(), port.getDialPort(), port.getInternalNumber());
			cloned.setBox(this);
			ports.add(cloned);
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

		//addConfiguredPort(new Port(-1, messages.getMessage("analog_telephones_all"), "9", "9"));
		if (response.size() != 1) {
			log.warn("addAnalogPorts: received invalid response size. Will not add any analog ports");
			outputStringVector(response);
		} else {
			try {
				int analogCount = Integer.parseInt(response.get(0));
				log.debug("addAnalogPorts: Detected " + analogCount + " analog phones");
				
				if (analogCount > 0) {
					query.clear();
	
					for (int i=0; i<analogCount; i++)
					{
						query.add(QUERY_ANALOG_NAME.replaceAll("%NUM%", Integer.toString(i)));
					}
					response = getQuery(query);
	
					if (response.size() != 1*analogCount) {
						log.warn("addAnalogPorts: Response invalid!");
					} else {
						for (int i=0; i<analogCount; i++)
						{
							String analogName = response.get(i+0);
							if (!"".equals(analogName) && !"er".equals(analogName))
							{
								Port port = new Port(i, analogName, Integer.toString(i+1), Integer.toString(i+1));
								log.debug("addAnalogPorts: Adding port " + port.toStringDetailed());
								addConfiguredPort(port);
							}
						}
					}
				}
			} catch (NumberFormatException nfe)
			{
				log.warn("No analog ports available.");
			}
		}
	}

	private void outputStringVector(Vector<String> response) {
		for (int i=0; i<response.size(); i++) {
			log.warn(response.get(i));
		}
	}

	private void addIsdnPorts()
	{
		// detect configured ports
		Vector<String> query = new Vector<String>();
		query.add(QUERY_ISDN_COUNT);
		Vector<String> response = getQuery(query);

		if (response.size() != 1) {
			log.warn("addIsdnPorts: received invalid response size. Will not add any ISDN ports");
			outputStringVector(response);
		} else {
			try {
				int isdnCount = Integer.parseInt(response.get(0));
				log.debug("addIsdnPorts: Detected " + isdnCount + " ISDN phones");

				if (isdnCount > 0)
				{
					addConfiguredPort(new Port(50, messages.getMessage("isdn_telephones_all"), "50", "50"));

					query.clear();
					for (int i=0; i<isdnCount; i++)
					{
						query.add(QUERY_ISDN_NUMBER.replaceAll("%NUM%", Integer.toString(i+1)));
						query.add(QUERY_ISDN_NAME.replaceAll("%NUM%", Integer.toString(i+1)));
					}
					response = getQuery(query);

					if (response.size() != 2*isdnCount) {
						log.warn("addIsdnPorts: Response invalid!");
					} else {
						for (int i=0; i<isdnCount; i++)
						{
							String number = response.get((i*2) + 0);
							String name = response.get((i*2) + 1);

							if ("er".equals(number) || ("".equals(number))) {
								log.warn("addIsdnPorts: number is not set. Will not add port");
							} else {
								if ("er".equals(name)) {
									log.warn("addIsdnPorts: name is not set for number " + number + ". Will not add port");
								} else {
									if ("".equals(name)) // if name is empty:
									{
										name = "ISDN " + Integer.toString(i+1);
									}

									Port port = new Port(50+(i+1), name, "5"+(i+1), "5"+(i+1));
									log.debug("addIsdnPorts: Adding port " + port.toStringDetailed());
									addConfiguredPort(port);
								}
							}
						}
					}

				}
			} catch (NumberFormatException nfe)
			{
				log.warn("No isdn devices available.");
			}
		}
	}

	private void addDectMiniPorts()
	{
		// detect configured ports
		Vector<String> query = new Vector<String>();
		query.add(QUERY_DECT_MINI_COUNT);
		Vector<String> response = getQuery(query);

		if (response.size() != 1) {
			log.warn("addDectMiniPorts: received invalid response size. Will not add any DECT ports");
			outputStringVector(response);
		} else {
			try {
				int dectCount = Integer.parseInt(response.get(0));
				log.debug("addDectMiniPorts: Detected " + dectCount + " DECT phones");

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

					if (response.size() != 4*dectCount) {
						log.warn("addDectMiniPorts: Response invalid!");
					} else {
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
							log.debug("ID: " + id);
							log.debug("Name: " + name);
							log.debug("Internal: " + internal);
							log.debug("Num: " + num);
							log.debug("Type: " + type);

							if ("".equals(name))
							{
								name = "DECT " + i;
							}

							if ("".equals(internal)) {
								log.warn("addDectMiniPorts: internal number is not set. Will not add port");
							} else {
								Port port = new Port(10+Integer.parseInt(num), name, "6"+num, internal);
								log.debug("addDectMiniPorts: Adding port " + port.toStringDetailed());
								addConfiguredPort(port);
							}
						}
					}
				}
			} catch (NumberFormatException nfe)
			{
				log.warn("No dect/mini devices available.");
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

		if (response.size() != 2) {
			log.warn("addVoIPPorts: received invalid response size. Will not add any VoIP ports");
			outputStringVector(response);
		} else {
			@SuppressWarnings("unused")
			boolean voipEnabled =  response.get(0).equals("1");
			try {
				int voipCount = Integer.parseInt(response.get(1));
				log.debug("addVoIPPorts: Detected " + voipCount + " VoIP phones");

				if (voipCount > 0) {
					query.clear();
					for (int i=0; i<voipCount; i++)
					{
						query.add(QUERY_VOIP_ACTIVATED.replaceAll("%NUM%", Integer.toString(i)));
						query.add(QUERY_VOIP_NAME.replaceAll("%NUM%", Integer.toString(i)));
					}
					response = getQuery(query);

					if (response.size()!=2*voipCount) {
						log.warn("addVoIPPorts: Response invalid!");
					} else {
						for (int i=0; i<voipCount; i++)
						{
							String voipName = response.get((i*2) + 1);
							if ("".equals(voipName))
							{
								voipName = messages.getMessage("voip_extension")+ " " + Integer.toString(i+620);
							}

							boolean activated = "1".equals(response.get((i*2) + 0));
							if (!activated) {
								log.warn("addVoIPPorts: VoIP account '" + voipName + "'is not activated. Will not add port");
							} else {
								// Wählhilfe mit VoIP geht zumindest ab 06.03 nicht mehr, ging sie davor? (getestet mit 06.03 und 06.30) Ab welcher FW bis zu welcher?
								Port port = new Port(20+i, voipName, Integer.toString(20+i), "62"+Integer.toString(i));
								log.debug("addVoIPPorts: Adding port " + port.toStringDetailed());
								addConfiguredPort(port);
							}
						}
					}
				}
			} catch (NumberFormatException nfe)
			{
				log.warn("No VoIP extensions available.");
			}
		}
	}

	private void addOtherPorts()
	{
		// add static configured ports
		addConfiguredPort(new Port(3, messages.getMessage("call_through"), "-1", "-1"));
		addConfiguredPort(new Port(4, messages.getMessage("isdn"), "-1", "-1"));
		addConfiguredPort(new Port(5, messages.getMessage("fax_fon"), "-1", "-1"));
		addConfiguredPort(new Port(6, messages.getMessage("answering_machine"), "-1", "-1"));
		addConfiguredPort(new Port(32, messages.getMessage("data_fon_1"), "-1", "-1"));
		addConfiguredPort(new Port(33, messages.getMessage("data_fon_2"), "-1", "-1"));
		addConfiguredPort(new Port(34, messages.getMessage("data_fon_3"), "-1", "-1"));
		addConfiguredPort(new Port(36, messages.getMessage("data_fon_isdn"), "-1", "-1"));
	}

	/**************************************************************************************
	 * Implementation of the BoxCallMonitorInterface
	 **************************************************************************************/

	/**
	 * @see BoxCallMonitorInterface.startCallMonitor
	 */
	public int startCallMonitor(Vector<CallMonitorStatusListener> listener) {
		log.debug("Starting call monitor ...");
		switch (Integer.parseInt(properties.getProperty("option.callMonitorType"))) //$NON-NLS-1$
		{
			case 1: {
				if ((firmware != null) && (firmware.isLowerThan(3, 96))) {
					String message = messages.getMessage("callmonitor_error_wrong_firmware"); //$NON-NLS-1$
					log.error(message);
					Debug.errDlg(message);

					for (int i=0; i<listener.size(); i++)
					{
						listener.get(i).setDisconnectedStatus(this.name);
					}
					return BoxCallMonitorInterface.CALLMONITOR_FIRMWARE_INCOMPATIBLE;
				} else {
					if ((firmware != null) && firmware.isLowerThan(4, 3)) {
						log.debug("Firmware is greater/or equal than 03.96 but lower than 04.03");
						if (callMonitor != null)
						{
							String message = messages.getMessage("callmonitor_already_started");
							log.error(message);
							Debug.errDlg(message);
						} else {
							log.debug("Creating FBoxCallMonitorV1");
							callMonitor = new FBoxCallMonitorV1(this, listener, true);
						}
					} else {
						log.debug("Firmware is greater/or equal than 04.03");
						if (callMonitor != null)
						{
							String message = messages.getMessage("callmonitor_already_started");
							log.error(message);
							Debug.errDlg(message);
						} else {
							log.debug("Creating FBoxCallMonitorV3");
							callMonitor = new FBoxCallMonitorV3(this, listener, true);
						}
					}
					return BoxCallMonitorInterface.CALLMONITOR_STARTED;
				}
			}
			case 2: {
				callMonitor = new YACCallMonitor(name,
						Integer.parseInt(properties.getProperty("option.yacport")), //$NON-NLS-1$
						listener);
				return BoxCallMonitorInterface.CALLMONITOR_STARTED;
			}
			case 3: {
				callMonitor = new CallmessageCallMonitor(name,
						Integer.parseInt(properties.getProperty("option.callmessageport")), //$NON-NLS-1$
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
	throws FeatureNotSupportedByFirmware, ClientProtocolException, IOException, LoginBlockedException, InvalidCredentialsException, PageNotFoundException {
		Vector<Call> result;
		setBoxConnected();

		if (callList == null || firmware == null || !fbc.isLoggedIn()) {
			try {
				updateSettings();
				if (callList == null) {
					String message = messages.getMessage("box.no_caller_list");
					log.error(message);
					Debug.errDlg(message);
					result = new Vector<Call>();
				} else {
					result = callList.getCallerList(progressListener);
				}
			} catch (WrongPasswordException e) {
				String message = messages.getMessage("box.wrong_password"); //$NON-NLS-1$
				log.error(message, e);
				Debug.errDlg(message);
				result = new Vector<Call>();
			} catch (InvalidFirmwareException e) {
				String message = messages.getMessage("unknown_firmware"); //$NON-NLS-1$
				log.error(message, e);
				Debug.errDlg(message);
				result = new Vector<Call>();
			}
		} else {
			result = callList.getCallerList(progressListener);
		}
		return result;
	}

	public void clearCallerList() throws ClientProtocolException, IOException, LoginBlockedException, InvalidCredentialsException, PageNotFoundException
	{
		if (callList == null) {
			String message = messages.getMessage("box.no_clear_caller_list");
			log.error(message);
			Debug.errDlg(message);
		} else {
			callList.clearCallerList();
		}
	}

	/**************************************************************************************
	 * Implementation of the BoxSipProviderInterface
	 **************************************************************************************/
	public void detectSipProvider() {
		sipProvider.detectSipProvider(this);
	}

	public SipProvider getSipProvider(int id)
	{
		return sipProvider.getSipProvider(id);
	}

	public Vector<SipProvider> getSipProvider() {
		return sipProvider.getSipProvider();
	}

	public SipProvider getSipProviderByRoute(String route) {
		return sipProvider.getSipProviderByRoute(route);
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
				URL_SERVICE_ADDONINFOS.replace("upnp", getIgdupnp()), URN_SERVICE_ADDONINFOS, xml); // 01.08.2015

//		Debug.msg("Result of getAddonInfos: "+ result);

		if (!result.equals(""))
		{
			try {
				XMLReader reader = SAXParserFactory.newInstance().newSAXParser()
						.getXMLReader();
				reader.setContentHandler(new AddonInfosXMLHandler(listener));
				reader.parse(new InputSource(new StringReader(result)));

			} catch (ParserConfigurationException e1) {
				log.error(e1.toString());
			} catch (SAXException e1) {
				log.error(e1.toString());
			} catch (IOException e1) {
				log.error(e1.toString());
			}
		}
	}

	/**
	 * This functions contains various web service calls that are not currently
	 * used but may be used in the future
	 *
	 */
	@SuppressWarnings("unused")
	private void getWebservice(){

		String xml =
	        "<?xml version=\"1.0\"?>\n" +
	        "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
	        +"s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
	        "<s:Body><u:GetDSLLinkInfo xmlns:u=\"urn:schemas-upnp-org:service:WANDSLLinkConfig:1\"></u:GetDSLLinkInfo>\n"	+
	        "</s:Body>\n" +
	        "</s:Envelope>";

		UPNPUtils.getSOAPData(protocol+"://" + getAddress() +
				URL_SERVICE_DSLLINK.replace("upnp", getIgdupnp()), URN_SERVICE_DSLLINK, xml); // 01.08.2015

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
				URL_SERVICE_STATUSINFO.replace("upnp", getIgdupnp()), URN_SERVICE_STATUSINFO, xml); // 01.08.2015

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
			"<s:Body><u:GetExternalIPAddress xmlns:u=\"urn:schemas-upnp-org:service:WANIPConnection:1\">\n" +
			"<NewExternalIPAddress>0.0.0.0</NewExternalIPAddress>\n" +
			"</u:GetExternalIPAddress>\n" +
			"</s:Body>\n" +
			"</s:Envelope>";

		String result = UPNPUtils.getSOAPData(protocol+"://" + getAddress() +
				URL_SERVICE_EXTERNALIP.replace("upnp", getIgdupnp()), URN_SERVICE_EXTERNALIP, xml);

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
				URL_SERVICE_COMMONLINK.replace("upnp", getIgdupnp()), URN_SERVICE_COMMONLINK, xml); // 01.08.2015

//		log.debug("Result of getCommonLinkProperties: "+ result);

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

	@SuppressWarnings("unused")
	private void getInfo() {
		String xml =
			"<?xml version=\"1.0\"?>\n" +
			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
			"s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
			"<s:Body><u:GetInfo xmlns:u=\"urn:schemas-any-com:service:Any:1\"></u:GetInfo>\n" +
			"</s:Body>\n" +
			"</s:Envelope>";

			UPNPUtils.getSOAPData(protocol+"://" + getAddress() +
					URL_SERVICE_GETINFO.replace("upnp", getIgdupnp()), URN_SERVICE_GETINFO, xml); // 01.08.2015

			//Debug.info("Result of getInfo: "+ getIgdupnp());
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
	@SuppressWarnings("unused")
	private void getAutoConfig() {
		String xml =
			"<?xml version=\"1.0\"?>\n" +
			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
			"s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
			"<s:Body><u:GetAutoConfig xmlns:u=\"urn:schemas-upnp-org:service:WANDSLLinkConfig:1\"></u:GetAutoConfig>\n" +
			"</s:Body>\n" +
			"</s:Envelope>";

		String result =  UPNPUtils.getSOAPData(protocol+"://" + getAddress() +
				URL_SERVICE_AUTOCONFIG.replace("upnp", getIgdupnp()), URN_SERVICE_AUTOCONFIG, xml); // 01.08.2015

		/*
		<?xml version="1.0"?>
		<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body>
		<u:GetAutoConfigResponse xmlns:u="urn:schemas-upnp-org:service:WANDSLLinkConfig:1">
		<NewAutoConfig>0</NewAutoConfig>
		</u:GetAutoConfigResponse>
		</s:Body> </s:Envelope>
		 */
	}

	@SuppressWarnings("unused")
	private void getConnectionTypeInfo() {
		String xml =
			"<?xml version=\"1.0\"?>\n" +
			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
			":encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
			"<s:Body><u:GetConnectionTypeInfo xmlns:u=\"urn:schemas-upnp-org:service:WANIPConnection:1\"></u:GetConnectionTypeInfo>\n" +
			"</s:Body>\n" +
			"</s:Envelope>";

			UPNPUtils.getSOAPData(protocol+"://" + getAddress() +
					URL_SERVICE_CONNECTIONTYPEINFO.replace("upnp", getIgdupnp()), URN_SERVICE_CONNECTIONTYPEINFO, xml); // 01.08.2015

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

	@SuppressWarnings("unused")
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

			UPNPUtils.getSOAPData(protocol+"://" + getAddress() +
					URL_SERVICE_GENERICPORTMAPPING.replace("upnp", getIgdupnp()), URN_SERVICE_GENERICPORTMAPPING, xml); // 01.08.2015

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
				URL_SERVICE_FORCETERMINATION.replace("upnp", getIgdupnp()), URN_SERVICE_FORCETERMINATION, xml); // 01.08.2015
	}

	public String getSIDUPNP() { // 15.08.2015
		String sSID = "0000000000000000";
		String xml =
		"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
		"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
		"s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
		"<s:Body><u:CreateUrlSID xmlns:u=\"urn:dslforum-org:service:DeviceConfig:1\">\n" +
		"</u:CreateUrlSID>\n" +
		"</s:Body>\n" +
		"</s:Envelope>";

		String result = UPNPUtils.getSOAPDataAuth(fbc, protocol+"://" + getAddress() +
			    URL_SERVICE_CREATEURLSID, URN_SERVICE_CREATEURLSID, xml);

		sSID = result;
		log.info("Result of DeviceConfig CreateUrlSID: " + result);
		return sSID;
	}

	public void setRebootUPNP() { // 15.08.2015
	String xml =
		"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
		"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
		"s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
		"<s:Body><u:Reboot xmlns:u=\"urn:dslforum-org:service:DeviceConfig:1\">\n" +
		//"<NewSessionID>" + sSID + "</NewSessionID>\n" +
		"</u:Reboot>\n" +
		"</s:Body>\n" +
		"</s:Envelope>";

		UPNPUtils.getSOAPDataAuth(fbc, protocol+"://" + getAddress() +
			    URL_SERVICE_REBOOT, URN_SERVICE_REBOOT, xml);
	}

	/**************************************************************************************
	 * Implementation of DoCall-Interface
	 * @throws PageNotFoundException 
	 * @throws org.jfritz.fboxlib.exceptions.InvalidCredentialsException 
	 * @throws LoginBlockedException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 **************************************************************************************/

	// 01.08.2015
	public String getQueryDialPort() throws ClientProtocolException, IOException, LoginBlockedException, InvalidCredentialsException, PageNotFoundException {
		final String FALLBACK_DIAL_PORT = "50";
		Vector<String> query = new Vector<String>();
		query.add(QUERY_DialPort);

		try {
			Vector<String> response = fbc.getQuery(query);
			if (response.size() == 1) {
				String ret_q = response.get(0);
				return ret_q;
			}
			else {
				return FALLBACK_DIAL_PORT;
			}
		} catch (Exception e) {
			return FALLBACK_DIAL_PORT;
		}
	}

    // 31.07.2015 Wahl per Lua
	private void generateDoCallPostDataDialPortLua(List<NameValuePair> postdata, String port) {
		postdata.add(new BasicNameValuePair("clicktodial", "on"));
		postdata.add(new BasicNameValuePair("port", port)); // port.getDialPort()
		postdata.add(new BasicNameValuePair("btn_apply", ""));
	}
	
	private void generateDoCallPostData(List<NameValuePair> postdata, String currentNumber, Port port) {
		postdata.add(new BasicNameValuePair("getpage", ""));
		postdata.add(new BasicNameValuePair("telcfg:settings/UseClickToDial", "1"));
		postdata.add(new BasicNameValuePair("telcfg:settings/DialPort", port.getDialPort()));
		postdata.add(new BasicNameValuePair("telcfg:command/Dial", currentNumber));
	}

	public void doCall(PhoneNumberOld number, Port port) {
		if (fbc.isLoggedIn()) {
			setBoxConnected();
			String currentNumber = number.getAreaNumber();
			currentNumber = currentNumber.replaceAll("\\+", "00"); //$NON-NLS-1$,  //$NON-NLS-2$

			List<NameValuePair> postdata = new ArrayList<NameValuePair>();

			try {
				if (firmware != null && firmware.isLowerThan(4, 21)) {
					// TODO: message, that firmware does not support the calling feature
				} else if (firmware != null && firmware.isLowerThan(6, 1)) {
					log.debug("doCall: Firmware is greater/or equal than 04.21 but lower than 06.1");
					generateDoCallPostData(postdata, currentNumber, port);
				    fbc.postToPageAndGetAsString(FritzBoxCommunication.URL_WEBCM, postdata);
				} else {
					log.debug("doCall: Firmware is greater/or equal than 06.01");
					
					log.debug("doCall: Setting dialing port to " + port.getDialPort());
					if (!port.getDialPort().equals(getQueryDialPort())) { // 18.11.2017 Nur wenn Dialport nicht 50=50 ist
						generateDoCallPostDataDialPortLua(postdata, port.getDialPort());
						fbc.postToPageAndGetAsString(URL_DIAL_FONBOOK_LUA, postdata);
					}

					String dial_query = "";
					dial_query = "dial=" + currentNumber + "&orig_port=" + port.getDialPort();
					dial_query = dial_query.replace("#", "%23"); // # %23
					dial_query = dial_query.replace("*", "%2A"); // * %2A
					fbc.getPageAsString(URL_FONBOOK_LIST_LUA + "?" + dial_query);
				}
			} catch (InvalidSessionIdException e) {
				e.printStackTrace();
				setBoxDisconnected();
				handleInvalidSessionIdException(e);
			} catch (SocketTimeoutException ste) {
				ste.printStackTrace();
				setBoxDisconnected();
			} catch (IOException e) {
				e.printStackTrace();
				setBoxDisconnected();
			} catch (LoginBlockedException e) {
				log.debug("Wrong password, maybe SID is invalid.");
				setBoxDisconnected();
				handleLoginBlockedException(e);
			} catch (InvalidCredentialsException e) {
				log.debug("Wrong password, maybe SID is invalid.");
				setBoxDisconnected();
				handleInvalidCredentialsException(e);
			} catch (PageNotFoundException e) {
				log.debug("Wrong password, maybe SID is invalid.");
				setBoxDisconnected();
				handlePageNotFoundException(e);
			}
		} else {
			// FIXME show error message that we are currently not connected!!
		}
	}

	private void generateHangupPostdata(List<NameValuePair> postdata, Port port) {
		postdata.add(new BasicNameValuePair("getpage", ""));
		postdata.add(new BasicNameValuePair("telcfg:settings/UseClickToDial", "1"));
		postdata.add(new BasicNameValuePair("telcfg:settings/DialPort", port.getDialPort()));
		postdata.add(new BasicNameValuePair("telcfg:command/Hangup", ""));
	}

	public void hangup(Port port)
	{
		if (fbc.isLoggedIn()) {
			setBoxConnected();
			List<NameValuePair> postdata = new ArrayList<NameValuePair>();

			try {
				if (firmware != null && firmware.isLowerThan(4, 21)) {
					// TODO: message, that firmware does not support the calling feature
				} else if (firmware != null && firmware.isLowerThan(6, 1)) {
					log.debug("hangup_Firmware is greater/or equal than 04.21 but lower than 06.1");
					generateHangupPostdata(postdata, port);
					fbc.postToPageAndGetAsString(FritzBoxCommunication.URL_WEBCM, postdata);
				} else {
					log.debug("hangup_Firmware is greater/or equal than 06.01");
					fbc.getPageAsString(URL_FONBOOK_LIST_LUA + "?" + "hangup=");
				}
			} catch (InvalidSessionIdException e) {
				setBoxDisconnected();
				handleInvalidSessionIdException(e);
			} catch (SocketTimeoutException ste) {
				ste.printStackTrace();
				setBoxDisconnected();
			} catch (IOException e) {
				e.printStackTrace();
				setBoxDisconnected();
			} catch (LoginBlockedException e) {
				log.debug("Wrong password, maybe SID is invalid.");
				setBoxDisconnected();
				handleLoginBlockedException(e);
			} catch (InvalidCredentialsException e) {
				log.debug("Wrong password, maybe SID is invalid.");
				setBoxDisconnected();
				handleInvalidCredentialsException(e);
			} catch (PageNotFoundException e) {
				log.debug("Wrong password, maybe SID is invalid.");
				setBoxDisconnected();
				handlePageNotFoundException(e);
			}
		} else {
			// FIXME show error message that we are currently not connected!!
		}
	}

	public void addBoxCallBackListener(BoxCallBackListener listener)
	{
		if (!callBackListener.contains(listener)) {
			callBackListener.add(listener);
		}
	}

	private void generateRebootPostdata(List<NameValuePair> postdata) {
		postdata.add(new BasicNameValuePair("getpage", "../html/reboot.html"));
		postdata.add(new BasicNameValuePair("var:pagename", "reset"));
		postdata.add(new BasicNameValuePair("var:menu", "system"));
		postdata.add(new BasicNameValuePair("var:pagemaster", ""));
		postdata.add(new BasicNameValuePair("time:settings/time", "1250935088%2C-120"));
		postdata.add(new BasicNameValuePair("var:tabReset","0"));
		postdata.add(new BasicNameValuePair("logic:command/reboot","../gateway/commands/saveconfig.html"));
	}

	public void reboot() {
		if (fbc.isLoggedIn()) {
			List<NameValuePair> postdata = new ArrayList<NameValuePair>();

			if ((firmware != null) && firmware.isLowerThan(5, 50)) {
				generateRebootPostdata(postdata);
				try {
					fbc.postToPageAndGetAsVector(FritzBoxCommunication.URL_WEBCM, postdata);
				} catch (InvalidSessionIdException e) {
					setBoxDisconnected();
					handleInvalidSessionIdException(e);
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
					setBoxDisconnected();
				} catch (IOException e) {
					e.printStackTrace();
					setBoxDisconnected();
				} catch (LoginBlockedException e) {
					handleLoginBlockedException(e);
				} catch (InvalidCredentialsException e) {
					handleInvalidCredentialsException(e);
				} catch (PageNotFoundException e) {
					handlePageNotFoundException(e);
				} 
			} else {
				setRebootUPNP(); // 01.08.2015
			}
		} else {
			// FIXME show error message that we are currently not connected!!
		}
	}

	private void handleInvalidSessionIdException(final InvalidSessionIdException e) {
		try {
			String message = messages.getMessage("box.invalid_session_id").replaceAll("%FIRMWARE%", fbc.getFirmwareVersion().toString());
			log.error(message, e);
			Debug.errDlg(message);
		} catch (Exception e1) {
			String message = messages.getMessage("box.invalid_session_id").replaceAll("%FIRMWARE%", "unknown");
			log.error(message, e);
			Debug.errDlg(message);
		}
	}
	
	private void handleInvalidCredentialsException(final InvalidCredentialsException e) {
		if (this.getFirmware().isLowerThan(05, 50)) {
			String message = messages.getMessage("box.wrong_password");
			log.error(message, e);
			Debug.errDlg(message);
		} else {
			String message = messages.getMessage("box.wrong_password_or_username");
			log.error(message, e);
			Debug.errDlg(message);
		}
	}

	private void handleLoginBlockedException(LoginBlockedException e) {
		if (this.getFirmware().isLowerThan(05, 50)) {
			String message = messages.getMessage("box.wrong_password.wait").replaceAll("%WAIT%", e.getRemainingBlockTime());
			log.error(message, e);
			Debug.errDlg(message);
		} else {
			String message = messages.getMessage("box.wrong_password_or_username.wait").replaceAll("%WAIT%", e.getRemainingBlockTime());
			log.error(message, e);
			Debug.errDlg(message);
		}
	}

	private void handlePageNotFoundException(PageNotFoundException e) {
		String message = "Could not execute command, page not found!";
		log.error(message, e);
		Debug.errDlg(message);
	}

	private void handleFirmwareNotDetectedException(final FirmwareNotDetectedException e) {
		String message = "Could not detect firmware!";
		log.error(message, e);
		Debug.errDlg(message);
	}

	public int getMaxRetryCount() {
		return max_retry_count;
	}

	public void refreshLogin() {
		if (fbc != null) {
			fbc.invalidateSid();
			try {
				detectFirmwareAndLogin();
			} catch (ClientProtocolException e) {
				log.error(e.getMessage());
				setBoxDisconnected();
			} catch (InvalidCredentialsException e) {
				setBoxDisconnected();
				handleInvalidCredentialsException(e);
			} catch (LoginBlockedException e) {
				setBoxDisconnected();
				handleLoginBlockedException(e);
			} catch (IOException e) {
				log.error(messages.getMessage("box.not_found"));
				setBoxDisconnected();
			} catch (PageNotFoundException e) {
				setBoxDisconnected();
				handlePageNotFoundException(e);
			} catch (FirmwareNotDetectedException e) {
				setBoxDisconnected();
				handleFirmwareNotDetectedException(e);
			}
		}
	}

	public String getIgdupnp() { // 01.08.2015
		return igdupnp;
	}

	public void setIgdupnp(String igdupnp) { // 01.08.2015
		this.igdupnp = igdupnp;
	}
	
	private boolean showLoginDialog(Exception e) {
		LoginDialog loginDialog = new LoginDialog(this);
		loginDialog.setException(e);
		loginDialog.setVisible(true);
		return loginDialog.hasOkBeenPressed();
	}
	
	public int checkMacAddress(FritzBox fritzBox) {
		int result = 0;
		// if a mac address is set and this box has a different mac address, ask user
		// if communication to this box should be allowed.
		String macStr = properties.getProperty("box.mac");
		if ((!("".equals(macStr))
		&& ( !("".equals(fritzBox.getMacAddress())))
		&& (fritzBox.getMacAddress() != null)))
		{
			ComplexJOptionPaneMessage msg = null;
			int answer = JOptionPane.YES_OPTION;
			if (messages.getMessage("unknown").equals(fritzBox.getMacAddress()))
			{
				log.info("MAC-Address could not be determined. Ask user how to proceed..."); //$NON-NLS-1$
				msg = new ComplexJOptionPaneMessage("legalInfo.macNotFound",
						messages.getMessage("mac_not_found") + "\n"
						+ messages.getMessage("accept_fritzbox_communication")); //$NON-NLS-1$
				if (msg.showDialogEnabled()) {
					answer = JOptionPane.showConfirmDialog(null,
							msg.getComponents(),
							messages.getMessage("information"), JOptionPane.YES_NO_OPTION);
					if (answer == JOptionPane.YES_OPTION)
					{
						msg.saveProperty();
						properties.saveStateProperties();
					}
				}
			} else if ( !(macStr.equals(fritzBox.getMacAddress())))
			{
				log.info("New FRITZ!Box detected. Ask user how to proceed..."); //$NON-NLS-1$
				msg = new ComplexJOptionPaneMessage("legalInfo.newBox",
						messages.getMessage("new_fritzbox") + "\n"
						+ messages.getMessage("accept_fritzbox_communication")); //$NON-NLS-1$
				if (msg.showDialogEnabled()) {
					answer = JOptionPane.showConfirmDialog(null,
							msg.getComponents(),
							messages.getMessage("information"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
					if (answer == JOptionPane.YES_OPTION)
					{
						msg.saveProperty();
						properties.saveStateProperties();
					}
				}
			}
			if (answer == JOptionPane.YES_OPTION) {
				log.info("User decided to accept connection."); //$NON-NLS-1$
				properties.setProperty("box.mac", fritzBox.getMacAddress());
				properties.saveConfigProperties();
				result = 0;
			} else {
				log.info("User decided to prohibit connection."); //$NON-NLS-1$
				result = Main.EXIT_CODE_FORBID_COMMUNICATION_WITH_FRITZBOX;
			}
		}
		return result;
	}
}
