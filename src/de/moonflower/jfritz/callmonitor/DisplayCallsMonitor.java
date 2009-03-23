/*
 * Created on 12.09.2006
 *
 */
package de.moonflower.jfritz.callmonitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.dialogs.simple.CallMessageDlg;
import de.moonflower.jfritz.phonebook.PhoneBook;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class DisplayCallsMonitor extends CallMonitorAdaptor {

    public void pendingCallOut(Call call) {
    	Person person = null;
    	if ( call.getPhoneNumber() != null )
    	{
    		person = PhoneBook.searchFirstAndLastNameToPhoneNumber(call.getPhoneNumber().getAreaNumber());
            Debug.info("Displaying call out message...");
            displayCallOutMsg(call, call.getPhoneNumber().getAreaNumber(), call.getRoute(), call.getPort(), person);
    	} else {
    		Debug.info("Displaying call out message...");
    		displayCallOutMsg(call, null, call.getRoute(), call.getPort(), person);
    	}
    }

    public void pendingCallIn(Call call) {
    	Person person = null;
    	if ( call.getPhoneNumber() != null )
    	{
    		person = PhoneBook.searchFirstAndLastNameToPhoneNumber(call.getPhoneNumber().getAreaNumber());
            Debug.info("Displaying call in message...");
            displayCallInMsg(call, call.getPhoneNumber().getAreaNumber(), call.getRoute(), call.getPort(), person);
    	} else {
            Debug.info("Displaying call in message...");
            displayCallInMsg(call, null, call.getRoute(), call.getPort(), person);
    	}
    }

    public void endOfCall(Call call) {
//        Debug.msg("Anruf beendet. Dauer des Anrufs: " + call.getDuration() + " Sekunden);
    }

    /**
     * Display call monitor message
     *
     * @param caller
     *            Caller number
     * @param called
     *            Called number
     */
    public void displayCallInMsg(Call call, String caller, String called, String port, Person person) {
        displayCallInMsg(call, caller, called, "", port, person); //$NON-NLS-1$
    }

    /**
     * Display call monitor message
     *
     * @param caller
     *            Caller number
     * @param called
     *            Called number
     * @param name
     *            Known name (only YAC)
     */
    public void displayCallInMsg(Call call, String callerInput, String calledInput, String name, String port, Person person) {

        Debug.debug("Caller: " + callerInput); //$NON-NLS-1$
        Debug.debug("Called: " + calledInput); //$NON-NLS-1$
        Debug.debug("Name: " + name); //$NON-NLS-1$
        Debug.debug("Port: " + port); //$NON-NLS-1$

        String callerstr = "", calledstr = "", portstr = ""; //$NON-NLS-1$,  //$NON-NLS-2$ //$NON-NLS-3$
        String firstname = "", surname = "", company = ""; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$

        callerstr = callerInput;
        if ( callerInput != null )
        {
          if (!callerInput.startsWith("SIP")) { //$NON-NLS-1$
              PhoneNumber caller = new PhoneNumber(callerInput, false);
              if (caller.getIntNumber().equals("")) { //$NON-NLS-1$
                  callerstr = Main.getMessage("unknown"); //$NON-NLS-1$
              } else {
                callerstr = caller.getIntNumber();
              }
          }
        } else {
          callerstr = Main.getMessage("unknown"); //$NON-NLS-1$
        }

        calledstr = calledInput;
        if ( calledInput != null )
        {
        	if (calledInput.startsWith("SIP")) //$NON-NLS-1$
              calledstr = JFritz.getSIPProviderTableModel().getSipProvider(calledInput, calledInput);
        }

        if (name.equals("") && !callerstr.equals(Main.getMessage("unknown")) && person != null) { //$NON-NLS-1$,  //$NON-NLS-2$
            firstname = person.getFirstName();
            surname = person.getLastName();
            company = person.getCompany();
            name = person.getFullname();
        }
        if (name.equals(""))name = Main.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$
        if (firstname.equals("") && surname.equals(""))surname = Main.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        if (company.equals(""))company = Main.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$

        if (callerstr.startsWith("+49"))callerstr = "0" + callerstr.substring(3); //$NON-NLS-1$,  //$NON-NLS-2$

		if (port.equals("4")) //$NON-NLS-1$
			portstr = "ISDN"; //$NON-NLS-1$
		else if (port.equals("0")) //$NON-NLS-1$
			portstr = "FON 1"; //$NON-NLS-1$
		else if (port.equals("1")) //$NON-NLS-1$
			portstr = "FON 2"; //$NON-NLS-1$
		else if (port.equals("2")) //$NON-NLS-1$
			portstr = "FON 3"; //$NON-NLS-1$
		else if (port.equals("10")) //$NON-NLS-1$
			portstr = "DECT 1"; //$NON-NLS-1$
		else if (port.equals("11")) //$NON-NLS-1$
			portstr = "DECT 2"; //$NON-NLS-1$
		else if (port.equals("12")) //$NON-NLS-1$
			portstr = "DECT 3"; //$NON-NLS-1$
		else if (port.equals("13")) //$NON-NLS-1$
			portstr = "DECT 4"; //$NON-NLS-1$
		else if (port.equals("14")) //$NON-NLS-1$
			portstr = "DECT 5"; //$NON-NLS-1$
		else if (port.equals("15")) //$NON-NLS-1$
			portstr = "DECT 6"; //$NON-NLS-1$
	    else if (port.equals("3")) //$NON-NLS-1$
		    portstr = "Durchwahl"; //$NON-NLS-1$
        else if (port.equals("32")) //$NON-NLS-1$
            portstr = "Daten Fon 1";     //$NON-NLS-1$
        else if (port.equals("33")) //$NON-NLS-1$
            portstr = "Daten Fon 2";        //$NON-NLS-1$
        else if (port.equals("34")) //$NON-NLS-1$
            portstr = "Daten Fon 3";       //$NON-NLS-1$
        else if (port.equals("36")) //$NON-NLS-1$
            portstr = "Daten S0"; //$NON-NLS-1$
		else if (port.equals("")) //$NON-NLS-1$
			portstr = ""; //$NON-NLS-1$
		else
			portstr = port;


        Debug.debug("Caller: " + callerstr); //$NON-NLS-1$
        Debug.debug("Called: " + calledstr); //$NON-NLS-1$
        Debug.debug("Name: " + name); //$NON-NLS-1$
        Debug.debug("Port: " + portstr); //$NON-NLS-1$

        switch (Integer.parseInt(Main.getProperty("option.popuptype"))) { //$NON-NLS-1$,  //$NON-NLS-2$
            case 0 : { // No Popup
                break;
            }
            case 1: { // Popup
            	CallMessageDlg callMsgDialog = new CallMessageDlg();
            	callMsgDialog.showIncomingCall(call, callerstr, calledstr, person);
            	break;
            }
            case 2: { // Balloon
                String outstring = Main.getMessage("incoming_call") + "\n " + Main.getMessage("from") //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
                + " " + callerstr; //$NON-NLS-1$
		        if (!name.equals(Main.getMessage("unknown"))) { //$NON-NLS-1$
		            outstring = outstring + " (" + name + ")"; //$NON-NLS-1$,  //$NON-NLS-2$
		        }
		        if (!calledstr.equals(Main.getMessage("unknown"))) { //$NON-NLS-1$
		            outstring = outstring
		                    + "\n " + Main.getMessage("to") + " " + calledstr; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
		        }
		        JFritz.infoMsg(outstring);
		        break;
            }
        }

        if (JFritzUtils.parseBoolean(Main.getProperty("option.playSounds"))) //$NON-NLS-1$
        {
            JFritz.playSound(JFritz.getRingSound(), 1.0f);
        }

        if (JFritzUtils.parseBoolean(Main.getProperty(
                "option.startExternProgram"))) { //$NON-NLS-1$,  //$NON-NLS-2$
            String programString = JFritzUtils.deconvertSpecialChars(Main.getProperty("option.externProgram")); //$NON-NLS-1$

            programString = programString.replaceAll("%Number", callerstr); //$NON-NLS-1$
            programString = programString.replaceAll("%Name", name); //$NON-NLS-1$
            programString = programString.replaceAll("%Called", calledstr); //$NON-NLS-1$
            programString = programString.replaceAll("%Port", portstr); //$NON-NLS-1$
            programString = programString.replaceAll("%Firstname", firstname); //$NON-NLS-1$
            programString = programString.replaceAll("%Surname", surname); //$NON-NLS-1$
            programString = programString.replaceAll("%Company", company); //$NON-NLS-1$

            if (programString.indexOf("%URLENCODE") > -1) { //$NON-NLS-1$
                try {
                    Pattern p;
                    p = Pattern.compile("%URLENCODE\\(([^;]*)\\);"); //$NON-NLS-1$
                    Matcher m = p.matcher(programString);
                    while (m.find()) {
                        String toReplace = m.group();
                        toReplace = toReplace.replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$,  //$NON-NLS-2$
                        toReplace = toReplace.replaceAll("\\(", "\\\\("); //$NON-NLS-1$, //$NON-NLS-2$
                        toReplace = toReplace.replaceAll("\\)", "\\\\)"); //$NON-NLS-1$, //$NON-NLS-2$
                        String toEncode = m.group(1);
                        programString = programString.replaceAll(toReplace,
                                URLEncoder.encode(toEncode, "UTF-8")); //$NON-NLS-1$
                    }
                } catch (UnsupportedEncodingException uee) {
                    Debug.error(uee.toString());
                }
            }

            if (programString.equals("")) { //$NON-NLS-1$
                Debug.errDlg(Main.getMessage("no_external_program") //$NON-NLS-1$
                        + programString);
                return;
            }
            Debug.info("Starte externes Programm: " + programString); //$NON-NLS-1$
            try {
                Runtime.getRuntime().exec(programString);
            } catch (IOException e) {
                Debug.errDlg(Main.getMessage("not_external_program_start") //$NON-NLS-1$
                        + programString);
                Debug.error(e.toString());
            }
        }

    }

    /**
     * Display call monitor message
     *
     * @param called
     *            Called number
     */
    public void displayCallOutMsg(Call call, String calledInput, String providerInput, String port, Person person) {
        Debug.debug("Called: " + calledInput); //$NON-NLS-1$
        Debug.debug("Provider: " + providerInput); //$NON-NLS-1$
        Debug.debug("Port: " + port); //$NON-NLS-1$

        String calledstr = "", providerstr = "", name = "", portstr = ""; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        String firstname = "", surname = "", company = ""; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$

        if ( calledInput != null )
        {
          calledstr = calledInput;
          if (!calledInput.startsWith("SIP")) { //$NON-NLS-1$
              PhoneNumber called = new PhoneNumber(calledInput, Main.getProperty("option.activateDialPrefix").toLowerCase()
                      .equals("true")
                      && (!(providerInput.indexOf("@") > 0)));
              if (!called.getIntNumber().equals("")) //$NON-NLS-1$
                  calledstr = called.getIntNumber();
          }
        }

        if ( providerInput != null )
        {
          providerstr = providerInput;
          if (providerInput.startsWith("SIP")) //$NON-NLS-1$
              providerstr = JFritz.getSIPProviderTableModel().getSipProvider(
                      providerInput, providerInput);
        }

        if ( person != null )
        {
          firstname = person.getFirstName();
          surname = person.getLastName();
          company = person.getCompany();
          name = person.getFullname();
        }

        if (name.equals(""))name = Main.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$
        if (firstname.equals("") && surname.equals(""))surname = Main.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        if (company.equals(""))company = Main.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$

        if (calledstr.startsWith("+49"))calledstr = "0" + calledstr.substring(3); //$NON-NLS-1$,  //$NON-NLS-2$

		if (port.equals("4")) //$NON-NLS-1$
			portstr = "ISDN"; //$NON-NLS-1$
		else if (port.equals("0")) //$NON-NLS-1$
			portstr = "FON 1"; //$NON-NLS-1$
		else if (port.equals("1")) //$NON-NLS-1$
			portstr = "FON 2"; //$NON-NLS-1$
		else if (port.equals("2")) //$NON-NLS-1$
			portstr = "FON 3"; //$NON-NLS-1$
	    else if (port.equals("3")) //$NON-NLS-1$
		    portstr = "Durchwahl"; //$NON-NLS-1$
		else if (port.equals("10")) //$NON-NLS-1$
			portstr = "DECT 1"; //$NON-NLS-1$
		else if (port.equals("11")) //$NON-NLS-1$
			portstr = "DECT 2"; //$NON-NLS-1$
		else if (port.equals("12")) //$NON-NLS-1$
			portstr = "DECT 3"; //$NON-NLS-1$
		else if (port.equals("13")) //$NON-NLS-1$
			portstr = "DECT 4"; //$NON-NLS-1$
		else if (port.equals("14")) //$NON-NLS-1$
			portstr = "DECT 5"; //$NON-NLS-1$
		else if (port.equals("15")) //$NON-NLS-1$
			portstr = "DECT 6"; //$NON-NLS-1$
        else if (port.equals("32")) //$NON-NLS-1$
            portstr = "Daten Fon 1";     //$NON-NLS-1$
        else if (port.equals("33")) //$NON-NLS-1$
            portstr = "Daten Fon 2";        //$NON-NLS-1$
        else if (port.equals("34")) //$NON-NLS-1$
            portstr = "Daten Fon 3";       //$NON-NLS-1$
        else if (port.equals("36")) //$NON-NLS-1$
            portstr = "Daten S0"; //$NON-NLS-1$
		else if (port.equals("")) //$NON-NLS-1$
			portstr = ""; //$NON-NLS-1$
		else
			portstr = port;

        Debug.debug("Provider: " + providerstr); //$NON-NLS-1$
        Debug.debug("Called: " + calledstr); //$NON-NLS-1$
        Debug.debug("Name: " + name); //$NON-NLS-1$
        Debug.debug("Port: " + portstr); //$NON-NLS-1$

        switch (Integer.parseInt(Main.getProperty("option.popuptype"))) { //$NON-NLS-1$,  //$NON-NLS-2$
            case 0 : { // No Popup
                break;
            }
            case 1: { // Popup
            	CallMessageDlg callMsgDialog = new CallMessageDlg();
            	callMsgDialog.showOutgoingCall(call, providerstr, calledstr, person);
            	break;
            }
            case 2: { // Balloon
                String outstring = Main.getMessage("outgoing_call") + "\n " //$NON-NLS-1$,  //$NON-NLS-2$
                	+ Main.getMessage("to") + " " + calledstr; //$NON-NLS-1$,  //$NON-NLS-2$
                if (!name.equals(Main.getMessage("unknown")))outstring += " (" + name + ")\n "; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
                else
                	outstring += "\n "; //$NON-NLS-1$
                outstring += Main.getMessage("through_provider") + " " + providerstr; //$NON-NLS-1$,  //$NON-NLS-2$

		        JFritz.infoMsg(outstring);
		        break;
            }
        }

        if (JFritzUtils.parseBoolean(Main.getProperty("option.playSounds"))) //$NON-NLS-1$
        {
            JFritz.playSound(JFritz.getCallSound(), 1.0f);
        }

        if (JFritzUtils.parseBoolean(Main.getProperty(
                "option.startExternProgram"))) { //$NON-NLS-1$,  //$NON-NLS-2$

            String programString = JFritzUtils.deconvertSpecialChars(Main.getProperty("option.externProgram")); //$NON-NLS-1$

			programString = programString.replaceAll("%Number", calledstr); //$NON-NLS-1$
			programString = programString.replaceAll("%Name", name); //$NON-NLS-1$
			programString = programString.replaceAll("%Called", providerstr); //$NON-NLS-1$
			programString = programString.replaceAll("%Port", portstr); //$NON-NLS-1$
			programString = programString.replaceAll("%Firstname", firstname); //$NON-NLS-1$
			programString = programString.replaceAll("%Surname", surname); //$NON-NLS-1$
			programString = programString.replaceAll("%Company", company); //$NON-NLS-1$

			if (programString.indexOf("%URLENCODE") > -1) { //$NON-NLS-1$
			try {
			    Pattern p;
			    p = Pattern.compile("%URLENCODE\\(([^;]*)\\);"); //$NON-NLS-1$
			    Matcher m = p.matcher(programString);
			    while (m.find()) {
			        String toReplace = m.group();
			        toReplace = toReplace.replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$,  //$NON-NLS-2$
			        toReplace = toReplace.replaceAll("\\(", "\\\\("); //$NON-NLS-1$, //$NON-NLS-2$
			        toReplace = toReplace.replaceAll("\\)", "\\\\)"); //$NON-NLS-1$, //$NON-NLS-2$
			        String toEncode = m.group(1);
			        programString = programString.replaceAll(toReplace,
			                URLEncoder.encode(toEncode, "UTF-8")); //$NON-NLS-1$
			    }
			} catch (UnsupportedEncodingException uee) {
			    Debug.error(uee.toString());
			}
			}

			if (programString.equals("")) { //$NON-NLS-1$
			Debug.errDlg(Main.getMessage("no_external_program") //$NON-NLS-1$
			        + programString);
			return;
			}
			Debug.info("Starte externes Programm: " + programString); //$NON-NLS-1$
			try {
				Runtime.getRuntime().exec(programString);
			} catch (IOException e) {
				Debug.errDlg(Main.getMessage("not_external_program_start") //$NON-NLS-1$
							+ programString);
				Debug.error(e.toString());
			}
        }
    }
}
