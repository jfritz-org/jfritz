package de.moonflower.jfritz.box.fritzbox.query;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class QueryOld implements IQuery {

	private static final String PARSE_LOGIN_REASON = "var theReason = parseInt\\(\"([^\"]*)\",10\\)";

	protected FritzBox fritzBox;
	protected MessageProvider messages = MessageProvider.getInstance();

	public QueryOld(FritzBox fritzBox) {
		this.fritzBox = fritzBox;
	}

	@Override
	public Vector<String> getQuery(Vector<String> queries) {
		Vector<String> response = new Vector<String>();

		if (fritzBox == null) {
			// TODO log error message
			return response;
		}

		List<NameValuePair> postdata = generatePostData(queries);

		final String urlstr = fritzBox.getWebcmUrl();

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
					postdata = generatePostData(queries);
				}
				response = JFritzUtils.postDataToUrlAndGetVectorResponse(fritzBox, urlstr, postdata, true, true);
				finished = true;
			} catch (WrongPasswordException e) {
				password_wrong = true;
				Debug.debug("Wrong password, maybe SID is invalid.");
				fritzBox.setBoxDisconnected();
			} catch (SocketTimeoutException ste) {
				ste.printStackTrace();
				fritzBox.setBoxDisconnected();
			} catch (InvalidFirmwareException e) {
				password_wrong = true;
				fritzBox.setBoxDisconnected();
			} catch (IOException e) {
				e.printStackTrace();
				fritzBox.setBoxDisconnected();
			} catch (Exception e) {
				e.printStackTrace();
				fritzBox.setBoxDisconnected();
			}
		}

		if ((response.size() != 0)
		  && (response.size() > (queries.size()+1)))
		{
			Pattern p = Pattern.compile(PARSE_LOGIN_REASON);
			for (int i=0; i<response.size(); i++)
			{
				Matcher m = p.matcher(response.get(i));
				if (m.find())
				{
					try {
						int loginReason = Integer.parseInt(m.group(1));
						if (loginReason == 2) // SID-Timeout
						{
							try {
								Debug.debug("SessionID expired, getting new SessionId!");
								fritzBox.detectFirmware();
								response = getQuery(queries);
								response.add(""); // add empty line to be removed further down in this method
							} catch (WrongPasswordException e) {
								Debug.errDlg(messages.getMessage("box.wrong_password"));
								fritzBox.setBoxDisconnected();
							} catch (InvalidFirmwareException e) {
								Debug.errDlg(messages.getMessage("box.address_wrong"));
								fritzBox.setBoxDisconnected();
							} catch (IOException e) {
								Debug.errDlg("I/O Exception");
								fritzBox.setBoxDisconnected();
							}
						}
					} catch (NumberFormatException nfe)
					{
						Debug.errDlg("Could not login to FritzBox. Please check password and try it again!");
						fritzBox.setBoxDisconnected();
					}
				}
			}
		}

		if (response.size() != 0)
		{
			response.remove(response.size()-1); // letzte Zeile entfernen (leerzeile)
		}

		Debug.debug("Query-Response: ");
		for (int i=0; i<response.size(); i++)
		{
			Debug.debug(response.get(i));
		}
		Debug.debug("---");
		return response;
	}

	private final List<NameValuePair> generatePostData(Vector<String> queries)
	{
		List<NameValuePair> postdata = new ArrayList<NameValuePair>();
		postdata.add(new BasicNameValuePair("getpage","../html/query.txt"));
		
		if (fritzBox == null || fritzBox.getFirmware() == null) {
			// TODO log error
			postdata.add(new BasicNameValuePair("var:cnt", "0"));
			return postdata;
		}

		postdata.add(new BasicNameValuePair("var:cnt", Integer.toString(queries.size())));
		for (int i=0; i<queries.size(); i++)
		{
			postdata.add(new BasicNameValuePair("var:n" + i, queries.get(i)));
		}

		try {
			fritzBox.appendSidOrPassword(postdata);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			fritzBox.setBoxDisconnected();
		}

		return postdata;
	}
}
