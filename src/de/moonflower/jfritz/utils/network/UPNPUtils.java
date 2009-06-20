package de.moonflower.jfritz.utils.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
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

	private final static int SSDP_MAX_BOXES = 5;

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
			while (i < SSDP_MAX_BOXES) {
				DatagramPacket packet = new DatagramPacket(new byte[1024],
						1024, SSDPPacket.getSSDPAddress(), socket
								.getLocalPort());
				socket.receive(packet);
				devices.add(new SSDPPacket(packet));
				i++;
			}
			socket.close();
		} catch (SocketTimeoutException e) {
			Debug.warning("Timeout for SSDP"); //$NON-NLS-1$
		} catch (SocketException e) {
            Debug.error(e.toString());
		} catch (IOException e) {
			Debug.error(e.toString());
		}
		return devices;
	}

	public static Vector<SSDPPacket> SSDP_discoverFritzBoxes(int timeout) {
		Vector<SSDPPacket> devices = SSDP_discoverDevices(timeout);
		Vector<SSDPPacket> fritzboxes = new Vector<SSDPPacket>();
		Enumeration<SSDPPacket> en = devices.elements();
		while (en.hasMoreElements()) {
			SSDPPacket p = en.nextElement();
			if (p.getServer().toLowerCase().indexOf("avm fritz!box") > 0) { //$NON-NLS-1$
				Debug.info("Box found at " + p.getIP().toString() + ": " //$NON-NLS-1$,  //$NON-NLS-2$
						+ p.getServer());
				fritzboxes.add(p);
			}
		}
		return fritzboxes;
	}

	/**
	 * function calls the web service specified by the url with the soap
	 * envelope specified in xml
	 *
	 * @param url of the web service
	 * @param urn
	 * @param xml soap element to be trasmitted
	 * @return
	 */
	public static String getSOAPData(String url, String urn, String xml) {

		String data = ""; //$NON-NLS-1$
		BufferedReader d = null;
		DataOutputStream printout = null;
		try {
			URL u = new URL(url);

			HttpURLConnection uc = (HttpURLConnection) u.openConnection();
			uc.setRequestMethod("POST");

			// 5 Sekunden-Timeout für Verbindungsaufbau
			uc.setConnectTimeout(5000);

			uc.setReadTimeout(2000);

			uc.setDoOutput(true);
			uc.setDoInput(true);

			byte[] bytes = xml.getBytes();
			uc.setRequestProperty("CONTENT-LENGTH", String.valueOf(bytes.length));

			uc.setRequestProperty("CONTENT-TYPE", //$NON-NLS-1$
							"text/xml; charset=\"utf-8\""); //$NON-NLS-1$
			uc.setRequestProperty("SOAPACTION", urn); //$NON-NLS-1$
			uc.setRequestProperty("USER-AGENT", "AVM UPnP/1.0 Client 1.0");

			printout = new DataOutputStream(uc
					.getOutputStream());
			printout.write(bytes);
			printout.close();

			//InputStream in = uc.getInputStream();
			d = new BufferedReader(new InputStreamReader(uc
					.getInputStream()));

			String str;
			while (null != ((str = d.readLine())))
				data += str + "\n"; //$NON-NLS-1$


		} catch (IOException e) {
            Debug.error(e.toString());
		}finally{
			try{
				if(d!=null)
					d.close();
			}catch(IOException ioe){
				Debug.error("Error closing Stream");
			}
			try{
				if(printout!=null)
					printout.close();
			}catch(IOException ioe){
				Debug.error("Error closing Stream");
			}
		}
		return data;
	}
}
