package de.moonflower.jfritz.autoupate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Calendar;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jd.nutils.OSDetector;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import de.moonflower.jfritz.constants.ProgramConstants;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.messages.UpdateMessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.JOptionPaneHtml;
import de.robotniko.fboxlib.exceptions.PageNotFoundException;

public class CheckForUpdate {

	private final String UPDATE_URL = "https://jfritz.org/update/checkUpdate.php";
	private final String USER_AGENT = "JFritzClient/"
			+ ProgramConstants.PROGRAM_VERSION + "."
			+ ProgramConstants.REVISION + " (" + OSDetector.getOSString() + ";" 
			// add additional fields here! ; is the delimiter. 
			// Don't forget to update checkUpdate.php and browserDetection.php
			+ ")";

	
	private boolean available = false;
	private String url;
	@SuppressWarnings("unused")
	private String version;
	@SuppressWarnings("unused")
	private String changelog;
	
	private MessageProvider messages = MessageProvider.getInstance();
	private UpdateMessageProvider updateMessages = UpdateMessageProvider.getInstance();

	private final int TIMEOUT_CONNECTION = 5000;
	private final int TIMEOUT_READ = 120000;

	private final CloseableHttpClient httpClient = HttpClients.createDefault();
	private final RequestConfig requestConfig = RequestConfig.custom()
			.setSocketTimeout(TIMEOUT_READ)
			.setConnectTimeout(TIMEOUT_CONNECTION).build();

	protected PropertyProvider properties = PropertyProvider.getInstance();

	public boolean isUpdateAvailable() {
		try {
			String result = getHttpContentAsString(UPDATE_URL);
			parseResponse(result);
			updateLastUpdateCheckTimestamp();
			return available;
		} catch (Exception e) {
			return false;
		}
	}
	
	private void parseResponse(final String result) {
		JSONObject o = (JSONObject)JSONValue.parse(result);
		available = (Long)o.get("available") == 1;
		url = (String)o.get("url");
		version = (String)o.get("version");
		changelog = (String)o.get("changelog");
		
		if (url.equals("")) {
			url = "https://jfritz.org";
		}
	}
	
	private void updateLastUpdateCheckTimestamp() {
		long now = Calendar.getInstance().getTimeInMillis();
		properties.setProperty("option.lastupdatetimestamp", Long.toString(now));
		properties.saveConfigProperties();
	}
	
	public void showUpdateNotification(final JFrame parentFrame) {
		String message = createUpdateMessage();
		JOptionPaneHtml.showMessageDialog(parentFrame, message, messages.getMessage("information"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
	}
	
	private String createUpdateMessage() {
		StringBuilder sb = new StringBuilder(100);
		sb.append("<html><body>");
		sb.append(updateMessages.getMessage("found_new_version"));
		sb.append("<br/><br/>");
		sb.append("<a href=\"");
		sb.append(url);
		sb.append("\">");
		sb.append(url);
		sb.append("</a>");
		sb.append("</body></html>");
		return sb.toString();
	}
	
	public void showNoUpdateAvailable(final JFrame parentFrame) {
		String message = updateMessages.getMessage("no_new_version_found");
		JOptionPane.showMessageDialog(parentFrame, message, messages.getMessage("information"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
	}
	
	private String getHttpContentAsString(String url)
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
