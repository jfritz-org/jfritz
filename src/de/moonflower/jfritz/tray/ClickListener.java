package de.moonflower.jfritz.tray;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class ClickListener implements ActionListener {
	public static final int CLICK_LEFT = 0;
	public static final int CLICK_RIGHT = 1;

	public static final int CLICK_COUNT_SINGLE = 0;
	public static final int CLICK_COUNT_DOUBLE = 1;

	private int clickType;
	private int clickCount;

	public ClickListener(int clickType, int clickCount) {
		this.clickType = clickType;
		this.clickCount = clickCount;
	}

	public int getClickType() {
		return clickType;
	}

	public int getClickCount() {
		return clickCount;
	}

	public abstract void actionPerformed(ActionEvent e);
}
