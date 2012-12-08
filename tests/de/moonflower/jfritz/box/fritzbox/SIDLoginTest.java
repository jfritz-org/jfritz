package de.moonflower.jfritz.box.fritzbox;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.util.Vector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.moonflower.jfritz.TestHelper;
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
    public void testPositive() {
    	try {
			doReturn("<SessionInfo><iswriteaccess>0</iswriteaccess><SID>0000000000000000</SID><Challenge>666a00e0</Challenge></SessionInfo>").when(this.mockedLoginHandler).getLoginSidResponse(anyString(), anyString());

			sidLogin.check("boxip", "urlstring", "password");

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
