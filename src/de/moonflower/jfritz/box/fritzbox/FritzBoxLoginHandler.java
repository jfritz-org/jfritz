package de.moonflower.jfritz.box.fritzbox;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.JFritzUtils;

public class FritzBoxLoginHandler {

	private static final String URL_FOR_XML_LOGIN = "cgi-bin/webcm";

	private static final String URL_FOR_LUA_LOGIN = "login_sid.lua";

	private static final FritzBoxLoginHandler INSTANCE = new FritzBoxLoginHandler();

	public static FritzBoxLoginHandler getInstance() {
		return INSTANCE;
	}

	public String getLoginSidResponseFromXml(String box_name, String urlstr) throws SocketTimeoutException, WrongPasswordException, IOException {
		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("getpage", "../html/login/sid.xml"));
		
		try {
			return JFritzUtils.postDataToUrlAndGetStringResponse(box_name, urlstr + URL_FOR_XML_LOGIN, postData, true, false);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String getLoginSidResponseFromLua(String box_name, String urlstr) throws SocketTimeoutException, WrongPasswordException, IOException {
		try {
			return JFritzUtils.getDataFromUrlToString(box_name, urlstr + URL_FOR_LUA_LOGIN, true, false);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String loginXml(String box_name, String urlstr, List<NameValuePair> postdata) throws SocketTimeoutException, WrongPasswordException, IOException {
		try {
			postdata.add(new BasicNameValuePair("getpage","../html/login_sid.xml"));
			return JFritzUtils.postDataToUrlAndGetStringResponse(box_name, urlstr + URL_FOR_XML_LOGIN, postdata, true, false);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String loginLua(String box_name, String urlstr, String sidResponse) throws SocketTimeoutException, WrongPasswordException, IOException {
		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("response", sidResponse));

		try {
			return JFritzUtils.postDataToUrlAndGetStringResponse(box_name, urlstr + URL_FOR_LUA_LOGIN, postData, true, false);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return "";
	}
}
