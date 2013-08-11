package de.robotniko.fbcrawler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import de.robotniko.fbcrawler.exceptions.SIDException;
import de.robotniko.fbcrawler.exceptions.WrongPasswordException;

public class SIDLogin {

	private static SIDLogin INSTANCE = new SIDLogin();
	private static String url = "/login_sid.lua";
	
	private String writeAccessRegex = "<iswriteaccess>([^<]*)</iswriteaccess>";
	private Pattern writeAccessPattern = Pattern.compile(writeAccessRegex);
	private String challengeRegex = "<challenge>([^<]*)</challenge>";
	private Pattern challengePattern = Pattern.compile(challengeRegex);
	private String sidRegex = "<sid>([^\"]*)</sid>";
	private Pattern sidPattern = Pattern.compile(sidRegex);

	private String sidOldRegex = "<input type=\"hidden\" name=\"sid\" value=\"([^\"]*)\"";
	private String sidInRedirectRegex = "location: [^\\?]*\\?sid=([^$]*)$";
	
	private String login_sid_response = "";
	
	private String boxUrl = "";
	private String password = "";
	private int writeAccess = -1;
	private String challenge = "";
	
	private String challengeResponse = "";
	private static String sid = "0000000000000000";
	
	public static boolean isValidLoginMethod(String boxUrl) throws IOException {
		HttpClient httpClient = new DefaultHttpClient();
		
        try {
            HttpGet httpget = new HttpGet(boxUrl + url);

            // Create a response handler
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            httpClient.execute(httpget, responseHandler);
    		return true;
        } catch (ClientProtocolException e) {
        	return false;
        } catch (IOException e) {
        	throw e;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }		
	}
	
	public static void login(String boxUrl, String password) throws SIDException, IOException, WrongPasswordException {
		INSTANCE.doLogin(boxUrl, password);
	}
	
	private void doLogin(String boxUrl, String password) throws SIDException, IOException, WrongPasswordException {
		this.boxUrl = boxUrl;
		replaceInvalidPasswordCharacters(password);
		
		getResponse();
		getWriteAccess();
		getSid();

		if (writeAccess == 0) { // answer challenge
			loginWithChallenge();
		} else if (writeAccess == 1) { // no challenge, use SID directly
			extractSidFromResponse(login_sid_response);
		} else {
			if ("0000000000000000".equals(sid)) {
				loginWithChallenge();
			} else {
				// nothing to do, we have a valid sid
			}
		}
	}
	
	private void replaceInvalidPasswordCharacters(String box_password) {
		// replace all unicodecharacters greater than 255 with the character '.'
		for (int i = 0; i < box_password.length(); i++) {
			int codePoint = box_password.codePointAt(i);
			if (codePoint > 255) {
				box_password = box_password.substring(0, i) + '.' + box_password.substring(i + 1);
			}
		}
		this.password = box_password;
	}
	
	private void getResponse() throws IOException {
		HttpClient httpClient = new DefaultHttpClient();
		
        try {
            HttpGet httpget = new HttpGet(boxUrl + url);
            System.out.println("executing request " + httpget.getURI());

            // Create a response handler
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            login_sid_response = httpClient.execute(httpget, responseHandler);
        } catch (ClientProtocolException e) {
        	challenge = e.getMessage();
        	throw e;
        } catch (IOException e) {
        	challenge = e.getMessage();
        	throw e;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
	}
	
	private void getWriteAccess() throws SIDException, IOException {
		Matcher matcher = writeAccessPattern.matcher(login_sid_response.toLowerCase());
		if (matcher.find()) {
			writeAccess = Integer.parseInt(matcher.group(1));
		}
	}
	
	private void getSid() {
		Matcher matcher = sidPattern.matcher(login_sid_response.toLowerCase());
		if (matcher.find()) {
			sid = matcher.group(1);
		} else {
			sid = "0000000000000000";
		}
	}
	
	private void loginWithChallenge() throws SIDException, IOException, WrongPasswordException {
		getChallenge();
		calculateResponseFromChallenge();
		
		loginUsingChallengeResponse();
	}
	
	private void getChallenge() throws SIDException, IOException {
        Matcher challengeMatcher = challengePattern.matcher(login_sid_response.toLowerCase());
		
        if (challengeMatcher.find()) {
        	challenge = challengeMatcher.group(1);
        } else {
        	throw new SIDException("Could not find CHALLENGE");
        }
	}

	private void calculateResponseFromChallenge() throws SIDException, IOException {
		try {
			challengeResponse = challenge + "-" + generateMD5(challenge + "-" + password);
		} catch (NoSuchAlgorithmException e) {
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
		}
		return md5Pass;
	}
	
	
	private void loginUsingChallengeResponse() throws IOException, WrongPasswordException {
		String response = "";

		HttpClient httpClient = new DefaultHttpClient();
		
        try {
            HttpGet httpget = new HttpGet(boxUrl + url + "?username=&response=" + challengeResponse);

            // Create a response handler
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            response = httpClient.execute(httpget, responseHandler);
        } catch (ClientProtocolException e) {
        	response = e.getMessage();
        	throw e;
        } catch (IOException e) {
        	response = e.getMessage();
        	throw e;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

		extractSidFromResponse(response);
		
		if ("0000000000000000".equals(sid)) {
			throw new WrongPasswordException("Wrong password, could not get a valid session ID");
		}
	}
	
	protected void extractSidFromResponse(String response) {
		Pattern sidPattern = Pattern.compile(sidRegex);
		Matcher sidMatcher = sidPattern.matcher(response.toLowerCase());
		if (sidMatcher.find()) {
			sid = sidMatcher.group(1);
		} else {
			sidPattern = Pattern.compile(sidOldRegex);
			sidMatcher = sidPattern.matcher(response.toLowerCase());
			if (sidMatcher.find()) {
				sid = sidMatcher.group(1);
			} else {
				sidPattern = Pattern.compile(sidInRedirectRegex);
				sidMatcher = sidPattern.matcher(response.toLowerCase());
				if (sidMatcher.find()) {
					sid = sidMatcher.group(1);
				} else {
//					Debug.error("Could not find SID!");
				}
			}
		}
	}

	public String getPage(String url) throws WrongPasswordException, IOException {
		if ("0000000000000000".equals(sid)) {
			throw new WrongPasswordException("Wrong password, could not get a valid session ID");
		}

		String response = "";

		HttpClient httpClient = new DefaultHttpClient();
		
        try {
            HttpGet httpget = new HttpGet(boxUrl + url + "?sid=" + sid);
            System.out.println("executing request " + httpget.getURI());

            // Create a response handler
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            response = httpClient.execute(httpget, responseHandler);
        } catch (ClientProtocolException e) {
        	response = e.getMessage();
        	throw e;
        } catch (IOException e) {
        	response = e.getMessage();
        	throw e;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
		
		return response;
	}
	
	public static String getSessionId() {
		return sid;
	}
}
