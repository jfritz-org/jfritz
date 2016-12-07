package de.moonflower.jfritz.box.fritzbox.callerlist;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.moonflower.jfritz.box.BoxCallListInterface;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.robotniko.fboxlib.fritzbox.FirmwareVersion;

public class FritzBoxCallerListFactoryTests {

	@Mock FirmwareVersion mockedFirmware;
	@Mock FritzBox mockedFritzBox;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_03_30() {
		mockFirmware(3, 30);

		BoxCallListInterface result = FritzBoxCallerListFactory.createFritzBoxCallListFromFirmware(mockedFirmware, mockedFritzBox, null);

		assertFirmwareClass(FritzBoxCallList_Pre_04_86.class, result);
	}

	private void mockFirmware(final int major, final int minor) {
		mockedFirmware = new FirmwareVersion((byte)0, (byte)major, (byte)minor);
	}

	private void assertFirmwareClass(Class expected, BoxCallListInterface actual) {
		if (expected == FritzBoxCallList_Actual.class) {
			Assert.assertTrue(actual instanceof FritzBoxCallList_Actual);
			Assert.assertTrue(actual instanceof FritzBoxCallList_Pre_05_28);
			Assert.assertTrue(actual instanceof FritzBoxCallList_Pre_04_86);
		} else if (expected == FritzBoxCallList_Pre_05_28.class) {
			Assert.assertFalse(actual instanceof FritzBoxCallList_Actual);
			Assert.assertTrue(actual instanceof FritzBoxCallList_Pre_05_28);
			Assert.assertTrue(actual instanceof FritzBoxCallList_Pre_04_86);
		} else if (expected == FritzBoxCallList_Pre_04_86.class) {
			Assert.assertFalse(actual instanceof FritzBoxCallList_Actual);
			Assert.assertFalse(actual instanceof FritzBoxCallList_Pre_05_28);
			Assert.assertTrue(actual instanceof FritzBoxCallList_Pre_04_86);
		}
	}

	@Test
	public void test_04_85() {
		mockFirmware(4, 85);

		BoxCallListInterface result = FritzBoxCallerListFactory.createFritzBoxCallListFromFirmware(mockedFirmware, mockedFritzBox, null);

		assertFirmwareClass(FritzBoxCallList_Pre_04_86.class, result);
	}

	@Test
	public void test_04_86() {
		mockFirmware(4, 86);

		BoxCallListInterface result = FritzBoxCallerListFactory.createFritzBoxCallListFromFirmware(mockedFirmware, mockedFritzBox, null);

		assertFirmwareClass(FritzBoxCallList_Pre_05_28.class, result);
	}

	@Test
	public void test_04_87() {
		mockFirmware(4, 87);

		BoxCallListInterface result = FritzBoxCallerListFactory.createFritzBoxCallListFromFirmware(mockedFirmware, mockedFritzBox, null);

		assertFirmwareClass(FritzBoxCallList_Pre_05_28.class, result);
	}

	@Test
	public void test_05_27() {
		mockFirmware(5, 27);

		BoxCallListInterface result = FritzBoxCallerListFactory.createFritzBoxCallListFromFirmware(mockedFirmware, mockedFritzBox, null);

		assertFirmwareClass(FritzBoxCallList_Pre_05_28.class, result);
	}

	@Test
	public void test_05_28() {
		mockFirmware(5, 28);

		BoxCallListInterface result = FritzBoxCallerListFactory.createFritzBoxCallListFromFirmware(mockedFirmware, mockedFritzBox, null);

		assertFirmwareClass(FritzBoxCallList_Actual.class, result);
	}

	@Test
	public void test_05_50() {
		mockFirmware(5, 50);

		BoxCallListInterface result = FritzBoxCallerListFactory.createFritzBoxCallListFromFirmware(mockedFirmware, mockedFritzBox, null);

		assertFirmwareClass(FritzBoxCallList_Actual.class, result);
	}
}
