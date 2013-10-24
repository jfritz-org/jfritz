package de.moonflower.jfritz.box.fritzbox.callerlist;

import java.util.Vector;

import de.moonflower.jfritz.box.BoxCallBackListener;
import de.moonflower.jfritz.box.BoxCallListInterface;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.robotniko.fboxlib.fritzbox.FirmwareVersion;

public class FritzBoxCallerListFactory {

	public static BoxCallListInterface createFritzBoxCallListFromFirmware(FirmwareVersion firmware, FritzBox fritzBox, Vector<BoxCallBackListener> callbackListener) {
		BoxCallListInterface result = null;

		if (firmware != null) {
			if (firmware.isLowerThan(4, 86)) {
				result = new FritzBoxCallList_Pre_04_86(fritzBox, callbackListener);
			} else if (firmware.isLowerThan(5, 28)) {
				result = new FritzBoxCallList_Pre_05_28(fritzBox, callbackListener);
			} else {
				result = new FritzBoxCallList_Actual(fritzBox, callbackListener);
			}
		}

		return result;
	}
}
