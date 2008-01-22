package de.moonflower.jfritz.utils.network;

	/**
	 * This interface is used for setting various information retrieved
	 * form the getAddonInfos upnp service of the box
	 *
	 *
	 * @author brian
	 *
	 */
public interface AddonInfosListener {

	public void setBytesRate(String sent, String received);

	public void setTotalBytesInfo(String sent, String received);

	public void setDNSInfo(String dns1, String dns2);

	public void setVoipDNSInfo(String voipDns1, String voipDns2);

	public void setDisconnectInfo(String disconnectTime, String idleTime);

	public void setOtherInfo(String upnpControl, String routedMode);

}

