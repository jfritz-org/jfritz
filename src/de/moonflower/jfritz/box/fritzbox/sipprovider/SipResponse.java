package de.moonflower.jfritz.box.fritzbox.sipprovider;

public class SipResponse {

	private int id;
	private boolean activated;
	private String msn;
	private String registrar;
	private String displayname;

	public SipResponse() {
		
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public String getMsn() {
		return msn;
	}

	public void setMsn(String msn) {
		this.msn = msn;
	}

	public String getRegistrar() {
		return registrar;
	}

	public void setRegistrar(String registrar) {
		this.registrar = registrar;
	}

	public String getDisplayname() {
		return displayname;
	}

	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("id: ");
		sb.append(id);
		
		sb.append(" activated: ");
		sb.append(activated);
		
		sb.append(" msn: ");
		if (msn == null) {
			sb.append("ERROR");
		} else {
			sb.append(msn);
		}

		sb.append(" registrar: ");
		if (registrar == null) {
			sb.append("ERROR");
		} else {
			sb.append(registrar);
		}
		
		sb.append(" displayname: ");
		if (displayname == null) {
			sb.append("ERROR");
		} else {
			sb.append(displayname);
		}
		
		return sb.toString();
	}
}
