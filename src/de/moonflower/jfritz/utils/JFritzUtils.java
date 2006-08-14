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

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.exceptions.WrongPasswordException;

/**
 * Static class for data retrieval from the fritz box
 * and for several global functions
 *
 * @author akw
 *
 */
public class JFritzUtils {

    public static final String FILESEP = System.getProperty("file.separator");			//$NON-NLS-1$
    public static final String PATHSEP = System.getProperty("path.separator");			//$NON-NLS-1$
	public static final String binID = FILESEP + "jfritz.jar";								//$NON-NLS-1$
	public static final String versionFile = "http://www.blubb.txt";

	/**
	 * This constant can be used to search for the lang-directory.
	 * @see #getFullPath(String)
	 */
    public static final String langID = FILESEP + "lang";

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
	 * This function tries to guess the full path for the given
	 * subdirectory.<br />
	 *
	 * <ol>
	 * <li>It searches for the directory in the class path.</li>
	 * <li>If it does not find it there, it assumes that jfritz.jar and
	 *     the subdirectory are in the same dir.</li>
	 * <li>If for some reason it fails to generate the full path to the
	 *     jfritz binary, it assumes that the subdirectory is in the
	 *     current working directory.</li>
	 * </ol>
	 *
	 * @param subDir the subdirectory to search for.
	 *               The directory must start with a leading file separator
	 *               and must not end with a file separator (e.g. "/lang"
	 *               for Linux).
	 *               It's best to use the predefined constants of this class.
	 * @return the full path to the subdirectory
	 * @see #langID
	 */
	public static String getFullPath(String subDir){

		Debug.msg("Subdirectory: "+ subDir);
		String[] classPath = System.getProperty("java.class.path").split(PATHSEP);	//$NON-NLS-1$
		String userDir = System.getProperty("user.dir");							//$NON-NLS-1$
		if (userDir.endsWith(FILESEP))
			userDir = userDir.substring(0, userDir.length() - 1);

		String binDir = null;
		String langDir = null;

		for (int i = 0; i < classPath.length; i++) {
			if (classPath[i].endsWith(binID))
				binDir = classPath[i].substring(0, classPath[i].length() - binID.length());
			else if (classPath[i].endsWith(subDir))
				langDir = classPath[i];
		}

		if (langDir == null) {
			langDir = (binDir != null) ? binDir + subDir : userDir + subDir;
		}

		Debug.msg("full path: " + langDir);											//$NON-NLS-1$

		return langDir;
	}

	/*
	 * This function capitalizes Strings
	 * example:
	 * hello, this is a test.
	 * ->Hello, This IS A Test.
	 */
    public static String capitalize( String str ) {
     StringBuffer strBuf = new StringBuffer();
     char ch;       // One of the characters in str.
     char prevCh;   // The character that comes before ch in the string.
     int i;         // A position in str, from 0 to str.length()-1.
     prevCh = '.';  // Prime the loop with any non-letter character.
     for ( i = 0;  i < str.length();  i++ ) {
        ch = str.charAt(i);
        if ( Character.isLetter(ch)  &&  ! Character.isLetter(prevCh) )
        	strBuf.append( Character.toUpperCase(ch) );
        else
        	strBuf.append(ch);
        prevCh = ch;  // prevCh for next iteration is ch.
     }
     return strBuf.toString();
  }

   public static boolean checkForNewVersion(){
		URL url = null;
		String data = ""; //$NON-NLS-1$

		String urlstr = "http://www.jfritz.org/update/current.txt "; //$NON-NLS-1$
		try {
			url = new URL(urlstr);
			if (url != null) {

				URLConnection con;
				try {
					con = url.openConnection();

					String header = ""; //$NON-NLS-1$
					String charSet = ""; //$NON-NLS-1$
					for (int i = 0;; i++) {
						String headerName = con.getHeaderFieldKey(i);
						String headerValue = con.getHeaderField(i);

						if (headerName == null && headerValue == null) {
							// No more headers
							break;
						}
						if ("content-type".equalsIgnoreCase(headerName)) { //$NON-NLS-1$
							String[] split = headerValue.split(" ", 2); //$NON-NLS-1$
							for (int j = 0; j < split.length; j++) {
								split[j] = split[j].replaceAll(";", ""); //$NON-NLS-1$,  //$NON-NLS-2$
								if (split[j].toLowerCase().startsWith(
										"charset=")) { //$NON-NLS-1$
									String[] charsetSplit = split[j].split("="); //$NON-NLS-1$
									charSet = charsetSplit[1];
								}
							}
						}
						header += headerName + ": " + headerValue + " | "; //$NON-NLS-1$,  //$NON-NLS-2$
					}
					Debug.msg("Header of Version file: " + header); //$NON-NLS-1$
					Debug.msg("CHARSET : " + charSet); //$NON-NLS-1$

					// Get used Charset
					BufferedReader d;
					if (charSet.equals("")) { //$NON-NLS-1$
						d = new BufferedReader(new InputStreamReader(con
								.getInputStream(), "ISO-8859-1")); //$NON-NLS-1$
					} else {
						d = new BufferedReader(new InputStreamReader(con
								.getInputStream(), charSet));
					}
					int i = 0;
					String str = ""; //$NON-NLS-1$

					// Get response data
					while ((i < 700) && (null != ((str = d.readLine())))) {
						data += str;
						i++;
					}
					d.close();
					Debug.msg("Begin processing Version File");

					if(Integer.valueOf(data.replaceAll("\\.","")).compareTo(Integer.valueOf(JFritz.PROGRAM_VERSION.replaceAll("\\.",""))) > 0){
						return true;
					}

				} catch (IOException e1) {
					Debug.err("Error while retrieving " + urlstr); //$NON-NLS-1$
				}
			}
		} catch (MalformedURLException e) {
			Debug.err("URL invalid: " + urlstr); //$NON-NLS-1$
		}
		return false;

   }

}