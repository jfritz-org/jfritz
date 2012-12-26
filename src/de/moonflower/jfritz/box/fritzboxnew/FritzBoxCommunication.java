package de.moonflower.jfritz.box.fritzboxnew;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;


public class FritzBoxCommunication {

	protected HttpHelper httpHelper = HttpHelper.getInstance();

	private String protocol = "http";
	private String host = "fritz.box";
	private String port = "80";

	private FritzBoxLogin fboxLogin;

	public FritzBoxCommunication() {
		this("http", "fritz.box", "80");
	}

	public FritzBoxCommunication(String protocol, String host, String port) {
		this.protocol = protocol;
		this.host = host;
		this.port = port;
	}

	public String getSystemStatus() throws ClientProtocolException, IOException {
		return httpHelper.getHttpContentAsString(generateUrlPrefix() + "/cgi-bin/system_status");
	}

	private String generateUrlPrefix() {
		return protocol + "://" + host + ":" + port;
	}
}
