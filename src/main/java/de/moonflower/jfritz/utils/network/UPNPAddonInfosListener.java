package de.moonflower.jfritz.utils.network;

	/**
	 * This interface is used for setting various information retrieved
	 * form the getAddonInfos upnp service of the box
	 *
	 *
	 * @author brian
	 *
	 */
public interface UPNPAddonInfosListener {

	void setBytesRate(String sent, String received);

	void setTotalBytesInfo(String sent, String received);

	void setDNSInfo(String dns1, String dns2);

}

