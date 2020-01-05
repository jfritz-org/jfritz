package de.moonflower.jfritz.box;

import de.moonflower.jfritz.utils.network.UPNPAddonInfosListener;
import de.moonflower.jfritz.utils.network.UPNPCommonLinkPropertiesListener;
import de.moonflower.jfritz.utils.network.UPNPExternalIpListener;
import de.moonflower.jfritz.utils.network.UPNPStatusInfoListener;

import java.io.IOException;

public interface BoxUPnPStatisticsInterface {
	void getInternetStats(UPNPAddonInfosListener listener) throws NoSuchFieldException, IOException;
	void getStatusInfo(UPNPStatusInfoListener listener) throws IOException, NoSuchFieldException;
	void getExternalIPAddress(UPNPExternalIpListener listener) throws IOException, NoSuchFieldException;
	void getCommonLinkInfo(UPNPCommonLinkPropertiesListener listener) throws IOException, NoSuchFieldException;
}
