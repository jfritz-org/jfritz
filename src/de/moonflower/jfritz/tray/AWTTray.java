package de.moonflower.jfritz.tray;
/**
 * Got this file from Spark project:
 * http://www.igniterealtime.org/community/message/146557#146557
 */

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import javax.swing.ImageIcon;


public class AWTTray implements Tray{
	private SystemTray tray;
	private TrayIcon icon;

	/** Creates a new instance of AWTTray */
	public AWTTray() {
		tray = SystemTray.getSystemTray();
	}

	public void setImage(ImageIcon i) {
		icon = new TrayIcon(i.getImage());
	}

	public void add(ImageIcon i) {
		icon = new TrayIcon(i.getImage());
		icon.setImageAutoSize(true);
		try {
			tray.add(icon);
		} catch (AWTException ex) {
			ex.printStackTrace();
		}
	}

	public void setTooltip(String s) {
		icon.setToolTip(s);
	}

	public void addMouseListener(MouseListener l) {
		icon.addMouseListener(l);
	}

	public void addActionListener(ActionListener l) {
		icon.addActionListener(l);
	}

	public void setPopupMenu(TrayMenu m) {
		icon.setPopupMenu(m.getPopupMenu());
	}

	public void displayMessage(String caption, String message, int type) {
		TrayIcon.MessageType tp = TrayIcon.MessageType.NONE;
		switch (type)
		{
		case MESSAGE_TYPE_ERROR: tp=TrayIcon.MessageType.ERROR; break;
		case MESSAGE_TYPE_INFO: tp=TrayIcon.MessageType.INFO; break;
		case MESSAGE_TYPE_NONE: tp=TrayIcon.MessageType.NONE; break;
		case MESSAGE_TYPE_WARNING: tp=TrayIcon.MessageType.WARNING; break;

		}
		icon.displayMessage(caption, message, tp);
	}

	public boolean isSupported() {
		return SystemTray.isSupported();
	}

	public void remove() {
		tray.remove(icon);
	}
}