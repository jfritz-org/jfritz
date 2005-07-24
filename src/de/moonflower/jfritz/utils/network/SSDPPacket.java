package de.moonflower.jfritz.utils.network;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;

/*
 * Created on 22.05.2005
 *
 */

/**
 * @author Arno Willig
 *
 */
public class SSDPPacket {

	final static String SSDP_ADDRESS = "239.255.255.250";

	final static String SSDP_DISCOVER = "M-SEARCH * HTTP/1.1\r\nST: upnp:rootdevice\r\n"
			+ "MX: 10\r\nMAN: \"ssdp:discover\"\r\nHOST: 239.255.255.250:1900\r\n\r\n";

	private DatagramPacket udpPacket;

	private String server = "", location = "", cachecontrol = "", ext = "",
			st = "", usn = "";

	/**
	 * creates a SSDPPacket from a DatagramPacket
	 *
	 * TODO: Vielleicht noch Prüfen, ob das Paket korrekt ist, ansonsten eine
	 * Exception werfen.
	 */
	public SSDPPacket(DatagramPacket packet) {
		this.udpPacket = packet;
		String[] data = new String(packet.getData()).split("\r\n");
		for (int i = 0; i < data.length; i++) {
			if (data[i].startsWith("SERVER:"))
				server = data[i].substring(7).trim();
			else if (data[i].startsWith("LOCATION:"))
				location = data[i].substring(9).trim();
			else if (data[i].startsWith("CACHE-CONTROL:"))
				cachecontrol = data[i].substring(15).trim();
			else if (data[i].startsWith("EXT:"))
				ext = data[i].substring(4).trim();
			else if (data[i].startsWith("ST:"))
				st = data[i].substring(3).trim();
			else if (data[i].startsWith("USN:"))
				usn = data[i].substring(4).trim();
		}
	}

	/**
	 *
	 * @return InetAdress of SSDP multicast
	 */
	public static InetAddress getSSDPAddress() {
		try {
			return InetAddress.getByName(SSDP_ADDRESS);
		} catch (UnknownHostException e) {
			return null;
		}
	}

	/**
	 *
	 * @return DatagramPacket for SSDP:Discover
	 */
	public static DatagramPacket getSSDPDiscoverPacket() {
		return new DatagramPacket(SSDP_DISCOVER.getBytes(), SSDP_DISCOVER
				.length(), getSSDPAddress(), 1900);
	}

	/**
	 * @return Returns the cachecontrol.
	 */
	public final String getCachecontrol() {
		return cachecontrol;
	}

	/**
	 * @return Returns the ext.
	 */
	public final String getExt() {
		return ext;
	}

	/**
	 * @return Returns the location.
	 */
	public final String getLocation() {
		return location;
	}

	/**
	 * @return Returns the server.
	 */
	public final String getServer() {
		return server;
	}

	/**
	 * @return Returns the st.
	 */
	public final String getSt() {
		return st;
	}

	/**
	 * @return Returns the usn.
	 */
	public final String getUsn() {
		return usn;
	}

	/**
	 * @return Returns the packet.
	 */
	public final DatagramPacket getUdpPacket() {
		return udpPacket;
	}

	/**
	 * @param packet
	 *            The packet to set.
	 */
	public final void setUdpPacket(DatagramPacket packet) {
		this.udpPacket = packet;
	}

	public final InetAddress getIP() {
		return udpPacket.getAddress();
	}

	public final String getShortName() {
		String parts[] = getServer().split(" ", 4);
		String name = parts[3];
		return name;
	}

	public String getMAC() {
		String parts[] = getServer().split(" ", 2);
		String mac = parts[0].substring(10);
		return mac;
	}

	public FritzBoxFirmware getFirmware() {
		//String parts[] = getServer().split(" ", 2);
		String fwstr = getServer().substring(getServer().lastIndexOf(" ") + 1);
		try {
			return new FritzBoxFirmware(fwstr);
		} catch (InvalidFirmwareException e) {
			return null;
		}
	}
}
