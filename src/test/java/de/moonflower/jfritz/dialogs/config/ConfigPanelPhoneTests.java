package de.moonflower.jfritz.dialogs.config;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class ConfigPanelPhoneTests {
	ConfigPanelPhone configPanel;
	
	@Before
	public void setUp() {
		configPanel = new ConfigPanelPhone();
	}

	@Test
	public void testCorrectAreaPrefix1() {
		String fixedString = configPanel.fixAreaPrefix("721", "0");
		Assert.assertEquals("0", fixedString);
	}

	@Test
	public void testCorrectAreaPrefix2() {
		String fixedString = configPanel.fixAreaPrefix("40", "0");
		Assert.assertEquals("0", fixedString);
	}

	@Test
	public void testWrongAreaPrefix1() {
		String fixedString = configPanel.fixAreaPrefix("40", "040");
		Assert.assertEquals("0", fixedString);
	}

	@Test
	public void testWrongAreaPrefix2() {
		String fixedString = configPanel.fixAreaPrefix("721", "0721");
		Assert.assertEquals("0", fixedString);
	}
	
	@Test
	public void testCorrectAreaCode1() {
		String fixedString = configPanel.fixAreaCode("721", "0");
		Assert.assertEquals("721", fixedString);
	}
	
	@Test
	public void testCorrectAreaCode2() {
		String fixedString = configPanel.fixAreaCode("40", "0");
		Assert.assertEquals("40", fixedString);
	}
	
	@Test
	public void testWrongAreaCode1() {
		String fixedString = configPanel.fixAreaCode("0721", "0");
		Assert.assertEquals("721", fixedString);
	}
	
	@Test
	public void testWrongAreaCode2() {
		String fixedString = configPanel.fixAreaCode("040", "0");
		Assert.assertEquals("40", fixedString);
	}
	
	@Test
	public void testWrongAreaCodeAndAreaPrefix1() {
		String fixedAreaCode = configPanel.fixAreaCode("040", "040");
		String fixedAreaPrefix = configPanel.fixAreaPrefix("040", "040");
		Assert.assertEquals("40", fixedAreaCode);
		Assert.assertEquals("0", fixedAreaPrefix);
	}
	
	@Test
	public void testWrongAreaCodeAndAreaPrefix2() {
		String fixedAreaCode = configPanel.fixAreaCode("0721", "0721");
		String fixedAreaPrefix = configPanel.fixAreaPrefix("0721", "0721");
		Assert.assertEquals("721", fixedAreaCode);
		Assert.assertEquals("0", fixedAreaPrefix);
	}

}
