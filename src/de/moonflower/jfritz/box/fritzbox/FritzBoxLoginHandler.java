package de.moonflower.jfritz.box.fritzbox;

import java.io.IOException;
import java.net.SocketTimeoutException;

import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.JFritzUtils;

public class FritzBoxLoginHandler {

	private static final String POSTDATA_LOGIN_XML = "getpage=../html/login_sid.xml";
	private static final FritzBoxLoginHandler INSTANCE = new FritzBoxLoginHandler();

	public static FritzBoxLoginHandler getInstance() {
		return INSTANCE;
	}

	public String getLoginSidResponse(String box_name, String urlstr) throws SocketTimeoutException, WrongPasswordException, IOException {
		return JFritzUtils.fetchDataFromURLToString(box_name, urlstr, POSTDATA_LOGIN_XML, true);
	}
}
