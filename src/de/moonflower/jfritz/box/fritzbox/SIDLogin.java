package de.moonflower.jfritz.box.fritzbox;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;

import de.moonflower.jfritz.box.BoxClass;
import de.moonflower.jfritz.exceptions.RedirectToLoginLuaException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.Debug;

public class SIDLogin {
	private boolean sidLogin;
	private boolean newSidLogin;
	private String sessionId;
	private String sidResponse;

	private final static String PATTERN_WRITE_ACCESS = "<iswriteaccess>([^<]*)</iswriteaccess>";
	private final static String PATTERN_CHALLENGE = "<challenge>([^<]*)</challenge>";
	private final static String PATTERN_SID = "<sid>([^\"]*)</sid>";
	private final static String PATTERN_SID_OLD = "<input type=\"hidden\" name=\"sid\" value=\"([^\"]*)\"";
	private final static String PATTERN_SID_IN_REDIRECT = "location: [^\\?]*\\?sid=([^$]*)$";

	protected FritzBoxLoginHandler loginHandler = FritzBoxLoginHandler.getInstance();

	public SIDLogin() {
		sidLogin = false;
		sessionId = "";
		sidResponse = "";
	}

	public void check(BoxClass box, String urlstr, String password) throws WrongPasswordException, IOException, RedirectToLoginLuaException {
		String login = "";
		try {
			Debug.debug("Try to get SID from XML");
			login = loginHandler.getLoginSidResponseFromXml(box, urlstr);
			newSidLogin = false;
		} catch (WrongPasswordException wpe) {
			Debug.debug("Try to get SID from LUA");
			login = loginHandler.getLoginSidResponseFromLua(box, urlstr);
			newSidLogin = true;
		} catch (RedirectToLoginLuaException rd) {
			Debug.debug("Detected redirect to LUA");
			login = loginHandler.getLoginSidResponseFromLua(box, urlstr);
			newSidLogin = true;
		}
		String box_password = replaceInvalidPasswordCharacters(password);

		Pattern challengePattern = Pattern.compile(PATTERN_CHALLENGE);
		Matcher challengeMatcher = challengePattern.matcher(login.toLowerCase());
		if (challengeMatcher.find()) {
			sidLogin = true;
		} else {
			sidLogin = false;
		}

		if (sidLogin) {
			Pattern writeAccessPattern = Pattern.compile(PATTERN_WRITE_ACCESS);
			Matcher matcher = writeAccessPattern.matcher(login.toLowerCase());
			if (matcher.find()) {
				int writeAccess = Integer.parseInt(matcher.group(1));

				if (writeAccess == 0) { // answer challenge
					Debug.debug("WriteAccess == 0, calculating response from challenge");
					calculateResponseFromChallenge(login, box_password);
				} else if (writeAccess == 1) { // no challenge, use SID directly
					Debug.debug("WriteAccess == 1, extracting sid from response");
					extractSidFromResponse(login);
				} else {
					Debug.error("Could not determine writeAccess in login_sid.xml");
				}
				// Debug.errDlg(Integer.toString(writeAccess) + " " + sessionId);
			} else {
				Debug.debug("Could not find writeAccess, calculating response from challenge");
				calculateResponseFromChallenge(login, box_password);
			}
		}
	}

	public void login(BoxClass box, String urlstr, List<NameValuePair> postdata) throws SocketTimeoutException, WrongPasswordException, IOException, RedirectToLoginLuaException {
		String response = "";
		if (newSidLogin) {
			response = loginHandler.loginLua(box, urlstr, this.sidResponse);
		} else {
			response = loginHandler.loginXml(box, urlstr, postdata);
		}
		extractSidFromResponse(response);
		if (this.getSessionId().equals("0000000000000000")) {
			response = loginHandler.loginLuaAlternative(box, urlstr, this.sidResponse);
			extractSidFromResponse(response);
		}
	}

	private void calculateResponseFromChallenge(String login,
			String box_password) {
		try {
			String challenge = "";
			Pattern challengePattern = Pattern.compile(PATTERN_CHALLENGE);
			Matcher challengeMatcher = challengePattern.matcher(login.toLowerCase());
			if (challengeMatcher.find()) {
				challenge = challengeMatcher.group(1);
				String md5Pass = generateMD5(challenge + "-" + box_password);
				sidResponse = challenge + '-' + md5Pass;
			} else {
				Debug.error("Could not determine challenge in login_sid.xml");
			}
		} catch (NoSuchAlgorithmException e) {
			Debug.netMsg("MD5 Algorithm not present in this JVM!");
			Debug.error(e.toString());
			e.printStackTrace();
		}
	}

	private String generateMD5(String pwd) throws NoSuchAlgorithmException {
		MessageDigest m = MessageDigest.getInstance("MD5");
		String md5Pass = "";
		byte passwordBytes[] = null;
		try {
			passwordBytes = pwd.getBytes("UTF-16LE");
			m.update(passwordBytes, 0, passwordBytes.length);
			md5Pass = new BigInteger(1, m.digest()).toString(16);
		} catch (UnsupportedEncodingException e) {
			Debug.errDlg("UTF-16LE encoding not supported by your system. Can not communicate with FRITZ!Box!");
		}
		return md5Pass;
	}

	private String replaceInvalidPasswordCharacters(String box_password) {
		// replace all unicodecharacters greater than 255 with the character '.'
		for (int i = 0; i < box_password.length(); i++) {
			int codePoint = box_password.codePointAt(i);
			if (codePoint > 255) {
				box_password = box_password.substring(0, i) + '.' + box_password.substring(i + 1);
			}
		}
		return box_password;
	}

	protected void extractSidFromResponse(String login) {
		Debug.debug("Extracting SID from response: " + login);
		Pattern sidPattern = Pattern.compile(PATTERN_SID);
		Matcher sidMatcher = sidPattern.matcher(login.toLowerCase());
		if (sidMatcher.find()) {
			Debug.info("Found SID using PATTERN_SID");
			sessionId = sidMatcher.group(1);
		} else {
			sidPattern = Pattern.compile(PATTERN_SID_OLD);
			sidMatcher = sidPattern.matcher(login.toLowerCase());
			if (sidMatcher.find()) {
				Debug.info("Found SID using PATTERN_SID_OLD");
				sessionId = sidMatcher.group(1);
			} else {
				sidPattern = Pattern.compile(PATTERN_SID_IN_REDIRECT);
				sidMatcher = sidPattern.matcher(login.toLowerCase());
				if (sidMatcher.find()) {
					Debug.info("Found SID using PATTERN_SID_IN_REDIRECT");
					sessionId = sidMatcher.group(1);
				} else {
					Debug.error("Could not find SID!");
				}
			}
		}
	}


	public void getSidFromResponse(Vector<String> data)
	{
		for (int i=0; i<data.size(); i++)
		{
			extractSidFromResponse(data.get(i));
		}
	}

	public boolean isSidLogin()
	{
		return sidLogin;
	}

	public String getResponse()
	{
		return sidResponse;
	}

	public String getSessionId()
	{
		return sessionId;
	}
}
