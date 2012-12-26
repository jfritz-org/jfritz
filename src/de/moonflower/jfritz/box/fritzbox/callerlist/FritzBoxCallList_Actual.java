package de.moonflower.jfritz.box.fritzbox.callerlist;

import java.util.Vector;

import de.moonflower.jfritz.box.BoxCallBackListener;
import de.moonflower.jfritz.box.fritzbox.FritzBox;

public class FritzBoxCallList_Actual extends FritzBoxCallList_Pre_05_50 {

	public FritzBoxCallList_Actual(FritzBox fritzBox, Vector<BoxCallBackListener> callbackListener) {
		super(fritzBox, callbackListener);
	}
}
