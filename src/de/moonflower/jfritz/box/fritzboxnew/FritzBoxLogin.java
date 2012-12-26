package de.moonflower.jfritz.box.fritzboxnew;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.moonflower.jfritz.utils.Debug;

public class FritzBoxLogin {

	protected HttpHelper httpHelper = HttpHelper.getInstance();

	public void login(final String protocol, final String host, final String port) {
//
//		try {
//			postdata = postdata + "&login:command/password=" + URLEncoder.encode(this.password, "ISO-8859-1");
//		} catch (UnsupportedEncodingException e) {
//			Debug.error("Encoding not supported");
//			e.printStackTrace();
//		}
//
//		String response = httpHelper.getHttpContentAsString(generateUrlPrefix() + "/cgi-bin/system_status");
	}

	public String getAuthUrlPart() {
		return "";
	}
}
