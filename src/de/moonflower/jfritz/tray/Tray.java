package de.moonflower.jfritz.tray;
/**
 * Got this file from Spark project:
 * http://www.igniterealtime.org/community/message/146557#146557
 */

import java.awt.event.MouseListener;
import javax.swing.ImageIcon;

public interface Tray {
	public static final int MESSAGE_TYPE_ERROR = 0;
	public static final int MESSAGE_TYPE_INFO = 1;
	public static final int MESSAGE_TYPE_NONE = 2;
	public static final int MESSAGE_TYPE_WARNING = 3;

	abstract public void setImage(ImageIcon i);
	abstract public void add(ImageIcon i);
	abstract public void setTooltip(String s);
	abstract public void addMouseListener(MouseListener l);
	abstract public void addActionListener(ClickListener l);
	abstract public void setPopupMenu(TrayMenu m);
	abstract public void displayMessage(String caption, String message, int type);
	abstract public boolean isSupported();
	abstract public void remove();
	abstract public void clearActionListeners();
}