package de.moonflower.jfritz.box;

public interface BoxStatusListener {
	public void setBoxConnected(String boxName);
	public void setBoxDisconnected(String boxName);
}
