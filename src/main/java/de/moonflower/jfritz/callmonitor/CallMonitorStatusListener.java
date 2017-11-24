package de.moonflower.jfritz.callmonitor;

public interface CallMonitorStatusListener {

	public void setConnectedStatus(String boxName);

	public void setDisconnectedStatus(String boxName);
}
