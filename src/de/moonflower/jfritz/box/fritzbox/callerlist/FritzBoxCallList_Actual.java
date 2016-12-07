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
import de.moonflower.jfritz.exceptions.FeatureNotSupportedByFirmware;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.IProgressListener;
import de.robotniko.fboxlib.exceptions.InvalidCredentialsException;
import de.robotniko.fboxlib.exceptions.LoginBlockedException;
import de.robotniko.fboxlib.exceptions.PageNotFoundException;

public class FritzBoxCallList_Actual extends FritzBoxCallList_Pre_05_28 {

	private static String GET_CSV_LIST = "?csv=";
    private Vector<Call> calls = new Vector<Call>();

	public FritzBoxCallList_Actual(FritzBox fritzBox, Vector<BoxCallBackListener> callbackListener) {
		super(fritzBox, callbackListener);
	}

	@Override
	public Vector<Call> getCallerList(Vector<IProgressListener> progressListener) throws FeatureNotSupportedByFirmware, ClientProtocolException, IOException, LoginBlockedException, InvalidCredentialsException, PageNotFoundException {
		String requestUrl = getFonCallsListLuaUrl() + GET_CSV_LIST;
		List<NameValuePair> postdata = new ArrayList<NameValuePair>();

		String response = fritzBox.postToPageAndGetAsString(requestUrl, postdata);
		parseResponse(response, progressListener);

		return calls;
	}

	private String getFonCallsListLuaUrl() {
		return "/fon_num/foncalls_list.lua"; //$NON-NLS-1$
	}


	private void parseResponse(String input, Vector<IProgressListener> progressListener) throws FeatureNotSupportedByFirmware {
		calls.clear();

		if (progressListener != null) {
			for (IProgressListener l: progressListener) {
				l.setMin(0);
				l.setMax(0);
				l.setProgress(0);
			}
		}

		CallListCsvParser parser = new CallListCsvParser();
		parser.setProgressListener(progressListener);

		calls.addAll(parser.parseCsvString(fritzBox, input));
	}

	@Override
	public void clearCallerList() throws ClientProtocolException, IOException, LoginBlockedException, InvalidCredentialsException, PageNotFoundException {
		List<NameValuePair> postdata = new ArrayList<NameValuePair>();
				
		postdata.add(new BasicNameValuePair("usejournal","on"));
		postdata.add(new BasicNameValuePair("clear",""));
		postdata.add(new BasicNameValuePair("callstab","all"));
		
		fritzBox.postToPageAndGetAsString(getFonCallsListLuaUrl(), postdata);
	}
}
