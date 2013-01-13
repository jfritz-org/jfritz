package de.moonflower.jfritz.box.fritzbox.callerlist;

import java.util.Vector;

import de.moonflower.jfritz.box.BoxCallBackListener;
import de.moonflower.jfritz.box.fritzbox.FritzBox;
import de.moonflower.jfritz.box.fritzbox.helper.FritzGetWithRetry;

public class FritzBoxCallList_Pre_05_28 extends FritzBoxCallList_Pre_04_86 {

	private static String POSTDATA_CLEAR_JOURNAL = "?telcfg:settings/ClearJournal&telcfg:settings/UseJournal=1";
	private FritzGetWithRetry fritzGet;

	public FritzBoxCallList_Pre_05_28(FritzBox fritzBox, Vector<BoxCallBackListener> callbackListener) {
		super(fritzBox, callbackListener);
		fritzGet = new FritzGetWithRetry(fritzBox);
	}

	@Override
	public void clearCallerList() {
		fritzGet.setPrependAccessMethod(true);
		fritzGet.getToVector(fritzBox.getWebcmUrl(), POSTDATA_CLEAR_JOURNAL, false);
	}
}
