package de.moonflower.jfritz.box.fritzboxnew;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class HttpHelper {
	private final static int TIMEOUT_CONNECTION = 15000;
	private final static int TIMEOUT_READ = 120000;

	private static HttpHelper INSTANCE = new HttpHelper();

	public static HttpHelper getInstance() {
		return INSTANCE;
	}

	private HttpHelper() {

	}

	public String getHttpContentAsString(String url) throws ClientProtocolException, IOException {
		String result = "";

		final HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT_CONNECTION);
		HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUT_READ);

		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
		    InputStream instream = entity.getContent();
		    try {
		        // do something useful
		    	result = EntityUtils.toString(entity, Charset.forName("UTF-8"));
		    	httpget.abort();
		    } finally {
		        instream.close();
		    }
		}
		return result;
	}
}
