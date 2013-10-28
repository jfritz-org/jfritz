package de.moonflower.jfritz.autoupate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.swing.JFrame;

import jd.nutils.OSDetector;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import de.moonflower.jfritz.constants.ProgramConstants;
import de.moonflower.jfritz.utils.Debug;
import de.robotniko.fboxlib.exceptions.PageNotFoundException;

public class CheckForUpdate {

	private static final String UPDATE_URL = "http://jfritz.org/update/checkUpdate.php";

	private static final String USER_AGENT = "JFritzClient/"
			+ ProgramConstants.PROGRAM_VERSION + "."
			+ ProgramConstants.REVISION + " (" + OSDetector.getOSString() + ";" 
			// add additional fields here! ; is the delimiter. 
			// Don't forget to update checkUpdate.php and browserDetection.php
			+ ")";

	private static final int TIMEOUT_CONNECTION = 5000;
	private static final int TIMEOUT_READ = 120000;

	private static final CloseableHttpClient httpClient = HttpClients.createDefault();
	private static final RequestConfig requestConfig = RequestConfig.custom()
			.setSocketTimeout(TIMEOUT_READ)
			.setConnectTimeout(TIMEOUT_CONNECTION).build();

	public static boolean isUpdateAvailable() {
		try {
			String result = getHttpContentAsString(UPDATE_URL);
			System.out.println(result);
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	
	public static void showUpdateNotification(final JFrame parentFrame) {
		Debug.debug("Show update notification is not yet implemented");
	}
	
	private static String getHttpContentAsString(String url)
			throws ClientProtocolException, IOException, PageNotFoundException {
		String result = "";

		HttpGet httpget = new HttpGet(url);
		httpget.setConfig(requestConfig);
		httpget.addHeader("User-Agent", USER_AGENT);

		try {
			CloseableHttpResponse response = httpClient.execute(httpget);
			if (response.getStatusLine().getStatusCode() == 404) {
				response.close();
				throw new PageNotFoundException("404 Not Found: " + url);
			}
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				try {
					// do something useful
					result = EntityUtils.toString(entity,
							Charset.forName("UTF-8"));
				} finally {
					instream.close();
				}
			}
			response.close();
		} catch (ClientProtocolException e) {
			result = e.getMessage();
			throw e;
		} catch (IOException e) {
			result = e.getMessage();
			throw e;
		}
		return result;
	}
}
