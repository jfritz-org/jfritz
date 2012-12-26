package de.moonflower.jfritz.box.fritzbox.query;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class QueryOld implements IQuery {

	private static final String POSTDATA_QUERY = "getpage=../html/query.txt";
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

		String postdata = generatePostData(queries);

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
					postdata = generatePostData(queries);
				}
				response = JFritzUtils.fetchDataFromURLToVector(fritzBox.getName(), urlstr, postdata, true);
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

	private final String generatePostData(Vector<String> queries)
	{
		if (fritzBox == null || fritzBox.getFirmware() == null) {
			// TODO log error
			return POSTDATA_QUERY + "&var:cnt=0";
		}

		String postdata = POSTDATA_QUERY + "&var:cnt=" + queries.size();
		for (int i=0; i<queries.size(); i++)
		{
			postdata = postdata + "&var:n" + i + "="+queries.get(i);
		}

		if (fritzBox.getFirmware().isSidLogin())
		{
			try {
			postdata = postdata + "&sid=" + URLEncoder.encode(fritzBox.getFirmware().getSessionId(), "ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				Debug.error("Encoding not supported");
				e.printStackTrace();
			}
		}
		else
		{
			try {
				postdata = postdata + "&login:command/password=" + URLEncoder.encode(fritzBox.getPassword(), "ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				Debug.error("Encoding not supported");
				e.printStackTrace();
			}
		}

		return postdata;
	}
}
