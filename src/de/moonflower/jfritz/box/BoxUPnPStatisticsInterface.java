package de.moonflower.jfritz.box;

import de.moonflower.jfritz.utils.network.UPNPAddonInfosListener;
import de.moonflower.jfritz.utils.network.UPNPCommonLinkPropertiesListener;
import de.moonflower.jfritz.utils.network.UPNPExternalIpListener;
import de.moonflower.jfritz.utils.network.UPNPStatusInfoListener;

public interface BoxUPnPStatisticsInterface {
	public void getInternetStats(UPNPAddonInfosListener listener);
	public void getStatusInfo(UPNPStatusInfoListener listener);
	public void getExternalIPAddress(UPNPExternalIpListener listener);
	public void getCommonLinkInfo(UPNPCommonLinkPropertiesListener listener);
}
