package de.moonflower.jfritz.utils.upnp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Vector;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;

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
			while (i < JFritz.SSDP_MAX_BOXES) {
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

	public static String getSOAPData() {
		// http://192.168.178.1:49000/upnp/control/WANCommonIFC1
		// UpstreamMaxBitRate, DownstreamMaxBitRate, PhysicalLinkStatus:
		// urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1#GetCommonLinkProperties
		// Bytes-Statistik, DNS:
		// urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1#GetAddonInfos
		// urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1#GetModulationType

		// http://192.168.178.1:49000/upnp/control/WANIPConn1
		// urn:schemas-upnp-org:service:WANIPConnection:1#GetExternalIPAddress
		// Uptime and Connection:
		// urn:schemas-upnp-org:service:WANIPConnection:1#GetStatusInfo

		String data = "";
		final String server = "http://192.168.178.1:49000/upnp/control/WANCommonIFC1";
		final String SOAP_ACTION = "urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1#GetAddonInfos";

		try {
			URL u = new URL(server);
			URLConnection uc = u.openConnection();

			uc.setDoOutput(true);
			uc.setDoInput(true);
			uc
					.setRequestProperty("Content-Type",
							"text/xml; charset=\"utf-8\"");
			uc.setRequestProperty("SOAPAction", SOAP_ACTION);

			DataOutputStream printout = new DataOutputStream(uc
					.getOutputStream());
			printout.close();

			InputStream in = uc.getInputStream();
			BufferedReader d = new BufferedReader(new InputStreamReader(uc
					.getInputStream()));

			String str;
			while (null != ((str = d.readLine())))
				data += str + "\n";

		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
}
