package de.moonflower.jfritz.box.fritzbox;

import java.io.IOException;
import java.net.SocketTimeoutException;

import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.JFritzUtils;

public class FritzBoxLoginHandler {

	private static final String URL_FOR_XML_LOGIN = "cgi-bin/webcm";
	private static final String POSTDATA_LOGIN_XML = "getpage=../html/login_sid.xml";

	private static final String URL_FOR_LUA_LOGIN = "login_sid.lua";

	private static final FritzBoxLoginHandler INSTANCE = new FritzBoxLoginHandler();

	public static FritzBoxLoginHandler getInstance() {
		return INSTANCE;
	}

	public String getLoginSidResponseFromXml(String box_name, String urlstr) throws SocketTimeoutException, WrongPasswordException, IOException {
		return JFritzUtils.fetchDataFromURLToString(box_name, urlstr + URL_FOR_XML_LOGIN, POSTDATA_LOGIN_XML, true);
	}

	public String getLoginSidResponseFromLua(String box_name, String urlstr) throws SocketTimeoutException, WrongPasswordException, IOException {
		return JFritzUtils.fetchDataFromURLToString(box_name, urlstr + URL_FOR_LUA_LOGIN, null, true);
	}

	public String loginXml(String box_name, String urlstr, String postdata) throws SocketTimeoutException, WrongPasswordException, IOException {
		return JFritzUtils.fetchDataFromURLToString(box_name, urlstr + URL_FOR_XML_LOGIN, postdata, true);
	}

	public String loginLua(String box_name, String urlstr, String response) throws SocketTimeoutException, WrongPasswordException, IOException {
		return JFritzUtils.fetchDataFromURLToString(box_name, urlstr + URL_FOR_LUA_LOGIN, response, true);
	}
}
