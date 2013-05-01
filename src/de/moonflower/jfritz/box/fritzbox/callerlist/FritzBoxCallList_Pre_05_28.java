package de.moonflower.jfritz.box.fritzbox.callerlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import de.moonflower.jfritz.box.BoxCallBackListener;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.box.fritzbox.helper.FritzGetWithRetry;

public class FritzBoxCallList_Pre_05_28 extends FritzBoxCallList_Pre_04_86 {

	private FritzGetWithRetry fritzGet;

	public FritzBoxCallList_Pre_05_28(FritzBox fritzBox, Vector<BoxCallBackListener> callbackListener) {
		super(fritzBox, callbackListener);
		fritzGet = new FritzGetWithRetry(fritzBox);
	}

	@Override
	public void clearCallerList() {
		fritzGet.setPrependAccessMethod(true);
		List<NameValuePair> postdata = new ArrayList<NameValuePair>();
		postdata.add(new BasicNameValuePair("telcfg:settings/ClearJournal",""));
		postdata.add(new BasicNameValuePair("telcfg:settings/UseJournal","1"));
		
		fritzGet.getToVector(fritzBox.getWebcmUrl(), postdata, false);
	}
}
