/*
 * Created on 09.09.2005
 *
 */
package de.moonflower.jfritz.dialogs.config;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * @author Robert Palmer
 *
 */
public abstract class CallMonitorConfigDialog extends JDialog{
	private static final long serialVersionUID = -8424740656843258182L;

	public CallMonitorConfigDialog(JDialog parent, boolean b) {
		super(parent, b);
	}

	public CallMonitorConfigDialog(JFrame parent, boolean b) {
		super(parent, b);
	}

	abstract public int showConfigDialog();
}
