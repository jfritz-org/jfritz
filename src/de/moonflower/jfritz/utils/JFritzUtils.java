/*
 *
 * Created on 06.05.2005
 *
 */
package de.moonflower.jfritz.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import de.moonflower.jfritz.box.BoxClass;
import de.moonflower.jfritz.exceptions.RedirectToLoginLuaException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.properties.PropertyProvider;

/**
 * Static class for data retrieval from the fritz box and for several global
 * functions
 *
 * @author akw
 *
 */
public class JFritzUtils {
	private static final Logger log = Logger.getLogger(JFritzUtils.class);

	private static final int CONNECTION_TIMEOUT = 5000; //$NON-NLS-1$
	private static final int READ_TIMEOUT = 120000; //$NON-NLS-1$
	public static final String FILESEP = System.getProperty("file.separator"); //$NON-NLS-1$
	public static final String PATHSEP = System.getProperty("path.separator"); //$NON-NLS-1$
	public static final String binID = FILESEP + "jfritz.jar"; //$NON-NLS-1$
	public static final String rootID = FILESEP; //$NON-NLS-1$
	/**
	 * This constant can be used to search for the lang-directory.
	 *
	 * @see #getFullPath(String)
	 */
	public static final String langID = FILESEP + "lang";

	private final static String PATTERN_WAIT_FOR_X_SECONDS = "var loginBlocked = parseInt\\(\"([^\"]*)\",10\\);";

	protected static PropertyProvider properties = PropertyProvider.getInstance();

	public static String getDataFromUrlToString(final BoxClass affectedBox,
			final String urlstr, boolean retrieveData, boolean isRedirectEnabled)
			throws WrongPasswordException, SocketTimeoutException, IOException, URISyntaxException, RedirectToLoginLuaException {
		HttpParams httpParams = initConnectionParameters();
		
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpUriRequest request = initGetRequest(urlstr);
		
		String result = getResponseToString(affectedBox, httpClient, request, urlstr, null, isRedirectEnabled);
		request.abort();
		httpClient.getConnectionManager().shutdown();
		return result;
	}

	public static Vector<String> getDataFromUrlToVector(final BoxClass affectedBox,
			final String urlstr, boolean retrieveData, boolean isRedirectEnabled)
			throws WrongPasswordException, SocketTimeoutException, IOException, URISyntaxException, RedirectToLoginLuaException {
		HttpParams httpParams = initConnectionParameters();
		
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpUriRequest request = initGetRequest(urlstr);
		
		Vector<String> result = getResponseToVector(affectedBox, httpClient, request, urlstr, null, isRedirectEnabled);
		request.abort();
		httpClient.getConnectionManager().shutdown();
		return result;
	}

	private static HttpParams initConnectionParameters() {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, READ_TIMEOUT);
		return httpParams;
	}

	private static HttpGet initGetRequest(final String urlstr)
			throws URISyntaxException {
		HttpGet request = new HttpGet();
		request.setURI(new URI(urlstr));
		return request;
	}
	
	private static HttpResponse getResponse(final BoxClass affectedBox,
			DefaultHttpClient httpClient, HttpUriRequest request, String urlstr, final List<NameValuePair> postdata, boolean isRedirectEnabled)
			throws IOException, ClientProtocolException, WrongPasswordException {
		if (isRedirectEnabled) {
			setRedirectHandler(httpClient, urlstr, postdata);
		}
		
		HttpResponse response = httpClient.execute(request);
		// TODO check for meta redirect
		return response;
	}
		
	private static String getResponseToString(final BoxClass affectedBox,
			DefaultHttpClient httpClient, HttpUriRequest request, String urlstr, final List<NameValuePair> postdata, boolean isRedirectEnabled)
			throws IOException, ClientProtocolException, WrongPasswordException, RedirectToLoginLuaException {
		HttpResponse response = getResponse(affectedBox, httpClient, request, urlstr, postdata, isRedirectEnabled);
		String responseString = EntityUtils.toString(response.getEntity());
		
		if (isRedirectToLogonLua(responseString)) {
			 throw new RedirectToLoginLuaException(affectedBox.getName(), "Detected redirect to login.lua");
		}
		
		containsWrongPassword(affectedBox, responseString);
		return responseString;
	}
	
	private static Vector<String> getResponseToVector(final BoxClass affectedBox,
			DefaultHttpClient httpClient, HttpUriRequest request, String urlstr, final List<NameValuePair> postdata, boolean isRedirectEnabled)
			throws IOException, ClientProtocolException, WrongPasswordException, RedirectToLoginLuaException {
		Vector<String> result = new Vector<String>();
		HttpResponse response = getResponse(affectedBox, httpClient, request, urlstr, postdata, isRedirectEnabled);

		InputStream is = response.getEntity().getContent();
		BufferedReader br = new BufferedReader(new InputStreamReader(is), 8*1024);
		
		String line = null;
		while ((line = br.readLine()) != null) {
			if (isRedirectToLogonLua(line)) {
				throw new RedirectToLoginLuaException(affectedBox.getName(), "Detected redirect to login.lua");
			}
			containsWrongPassword(affectedBox, line);
			
			result.add(line);
		}

		br.close();
		is.close();

		return result;
	}
	
	private static void setRedirectHandler(final DefaultHttpClient httpClient, final String urlstr, final List<NameValuePair> postdata) {
		httpClient.setRedirectStrategy(new DefaultRedirectStrategy() {
		String lastRedirect = null;
        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)  {
            boolean isRedirect=false;
            try {
                isRedirect = super.isRedirected(request, response, context);
            } catch (ProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (!isRedirect) {
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == 301 || responseCode == 302) {
                    return true;
//                } else if (responseCode == 200) {
//                	String responseBody;
//					try {
//						responseBody = EntityUtils.toString(response.getEntity());
//						Pattern p = Pattern.compile("<meta http-equiv=(?:\")?refresh(?:\")? content=\"[^=]*=([^\"]*)\">");
//
//						Matcher m = p.matcher(responseBody.toLowerCase());
//						if (m.find()) {
//							lastRedirect = m.group(1);
//							isRedirect = true;
//						}
//					} catch (Exception e) {
//						// nothing to do here
//					}
            	}
            }
            return isRedirect;
        }
        
        
        public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != 200) {
            	return super.getRedirect(request, response, context);
            } else {
//                URI uri;
//				try {
//					uri = new URI(urlstr + lastRedirect);
//	                String method = request.getRequestLine().getMethod();
//	                if (method.equalsIgnoreCase(HttpHead.METHOD_NAME)) {
//	                    return new HttpHead(uri);
//	                } else if (method.equalsIgnoreCase(HttpGet.METHOD_NAME)){
//	                    return new HttpGet(uri);
//	                } else {
//	                	HttpPost p = new HttpPost(uri);
//	                	p.setParams(request.getParams());
//	            		if (postdata != null) {
//	            			p.setEntity(new UrlEncodedFormEntity(postdata));
//	            		}
//	                	return p;
//	                }
//				} catch (URISyntaxException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (UnsupportedEncodingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
            }

        	return null;
        }
	});
	}
	
	private static boolean isRedirectToLogonLua(String response) {
		Pattern redirectPattern = Pattern.compile("<meta http-equiv=(?:\")?refresh(?:\")? content=\"[^=]*=([^\"]*)\">");
		Matcher redirectMatcher = redirectPattern.matcher(response.toLowerCase());
		if (redirectMatcher.find()) {
			if (redirectMatcher.group(1).toLowerCase().contains("login.lua")) {
				return true;
			}
		}
		return false;
	}

	private static void containsWrongPassword(final BoxClass affectedBox,
			String responseString) throws WrongPasswordException {
		if ((responseString.indexOf("Das angegebene Kennwort ist ") >= 0) //$NON-NLS-1$
				|| (responseString.indexOf("Password not valid") >= 0)
				|| (responseString.indexOf("<!--loginPage-->") >= 0)
				|| (responseString.indexOf("FRITZ!Box Anmeldung") >= 0)
				|| (responseString.indexOf("login_form") >= 0 )) {
			log.debug("Wrong password detected: " + responseString);

			int wait = 3;
			Pattern waitSeconds = Pattern.compile(PATTERN_WAIT_FOR_X_SECONDS);
			Matcher m = waitSeconds.matcher(responseString);
			if (m.find()) {
				try {
					wait = Integer.parseInt(m.group(1));
				} catch (NumberFormatException nfe) {
					wait = 3;
				}
			}
			
			affectedBox.invalidateSession();

			throw new WrongPasswordException(affectedBox.getName(),
					"Password invalid", wait + 2); //$NON-NLS-1$
		}
	}
	
	public static String postDataToUrlAndGetStringResponse(final BoxClass affectedBox,
			final String urlstr, final List<NameValuePair> postdata, boolean retrieveData, boolean isRedirectEnabled)
			throws WrongPasswordException, SocketTimeoutException, IOException, URISyntaxException, RedirectToLoginLuaException {
		HttpParams httpParams = initConnectionParameters();
		
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpUriRequest request = initPostRequest(urlstr, postdata);
		
		String response = getResponseToString(affectedBox, httpClient, request, urlstr, postdata, isRedirectEnabled);
		request.abort();
		httpClient.getConnectionManager().shutdown();
		return response;
	}

	public static Vector<String> postDataToUrlAndGetVectorResponse(final BoxClass affectedBox,
			final String urlstr, final List<NameValuePair> postdata, boolean retrieveData, boolean isRedirectEnabled)
			throws WrongPasswordException, SocketTimeoutException, IOException, URISyntaxException, RedirectToLoginLuaException {
		HttpParams httpParams = initConnectionParameters();
		
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpUriRequest request = initPostRequest(urlstr, postdata);
		
		Vector<String> response = getResponseToVector(affectedBox, httpClient, request, urlstr, postdata, isRedirectEnabled);
		request.abort();
		httpClient.getConnectionManager().shutdown();
		return response;
	}

	private static HttpGet initGetRequest(final String urlstr,
			final List<NameValuePair> postdata) throws URISyntaxException,
			UnsupportedEncodingException {
		HttpGet request = new HttpGet();
		StringBuilder arguments = new StringBuilder();
		if (postdata != null) {
			int i=0; 
			for (NameValuePair nvp: postdata) {
				if (i++ == 0) {
					arguments.append("?");
				} else {
					arguments.append("&");
				}
				arguments.append(nvp.getName());
				arguments.append("=");
				arguments.append(nvp.getValue());
			}
		}
		request.setURI(new URI(urlstr + arguments.toString()));
		return request;
	}

	private static HttpPost initPostRequest(final String urlstr,
			final List<NameValuePair> postdata) throws URISyntaxException,
			UnsupportedEncodingException {
		HttpPost request = new HttpPost();
		request.setURI(new URI(urlstr));
		if (postdata != null) {
			request.setEntity(new UrlEncodedFormEntity(postdata));
		}
		return request;
	}

	/**
	 * removes all duplicate whitespaces from inputStr
	 *
	 * @param inputStr
	 * @return outputStr
	 */
	public static String removeDuplicateWhitespace(String inputStr) {
		Pattern p = Pattern.compile("\\s+"); //$NON-NLS-1$
		Matcher matcher = p.matcher(inputStr);
		String outputStr = matcher.replaceAll(" "); //$NON-NLS-1$
		outputStr.replaceAll(">\\s+<", "><"); //$NON-NLS-1$,  //$NON-NLS-2$
		return outputStr;
	}

	/**
	 * creates a String with version and date of CVS Id-Tag
	 *
	 * @param tag
	 * @return String with version and date of CVS Id-Tag
	 */
	public static String getVersionFromCVSTag(String tag) {
		// "$Id: Main.java 43 2009-08-04 09:08:06Z robotniko $"
		String[] parts = tag.split(" "); //$NON-NLS-1$
		return "SVN v" + parts[2] + " (" + parts[3] + ")"; //$NON-NLS-1$, //$NON-NLS-2$,  //$NON-NLS-3$
	}

	/**
	 * Wandelt einen String in einen boolean-Wert um
	 *
	 * @param input
	 * @return boolean value of input
	 */
	public static boolean parseBoolean(String input) {
		if (input != null && input.equalsIgnoreCase("true")) //$NON-NLS-1$
			return true;
		else
			return false;
	}

	public static Date setStartOfDay(Date d) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}

	public static Date setEndOfDay(Date d) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		return cal.getTime();
	}

	public static String convertSpecialChars(String input) {
		// XML Sonderzeichen durch ASCII Codierung ersetzen
		String out = input;
		out = out.replaceAll("&", "&#38;"); //$NON-NLS-1$,  //$NON-NLS-2$
		out = out.replaceAll("'", "&#39;"); //$NON-NLS-1$,  //$NON-NLS-2$
		out = out.replaceAll("<", "&#60;"); //$NON-NLS-1$,  //$NON-NLS-2$
		out = out.replaceAll(">", "&#62;"); //$NON-NLS-1$,  //$NON-NLS-2$
		out = out.replaceAll("\"", "&#34;"); //$NON-NLS-1$,  //$NON-NLS-2$
		out = out.replaceAll("=", "&#61;"); //$NON-NLS-1$,  //$NON-NLS-2$
		return out;
	}

	public static String deconvertSpecialChars(String input) {
		// XML Sonderzeichen durch ASCII Codierung ersetzen
		String out = input;
		out = out.replaceAll("&#38;", "&"); //$NON-NLS-1$,  //$NON-NLS-2$
		out = out.replaceAll("&#39;", "'"); //$NON-NLS-1$,  //$NON-NLS-2$
		out = out.replaceAll("&#60;", "<"); //$NON-NLS-1$,  //$NON-NLS-2$
		out = out.replaceAll("&#62;", ">"); //$NON-NLS-1$,  //$NON-NLS-2$
		out = out.replaceAll("&#61;", "="); //$NON-NLS-1$,  //$NON-NLS-2$
		out = out.replaceAll("&#34;", "\""); //$NON-NLS-1$,  //$NON-NLS-2$
		return out;
	}

	/**
	 * This function tries to guess the full path for the given subdirectory.<br />
	 *
	 * <ol>
	 * <li>It searches for the directory in the class path.</li>
	 * <li>If it does not find it there, it assumes that jfritz.jar and the
	 * subdirectory are in the same dir.</li>
	 * <li>If for some reason it fails to generate the full path to the jfritz
	 * binary, it assumes that the subdirectory is in the current working
	 * directory.</li>
	 * </ol>
	 *
	 * @param subDir
	 *            the subdirectory to search for. The directory must start with
	 *            a leading file separator and must not end with a file
	 *            separator (e.g. "/lang" for Linux). It's best to use the
	 *            predefined constants of this class.
	 * @return the full path to the subdirectory
	 * @see #langID
	 */
	public static String getFullPath(String subDir) {

		String[] classPath = System
				.getProperty("java.class.path").split(PATHSEP); //$NON-NLS-1$
		String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
		if (userDir.endsWith(FILESEP))
			userDir = userDir.substring(0, userDir.length() - 1);

		String binDir = null;
		String langDir = null;

		for (int i = 0; i < classPath.length; i++) {
			if (classPath[i].endsWith(binID))
				binDir = classPath[i].substring(0, classPath[i].length()
						- binID.length());
			else if (classPath[i].endsWith(subDir))
				langDir = classPath[i];
		}

		if (langDir == null) {
			langDir = (binDir != null) ? binDir + subDir : userDir + subDir;
		}

		return langDir;
	}

	/*
	 * This function capitalizes Strings example: hello, this is a test.
	 * ->Hello, This IS A Test.
	 */
	public static String capitalize(String str) {
		StringBuffer strBuf = new StringBuffer();
		char ch; // One of the characters in str.
		char prevCh; // The character that comes before ch in the string.
		int i; // A position in str, from 0 to str.length()-1.
		prevCh = '.'; // Prime the loop with any non-letter character.
		for (i = 0; i < str.length(); i++) {
			ch = str.charAt(i);
			if (Character.isLetter(ch) && !Character.isLetter(prevCh))
				strBuf.append(Character.toUpperCase(ch));
			else
				strBuf.append(ch);
			prevCh = ch; // prevCh for next iteration is ch.
		}
		return strBuf.toString();
	}

	public static int parseInt(String property) {
		try {
			return Integer.parseInt(property);
		} catch (NumberFormatException nfe) {
			log.warn("error converting Int returning 0 instead");
		}
		return 0;
	}

	public static String removeLeadingSpaces(final String str) {
		String currentStr = str;
		while (currentStr.startsWith(" ")) {
			currentStr = currentStr.substring(1);
		}
		while (currentStr.startsWith("\u00a0")) {
			currentStr = currentStr.substring(1);
		}
		return currentStr;
	}

	public static String toAscii(String str) {
		String out = "";
		for (int i = 0; i < str.length(); i++) {
			out = out + "#" + Integer.toHexString(str.charAt(i));
		}
		return out;
	}

	public static String replaceSpecialCharsUTF(final String str) {
		String currentStr = str;
		currentStr = currentStr.replaceAll("&#x[00]*C4;", "Ä");
		currentStr = currentStr.replaceAll("&#x[00]*D6;", "Ö");
		currentStr = currentStr.replaceAll("&#x[00]*DC;", "Ü");

		currentStr = currentStr.replaceAll("&#x[00]*E4;", "ä");
		currentStr = currentStr.replaceAll("&#x[00]*F6;", "ö");
		currentStr = currentStr.replaceAll("&#x[00]*FC;", "ü");
		currentStr = currentStr.replaceAll("&#x[00]*DF;", "ß");
		currentStr = currentStr.replaceAll("&#x[00]*A0;", " ");

		return currentStr;
	}

	public static long getTimestamp() {
		Calendar cal = Calendar.getInstance();
		return cal.getTimeInMillis();
	}

	public static void fillVectorByString(Vector<String> vector, String input,
			String sep) {
		String[] parts = properties.getStateProperty(input).split(sep);
		for (String part : parts) {
			vector.add(part);
		}
	}

	public static int subtractDays(Date date1, Date date2) {
		GregorianCalendar gc1 = new GregorianCalendar();
		GregorianCalendar gc2 = new GregorianCalendar();

		gc1.setTime(date1);
		gc2.setTime(date2);

		int days1 = 0;
		int days2 = 0;
		int maxYear = Math.max(gc1.get(Calendar.YEAR), gc2.get(Calendar.YEAR));

		GregorianCalendar gctmp = (GregorianCalendar) gc1.clone();
		for (int f = gctmp.get(Calendar.YEAR); f < maxYear; f++) {
			days2 += gctmp.getActualMaximum(Calendar.DAY_OF_YEAR);
			gctmp.add(Calendar.YEAR, 1);
		}

		gctmp = (GregorianCalendar) gc2.clone();
		for (int f = gctmp.get(Calendar.YEAR); f < maxYear; f++) {
			days1 += gctmp.getActualMaximum(Calendar.DAY_OF_YEAR);
			gctmp.add(Calendar.YEAR, 1);
		}

		days1 += gc1.get(Calendar.DAY_OF_YEAR) - 1;
		days2 += gc2.get(Calendar.DAY_OF_YEAR) - 1;

		if (days1 - days2 < 0) {
			log.debug("Negative date difference: " + date1 + " - " + date2);
		}
		return (days1 - days2);
	}

}