package de.moonflower.jfritz.utils.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
//import java.io.InputStream;
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
 * TODO: Bugfix: Exception if no UPNP enabled
 */
public class UPNPUtils {

	/**
	 * @param timeout
	 * @return Vector of SSDPPackets
	 */
	public static Vector<SSDPPacket> SSDP_discoverDevices(int timeout) {
		Vector<SSDPPacket> devices = new Vector<SSDPPacket>();
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
			Debug.msg("Timeout for SSDP"); //$NON-NLS-1$
		} catch (SocketException e) {
            Debug.err(e.toString());
		} catch (IOException e) {
			Debug.err(e.toString());
		}
		return devices;
	}

	public static Vector SSDP_discoverFritzBoxes(int timeout) {
		Vector<SSDPPacket> devices = SSDP_discoverDevices(timeout);
		Vector<SSDPPacket> fritzboxes = new Vector<SSDPPacket>();
		Enumeration<SSDPPacket> en = devices.elements();
		while (en.hasMoreElements()) {
			SSDPPacket p = en.nextElement();
			if (p.getServer().toLowerCase().indexOf("avm fritz!box") > 0) { //$NON-NLS-1$
				Debug.msg("Box found at " + p.getIP().toString() + ": " //$NON-NLS-1$,  //$NON-NLS-2$
						+ p.getServer());
				fritzboxes.add(p);
			}
		}
		return fritzboxes;
	}

	public static String getSOAPData(String url, String urn) {
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

		String data = ""; //$NON-NLS-1$
		BufferedReader d = null;
		DataOutputStream printout = null;
		try {
			URL u = new URL(url);
			URLConnection uc = u.openConnection();

			// 5 Sekunden-Timeout für Verbindungsaufbau
			uc.setConnectTimeout(5000);

			uc.setDoOutput(true);
			uc.setDoInput(true);
			uc
					.setRequestProperty("Content-Type", //$NON-NLS-1$
							"text/xml; charset=\"utf-8\""); //$NON-NLS-1$
			uc.setRequestProperty("SOAPAction", urn); //$NON-NLS-1$

			printout = new DataOutputStream(uc
					.getOutputStream());
			printout.close();

			//InputStream in = uc.getInputStream();
			d = new BufferedReader(new InputStreamReader(uc
					.getInputStream()));

			String str;
			while (null != ((str = d.readLine())))
				data += str + "\n"; //$NON-NLS-1$

		} catch (IOException e) {
            Debug.err(e.toString());
		}finally{
			try{
				if(d!=null)
					d.close();
			}catch(IOException ioe){
				Debug.err("Error closing Stream");
			}
			try{
				if(printout!=null)
					printout.close();
			}catch(IOException ioe){
				Debug.err("Error closing Stream");
			}
		}
		return data;
	}
}
