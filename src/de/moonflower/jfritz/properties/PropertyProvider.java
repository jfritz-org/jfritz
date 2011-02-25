package de.moonflower.jfritz.properties;

import java.awt.Frame;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.UIManager;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.callerlist.CallerTable;
import de.moonflower.jfritz.callerlist.filter.CallFilter;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzProperties;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.threeStateButton.ThreeStateButton;

public class PropertyProvider {
	private final static String CONFIG_PROPERTIES_FILE = "jfritz.properties.xml"; //$NON-NLS-1$
	private final static String STATE_PROPERTIES_FILE = "jfritz.state.properties.xml"; //$NON-NLS-1$

	private JFritzProperties config_properties;
	private JFritzProperties state_properties;

	private static PropertyProvider INSTANCE = new PropertyProvider();

	public static PropertyProvider getInstance() {
		return INSTANCE;
	}

	/**
	 * Loads properties from xml files
	 * @return True if config wizard has to be shown, false otherwise
	 */
	public boolean loadProperties(final boolean replace) {
		boolean showConfWizard = false;

		config_properties = new JFritzProperties(loadDefaultProperties());
		try {
			config_properties.loadFromXML(Main.SAVE_DIR + CONFIG_PROPERTIES_FILE);
		} catch (FileNotFoundException e) {
			Debug.warning("File " + Main.SAVE_DIR + CONFIG_PROPERTIES_FILE //$NON-NLS-1$
					+ " not found => showing config wizard"); //$NON-NLS-1$
			showConfWizard = true;
		} catch (IOException ioe) {
			Debug.warning("File " + Main.SAVE_DIR + CONFIG_PROPERTIES_FILE //$NON-NLS-1$
					+ " not readable => showing config wizard"); //$NON-NLS-1$
			showConfWizard = true;
		}

		state_properties = new JFritzProperties(loadDefaultWindowProperties());
		try {
			state_properties.loadFromXML(Main.SAVE_DIR + STATE_PROPERTIES_FILE);
		} catch (FileNotFoundException e) {
			Debug.warning("File " + Main.SAVE_DIR + STATE_PROPERTIES_FILE //$NON-NLS-1$
					+ " not found. Using default values."); //$NON-NLS-1$
		} catch (IOException ioe) {
			Debug.warning("File " + Main.SAVE_DIR + STATE_PROPERTIES_FILE //$NON-NLS-1$
					+ " not readable. Using default values."); //$NON-NLS-1$
		}

		if (replace) {
			replaceOldProperties();
		}

		return showConfWizard;
	}

	/**
	 * This method sets the default properties
	 * @return Set of default properties.
	 */
	private static JFritzProperties loadDefaultProperties()
	{
		JFritzProperties defProps = new JFritzProperties();
		// Default properties
		defProps.setProperty("area.code", "721");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("area.prefix", "0");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("backup.path", ".");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("box.address", "192.168.178.1");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("box.mac", "");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("box.password", "121-203-238-10-54-180-181-42");//$NON-NLS-1$, //$NON-NLS-2$ // empty string as default PW
		defProps.setProperty("box.port", "80");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("clients.port", "4455");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("country.code", "+49");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("country.prefix", "00");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("dial.prefix", " ");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("fetch.timer", "5");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("inet.monitoring", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("jfritz.seed", "");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("jfritz.pwd", "");//$NON-NLS-1$, //$NON-NLS-2$
		//"en_US"
		defProps.setProperty("locale", "");//$NON-NLS-1$, //$NON-NLS-2$, //$NON-NLS-3$
		defProps.setProperty("max.Connections", "2");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("network.type", "0");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.activateDialPrefix", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.autostartcallmonitor", "true");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.callmessageport", "23232");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.callMonitorType", "1");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.callmonitor.ignoreMSN", "");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.callmonitor.fetchAfterDisconnect", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.callmonitor.monitorIncomingCalls", "true");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.callmonitor.monitorOutgoingCalls", "true");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.checkNewVersionAfterStart", "true");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.clientTelephoneBook", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.clientCallList", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.clientCallMonitor", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.clientStandAlone", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.confirmOnExit", "true");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.connectOnStartup", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.createBackup", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.createBackupAfterFetch", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.keepImportantBackupsOnly", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.deleteAfterFetch", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.externProgram", "");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.fetchAfterStart", "true");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.listenOnStartup", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.lookupAfterFetch", "true");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.minimize", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.notifyOnCalls", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.playSounds", "true");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.popuptype", "1");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.popupDelay", "0");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.startExternProgram", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.startMinimized", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.syslogclientip", "192.168.178.21");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.syslogpassthrough", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.timerAfterStart", "true");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.useSSDP", "true");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.watchdog.fetchAfterStandby", "true");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.yacport", "10629");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("options.exportCSVpath", ".");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("options.exportXMLpath", ".");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("options.exportCSVpathOfPhoneBook", ".");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("server.name", "");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("server.port", "4455");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("server.login", "");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("server.password", "");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("syslog.checkSyslog", "true");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("syslog.checkTelefon", "true");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("server.password", "");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("telefond.laststarted", "");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("telnet.user", "");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("telnet.password", "");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("tray.clickCount", "2");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("window.useDecorations", "true");//$NON-NLS-1$, //$NON-NLS-2$

		// set all callertable columns to visible
		Enumeration<String> columns = CallerTable.getCallerTableColumns().elements();
		String currentColumn = "";
		while (columns.hasMoreElements())
		{
			currentColumn = columns.nextElement();
			defProps.setProperty("option.showCallerListColumn."+currentColumn, "true");//$NON-NLS-1$, //$NON-NLS-2$
		}
		return defProps;
	}

	private static JFritzProperties loadDefaultWindowProperties()
	{
		JFritzProperties defProps = new JFritzProperties();
		defProps.setProperty("window.state.old", Integer.toString(Frame.NORMAL));//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("window.state", Integer.toString(Frame.MAXIMIZED_BOTH));//$NON-NLS-1$, //$NON-NLS-2$

		defProps.setProperty("position.left", "10");//$NON-NLS-1$
		defProps.setProperty("position.top", "10");//$NON-NLS-1$
		defProps.setProperty("position.width", "640");//$NON-NLS-1$
		defProps.setProperty("position.height", "480");//$NON-NLS-1$

		defProps.setProperty("calldialog.lastport", "0");//$NON-NLS-1$, //$NON-NLS-2$

		// Filter properties
		defProps.setProperty(CallFilter.FILTER_SIP_PROVIDERS, "$ALL$");//$NON-NLS-1$
		defProps.setProperty(CallFilter.FILTER_PORT_LIST, "$ALL$");//$NON-NLS-1$
		defProps.setProperty(CallFilter.FILTER_COMMENT, "0");//$NON-NLS-1$
		defProps.setProperty(CallFilter.FILTER_DATE, "0");//$NON-NLS-1$
		defProps.setProperty(CallFilter.FILTER_CALLBYCALL, "0");//$NON-NLS-1$
		defProps.setProperty(CallFilter.FILTER_CALLOUT, "0");//$NON-NLS-1$
		defProps.setProperty(CallFilter.FILTER_ANONYM, "0");//$NON-NLS-1$
		defProps.setProperty(CallFilter.FILTER_FIXED, "0");//$NON-NLS-1$
		defProps.setProperty(CallFilter.FILTER_HANDY, "0");//$NON-NLS-1$
		defProps.setProperty(CallFilter.FILTER_CALLIN_NOTHING, "0");//$NON-NLS-1$
		defProps.setProperty(CallFilter.FILTER_CALLINFAILED, "0");//$NON-NLS-1$
		defProps.setProperty(CallFilter.FILTER_SEARCH_TEXT, "");//$NON-NLS-1$
		defProps.setProperty(CallFilter.FILTER_SEARCH, "0");//$NON-NLS-1$
		defProps.setProperty(CallFilter.FILTER_DATE_SPECIAL, " ");//$NON-NLS-1$
		defProps.setProperty(CallFilter.FILTER_DATE_START, "11.11.11 11:11");//$NON-NLS-1$
		defProps.setProperty(CallFilter.FILTER_DATE_END, "11.11.11 11:11");//$NON-NLS-1$

		// set default callerlist column width
		String default_column_width = "70";
		Enumeration<String> columns = CallerTable.getCallerTableColumns().elements();
		String currentColumn = "";
		while (columns.hasMoreElements())
		{
			currentColumn = columns.nextElement();
			if (currentColumn.equals("type"))
			{
				defProps.setProperty("callerTable.column." + currentColumn + ".width", "35");
			}
			else if (currentColumn.equals("date"))
			{
				defProps.setProperty("callerTable.column." + currentColumn + ".width", "85");
			}
			else if (currentColumn.equals("callbycall"))
			{
				defProps.setProperty("callerTable.column." + currentColumn + ".width", "70");
			}
			else if (currentColumn.equals("number"))
			{
				defProps.setProperty("callerTable.column." + currentColumn + ".width", "185");
			}
			else if (currentColumn.equals("participant"))
			{
				defProps.setProperty("callerTable.column." + currentColumn + ".width", "185");
			}
			else if (currentColumn.equals("picture"))
			{
				defProps.setProperty("callerTable.column." + currentColumn + ".width", "50");
			}
			else if (currentColumn.equals("port"))
			{
				defProps.setProperty("callerTable.column." + currentColumn + ".width", "115");
			}
			else if (currentColumn.equals("route"))
			{
				defProps.setProperty("callerTable.column." + currentColumn + ".width", "80");
			}
			else if (currentColumn.equals("duration"))
			{
				defProps.setProperty("callerTable.column." + currentColumn + ".width", "60");
			}
			else if (currentColumn.equals("comment"))
			{
				defProps.setProperty("callerTable.column." + currentColumn + ".width", "135");
			}
			else
			{
				defProps.setProperty("callerTable.column." + currentColumn + ".width", default_column_width);//$NON-NLS-1$, //$NON-NLS-2$
			}
		}



		// column order
		for (int i=0; i<CallerTable.getCallerTableColumns().size();i++)
		{
			defProps.setProperty("callerTable.column"+i+".name", CallerTable.getCallerTableColumns().get(i));
		}

		defProps.setProperty("option.picture.default_path", ".");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("filter.Phonebook.search", "");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("filter_private", "false");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("options.exportVCARDpath", ".");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("option.phonebook.import_xml_path", ".");//$NON-NLS-1$, //$NON-NLS-2$
		defProps.setProperty("lookandfeel", UIManager.getSystemLookAndFeelClassName());//$NON-NLS-1$, //$NON-NLS-2$

		return defProps;
	}

	/**
	 * Replace old property values with new one
	 *
	 */
	private void replaceOldProperties() {

		Vector<String> allCallerListColumns = CallerTable.getCallerTableColumns();

		String currentColumn = "";
		int currentIndex = 0;
		boolean foundOldEntries = false;
		// copy the previous column order to new structure
		for (int i=0; i<CallerTable.getCallerTableColumnsCount(); i++)
		{
			currentColumn = state_properties.getProperty("column"+i+".name");
			state_properties.remove("column"+i+".name");
			if ((currentColumn != null)
			   && (!"".equals(currentColumn)))
			{
				state_properties.setProperty("callerTable.column"+currentIndex+".name", currentColumn);
				state_properties.remove("column"+i+".name");
				allCallerListColumns.remove(currentColumn);
				currentIndex++;
				foundOldEntries = true;
			}
		}

		// add all remaining hidden columns at the end of our new structure
		for (int i=0; foundOldEntries && (i<allCallerListColumns.size());i++)
		{
			state_properties.setProperty("callerTable.column"+currentIndex+".name", allCallerListColumns.get(i));
			currentIndex++;
		}

		Enumeration<String> callerListColumns = CallerTable.getCallerTableColumns().elements();
		currentColumn = "";
		String currentWidth = "";
		while (callerListColumns.hasMoreElements())
		{
			currentColumn = callerListColumns.nextElement();
			currentWidth = state_properties.getProperty("column." + currentColumn + ".width");
			if (currentWidth != null)
			{
				state_properties.setProperty("callerTable.column." + currentColumn + ".width", currentWidth);
				state_properties.remove("column." + currentColumn + ".width");
			}
		}

		String currentProp = config_properties.getProperty("option.showCallByCallColumn");
		if ( currentProp != null)
		{
			config_properties.setProperty("option.showCallerListColumn."+CallerTable.COLUMN_CALL_BY_CALL, currentProp);
			config_properties.remove("option.showCallByCallColumn");
		}

		currentProp = config_properties.getProperty("option.showCommentColumn");
		if ( currentProp != null)
		{
			config_properties.setProperty("option.showCallerListColumn."+CallerTable.COLUMN_COMMENT, currentProp);
			config_properties.remove("option.showCommentColumn");
		}

		currentProp = config_properties.getProperty("option.showPictureColumn");
		if ( currentProp != null)
		{
			config_properties.setProperty("option.showCallerListColumn."+CallerTable.COLUMN_PICTURE, currentProp);
			config_properties.remove("option.showPictureColumn");
		}

		currentProp = config_properties.getProperty("option.showPortColumn");
		if ( currentProp != null)
		{
			config_properties.setProperty("option.showCallerListColumn."+CallerTable.COLUMN_PORT, currentProp);
			config_properties.remove("option.showPortColumn");
		}

		config_properties.remove("telnet.user");
		config_properties.remove("telnet.password");

		// no startup password set yet
		if (config_properties.getProperty("option.syslogEnabled") == null)
		{
			config_properties.setProperty("option.syslogEnabled", "true");
			if (config_properties.getProperty("box.password") != null)
			{
				String box_pw = Encryption.decrypt(config_properties.getProperty("box.password"));
				if ("".equals(box_pw))
				{
					String defaultPw = Main.PROGRAM_SECRET;
					config_properties.setProperty("jfritz.seed", Encryption.encrypt(Main.PROGRAM_SEED + defaultPw));
					config_properties.setProperty("jfritz.pwd", Encryption.encrypt(Main.PROGRAM_SECRET + defaultPw));
				}
				else
				{
					String seed_pw = Encryption.encrypt(Main.PROGRAM_SEED + box_pw);
					config_properties.setProperty("jfritz.seed", seed_pw);
					if (config_properties.getProperty("jfritz.password") != null)
					{
						String jf_pw = Encryption.encrypt(Main.PROGRAM_SECRET + box_pw);
						config_properties.setProperty("jfritz.pwd", jf_pw);
					}
				}
			}

			if (config_properties.getProperty("jfritz.password") != null)
			{
				config_properties.remove("jfritz.password");
			}
		}

		// replace old SIP Filter configuration
		String filter_sip = "";
		String filter_sip_providers = "";
		if ((filter_sip = state_properties.getProperty(CallFilter.FILTER_SIP)) != null) {
			if ((filter_sip_providers = state_properties.getProperty(CallFilter.FILTER_SIP_PROVIDERS)) != null ) {
				if (filter_sip.equals(Integer.toString(ThreeStateButton.SELECTED))) {
					if (filter_sip_providers.equals("")) {
						setStateProperty(CallFilter.FILTER_SIP_PROVIDERS, "$ALL$");
					} else {
						String newString = "";
						Vector<String> split = new Vector<String>();
						JFritzUtils.fillVectorByString(split, CallFilter.FILTER_SIP_PROVIDERS, " ");
						for (int i=0; i<split.size(); i++) {
							newString = newString + split.get(i) + ";";
						}
						newString = newString.substring(0, newString.length()-1);
						setStateProperty(CallFilter.FILTER_SIP_PROVIDERS, newString);
					}
				} else if (filter_sip.equals(Integer.toString(ThreeStateButton.INVERTED))) {
					setStateProperty(CallFilter.FILTER_SIP_PROVIDERS, "$ALL$");
				} else if (filter_sip.equals(Integer.toString(ThreeStateButton.NOTHING))) {
					setStateProperty(CallFilter.FILTER_SIP_PROVIDERS, "$ALL$");
				}
			}
		}
		if (state_properties.getProperty(CallFilter.FILTER_SIP) != null)
		{
			state_properties.remove(CallFilter.FILTER_SIP);
		}

		saveStateProperties();
		saveConfigProperties();
	}

	/**
	 * Get config properties with default value
	 * @param property
	 *            Property to get the value from
	 * @param defaultValue
	 *            Default value to be returned if property does not exist
	 * @return Returns value of a specific property
	 */
	private String getProperty(String property, String defaultValue) {
		if (config_properties != null) {
			return config_properties.getProperty(property, defaultValue);
		} else {
			return null;
		}
	}

	/**
	 * Saves config properties to xml files
	 * ip, password, options
	 */
	public void saveConfigProperties() {
		try {
			Debug.always("Save config properties"); //$NON-NLS-1$
			config_properties.storeToXML(Main.SAVE_DIR + CONFIG_PROPERTIES_FILE);
		} catch (IOException e) {
			Debug.error("Couldn't save config properties"); //$NON-NLS-1$
		}
	}

	/**
	 * Saves state properties to xml files
	 * window-state, filter-state ...
	 */
	public void saveStateProperties() {
		try {
			Debug.always("Save state properties"); //$NON-NLS-1$
			state_properties.storeToXML(Main.SAVE_DIR + STATE_PROPERTIES_FILE);
		} catch (IOException e) {
			Debug.error("Couldn't save state properties"); //$NON-NLS-1$
		}
	}

	/**
	 * Get state properties with default value
	 * @param property
	 *            Property to get the value from
	 * @param defaultValue
	 *            Default value to be returned if property does not exist
	 * @return Returns value of a specific property
	 */
	public String getStateProperty(String property, String defaultValue) {
		return state_properties.getProperty(property, defaultValue);
	}

	/**
	 * Get state properties
	 * @param property
	 *            Property to get the value from
	 * @return Returns value of a specific property
	 */
	public String getStateProperty(String property) {
		return getStateProperty(property, ""); //$NON-NLS-1$
	}

	/**
	 * Get config properties
	 * @param property
	 *            Property to get the value from
	 * @return Returns value of a specific property
	 */
	public String getProperty(String property) {
		return getProperty(property, ""); //$NON-NLS-1$
	}

	/**
	 * Sets a config property to a specific value
	 *
	 * @param property
	 *            Property to be set
	 * @param value
	 *            Value of property
	 */
	public void setProperty(String property, String value) {
		config_properties.setProperty(property, value);
	}

	/**
	 * Sets a config property to a specific value
	 *
	 * @param property
	 *            Property to be set
	 * @param value
	 *            Value of property
	 */
	public void setProperty(String property, boolean value) {
		config_properties.setProperty(property, String.valueOf(value));
	}

	/**
	 * Sets a state property to a specific value
	 *
	 * @param property
	 *            Property to be set
	 * @param value
	 *            Value of property
	 */
	public void setStateProperty(String property, String value) {
		state_properties.setProperty(property, value);
	}

	/**
	 * Sets a state property to a specific value
	 *
	 * @param property
	 *            Property to be set
	 * @param value
	 *            Value of property
	 */
	public void setStateProperty(String property, boolean value) {
		state_properties.setProperty(property, String.valueOf(value));
	}

	/**
	 * Removes a config property
	 *
	 * @param property
	 *            Property to be removed
	 */
	public void removeProperty(String property) {
		config_properties.remove(property);
	}

	/**
	 * Removes a state property
	 *
	 * @param property
	 *            Property to be removed
	 */
	public void removeStateProperty(String property) {
		state_properties.remove(property);
	}
}
