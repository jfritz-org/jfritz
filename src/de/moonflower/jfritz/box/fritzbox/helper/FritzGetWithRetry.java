package de.moonflower.jfritz.box.fritzbox.helper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;

import org.apache.http.NameValuePair;

import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class FritzGetWithRetry {

	private FritzBox fritzBox;
	private boolean prependAccessMethod = false;

	public FritzGetWithRetry(FritzBox fritzBox) {
		this.fritzBox = fritzBox;
	}

	public void setPrependAccessMethod(boolean shouldPrepend) {
		this.prependAccessMethod = shouldPrepend;
	}

	public Vector<String> getToVector(final String url, final List<NameValuePair> postdata, boolean shouldGetResult) {
        Vector<String> result = new Vector<String>();

		try {
			appendAccessMethodAndAuth(postdata);
		} catch (UnsupportedEncodingException e) {
			Debug.error("Encoding not supported! " + e.toString());
		}

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

					postdata.clear();
					try {
						appendAccessMethodAndAuth(postdata);
					} catch (UnsupportedEncodingException e) {
						Debug.error("Encoding not supported! " + e.toString());
					}
				}
				result = JFritzUtils.postDataToUrlAndGetVectorResponse(fritzBox.getName(), url, postdata, shouldGetResult, true);
				finished = true;
			} catch (WrongPasswordException e) {
				password_wrong = true;
				Debug.debug("Wrong password, maybe SID is invalid.");
				fritzBox.setBoxDisconnected();
			} catch (SocketTimeoutException ste) {
				ste.printStackTrace();
				fritzBox.setBoxDisconnected();
			} catch (IOException e) {
				e.printStackTrace();
				fritzBox.setBoxDisconnected();
			} catch (InvalidFirmwareException e) {
				password_wrong = true;
				fritzBox.setBoxDisconnected();
			} catch (URISyntaxException e) {
				e.printStackTrace();
				fritzBox.setBoxDisconnected();
			}
		}
		return result;
	}

	private void appendAccessMethodAndAuth(List<NameValuePair> postdata) throws UnsupportedEncodingException {
		if (prependAccessMethod) {
			fritzBox.appendAccessMethod(postdata);
		} else {
			fritzBox.appendSidOrPassword(postdata);
		}
	}
}
