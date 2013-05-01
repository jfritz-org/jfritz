package de.moonflower.jfritz.box.fritzbox.callerlist;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import de.moonflower.jfritz.box.BoxCallBackListener;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.box.fritzbox.helper.FritzGetWithRetry;
import de.moonflower.jfritz.box.fritzboxnew.HttpHelper;
import de.moonflower.jfritz.exceptions.FeatureNotSupportedByFirmware;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.IProgressListener;
import de.moonflower.jfritz.utils.JFritzUtils;

public class FritzBoxCallList_Actual extends FritzBoxCallList_Pre_05_28 {

	private static String GET_CSV_LIST = "?csv=";
	private FritzGetWithRetry fritzGet;

	protected HttpHelper httpHelper = HttpHelper.getInstance();
    private Vector<Call> calls = new Vector<Call>();

	public FritzBoxCallList_Actual(FritzBox fritzBox, Vector<BoxCallBackListener> callbackListener) {
		super(fritzBox, callbackListener);
		fritzGet = new FritzGetWithRetry(fritzBox);
	}

	@Override
	public Vector<Call> getCallerList(Vector<IProgressListener> progressListener) throws IOException, MalformedURLException, FeatureNotSupportedByFirmware {
		String requestUrl = getFonCallsListLuaUrl() + GET_CSV_LIST;
		List<NameValuePair> postdata = new ArrayList<NameValuePair>();
		fritzBox.appendSidOrPassword(postdata);

		String response;
		try {
			response = JFritzUtils.postDataToUrlAndGetStringResponse(fritzBox.getName(), requestUrl, postdata, true, true);
			parseResponse(response, progressListener);
		} catch (WrongPasswordException e) {
			e.printStackTrace();
			fritzBox.setBoxDisconnected();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fritzBox.setBoxDisconnected();
		}

		return calls;
	}

	private String getFonCallsListLuaUrl() {
		return fritzBox.getUrlPrefix() + "/fon_num/foncalls_list.lua"; //$NON-NLS-1$, //$NON-NLS-2$, //$NON-NLS-3$
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
	public void clearCallerList() {
		fritzGet.setPrependAccessMethod(true);
		List<NameValuePair> postdata = new ArrayList<NameValuePair>();
				
		postdata.add(new BasicNameValuePair("usejournal","on"));
		postdata.add(new BasicNameValuePair("clear",""));
		postdata.add(new BasicNameValuePair("callstab","all"));
		
		fritzGet.getToVector(getFonCallsListLuaUrl(), postdata, false);
	}
}
