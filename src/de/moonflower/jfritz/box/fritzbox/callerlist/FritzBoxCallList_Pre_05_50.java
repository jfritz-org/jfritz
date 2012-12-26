package de.moonflower.jfritz.box.fritzbox.callerlist;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.Vector;

import de.moonflower.jfritz.box.BoxCallBackListener;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class FritzBoxCallList_Pre_05_50 extends FritzBoxCallList_Pre_04_86 {

	private static String POSTDATA_CLEAR_JOURNAL = "&telcfg:settings/ClearJournal&telcfg:settings/UseJournal=1";

	public FritzBoxCallList_Pre_05_50(FritzBox fritzBox, Vector<BoxCallBackListener> callbackListener) {
		super(fritzBox, callbackListener);
	}

	@Override
	public void clearCallerList() {
        String postdata = POSTDATA_CLEAR_JOURNAL;

		try {
			postdata = fritzBox.getPostData(postdata);
		} catch (UnsupportedEncodingException e) {
			Debug.error("Encoding not supported! " + e.toString());
		}

		String urlstr = fritzBox.getWebcmUrl();

		boolean finished = false;
		boolean password_wrong = false;
		int retry_count = 0;

		while (!finished && (retry_count < fritzBox.getMaxRetryCount()))
		{
			try {
				retry_count++;
				if (password_wrong)
				{
					password_wrong = false;
					Debug.debug("Detecting new firmware, getting new SID");
					fritzBox.detectFirmware();
			        postdata = POSTDATA_CLEAR_JOURNAL;

					try {
						postdata = fritzBox.getPostData(postdata);
					} catch (UnsupportedEncodingException e) {
						Debug.error("Encoding not supported! " + e.toString());
					}
				}
				JFritzUtils.fetchDataFromURLToVector(fritzBox.getName(), urlstr, postdata, true);
				finished = true;
			} catch (WrongPasswordException e) {
				password_wrong = true;
				Debug.debug("Wrong password, maybe SID is invalid.");
				fritzBox.setBoxDisconnected();
			} catch (SocketTimeoutException ste) {
				ste.printStackTrace();
				fritzBox.setBoxDisconnected();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fritzBox.setBoxDisconnected();
			} catch (InvalidFirmwareException e) {
				password_wrong = true;
				fritzBox.setBoxDisconnected();
			}
		}
	}
}
