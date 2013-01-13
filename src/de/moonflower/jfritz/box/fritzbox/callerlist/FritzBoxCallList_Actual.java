package de.moonflower.jfritz.box.fritzbox.callerlist;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import de.moonflower.jfritz.box.BoxCallBackListener;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.box.fritzbox.helper.FritzGetWithRetry;
import de.moonflower.jfritz.box.fritzboxnew.HttpHelper;
import de.moonflower.jfritz.exceptions.FeatureNotSupportedByFirmware;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.IProgressListener;
import de.moonflower.jfritz.utils.Debug;

public class FritzBoxCallList_Actual extends FritzBoxCallList_Pre_05_28 {

	private static String GET_CSV_LIST = "?csv=";
	private static String POSTDATA_CLEAR_JOURNAL = "&telcfg:settings/ClearJournal&telcfg:settings/UseJournal=1";
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
		requestUrl = fritzBox.appendSidOrPassword(requestUrl);

		String response = httpHelper.getHttpContentAsString(requestUrl);
		parseResponse(response, progressListener);

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
		fritzGet.getToVector(fritzBox.getWebcmUrl(), POSTDATA_CLEAR_JOURNAL, false);
	}
}
