package de.moonflower.jfritz.upnp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.Vector;

import de.moonflower.jfritz.Debug;

/*
 * Created on 22.05.2005
 *
 */

/**
 * @author Arno Willig
 *
 */
public class UPNPUtils {

	/**
	 * @param timeout
	 * @return Vector of SSDPPackets
	 */
	public static Vector SSDP_discoverDevices(int timeout) {
		Vector devices = new Vector();
		try {
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(timeout);
			socket.send(SSDPPacket.getSSDPDiscoverPacket());

			int i = 0;
			while (i < 10) {
				DatagramPacket packet = new DatagramPacket(new byte[1024],
						1024, SSDPPacket.getSSDPAddress(), socket
								.getLocalPort());
				socket.receive(packet);
				devices.add(new SSDPPacket(packet));
				i++;
			}
			socket.close();
		} catch (SocketTimeoutException e) {
			Debug.msg("Timeout for SSDP");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return devices;
	}

	public static Vector SSDP_discoverFritzBoxes(int timeout) {
		Vector devices = SSDP_discoverDevices(timeout);
		Vector fritzboxes = new Vector();
		Enumeration en = devices.elements();
		while (en.hasMoreElements()) {
			SSDPPacket p = (SSDPPacket) en.nextElement();
			if (p.getServer().toLowerCase().indexOf("avm fritz!box") > 0) {
				Debug.msg("Box found at " + p.getIP().toString() + ": "
						+ p.getServer());
				fritzboxes.add(p);
			}
		}
		return fritzboxes;
	}

}
