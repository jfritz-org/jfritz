package de.moonflower.jfritz.box.fritzbox.callerlist;

import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.moonflower.jfritz.box.BoxCallListInterface;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.box.fritzbox.FritzBoxFirmware;

public class FritzBoxCallerListFactoryTests {

	@Mock FritzBoxFirmware mockedFirmware;
	@Mock FritzBox mockedFritzBox;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_03_30() {
		when(this.mockedFirmware.getMajorFirmwareVersion()).thenReturn((byte)3);
		when(this.mockedFirmware.getMinorFirmwareVersion()).thenReturn((byte)30);

		BoxCallListInterface result = FritzBoxCallerListFactory.createFritzBoxCallListFromFirmware(mockedFirmware, mockedFritzBox, null);

		Assert.assertTrue(result instanceof FritzBoxCallList_Pre_04_86);
	}

	@Test
	public void test_04_85() {
		when(this.mockedFirmware.getMajorFirmwareVersion()).thenReturn((byte)4);
		when(this.mockedFirmware.getMinorFirmwareVersion()).thenReturn((byte)85);

		BoxCallListInterface result = FritzBoxCallerListFactory.createFritzBoxCallListFromFirmware(mockedFirmware, mockedFritzBox, null);

		Assert.assertTrue(result instanceof FritzBoxCallList_Pre_04_86);
	}

	@Test
	public void test_04_86() {
		when(this.mockedFirmware.getMajorFirmwareVersion()).thenReturn((byte)4);
		when(this.mockedFirmware.getMinorFirmwareVersion()).thenReturn((byte)86);

		BoxCallListInterface result = FritzBoxCallerListFactory.createFritzBoxCallListFromFirmware(mockedFirmware, mockedFritzBox, null);

		Assert.assertTrue(result instanceof FritzBoxCallList_Pre_05_50);
	}

	@Test
	public void test_04_87() {
		when(this.mockedFirmware.getMajorFirmwareVersion()).thenReturn((byte)4);
		when(this.mockedFirmware.getMinorFirmwareVersion()).thenReturn((byte)87);

		BoxCallListInterface result = FritzBoxCallerListFactory.createFritzBoxCallListFromFirmware(mockedFirmware, mockedFritzBox, null);

		Assert.assertTrue(result instanceof FritzBoxCallList_Pre_05_50);
	}

	@Test
	public void test_05_49() {
		when(this.mockedFirmware.getMajorFirmwareVersion()).thenReturn((byte)5);
		when(this.mockedFirmware.getMinorFirmwareVersion()).thenReturn((byte)49);

		BoxCallListInterface result = FritzBoxCallerListFactory.createFritzBoxCallListFromFirmware(mockedFirmware, mockedFritzBox, null);

		Assert.assertTrue(result instanceof FritzBoxCallList_Pre_05_50);
	}

	@Test
	public void test_05_50() {
		when(this.mockedFirmware.getMajorFirmwareVersion()).thenReturn((byte)5);
		when(this.mockedFirmware.getMinorFirmwareVersion()).thenReturn((byte)50);

		BoxCallListInterface result = FritzBoxCallerListFactory.createFritzBoxCallListFromFirmware(mockedFirmware, mockedFritzBox, null);

		Assert.assertTrue(result instanceof FritzBoxCallList_Actual);
	}

	@Test
	public void test_05_51() {
		when(this.mockedFirmware.getMajorFirmwareVersion()).thenReturn((byte)5);
		when(this.mockedFirmware.getMinorFirmwareVersion()).thenReturn((byte)51);

		BoxCallListInterface result = FritzBoxCallerListFactory.createFritzBoxCallListFromFirmware(mockedFirmware, mockedFritzBox, null);

		Assert.assertTrue(result instanceof FritzBoxCallList_Actual);
	}
}
