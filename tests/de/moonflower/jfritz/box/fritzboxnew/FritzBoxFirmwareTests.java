package de.moonflower.jfritz.box.fritzboxnew;

import static org.mockito.Mockito.doReturn;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.moonflower.jfritz.box.fritzboxnew.FritzBoxFirmware;
import de.moonflower.jfritz.box.fritzboxnew.FritzBoxCommunication;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;

public class FritzBoxFirmwareTests {

	@Mock private FritzBoxCommunication mockedFbc;
	private FritzBoxFirmware detectFirmware;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		detectFirmware = new FritzBoxFirmware();
	}

	@Test(expected=InvalidFirmwareException.class)
	public void responseNull() throws InvalidFirmwareException {
		String response = null;
		detectFirmware.parseResponse(response);
	}

	@Test(expected=InvalidFirmwareException.class)
	public void responseEmpty() throws InvalidFirmwareException {
		String response = "";
		detectFirmware.parseResponse(response);
	}

	@Test(expected=InvalidFirmwareException.class)
	public void noHyphen() throws InvalidFirmwareException {
		String response = "abcdef";
		detectFirmware.parseResponse(response);
	}

	@Test(expected=InvalidFirmwareException.class)
	public void tooShort() throws InvalidFirmwareException {
		String response = "abc-def";
		detectFirmware.parseResponse(response);
	}

	@Test(expected=InvalidFirmwareException.class)
	public void tooLong() throws InvalidFirmwareException {
		String response = "1-2-3-4-5-6-7-8-9-0-1-2";
		detectFirmware.parseResponse(response);
	}

	@Test
	public void test_7390_840522_22574_1und1_de() throws InvalidFirmwareException {
		String response = wrapInHtml("FRITZ!Box Fon WLAN 7390 (UI)-B-000103-020314-055724-201216-217902-840522-22574-1und1");
		detectFirmware.parseResponse(response);

		Assert.assertEquals("FRITZ!Box Fon WLAN 7390 (UI)", detectFirmware.name);
		Assert.assertEquals("B", detectFirmware.annex);
		Assert.assertEquals(84, detectFirmware.boxtype);
		Assert.assertEquals(5, detectFirmware.majorFirmwareVersion);
		Assert.assertEquals(22, detectFirmware.minorFirmwareVersion);
		Assert.assertEquals(22574, detectFirmware.revision);
		Assert.assertEquals("1und1", detectFirmware.branding);
		Assert.assertEquals("de", detectFirmware.language);
	}

	private String wrapInHtml(String input) {
		return "<html><body>" + input + "</body></html>";
	}

	@Test
	public void test_7390_840488_18808_avm_de() throws InvalidFirmwareException {
		String response = wrapInHtml("FRITZ!Box Fon WLAN 7390-B-190210-010106-630046-320710-787902-840488-18808-avm");
		detectFirmware.parseResponse(response);

		Assert.assertEquals("FRITZ!Box Fon WLAN 7390", detectFirmware.name);
		Assert.assertEquals("B", detectFirmware.annex);
		Assert.assertEquals(84, detectFirmware.boxtype);
		Assert.assertEquals(4, detectFirmware.majorFirmwareVersion);
		Assert.assertEquals(88, detectFirmware.minorFirmwareVersion);
		Assert.assertEquals(18808, detectFirmware.revision);
		Assert.assertEquals("avm", detectFirmware.branding);
		Assert.assertEquals("de", detectFirmware.language);
	}

	@Test
	public void test_7270_740522_22574_1und1_de() throws InvalidFirmwareException {
		String response = wrapInHtml("FRITZ!Box Fon WLAN 7270 v3 (UI)-B-220107-020425-604741-073467-787902-740522-22574-1und1");
		detectFirmware.parseResponse(response);

		Assert.assertEquals("FRITZ!Box Fon WLAN 7270 v3 (UI)", detectFirmware.name);
		Assert.assertEquals("B", detectFirmware.annex);
		Assert.assertEquals(74, detectFirmware.boxtype);
		Assert.assertEquals(5, detectFirmware.majorFirmwareVersion);
		Assert.assertEquals(22, detectFirmware.minorFirmwareVersion);
		Assert.assertEquals(22574, detectFirmware.revision);
		Assert.assertEquals("1und1", detectFirmware.branding);
		Assert.assertEquals("de", detectFirmware.language);
	}

	@Test
	public void test_7330_1160522_22574_1und1_de() throws InvalidFirmwareException {
		String response = wrapInHtml("FRITZ!Box 7330 SL (UI)-B-052405-000007-250637-757236-787902-1160522-22574-1und1");
		detectFirmware.parseResponse(response);

		Assert.assertEquals("FRITZ!Box 7330 SL (UI)", detectFirmware.name);
		Assert.assertEquals("B", detectFirmware.annex);
		Assert.assertEquals(116, detectFirmware.boxtype);
		Assert.assertEquals(5, detectFirmware.majorFirmwareVersion);
		Assert.assertEquals(22, detectFirmware.minorFirmwareVersion);
		Assert.assertEquals(22574, detectFirmware.revision);
		Assert.assertEquals("1und1", detectFirmware.branding);
		Assert.assertEquals("de", detectFirmware.language);
	}

	@Test
	public void test_WLAN_080449_10836_avne_en() throws InvalidFirmwareException {
		String response = wrapInHtml("FRITZ!Box Fon WLAN-B-111500-000104-303630-437613-787902-080449-10836-avme-en");
		detectFirmware.parseResponse(response);

		Assert.assertEquals("FRITZ!Box Fon WLAN", detectFirmware.name);
		Assert.assertEquals("B", detectFirmware.annex);
		Assert.assertEquals(8, detectFirmware.boxtype);
		Assert.assertEquals(4, detectFirmware.majorFirmwareVersion);
		Assert.assertEquals(49, detectFirmware.minorFirmwareVersion);
		Assert.assertEquals(10836, detectFirmware.revision);
		Assert.assertEquals("avme", detectFirmware.branding);
		Assert.assertEquals("en", detectFirmware.language);
	}

	@Test
	public void test_6360_850525_22677_kabelbw_de() throws InvalidFirmwareException {
		String response = wrapInHtml("FRITZ!Box 6360 Cable (kbw)-Kabel-212806-010307-577727-370307-787902-850525-22677-kabelbw");
		detectFirmware.parseResponse(response);

		Assert.assertEquals("FRITZ!Box 6360 Cable (kbw)", detectFirmware.name);
		Assert.assertEquals("Kabel", detectFirmware.annex);
		Assert.assertEquals(85, detectFirmware.boxtype);
		Assert.assertEquals(5, detectFirmware.majorFirmwareVersion);
		Assert.assertEquals(25, detectFirmware.minorFirmwareVersion);
		Assert.assertEquals(22677, detectFirmware.revision);
		Assert.assertEquals("kabelbw", detectFirmware.branding);
		Assert.assertEquals("de", detectFirmware.language);
	}

	@Test
	public void test_7570_750491_19965_avme_de() throws InvalidFirmwareException {
		String response = wrapInHtml("FRITZ!Box Fon WLAN 7570 vDSL-B-000000-000000-000000-000000-000000-750491-19965-avme-de");
		detectFirmware.parseResponse(response);

		Assert.assertEquals("FRITZ!Box Fon WLAN 7570 vDSL", detectFirmware.name);
		Assert.assertEquals("B", detectFirmware.annex);
		Assert.assertEquals(75, detectFirmware.boxtype);
		Assert.assertEquals(4, detectFirmware.majorFirmwareVersion);
		Assert.assertEquals(91, detectFirmware.minorFirmwareVersion);
		Assert.assertEquals(19965, detectFirmware.revision);
		Assert.assertEquals("avme", detectFirmware.branding);
		Assert.assertEquals("de", detectFirmware.language);
	}

	@Test
	public void test_7312_1170523_22847_1und1_de() throws InvalidFirmwareException {
		String response = wrapInHtml("FRITZ!Box 7312 (UI)-B-071403-000017-100563-156351-787902-1170523-22847-1und1");
		detectFirmware.parseResponse(response);

		Assert.assertEquals("FRITZ!Box 7312 (UI)", detectFirmware.name);
		Assert.assertEquals("B", detectFirmware.annex);
		Assert.assertEquals(117, detectFirmware.boxtype);
		Assert.assertEquals(5, detectFirmware.majorFirmwareVersion);
		Assert.assertEquals(23, detectFirmware.minorFirmwareVersion);
		Assert.assertEquals(22847, detectFirmware.revision);
		Assert.assertEquals("1und1", detectFirmware.branding);
		Assert.assertEquals("de", detectFirmware.language);
	}

	@Test
	public void test_6360_850528_23625_unity_de() throws InvalidFirmwareException {
		String response = wrapInHtml("FRITZ!Box 6360 Cable (um)-Kabel-211600-000010-054167-341625-787902-850528-23625-unity");
		detectFirmware.parseResponse(response);

		Assert.assertEquals("FRITZ!Box 6360 Cable (um)", detectFirmware.name);
		Assert.assertEquals("Kabel", detectFirmware.annex);
		Assert.assertEquals(85, detectFirmware.boxtype);
		Assert.assertEquals(5, detectFirmware.majorFirmwareVersion);
		Assert.assertEquals(28, detectFirmware.minorFirmwareVersion);
		Assert.assertEquals(23625, detectFirmware.revision);
		Assert.assertEquals("unity", detectFirmware.branding);
		Assert.assertEquals("de", detectFirmware.language);
	}

//	FRITZ!Box Fon WLAN 7050 (UI)-B-182303-030229-064572-510141-787902-140433-7238-1und1
//	FRITZ!Box Fon WLAN 7390-B-151308-000125-223256-726042-147902-840522-22574-avm
//	FRITZ!Box Fon WLAN 7270 v3-B-191408-010214-273031-462044-197902-740522-22574-avm
//	FRITZ!Box Fon WLAN 7240-B-212107-000104-725364-203310-147902-730522-22574-avm
//	FRITZ!Box Fon WLAN 7390-B-192204-020417-266755-643405-787902-840527-23565-avm
//	FRITZ!Box Fon WLAN-B-111500-000104-303630-437613-787902-080449-10836-avme-en
//	FRITZ!Box Fon WLAN 7270 v2-B-041411-000314-775335-655674-197902-540529-24296-avm
//	FRITZ!Box Fon WLAN 7390-B-110506-000023-243373-563631-787902-840550-24230-avm
//	FRITZ!Box Fon WLAN 7270 v1-B-150601-020207-652064-206152-787902-540488-18902-avm
//	FRITZ!Box Fon WLAN 7270-B-182409-000317-436757-744335-217902-540480-16540-avm
//	FRITZ!Box Fon WLAN 7170-B-092007-040628-457563-147110-217902-290487-19985-avm
//	FRITZ!Box Fon WLAN 7170-B-232101-010617-457563-147110-217902-290487-19985-avm
//	FRITZ!Box Fon WLAN 7170-B-202304-030824-321264-210372-217902-290480-16352-avm
//	FRITZ!Box Fon WLAN 7170-B-150105-010603-006420-020317-217902-290482-17260-avme-en
//	FRITZ!Box Fon WLAN 7390-B-041610-000311-154106-335352-217902-840522-22574-avm
//	FRITZ!Box Fon WLAN 7113-B-030909-010209-103166-333136-217902-600467-13639-avm
//	FRITZ!Box Fon-B-021200-000100-322100-005115-217902-060433-7703-avm
//	FRITZ!Fon 7150-B-031203-010229-577226-323706-217902-380471-14616-avm
//	FRITZ!Box 6360 Cable (kbw)-Kabel-212806-010307-577727-370307-787902-850525-22677-kabelbw
//	FRITZ!Box Fon WLAN-B-151002-030405-204524-232217-836702-080434-7804-avm
//	FRITZ!Box Fon WLAN 7240-B-101810-020821-513752-526014-146702-730529-24234-avm
//	FRITZ!Box Fon WLAN 7270 v3 (UI)-B-222908-020429-604741-073467-787902-740522-22574-1und1
//	FRITZ!Box Fon WLAN 7390 (UI)-B-031110-000306-137077-747027-787902-840550-24230-1und1


	@Test
	public void test() throws ClientProtocolException, InvalidFirmwareException, IOException {
		doReturn("<html><body>FRITZ!Box Fon WLAN 7390 (UI)-B-200103-020314-055724-201216-217902-840522-22574-1und1</body></html>").when(this.mockedFbc).getSystemStatus();

		FritzBoxFirmware fw = FritzBoxFirmware.detectFirmwareVersion(mockedFbc);
		Assert.assertEquals("FRITZ!Box Fon WLAN 7390 (UI)", fw.name);
		Assert.assertEquals("B", fw.annex);
		Assert.assertEquals(84, fw.boxtype);
		Assert.assertEquals(5, fw.majorFirmwareVersion);
		Assert.assertEquals(22, fw.minorFirmwareVersion);
		Assert.assertEquals(22574, fw.revision);
		Assert.assertEquals("1und1", fw.branding);
		Assert.assertEquals("de", fw.language);
	}

}
