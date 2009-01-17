package de.moonflower.jfritz;

import javax.swing.JToolBar;

public class StatusBarPanel extends JToolBar implements Comparable<StatusBarPanel> {

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

	public int compareTo(StatusBarPanel o) {
		if (o.priority < this.priority)
			return 1;
		else if (o.priority > this.priority)
			return -1;
		else {
			return 0;
		}
	}
}
