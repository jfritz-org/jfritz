/*
 *
 * Created on 16.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.sip;

import java.util.Comparator;
import java.util.Date;

import de.moonflower.jfritz.struct.Call;

/**
 * @author rob
 *
 */
public class SipProvider {

    private int providerID;

    private boolean active;

    private String providerName, phoneNumber;

    public SipProvider(int providerID, String phoneNumber, String providerName) {
        this.providerID = providerID;
        this.providerName = providerName;
        this.phoneNumber = phoneNumber;
        active = false;
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
        return phoneNumber + "@" + providerName; //$NON-NLS-1$
    }

    /**
     * @return Returns the providerID.
     */
    public final int getProviderID() {
        return providerID;
    }

    /**
     * @return returns if given id is equal to providerID
     */
    public boolean isProviderID(int id) {
    	return id == providerID;
    }

    /**
     *
     * @param providerID The providerID to set.
     */
    public void setProviderID(int providerID) {
        this.providerID = providerID;
    }

    /**
     * Set VoIP-Provider active state
     *
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
     * @return Returns XML String
     */
    public String toXML() {
        String sep = System.getProperty("line.separator", "\n"); //$NON-NLS-1$,  //$NON-NLS-2$
        String output = ""; //$NON-NLS-1$
        output = ("<entry id=\"" + providerID + "\">" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
        output = output + ("\t<name>" + providerName + "</name>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
        output = output + ("\t<number>" + phoneNumber + "</number>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
        output = output + ("\t<active>" + active + "</active>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
        output = output + ("</entry>"); //$NON-NLS-1$
        return output;
    }

    /**
     * This comparator is used to sort vectors of data
     */
    public class ColumnSorter implements Comparator<Call> {

		public int compare(Call v1, Call v2) {
            Date o1, o2;

            o1 = v1.getCalldate();
            o2 = v2.getCalldate();

            // Sort nulls so they appear last, regardless
            // of sort order
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            } else {
            	return 	o1.compareTo(o2);
            }
		}
    }
}
