package de.moonflower.jfritz.box.fritzbox.callerlist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import de.moonflower.jfritz.box.BoxCallBackListener;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import org.jfritz.fboxlib.exceptions.InvalidCredentialsException;
import org.jfritz.fboxlib.exceptions.LoginBlockedException;
import org.jfritz.fboxlib.exceptions.PageNotFoundException;

public class FritzBoxCallList_Pre_05_28 extends FritzBoxCallList_Pre_04_86 {

	public FritzBoxCallList_Pre_05_28(FritzBox fritzBox, Vector<BoxCallBackListener> callbackListener) {
		super(fritzBox, callbackListener);
	}

	@Override
	public void clearCallerList() throws ClientProtocolException, IOException, LoginBlockedException, InvalidCredentialsException, PageNotFoundException {
		List<NameValuePair> postdata = new ArrayList<NameValuePair>();
		postdata.add(new BasicNameValuePair("telcfg:settings/ClearJournal",""));
		postdata.add(new BasicNameValuePair("telcfg:settings/UseJournal","1"));

		fritzBox.postToPageAndGetAsVector(fritzBox.getWebcmUrl(), postdata);
	}
}
