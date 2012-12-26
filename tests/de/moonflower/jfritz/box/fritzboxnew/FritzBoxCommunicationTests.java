package de.moonflower.jfritz.box.fritzboxnew;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.moonflower.jfritz.box.fritzboxnew.FritzBoxCommunication;
import de.moonflower.jfritz.box.fritzboxnew.HttpHelper;

public class FritzBoxCommunicationTests {

	private FritzBoxCommunication fbc;
	@Mock private HttpHelper mockedHttpHelper;

	@Before
	public void setup() throws ClientProtocolException, IOException {
    	MockitoAnnotations.initMocks(this);

    	fbc = new FritzBoxCommunication();
		fbc.httpHelper = mockedHttpHelper;

		doReturn("<html><body>FRITZ!Box Fon WLAN 7390 (UI)-B-200103-020314-055724-201216-217902-840522-22574-1und1</body></html>").when(this.mockedHttpHelper).getHttpContentAsString(anyString());
	}

	@Test
	public void getSystemStatus_Positive() throws ClientProtocolException, IOException {
		String result = fbc.getSystemStatus();
		Assert.assertEquals("<html><body>FRITZ!Box Fon WLAN 7390 (UI)-B-200103-020314-055724-201216-217902-840522-22574-1und1</body></html>", result);
	}
}
