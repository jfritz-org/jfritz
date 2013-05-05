package de.moonflower.jfritz.box.fritzbox;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import org.apache.http.Header;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.moonflower.jfritz.TestHelper;
import de.moonflower.jfritz.box.BoxClass;
import de.moonflower.jfritz.utils.Debug;

public class SIDLoginTest {
	@Mock private FritzBoxLoginHandler mockedLoginHandler;

	private SIDLogin sidLogin;

	@BeforeClass
    public static void setup() {
    	TestHelper.initLogging();

    	Debug.on();
    	Debug.setVerbose(true);
    	Debug.setDebugLevel(Debug.LS_DEBUG);
    }

    @Before
    public void setUp() throws Exception {
    	MockitoAnnotations.initMocks(this);

    	sidLogin = new SIDLogin();
    	sidLogin.loginHandler = mockedLoginHandler;

    }
    
    @Test
    public void testExtractFromRedirect() {
    	String redirectResponse = "Location: http://192.168.178.1:80/home/home.lua?sid=eb098f201571fc96";
    	
    	SIDLogin login = new SIDLogin();
    	login.extractSidFromResponse(redirectResponse);
    	Assert.assertEquals("eb098f201571fc96", login.getSessionId());
    }
    
    @Test
    public void calculateResponse() throws NoSuchAlgorithmException {
    	String challenge = "10f65791";
    	String password = "JFritz1230";
    	
    	String md5Pass = generateMD5(challenge + "-" + password);
    	
    	System.out.println(md5Pass);
    	System.out.println("done");
    }
    

private String generateMD5(String pwd) throws NoSuchAlgorithmException {
	MessageDigest m = MessageDigest.getInstance("MD5");
	String md5Pass = "";
	byte passwordBytes[] = null;
	try {
	passwordBytes = pwd.getBytes("UTF-16LE");
	m.update(passwordBytes, 0, passwordBytes.length);
	md5Pass = new BigInteger(1, m.digest()).toString(16);
	} catch (UnsupportedEncodingException e) {
	Debug.errDlg("UTF-16LE encoding not supported by your system. Can not communicate with FRITZ!Box!");
	}
	return md5Pass;
}

    @Test
    public void testPositive() {
    	try {
			doReturn("<SessionInfo><iswriteaccess>0</iswriteaccess><SID>0000000000000000</SID><Challenge>666a00e0</Challenge></SessionInfo>").when(this.mockedLoginHandler).getLoginSidResponseFromXml(any(BoxClass.class), anyString());

			// FIX this test
//			sidLogin.check("boxip", "urlstring", "password");

			Assert.assertTrue(sidLogin.isSidLogin());
			Assert.assertEquals("666a00e0-b6881f7185364c6e648472a00eb1518", sidLogin.getResponse());
			Assert.assertEquals("", sidLogin.getSessionId());
		} catch (Exception e) {
			Assert.fail("Could not setup test");
		}
    }

    @Test
    public void getSidFromResponsePositive() {
    	Vector<String> input = new Vector<String>();
    	input.add("<input type=\"hidden\" name=\"sid\" value=\"61ce68a044489bff\"");
    	sidLogin.getSidFromResponse(input);

    	Assert.assertEquals("61ce68a044489bff", sidLogin.getSessionId());
    }

}
