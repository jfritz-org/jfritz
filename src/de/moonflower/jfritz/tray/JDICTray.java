package de.moonflower.jfritz.tray;
/**
 * Got this file from Spark project:
 * http://www.igniterealtime.org/community/message/146557#146557
 */

import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import javax.swing.ImageIcon;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;


public class JDICTray implements Tray {
	private SystemTray tray;
	private TrayIcon icon;

	/** Creates a new instance of JDICTray */
	public JDICTray() {
		tray = SystemTray.getDefaultSystemTray();
	}

	public void setImage(ImageIcon i) {
		icon.setIcon(i);
	}

	public void add(ImageIcon i) {
		icon = new TrayIcon(i);
		tray.addTrayIcon(icon);
	}

	public void setTooltip(String s) {
		icon.setToolTip(s);
	}

	public void addMouseListener(MouseListener l) {
	}

	public void addActionListener(ActionListener l) {
		icon.addActionListener(l);
	}

	public void setPopupMenu(TrayMenu m) {
		icon.setPopupMenu(m.getJPopupMenu());
	}

	public void displayMessage(String caption, String message, int type) {
		icon.displayMessage(caption, message, type);
	}

	public boolean isSupported() {
		return tray != null;
	}

	public void remove() {
		tray.removeTrayIcon(icon);
	}
}