/*
 *
 * Created on 16.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.sip;

/**
 * @author rob
 *
 */
public class SipProvider {

	private int providerID;

	private boolean active;

	private String providerName, phoneNumber;

	private int startDate, festnetzTakt1, festnetzTakt2, festnetzFreiminuten,
				mobileTakt1, mobileTakt2, mobileFreiminuten;

	private double festnetzKosten, mobileKosten;

	public SipProvider(int providerID, String phoneNumber, String providerName) {
		this.providerID = providerID;
		this.providerName = providerName;
		this.phoneNumber = phoneNumber;
		active = false;
		startDate = 1;
		festnetzTakt1 = 60;
		festnetzTakt2 = 60;
		festnetzKosten = 1.5;
		festnetzFreiminuten = 0;
		mobileTakt1 = 60;
		mobileTakt2 = 60;
		mobileKosten = 23.0;
		mobileFreiminuten = 0;
	}

	/**
	 * @return Returns phone number
	 */
	public final String getNumber() {
		return phoneNumber;
	}

	/**
	 * @return Returns name of sip-provider or IP
	 */
	public final String getProvider() {
		return providerName;
	}

	public String toString() {
		return phoneNumber + "@" + providerName;
	}

	/**
	 * @return Returns the providerID.
	 */
	public final int getProviderID() {
		return providerID;
	}

	/**
	 * Set VoIP-Provider active state
	 * @param state
	 */
	public final void setActive(boolean state) {
	    active = state;
	}

	/**
	 *
	 * @return VoIP-Provider active state
	 */
	public final boolean isActive() {
	    return active;
	}

	/**
	 * Set start date of month
	 * @param date
	 */
	public final void setStartDate(int date) {
	    startDate = date;
	}

	/**
	 *
	 * @return Returns start date of month
	 */
	public final int getStartDate() {
	    return startDate;
	}

	/**
	 * @return Returns XML String
	 */
	public String toXML() {
		String sep = System.getProperty("line.separator", "\n");
		String output = "";
		output = ("<entry id=\"" + providerID + "\">" + sep);
		output = output + ("\t<name>" + providerName + "</name>" + sep);
		output = output + ("\t<number>" + phoneNumber + "</number>" + sep);
		output = output + ("\t<active>" + active + "</active>" + sep);
		output = output + ("\t<startdate>" + startDate + "</startdate>" + sep);
		output = output + ("\t<festnetztakt1>" + festnetzTakt1 + "</festnetztakt1>" + sep);
		output = output + ("\t<festnetztakt2>" + festnetzTakt2 + "</festnetztakt2>" + sep);
		output = output + ("\t<festnetzkosten>" + festnetzKosten + "</festnetzkosten>" + sep);
		output = output + ("\t<festnetzfreiminuten>" + festnetzFreiminuten + "</festnetzfreiminuten>" + sep);
		output = output + ("\t<mobiletakt1>" + mobileTakt1 + "</mobiletakt1>" + sep);
		output = output + ("\t<mobiletakt2>" + mobileTakt2 + "</mobiletakt2>" + sep);
		output = output + ("\t<mobilekosten>" + mobileKosten + "</mobilekosten>" + sep);
		output = output + ("\t<mobilefreiminuten>" + mobileFreiminuten + "</mobilefreiminuten>" + sep);
		output = output + ("</entry>");
		return output;
	}

    /**
     * @return Returns Festnetztaktung in der ersten Minute
     */
    public int getFestnetzTakt1() {
        return festnetzTakt1;
    }
    /**
     * @param festnetzTakt1 Setze Festnetztaktung in der ersten Minute
     */
    public void setFestnetzTakt1(int festnetzTakt1) {
        this.festnetzTakt1 = festnetzTakt1;
    }
    /**
     * @return Returns Festnetztaktung ab der zweiten Minute
     */
    public int getFestnetzTakt2() {
        return festnetzTakt2;
    }
    /**
     * @param festnetzTakt2 Setze Festnetztaktung ab der zweiten Minute
     */
    public void setFestnetzTakt2(int festnetzTakt2) {
        this.festnetzTakt2 = festnetzTakt2;
    }

    /**
     * @return Returns Kosten für ein Festnetzgespräch in cent pro Minute
     */
    public double getFestnetzKosten() {
        return festnetzKosten;
    }

    /**
     * @param festnetzKosten Setze Kosten für ein Festnetzgespräch in cent pro Minute
     */
    public void setFestnetzKosten(double festnetzKosten) {
        this.festnetzKosten = festnetzKosten;
    }

    /**
     * @return Returns Freiminuten ins Festnetz.
     */
    public int getFestnetzFreiminuten() {
        return festnetzFreiminuten;
    }

    /**
     * @param festnetzFreiminuten Setze Freiminuten ins Festnetz
     */
    public void setFestnetzFreiminuten(int festnetzFreiminuten) {
        this.festnetzFreiminuten = festnetzFreiminuten;
    }
    /**
     * @return Returns Freiminuten ins Mobilfunknetz.
     */
    public int getMobileFreiminuten() {
        return mobileFreiminuten;
    }
    /**
     * @param mobileFreiminuten Setze Freiminuten ins Mobilfunknetz.
     */
    public void setMobileFreiminuten(int mobileFreiminuten) {
        this.mobileFreiminuten = mobileFreiminuten;
    }
    /**
     * @return Returns Kosten für ein Mobilfunkgespräch.
     */
    public double getMobileKosten() {
        return mobileKosten;
    }
    /**
     * @param mobileKosten Setze Kosten für ein Mpbilfunkgespräch.
     */
    public void setMobileKosten(double mobileKosten) {
        this.mobileKosten = mobileKosten;
    }
    /**
     * @return Returns Mobilfunktaktung in der ersten Minute.
     */
    public int getMobileTakt1() {
        return mobileTakt1;
    }
    /**
     * @param mobileTakt1 Setze Mobilfunktaktung in der ersten Minute.
     */
    public void setMobileTakt1(int mobileTakt1) {
        this.mobileTakt1 = mobileTakt1;
    }
    /**
     * @return Returns Mobilfunktaktung ab der zweiten Minute.
     */
    public int getMobileTakt2() {
        return mobileTakt2;
    }
    /**
     * @param mobileTakt2 Mobilfunktaktung ab der zweiten Minute.
     */
    public void setMobileTakt2(int mobileTakt2) {
        this.mobileTakt2 = mobileTakt2;
    }
}
