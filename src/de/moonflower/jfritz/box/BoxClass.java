package de.moonflower.jfritz.box;

import java.util.Vector;

import de.moonflower.jfritz.Main;
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
	protected String name;
	protected String description;

	protected String address;
	protected String port;
	protected String password;
	protected String macAddress;

	/** SSDP constants **/
	private final static int SSDP_TIMEOUT = 1000;
	protected static SSDPdiscoverThread ssdpthread;

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
		if (JFritzUtils.parseBoolean(Main.getProperty("option.useSSDP")) //$NON-NLS-1$
			&& !(Main.getProperty("network.type").equals("2")
						&& Boolean.parseBoolean(Main.getProperty("option.clientCallList")))) { //$NON-NLS-1$
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

	public abstract void addBoxStatusListener(BoxStatusListener listener);
	public abstract void removeBoxStatusListener(BoxStatusListener listener);

	public abstract void addBoxCallBackListener(BoxCallBackListener listener);

	public abstract String getExternalIP();
}
