package de.moonflower.jfritz;

import javax.swing.JToolBar;

public class StatusBarPanel extends JToolBar implements Comparable {

	private static final long serialVersionUID = -218612992436900904L;

	private int priority;

	public StatusBarPanel(int priority)
	{
		this.priority = priority;
	}

	public int getPriority()
	{
		return priority;
	}

	public int compareTo(Object o) {
		if (((StatusBarPanel) o).priority < this.priority)
			return 1;
		else if (((StatusBarPanel) o).priority > this.priority)
			return -1;
		else {
			return 0;
		}

	}
}
