package de.moonflower.jfritz.tray;

import java.awt.*;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;


public class TrayMenu {
	private JPopupMenu jPopup;
	private PopupMenu popup;


	public TrayMenu(String name)
	{
		jPopup = new JPopupMenu(name);
		popup = new PopupMenu(name);
	}

	public void add(TrayMenuItem item)
	{
		jPopup.add(item.getJMenuItem());
		popup.add(item.getMenuItem());
	}

	public void add(JMenu menu)
	{
		jPopup.add(menu);
	}

	public void add(Menu menu) {
		popup.add(menu);
	}

	public void addSeparator()
	{
		jPopup.addSeparator();
		popup.addSeparator();
	}

	public JPopupMenu getJPopupMenu()
	{
		return jPopup;
	}

	public PopupMenu getPopupMenu()
	{
		return popup;
	}
}
