package de.moonflower.jfritz.utils.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;
import de.moonflower.jfritz.utils.Debug;

/*
 * Created on 22.05.2005
 *
 */

/**
 * @author Arno Willig
 *
 */
public class SSDPPacket {

    final static String SSDP_ADDRESS = "239.255.255.250"; //$NON-NLS-1$

    final static String SSDP_DISCOVER = "M-SEARCH * HTTP/1.1\r\nST: upnp:rootdevice\r\n" //$NON-NLS-1$
            + "MX: 10\r\nMAN: \"ssdp:discover\"\r\nHOST: 239.255.255.250:1900\r\n\r\n"; //$NON-NLS-1$

    private DatagramPacket udpPacket;

    private String server = "", location = "", cachecontrol = "", ext = "", //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$,  //$NON-NLS-4$
            st = "", usn = ""; //$NON-NLS-1$,  //$NON-NLS-2$

    /**
     * creates a SSDPPacket from a DatagramPacket
     *
     * TODO: Vielleicht noch Prüfen, ob das Paket korrekt ist, ansonsten eine
     * Exception werfen.
     */
    public SSDPPacket(DatagramPacket packet) {
        this.udpPacket = packet;
        String[] data = new String(packet.getData()).split("\r\n"); //$NON-NLS-1$
        for (int i = 0; i < data.length; i++) {
            if (data[i].startsWith("SERVER:")) //$NON-NLS-1$
                server = data[i].substring(7).trim();
            else if (data[i].startsWith("LOCATION:")) //$NON-NLS-1$
                location = data[i].substring(9).trim();
            else if (data[i].startsWith("CACHE-CONTROL:")) //$NON-NLS-1$
                cachecontrol = data[i].substring(15).trim();
            else if (data[i].startsWith("EXT:")) //$NON-NLS-1$
                ext = data[i].substring(4).trim();
            else if (data[i].startsWith("ST:")) //$NON-NLS-1$
                st = data[i].substring(3).trim();
            else if (data[i].startsWith("USN:")) //$NON-NLS-1$
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
        String parts[] = getServer().split(" ", 4); //$NON-NLS-1$
        String name = parts[3];
        return name;
    }

    private FritzBoxFirmware getFirmware() {
        try {
        	FritzBoxFirmware firmware = FritzBoxFirmware.detectFirmwareVersion(this.getIP().toString().substring(1), "", Main.getProperty("box.port", "80"));
        	Debug.msg("SSDP: FritzBox-IP: "+this.getIP().toString().substring(1));
        	Debug.msg("SSDP: FritzBox-Firmware: "+firmware.getFirmwareVersion());
            return firmware;
        } catch (WrongPasswordException wpe) {
            return null;
        } catch (InvalidFirmwareException e) {
            return null;
        } catch (IOException ioe) {
            return null;
        }
    }
}
