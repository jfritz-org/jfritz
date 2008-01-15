package de.moonflower.jfritz.monitoring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TimerTask;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;

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
public class UpdateInternetTask extends TimerTask {

	private MonitoringPanel monitoringPanel;

	private Pattern pRate = Pattern.compile(
			"<NewByteSendRate>([0-9]*)</NewByteSendRate>\\n<NewByteReceiveRate>([0-9]*)</NewByteReceiveRate>");

	private Matcher mRate;

	public UpdateInternetTask(MonitoringPanel mPanel){
		monitoringPanel = mPanel;
	}

	public void run() {

		//get the SOAP response from the box
		String xml = JFritz.getFritzBox().getInternetStats();

		//I would have used a sax parser, but this should be quicker
		//as i only really need these two fields
		mRate = pRate.matcher(xml);
		if(mRate.find()){
			monitoringPanel.updateInternetUsage(mRate.group(2), mRate.group(1));
		}
	}

}
