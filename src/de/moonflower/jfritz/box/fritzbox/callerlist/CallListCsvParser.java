package de.moonflower.jfritz.box.fritzbox.callerlist;

import java.util.Vector;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.exceptions.FeatureNotSupportedByFirmware;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.IProgressListener;
import de.moonflower.jfritz.utils.Debug;

public class CallListCsvParser {
	private final static Logger log = Logger.getLogger(CallListCsvParser.class);

	private MessageProvider messages = MessageProvider.getInstance();

	private FritzBox fritzBox;
	private String[] lines;
	private String separator = ";";

	private Vector<Call> calls = new Vector<Call>();
	private CallListCsvLineParser lineParser;
	private Vector<IProgressListener> progressListener;

	public void setProgressListener(Vector<IProgressListener> progressListener) {
		this.progressListener = progressListener;
	}

	public Vector<Call> parseCsvString(FritzBox fritzBox, String input) throws FeatureNotSupportedByFirmware {
		this.fritzBox = fritzBox;
		calls.clear();

		if (input == null || "".equals(input)) {
			Debug.error(log, "CallListCsvParser: input is null or empty");
			throw new FeatureNotSupportedByFirmware("Get caller list", messages.getMessage("box.no_caller_list"));
		}

		splitInput(input);

		if (progressListener != null) {
			for (IProgressListener listener: progressListener) {
				listener.setMax(lines.length);
			}
		}

		parseAllLines();

		return calls;
	}

	private void splitInput(String input) {
		lines = input.split("\n");
	}

	private void parseAllLines() throws FeatureNotSupportedByFirmware {
		parseFirstLine();

		if (lines.length > 1) {
			parseSecondLine();
		} else {
			Debug.error(log, "CallListCsvParser: missing header line");
			throw new FeatureNotSupportedByFirmware("Get caller list", messages.getMessage("box.no_caller_list"));
		}

		if (lines.length > 2) {
			parseRemainingLines();
		}
	}

	private void parseFirstLine() throws FeatureNotSupportedByFirmware {
		String separatorLine = lines[0];
		if (separatorLine.startsWith("sep=")) {
			parseSeparatorLine(separatorLine);
		} else {
			Debug.error(log, "CallListCsvParser: expected sep=; but got: " + separatorLine);
			throw new FeatureNotSupportedByFirmware("Get caller list", messages.getMessage("box.no_caller_list"));
		}
	}

	private void parseSeparatorLine(String separatorLine)
			throws FeatureNotSupportedByFirmware {
		String[] splitted = separatorLine.split("=");
		if (splitted.length == 2) {
			separator = splitted[1];
			if (progressListener != null) {
				for (IProgressListener listener: progressListener) {
					listener.setProgress(1);
				}
			}
		} else {
			Debug.error(log, "CallListCsvParser: could not extract separator from line: " + separatorLine);
			throw new FeatureNotSupportedByFirmware("Get caller list", messages.getMessage("box.no_caller_list"));
		}
	}

	private void parseSecondLine() {
		@SuppressWarnings("unused")
		String headerLine = lines[1];
		if (progressListener != null) {
			for (IProgressListener listener: progressListener) {
				listener.setProgress(2);
			}
		}
	}

	private void parseRemainingLines() {
		lineParser = new CallListCsvLineParser(separator);

		Call call = null;
		for (int i=2; i<lines.length; i++) {
			try {
				call = lineParser.parseLine(fritzBox, lines[i]);
				calls.add(call);

				if (progressListener != null) {
					for (IProgressListener listener: progressListener) {
						listener.setProgress(i+1);
					}
				}
			} catch (FeatureNotSupportedByFirmware fns) {
				Debug.warning(log, fns.toString());
			}
		}
	}
}
