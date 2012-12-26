package de.moonflower.jfritz.box.fritzboxnew;

public abstract class FritzBox {

	protected String host;
	protected FritzBoxFirmware firmware;

	protected String user;
	protected String password;

	public void login() {
		throw new RuntimeException("Not implemented!");
	}
}
