package de.moonflower.jfritz.upnp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.Vector;

/*
 * Created on 22.05.2005
 *
 */

/**
 * @author Arno Willig
 *
 */
public class UPNPUtils {

	public UPNPUtils() {
	}

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
			System.err.println("Timeout for SSDP");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return devices;
	}

	public static void main(String[] args) {
		Vector dev = SSDP_discoverDevices(2000);
		Enumeration en = dev.elements();
		while (en.hasMoreElements()) {
			SSDPPacket p = (SSDPPacket) en.nextElement();
			System.out.println("UPNP: " + p.getUdpPacket().getAddress());
		}
	}
}
