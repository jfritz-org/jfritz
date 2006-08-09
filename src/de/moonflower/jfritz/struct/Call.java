/*
 *
 * Created on 08.05.2005
 *
 */
package de.moonflower.jfritz.struct;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.Debug;

/**
 * @author Arno Willig
 */
public class Call {

    private JFritz jfritz;

    private CallType calltype;

    private Date calldate;

    private PhoneNumber number;

    private String route;

    private String port;

    private int duration;

    private double cost= -1;

    private String comment = ""; //$NON-NLS-1$

    public Call(JFritz jfritz, CallType calltype, Date calldate,
            PhoneNumber number, String port, String route, int duration) {
        this.jfritz = jfritz;
        this.calltype = calltype;
        this.calldate = calldate;
        this.number = number;

        //fix so that an empty number doesnt get linked to an empty entry in the telephone book
        if(this.number != null && this.number.toString().equals("") )
        	this.number = null;

        this.route = route;
//      Parse the SIP Provider and save it correctly
		if (this.route.startsWith("Internet: ")) {
			Enumeration en = jfritz.getSIPProviderTableModel()
					.getProviderList().elements();
			while (en.hasMoreElements()) {
				SipProvider sipProvider = (SipProvider) en.nextElement();
				if (sipProvider.getNumber().equals(this.route.substring(10))) {
					this.route = "SIP" + sipProvider.getProviderID();
					break;
				}
			}
		}


        this.port = port;
        this.duration = duration;
    }

    /**
     * @return Returns the calldate.
     */
    public Date getCalldate() {
        return calldate;
    }

    /**
     * @return Returns the calltype.
     */
    public CallType getCalltype() {
        return calltype;
    }

    /**
     * @return Returns the number.
     */
    public PhoneNumber getPhoneNumber() {
        return number;
    }

    /**
     * @return Returns the person the number belongs to or null.
     */
    public Person getPerson() {
    	Person retValue = getPerson(false);
    	if (retValue==null)
    		retValue = getPerson(true);
    	return retValue;
    }

    /**
     * @return Returns the person the number belongs to or null.
     */
    public Person getPerson(boolean considerMain) {
        if ((number == null) || (number.equals(new PhoneNumber("")))) //$NON-NLS-1$
            return null;
        else
        	return jfritz.getPhonebook().findPerson(number,considerMain);
    }

    /**
     * @return Returns the port.
     */
    public String getPort() {
        return port;
    }

    /**
     * @return Returns the route.
     */
    public String getRoute() {
        return route;
    }

    /**
     * @return Returns the duration.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @return Returns CSV String
     */
    public String toCSV() {
        SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy"); //$NON-NLS-1$
        SimpleDateFormat time = new SimpleDateFormat("HH:mm"); //$NON-NLS-1$
        String outString = ""; //$NON-NLS-1$

        // type
        switch (calltype.toInt()) {
        case 1: {
            outString = "\"Incoming\""; //$NON-NLS-1$
            break;
        }
        case 2: {
            outString = "\"Missed\""; //$NON-NLS-1$
            break;
        }
        case 3: {
            outString = "\"Outgoing\""; //$NON-NLS-1$
            break;
        }
        }

        // date
        outString = outString.concat(";\"" + date.format(calldate) + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

        // time
        outString = outString.concat(";\"" + time.format(calldate) + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

        // number
        if (number == null)
            outString = outString.concat(";\"\""); //$NON-NLS-1$
        else
            outString = outString.concat(";\"" + number + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

        // route
        if (route == null)
            outString = outString.concat(";\"\""); //$NON-NLS-1$
        else {
            //String sipRoute = ""; //$NON-NLS-1$
            String convertedRoute = route;
        	if(route.startsWith("SIP")){
        			Enumeration en = jfritz.getSIPProviderTableModel()
        			.getProviderList().elements();
        		while (en.hasMoreElements()) {
        			SipProvider sipProvider = (SipProvider) en.nextElement();
        			if (route.substring(3).equals(String.valueOf(sipProvider.getProviderID()))) {
        				convertedRoute = "Internet: " + sipProvider.getNumber();
        				break;
        			}
        		}
            }

            /* This is the old format
            if (route.startsWith("SIP")) { //$NON-NLS-1$
                Enumeration en = jfritz.getSIPProviderTableModel()
                        .getProviderList().elements();
                while (en.hasMoreElements()) {
                    SipProvider sipProvider = (SipProvider) en.nextElement();
                    if (sipProvider.getProviderID() == Integer.parseInt(route
                            .substring(3))) {
                        sipRoute = sipProvider.toString();
                    }
                }
            }

            if (sipRoute.equals("")) { //$NON-NLS-1$
                outString = outString.concat(";\"" + route + "\""); //$NON-NLS-1$,  //$NON-NLS-2$
            } else {
                outString = outString.concat(";\"" + sipRoute + "\""); //$NON-NLS-1$,  //$NON-NLS-2$
            }*/

        	outString = outString.concat(";\"" + convertedRoute + "\"");

        }

        // port
        if (port.equals("4")) //$NON-NLS-1$
            outString = outString.concat(";\"ISDN\""); //$NON-NLS-1$
        else if (port.equals("0")) //$NON-NLS-1$
            outString = outString.concat(";\"FON1\""); //$NON-NLS-1$
        else if (port.equals("1")) //$NON-NLS-1$
            outString = outString.concat(";\"FON2\""); //$NON-NLS-1$
        else if (port.equals("2")) //$NON-NLS-1$
            outString = outString.concat(";\"FON3\""); //$NON-NLS-1$
        else if (port.equals("32")) //$NON-NLS-1$
            outString = outString.concat(";\"DATA\""); //$NON-NLS-1$
        else if (port.equals("33")) //$NON-NLS-1$
            outString = outString.concat(";\"DATA\""); //$NON-NLS-1$
        else if (port.equals("34")) //$NON-NLS-1$
            outString = outString.concat(";\"DATA\""); //$NON-NLS-1$
        else if (port.equals("35")) //$NON-NLS-1$
            outString = outString.concat(";\"DATA\""); //$NON-NLS-1$
        else if (port.equals("36")) //$NON-NLS-1$
            outString = outString.concat(";\"DATA\""); //$NON-NLS-1$
        else if (port.equals("")) //$NON-NLS-1$
            outString = outString.concat(";\"\""); //$NON-NLS-1$
        else
            outString = outString.concat(";\"" + port + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

        // duration
        outString = outString.concat(";\"" + duration + "\""); //$NON-NLS-1$, //$NON-NLS-2$

        // address
        if (getPerson() != null) {
            outString = outString.concat(";\"" + getPerson().getFullname() //$NON-NLS-1$
                    + "\""); //$NON-NLS-1$
            outString = outString
                    .concat(";\"" + getPerson().getStreet() + "\""); //$NON-NLS-1$, //$NON-NLS-2$
            if (getPerson().getPostalCode().equals("")) { //$NON-NLS-1$
                outString = outString.concat(";\"" + getPerson().getCity() //$NON-NLS-1$
                        + "\""); // city might be "" //$NON-NLS-1$
            } else if (getPerson().getCity().equals("")) { //$NON-NLS-1$
                outString = outString.concat(";\"" //$NON-NLS-1$
                        + getPerson().getPostalCode() + "\""); //$NON-NLS-1$
                // postCode might be ""
            } else { // postCode AND city != ""
                outString = outString.concat(";\"" //$NON-NLS-1$
                        + getPerson().getPostalCode() + " " //$NON-NLS-1$
                        + getPerson().getCity() + "\""); //$NON-NLS-1$
            }
        } else
            outString = outString.concat(";\"\";\"\";\"\""); //$NON-NLS-1$

        // CallByCall
        if (number != null && number.hasCallByCall()) {
            outString = outString.concat(";\"" + number.getCallByCall() + "\""); //$NON-NLS-1$,  //$NON-NLS-2$
        } else {
            outString = outString.concat(";\"\""); //$NON-NLS-1$
        }

        // comment
        outString = outString.concat(";\"" + comment + "\""); //$NON-NLS-1$,  //$NON-NLS-2$

        return outString;
    }

    /**
     * @return Returns XML String
     */
    public String toXML() {
        String sep = System.getProperty("line.separator", "\n"); //$NON-NLS-1$,  //$NON-NLS-2$
        String output = ""; //$NON-NLS-1$
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm"); //$NON-NLS-1$
        output = ("<entry calltype=\"" + calltype.toString() + "\">" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
        output = output + ("\t<date>" + df.format(calldate) + "</date>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
        if (number != null) {
            if (number.getCallByCall().length() > 0) {
                output = output
                        + ("\t<caller callbycall=\"" + number.getCallByCall() //$NON-NLS-1$
                                + "\">" + number.getIntNumber() + "</caller>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
            } else {
                output = output
                        + ("\t<caller>" + number.getIntNumber() + "</caller>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
            }
        }
        if (!port.equals("")) //$NON-NLS-1$
            output = output + ("\t<port>" + JFritzUtils.convertSpecialChars(port) + "</port>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
        if (!route.equals("")){ //$NON-NLS-1$
            String convertedRoute = route;
        	if(route.startsWith("SIP")){
        			Enumeration en = jfritz.getSIPProviderTableModel()
        			.getProviderList().elements();
        		while (en.hasMoreElements()) {
        			SipProvider sipProvider = (SipProvider) en.nextElement();
        			if (route.substring(3).equals(String.valueOf(sipProvider.getProviderID()))) {
        				convertedRoute = "Internet: " + sipProvider.getNumber();
        				break;
        			}
        		}
            }
        	output = output + ("\t<route>" + JFritzUtils.convertSpecialChars(convertedRoute) + "</route>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
        }if (duration > 0)
            output = output + ("\t<duration>" + duration + "</duration>" + sep); //$NON-NLS-1$, //$NON-NLS-2$

        output = output + ("\t<comment>" + JFritzUtils.convertSpecialChars(comment) + "</comment>" + sep); //$NON-NLS-1$,  //$NON-NLS-2$
        output = output + ("</entry>"); //$NON-NLS-1$
        return output;
    }

    public String toString() {
        return toCSV();
    }

    /**
     * Returns cost of call
     * @return cost
     */
    public double getCost() {
        return cost;
    }

    /**
     * Set cost of call
     * @param cost The cost of call
     */
    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     * @return Returns the comment.
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment The comment to set.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * This function compares the contents of the current call object
     * with the contents of the parameter call object
     *
     * @author Brian Jensen
     *
     * @param call, the call value to be compared with
     * @return a boolean value indicating if this call equals the call given as parameter
     */
    public boolean equals(Call call){

    	//prepare the two objects for comparing
    	String nr1 = "", nr2 = ""; //$NON-NLS-1$,  //$NON-NLS-2$
    	if (this.getPhoneNumber() != null)
    		nr1 = this.getPhoneNumber().getFullNumber();
    	if (call.getPhoneNumber() != null)
    		nr2 = call.getPhoneNumber().getFullNumber();
    	String route1 = "", route2 = ""; //$NON-NLS-1$,  //$NON-NLS-2$
    	if (this.getRoute() != null)
    		route1 = this.getRoute();
    	if (call.getRoute() != null)
    		route2 = call.getRoute();

    	if ((nr1).equals(nr2)
            && (this.getPort().equals(call.getPort()))
            && (this.getDuration() == call.getDuration())
            && (this.getCalltype().toInt() == call.getCalltype().toInt())
            && (route1.equals(route2)))
    		return true;
    	else
    		return false;

    }




}