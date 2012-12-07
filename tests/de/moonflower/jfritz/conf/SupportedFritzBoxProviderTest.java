package de.moonflower.jfritz.conf;

import static org.mockito.Mockito.doReturn;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.moonflower.jfritz.messages.MessageProvider;

public class SupportedFritzBoxProviderTest {

    @Mock private MessageProvider mockedMessages;

	private SupportedFritzBoxProvider boxProvider = SupportedFritzBoxProvider.getInstance();

    @Before
    public void setUp() throws Exception {
    	MockitoAnnotations.initMocks(this);

    	boxProvider.messageProvider = mockedMessages;
    	doReturn("unknown").when(this.mockedMessages).getMessage("unknown");
    }


	@Test
	public void test7390() {
		byte id = 84;
		Assert.assertEquals("AVM FRITZ!Box Fon WLAN 7390", boxProvider.getBoxById(id));
	}

	@Test
	public void test7320() {
		byte id = 100;
		Assert.assertEquals("AVM FRITZ!Box Fon WLAN 7320", boxProvider.getBoxById(id));
	}

	@Test
	public void testLessThanZero() {
		byte id = -1;
		Assert.assertEquals("unknown", boxProvider.getBoxById(id));
	}

	@Test
	public void testZero() {
		byte id = 0;
		Assert.assertEquals("unknown", boxProvider.getBoxById(id));
	}
}
