package de.moonflower.jfritz.monitoring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TimerTask;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.network.AddonInfosListener;

/**
 * This class is called automatically by the scheduler,
 * it parses the current internet activity using the SOAP Backend
 * on the box. Then is calls monitoringPanel.updateInternetUsage(..)
 * to update the panel
 *
 *
 * @author Brian Jensen
 *
 */
public class UpdateInternetTask extends TimerTask implements AddonInfosListener {

	private MonitoringPanel monitoringPanel;

	public UpdateInternetTask(MonitoringPanel mPanel){
		monitoringPanel = mPanel;
	}

	public void run() {

		//Access the AddonsInfo web service of the box
		JFritz.getFritzBox().getInternetStats(this);

	}

	public void setBytesRate(String sent, String received){
		monitoringPanel.updateInternetUsage(sent, received);
	}

	public void setTotalBytesInfo(String sent, String received){
		//not needed here
	}

	public void setDNSInfo(String dns1, String dns2){
		//not needed here
	}

	public void setVoipDNSInfo(String voipDns1, String voipDns2){
		//not needed here
	}

	public void setDisconnectInfo(String disconnectTime, String idleTime){
		//not needed here
	}

	public void setOtherInfo(String upnpControl, String routedMode){
		//not needed here
	}

}
