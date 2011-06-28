package de.moonflower.jfritz.conf;

import org.junit.Assert;
import org.junit.Test;

public class SupportedFritzBoxProviderTest {

	private SupportedFritzBoxProvider boxProvider = SupportedFritzBoxProvider.getInstance();

	@Test
	public void test7390() {
		byte id = 84;
		Assert.assertEquals("FRITZ!Box 7390", boxProvider.getBoxById(id));
	}

	@Test
	public void test7320() {
		byte id = 100;
		Assert.assertEquals("FRITZ!Box 7320", boxProvider.getBoxById(id));
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
