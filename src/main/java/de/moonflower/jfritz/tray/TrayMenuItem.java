package de.moonflower.jfritz.tray;

import java.awt.MenuItem;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

public class TrayMenuItem {
	private JMenuItem jMenuItem;
	private MenuItem menuItem;

	public TrayMenuItem(String name)
	{
		jMenuItem = new JMenuItem(name);
		menuItem = new MenuItem(name);
	}

	public void setActionCommand(String actionCommand)
	{
		jMenuItem.setActionCommand(actionCommand);
		menuItem.setActionCommand(actionCommand);
	}

	public void addActionListener(ActionListener l)
	{
		jMenuItem.addActionListener(l);
		menuItem.addActionListener(l);
	}

	public JMenuItem getJMenuItem()
	{
		return jMenuItem;
	}

	public MenuItem getMenuItem()
	{
		return menuItem;
	}
}
