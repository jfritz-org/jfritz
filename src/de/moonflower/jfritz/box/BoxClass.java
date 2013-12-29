package de.moonflower.jfritz.box;

import java.util.Vector;

import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.network.SSDPPacket;
import de.moonflower.jfritz.utils.network.SSDPdiscoverThread;

public abstract class BoxClass
	implements BoxCallMonitorInterface,
			   BoxCallListInterface,
			   BoxSipInterface,
			   BoxUPnPStatisticsInterface,
			   BoxPortsInterface,
			   BoxDoCallInterface,
			   BoxNewIpInterface
{
	private Vector<BoxStatusListener> boxListener;

	protected String name;
	protected String description;

	protected String protocol;
	protected String address;
	protected String port;
	protected String username;
	protected String password;
	protected String macAddress;
	protected boolean useUsername;

	/** SSDP constants **/
	private final static int SSDP_TIMEOUT = 1000;
	protected static SSDPdiscoverThread ssdpthread;
	protected static PropertyProvider properties = PropertyProvider.getInstance();
	
	public BoxClass() {
		boxListener = new Vector<BoxStatusListener>(4);
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

	public void setBoxConnected() {
		for (BoxStatusListener listener: boxListener) {
			listener.setBoxConnected(name);
		}
	}

	public void setBoxDisconnected() {
		for (BoxStatusListener listener: boxListener) {
			listener.setBoxDisconnected(name);
		}
	}

	/**
	 * The user can define a random name describing this box.
	 * @return The name of the box.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * The user can set a description, describing the location of the box
	 * or different data.
	 * @return The description of the box.
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Get used protocol (HTTP / HTTPS)
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Get the address (IP / URL) of the box.
	 * @return Address of the box.
	 */
	public String getAddress()
	{
		return address;
	}

	/**
	 * Get the port of the box (probably 80 (HTTP))
	 * @return The port of the box.
	 */
	public String getPort()
	{
		return port;
	}
	
	/**
	 * Should we use the username?
	 * @return True if username shall be used
	 */
	public boolean shallUseUsername() {
		return useUsername;
	}
	
	/**
	 * Get the username of the box.
	 * @return The username of the box.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Get the password of the box.
	 * @return The password of the box.
	 */
	public String getPassword()
	{
		return password;
	}
	
	/**
	 * Get the mac address of the box.
	 * @return
	 */
	public String getMacAddress()
	{
		return macAddress;
	}

	/**
	 * Set the name of the box.
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Set the description of the box.
	 * @param desc
	 */
	public void setDescription(String desc)
	{
		this.description = desc;
	}

	/**
	 * Set used protocol (HTTP, HTTPS) of the box.
	 * @param protocol
	 */
	public void setProtocol(String protocol)
	{
		this.protocol = protocol;
	}

	/**
	 * Set the URL / IP-Address of the box.
	 * @param address
	 */
	public void setAddress(String address)
	{
		this.address = address;
	}

	/**
	 * Set the port of the box.
	 * @param port
	 */
	public void setPort(String port)
	{
		this.port = port;
	}

	/**
	 * Set use username
	 */
	public void setUseUsername(boolean useIt) {
		this.useUsername = useIt;
	}
	
	/**
	 * Set the username of the box
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * Set the password of the box.
	 * @param password
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * Set the mac address of the box.
	 * @param mac
	 */
	public void setMacAddress(String mac)
	{
		this.macAddress = mac;
	}

	public static final void detectBoxesWithSSDP()
	{
		Debug.info("Searching for  FritzBox per UPnP / SSDP");//$NON-NLS-1$

		ssdpthread = new SSDPdiscoverThread(SSDP_TIMEOUT);
		ssdpthread.start();
		try {
			ssdpthread.join();//FIXME start a thread just to call join in the next line?
		} catch (InterruptedException ie) {
        	Thread.currentThread().interrupt();
		}
	}

	/**
	 * @return Returns the fritzbox devices.
	 */
	public static final Vector<SSDPPacket> getDevices() {
		//avoid using the ssdp thread if jfritz is running as a client and using the call list from server
		if (JFritzUtils.parseBoolean(properties.getProperty("option.useSSDP")) //$NON-NLS-1$
			&& !(properties.getProperty("network.type").equals("2")
						&& Boolean.parseBoolean(properties.getProperty("option.clientCallList")))) { //$NON-NLS-1$
			try {
				ssdpthread.join();
			} catch (InterruptedException e) {
			}
			return ssdpthread.getDevices();
		} else {
			Debug.netMsg("jfritz is configured as a client, canceling box detection");
			return null;
		}
	}

	public abstract void addBoxCallBackListener(BoxCallBackListener listener);

	public abstract String getExternalIP();

	public abstract void reboot() throws WrongPasswordException;
	
	public abstract void refreshLogin();
}
