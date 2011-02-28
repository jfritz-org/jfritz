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
import de.moonflower.jfritz.dialogs.simple.CallMessageDlg;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.phonebook.PhoneBook;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.sounds.PlaySound;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class DisplayCallsMonitor extends CallMonitorAdaptor {

	private PlaySound sound;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	public DisplayCallsMonitor(final PlaySound sound) {
		this.sound = sound;
	}

    public void pendingCallOut(Call call) {
        Debug.info("Displaying call out message...");
    	Person person = null;
    	if ( call.getPhoneNumber() != null )
    	{
    		person = PhoneBook.searchFirstAndLastNameToPhoneNumber(call.getPhoneNumber().getAreaNumber());
            displayCallOutMsg(call, call.getPhoneNumber().getAreaNumber(), call.getRoute(), call.getPort().getName(), person);
    	} else {
    		displayCallOutMsg(call, null, call.getRoute(), call.getPort().getName(), person);
    	}
    }

    public void pendingCallIn(Call call) {
        Debug.info("Displaying call in message...");
    	Person person = null;
    	if ( call.getPhoneNumber() != null )
    	{
    		person = PhoneBook.searchFirstAndLastNameToPhoneNumber(call.getPhoneNumber().getAreaNumber());
            displayCallInMsg(call, call.getPhoneNumber().getAreaNumber(), call.getRoute(), call.getPort().getName(), person);
    	} else {
            displayCallInMsg(call, null, call.getRoute(), call.getPort().getName(), person);
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

        String callerstr = "", calledstr = ""; //$NON-NLS-1$,  //$NON-NLS-2$ //$NON-NLS-3$
        String firstname = "", surname = "", company = ""; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$

        callerstr = callerInput;
        if ( callerInput != null )
        {
          if (!callerInput.startsWith("SIP")) { //$NON-NLS-1$
              PhoneNumberOld caller = new PhoneNumberOld(callerInput, false);
              if (caller.getIntNumber().equals("")) { //$NON-NLS-1$
                  callerstr = messages.getMessage("unknown"); //$NON-NLS-1$
              } else {
                callerstr = caller.getIntNumber();
              }
          }
        } else {
          callerstr = messages.getMessage("unknown"); //$NON-NLS-1$
        }

        calledstr = calledInput;

        if (name.equals("") && !callerstr.equals(messages.getMessage("unknown")) && person != null) { //$NON-NLS-1$,  //$NON-NLS-2$
            firstname = person.getFirstName();
            surname = person.getLastName();
            company = person.getCompany();
            name = person.getFullname();
        }
        if (name.equals(""))name = messages.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$
        if (firstname.equals("") && surname.equals(""))surname = messages.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        if (company.equals(""))company = messages.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$

        if (callerstr.startsWith("+49"))callerstr = "0" + callerstr.substring(3); //$NON-NLS-1$,  //$NON-NLS-2$

        Debug.debug("Caller: " + callerstr); //$NON-NLS-1$
        Debug.debug("Called: " + calledstr); //$NON-NLS-1$
        Debug.debug("Name: " + name); //$NON-NLS-1$
        Debug.debug("Port: " + port); //$NON-NLS-1$

        switch (Integer.parseInt(properties.getProperty("option.popuptype"))) { //$NON-NLS-1$,  //$NON-NLS-2$
            case 0 : { // No Popup
                break;
            }
            case 1: { // Popup
            	CallMessageDlg callMsgDialog = new CallMessageDlg();
            	callMsgDialog.showIncomingCall(call, callerstr, calledstr, person);
            	break;
            }
            case 2: { // Balloon
                String outstring = messages.getMessage("incoming_call") + "\n " + messages.getMessage("from") //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
                + " " + callerstr; //$NON-NLS-1$
		        if (!name.equals(messages.getMessage("unknown"))) { //$NON-NLS-1$
		            outstring = outstring + " (" + name + ")"; //$NON-NLS-1$,  //$NON-NLS-2$
		        }
		        if (!calledstr.equals(messages.getMessage("unknown"))) { //$NON-NLS-1$
		            outstring = outstring
		                    + "\n " + messages.getMessage("to") + " " + calledstr; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
		        }
		        JFritz.infoMsg(outstring);
		        break;
            }
        }

        if (JFritzUtils.parseBoolean(properties.getProperty("option.playSounds"))) //$NON-NLS-1$
        {
        	sound.playRingSound(1.0f);
        }

        if (JFritzUtils.parseBoolean(properties.getProperty("option.startExternProgram"))) { //$NON-NLS-1$
            startExternalProgramCallIn(callerstr, name, calledstr, port, firstname, surname, company);
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

        String calledstr = "", providerstr = "", name = ""; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        String firstname = "", surname = "", company = ""; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$

        if ( calledInput != null )
        {
          calledstr = calledInput;
          if (!calledInput.startsWith("SIP")) { //$NON-NLS-1$
              PhoneNumberOld called = new PhoneNumberOld(calledInput, JFritzUtils.parseBoolean(properties.getProperty("option.activateDialPrefix"))
                      && (!(providerInput.indexOf("@") > 0)));
              if (!called.getIntNumber().equals("")) //$NON-NLS-1$
                  calledstr = called.getIntNumber();
          }
        }

        if ( providerInput != null )
        {
          providerstr = providerInput;
        }

        if ( person != null )
        {
          firstname = person.getFirstName();
          surname = person.getLastName();
          company = person.getCompany();
          name = person.getFullname();
        }

        if (name.equals(""))name = messages.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$
        if (firstname.equals("") && surname.equals(""))surname = messages.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        if (company.equals(""))company = messages.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$

        if (calledstr.startsWith("+49"))calledstr = "0" + calledstr.substring(3); //$NON-NLS-1$,  //$NON-NLS-2$

        Debug.debug("Provider: " + providerstr); //$NON-NLS-1$
        Debug.debug("Called: " + calledstr); //$NON-NLS-1$
        Debug.debug("Name: " + name); //$NON-NLS-1$
        Debug.debug("Port: " + port); //$NON-NLS-1$

        switch (Integer.parseInt(properties.getProperty("option.popuptype"))) { //$NON-NLS-1$,  //$NON-NLS-2$
            case 0 : { // No Popup
                break;
            }
            case 1: { // Popup
            	CallMessageDlg callMsgDialog = new CallMessageDlg();
            	callMsgDialog.showOutgoingCall(call, providerstr, calledstr, person);
            	break;
            }
            case 2: { // Balloon
                String outstring = messages.getMessage("outgoing_call") + "\n " //$NON-NLS-1$,  //$NON-NLS-2$
                	+ messages.getMessage("to") + " " + calledstr; //$NON-NLS-1$,  //$NON-NLS-2$
                if (!name.equals(messages.getMessage("unknown")))outstring += " (" + name + ")\n "; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
                else
                	outstring += "\n "; //$NON-NLS-1$
                outstring += messages.getMessage("through_provider") + " " + providerstr; //$NON-NLS-1$,  //$NON-NLS-2$

		        JFritz.infoMsg(outstring);
		        break;
            }
        }

        if (JFritzUtils.parseBoolean(properties.getProperty("option.playSounds"))) { //$NON-NLS-1$
        	sound.playCallSound(1.0f);
        }

        if (JFritzUtils.parseBoolean(properties.getProperty("option.startExternProgram"))) { //$NON-NLS-1$
            startExternalProgramCallOut(calledstr, name, providerstr, port, firstname, surname, company);
        }
    }

    private void startExternalProgramCallIn(final String number, final String name,
			final String called, final String port, final String firstname, final String surname, final String company) {
    	String program = JFritzUtils.deconvertSpecialChars(properties.getProperty("option.externProgram")); //$NON-NLS-1$
    	startExternalProgram(program, "In", number, name, called, port, firstname, surname, company);
    }

    private void startExternalProgramCallOut(final String number, final String name,
			final String called, final String port, final String firstname, final String surname, final String company) {
    	String program = JFritzUtils.deconvertSpecialChars(properties.getProperty("option.externProgram")); //$NON-NLS-1$
    	startExternalProgram(program, "Out", number, name, called, port, firstname, surname, company);
    }

	private void startExternalProgram(final String program, final String calltype, final String number, final String name,
			final String called, final String port, final String firstname, final String surname, final String company) {
		String programString = program;
		programString = programString.replaceAll("%CallType", calltype); //$NON-NLS-1$
		programString = programString.replaceAll("%Number", number); //$NON-NLS-1$
		programString = programString.replaceAll("%Name", name); //$NON-NLS-1$
		programString = programString.replaceAll("%Called", called); //$NON-NLS-1$
		programString = programString.replaceAll("%Port", port); //$NON-NLS-1$
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
		Debug.errDlg(messages.getMessage("no_external_program") //$NON-NLS-1$
		        + programString);
		} else {
			Debug.info("Starte externes Programm: " + programString); //$NON-NLS-1$
			try {
				Runtime.getRuntime().exec(programString);
			} catch (IOException e) {
				Debug.errDlg(messages.getMessage("not_external_program_start") //$NON-NLS-1$
							+ programString);
				Debug.error(e.toString());
			}
		}
	}
}
