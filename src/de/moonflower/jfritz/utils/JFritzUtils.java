/*
 *
 * Created on 06.05.2005
 *
 */
package de.moonflower.jfritz.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.HTMLUtil;

/**
 * Static class for data retrieval from the fritz box
 * and for several global functions
 *
 * @author akw
 *
 */
public class JFritzUtils {

    static final String FILESEP = System.getProperty("file.separator");			//$NON-NLS-1$
    static final String PATHSEP = System.getProperty("path.separator");			//$NON-NLS-1$
	static final String binID = FILESEP + "jfritz.jar";								//$NON-NLS-1$


	/**
	 * fetches html data from url using POST requests
	 *
	 * @param urlstr
	 * @param postdata
	 * @return html data
	 * @throws WrongPasswordException
	 * @throws IOException
	 */
	public static String fetchDataFromURL(String urlstr, String postdata,
			boolean retrieveData) throws WrongPasswordException, IOException {
		URL url = null;
		URLConnection urlConn;
		DataOutputStream printout;
		String data = ""; //$NON-NLS-1$
		boolean wrong_pass = false;
		Debug.msg("Urlstr: " + urlstr);
		Debug.msg("Postdata: " + postdata);

		try {
			url = new URL(urlstr);
		} catch (MalformedURLException e) {
			Debug.err("URL invalid: " + urlstr); //$NON-NLS-1$
			throw new WrongPasswordException("URL invalid: " + urlstr); //$NON-NLS-1$
		}

		if (url != null) {
			urlConn = url.openConnection();
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			urlConn.setUseCaches(false);
			// Sending postdata
			if (postdata != null) {
				urlConn.setRequestProperty("Content-Type", //$NON-NLS-1$
						"application/x-www-form-urlencoded"); //$NON-NLS-1$
				printout = new DataOutputStream(urlConn.getOutputStream());
				printout.writeBytes(postdata);
				printout.flush();
				printout.close();
			}

			BufferedReader d;

			try {
				// Get response data
				d = new BufferedReader(new InputStreamReader(urlConn
						.getInputStream()));
				String str;
				while (null != ((str = HTMLUtil.stripEntities(d.readLine())))) {
					// Password seems to be wrong
					if (str.indexOf("FRITZ!Box Anmeldung") > 0) //$NON-NLS-1$
						wrong_pass = true;
					if (retrieveData)
						data += str;
				}
				d.close();
			} catch (IOException e1) {
				throw new IOException("Network unavailable"); //$NON-NLS-1$
			}

			if (wrong_pass)
				throw new WrongPasswordException("Password invalid"); //$NON-NLS-1$
		}
		return data;
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
		String[] parts = tag.split(" "); //$NON-NLS-1$
		return "CVS v" + parts[2] + " (" + parts[3] + ")"; //$NON-NLS-1$, //$NON-NLS-2$,  //$NON-NLS-3$
	}

	public static boolean parseBoolean(String input) {
		if (input != null && input.equalsIgnoreCase("true")) //$NON-NLS-1$
			return true;
		else
			return false;
	}

	public static String lookupAreaCode(String number) {
		Debug.msg("Looking up " + number + "..."); //$NON-NLS-1$,  //$NON-NLS-2$
		// FIXME: Does not work (Cookies)
		String urlstr = "http://www.vorwahl.de/national.php"; //$NON-NLS-1$
		String postdata = "search=1&vorwahl=" + number; //$NON-NLS-1$
		String data = ""; //$NON-NLS-1$
		try {
			data = fetchDataFromURL(urlstr, postdata, true);
		} catch (Exception e) {
		}
		Debug.msg("DATA: " + data.trim()); //$NON-NLS-1$
		return ""; //$NON-NLS-1$
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
	 * This function searches through the class path for the given subdirectory
	 * and if it is not found then the full path to the jfritz binary is generated
	 * This function assumes jfritz.jar and the subdirectory and in the same dir
	 *
	 * @author Brian Jensen
	 * @param subDir the subdirectory in the jfritz.jar dir
	 * @return full path or the classpath entry to the subdirectory
	 */
	public static String getFullPath(String subDir){

		Debug.msg("Subdirectory: "+ subDir);
		String[] classPath = System.getProperty("java.class.path").split(PATHSEP);	//$NON-NLS-1$
		String userDir = System.getProperty("user.dir");							//$NON-NLS-1$

		if (!userDir.endsWith(FILESEP))
			userDir = userDir + FILESEP;

		String binDir = null;
		String langDir = null;

		if(classPath != null && classPath.length > 0){
			for (int i = 0; i < classPath.length; i++) {
				if (classPath[i].endsWith(binID)){
					binDir = classPath[i].substring(0, classPath[i].length() - binID.length() +1);
					break;
				}else if (classPath[i].endsWith(subDir))
					langDir = classPath[i];
			}

			if (langDir == null) {
				if(binDir != null)
					if(binDir.startsWith(FILESEP))
						langDir = binDir + subDir;
					else
						langDir = userDir + binDir + subDir;
				else
					langDir = userDir + subDir;

				//langDir = (binDir != null) ? userDir + binDir + subDir : userDir + subDir;
			}

		}else{
			langDir = userDir + subDir;
		}
		Debug.msg("full path: " + langDir);											//$NON-NLS-1$

		return langDir;
	}



}