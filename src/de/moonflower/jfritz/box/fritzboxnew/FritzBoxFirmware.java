package de.moonflower.jfritz.box.fritzboxnew;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.utils.HTMLUtil;

public class FritzBoxFirmware {

	protected byte boxtype;
	protected byte majorFirmwareVersion;
	protected byte minorFirmwareVersion;

	protected int revision;

	protected String name;
	protected String annex;
	protected String branding;
	protected String language;

	public static FritzBoxFirmware detectFirmwareVersion(FritzBoxCommunication fbc) throws ClientProtocolException, InvalidFirmwareException, IOException {
		FritzBoxFirmware result = new FritzBoxFirmware();
		result.parseResponse(fbc.getSystemStatus());

		return result;
	}

	protected void parseResponse(final String input) throws InvalidFirmwareException {
		if (input == null) {
			throw new InvalidFirmwareException("Could not parse input, you submitted NULL");
		}

		String inputWithoutHtmlTags = HTMLUtil.stripHTMLTags(input);

		String[] splitted = inputWithoutHtmlTags.split("-");
		if (splitted.length != 10 && splitted.length != 11) {
			throw new InvalidFirmwareException("Expected response with 10 or 11 fields, but got " + splitted.length);
		}

		extractFirmwareValues(splitted);
	}

	private void extractFirmwareValues(final String[] splitted) throws InvalidFirmwareException {
		name = splitted[0];
		annex = splitted[1];

		extractFirmwareVersion(splitted[7]);
		extractRevision(splitted[8]);
		branding = splitted[9];
		extractLanguage(splitted);
	}

	private void extractFirmwareVersion(final String input) throws InvalidFirmwareException {
		String fw = input;
		int fwLength = fw.length();

		try {
			minorFirmwareVersion = Byte.parseByte(input.substring(fwLength - 2));
			majorFirmwareVersion = Byte.parseByte(input.substring(fwLength-4, fwLength-2));
			boxtype = Byte.parseByte(input.substring(0, fwLength-4));
		} catch (NumberFormatException nfe) {
			throw new InvalidFirmwareException("Could not convert firmware string to byte: " + fw);
		}
	}

	private void extractRevision(final String input) throws InvalidFirmwareException {
		try {
			revision = Integer.parseInt(input);
		} catch (NumberFormatException nfe) {
			throw new InvalidFirmwareException("Could not convert revision string to byte: " + input);
		}
	}

	private void extractLanguage(final String[] splitted) {
		language = "de";

		if (splitted.length == 11) {
			language = splitted[10];
		}
	}
}
