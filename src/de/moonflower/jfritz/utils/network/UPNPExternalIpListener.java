package de.moonflower.jfritz.utils.network;

	/**
	 * This interface is used for setting the external ip
	 * retrieved from the UPNP-Service.
	 *
	 * @author Robert
	 *
	 */
public interface UPNPExternalIpListener {

	void setExternalIp(String externalIp);
}

