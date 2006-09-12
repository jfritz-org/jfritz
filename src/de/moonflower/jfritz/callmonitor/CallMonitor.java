/*
 * Created on 10.09.2006
 *
 */
package de.moonflower.jfritz.callmonitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

/**
 * Diese Klasse enthält eine Liste aller initialiesierter und etablierter Anrufe.
 * Sie wird von den Anrufmonitoren verwendet, um Anrufe anzuzeigen.
 *
 * @author Robert Palmer
 *
 */
public class CallMonitor {

    public static int PENDING = 0;
    public static int ESTABLISHED = 1;
    public static int NONE = 2;

    // Ankommende oder abgehende Anrufe, bei denen noch keine Verbindung
    // zustandegekommen ist
    private HashMap pendingCalls = new HashMap();

    // Anrufe, bei denen schon eine Verbindung besteht
    private HashMap establishedCalls = new HashMap();

    // Fügt den Anruf call in die Liste der "schwebenden" Anrufe ein
    public void addNewCall(int id, Call call) {
        pendingCalls.put(new Integer(id), call);
    }

    /**
     *  Transferiert den Anruf von der Liste der "schwebenden" Anrufe in die Liste der etablierten Anrufe
     *  @param id, call id
     */
    public void establishCall(int id) {
        Integer callID = new Integer(id);
        if (pendingCalls.keySet().contains(callID)) {
            establishedCalls.put(callID, pendingCalls.get(new Integer(
                    id)));
            pendingCalls.remove(callID);
        }
    }

    // Entfernt den Anruf aus einer der beiden Listen
    public void removeCall(int id) {
        if (pendingCalls.keySet().contains(new Integer(id))) {
            pendingCalls.remove(new Integer(id));
        }
        if (establishedCalls.keySet().contains(new Integer(id))) {
            establishedCalls.remove(new Integer(id));
        }
    }

    // Liefert den Status des Anrufs zurück (pending, established, none)
    public int getCallState(int id) {
        if (pendingCalls.keySet().contains(new Integer(id))) {
            return PENDING;
        } else if (establishedCalls.keySet().contains(new Integer(id))) {
            return ESTABLISHED;
        } else return NONE;
    }

    // Liefert die Daten des Anrufs
    public Call getCall(int id) {
        if ( getCallState(id) == PENDING ) {
            return (Call) pendingCalls.get(new Integer(id));
        } else if ( getCallState(id) == ESTABLISHED ) {
            return (Call) establishedCalls.get(new Integer(id));
        } else return null;
    }

    // Anzahl "schwebender" Anrufe
    public int getPendingSize() {
        return pendingCalls.size();
    }

    // Anzahl der etablierten Anrufe
    public int getEstablishedSize() {
        return establishedCalls.size();
    }
    /**
     * Display call monitor message
     *
     * @param caller
     *            Caller number
     * @param called
     *            Called number
     */
    public void displayCallInMsg(String caller, String called) {
        displayCallInMsg(caller, called, ""); //$NON-NLS-1$
    }

    private String searchNameToPhoneNumber(String caller) {
        String name = ""; //$NON-NLS-1$
        PhoneNumber callerPhoneNumber = new PhoneNumber(caller);
        Debug.msg("Searchin in local database ..."); //$NON-NLS-1$
        Person callerperson = JFritz.getPhonebook().findPerson(callerPhoneNumber);
        if (callerperson != null) {
            name = callerperson.getFullname();
            Debug.msg("Found in local database: " + name); //$NON-NLS-1$
        } else {
            Debug.msg("Searchin on dasoertliche.de ..."); //$NON-NLS-1$
            Person person = ReverseLookup.lookup(callerPhoneNumber);
            if (!person.getFullname().equals("")) { //$NON-NLS-1$
                name = person.getFullname();
                Debug.msg("Found on dasoertliche.de: " + name); //$NON-NLS-1$
                Debug.msg("Add person to database"); //$NON-NLS-1$
                JFritz.getPhonebook().addEntry(person);
                JFritz.getPhonebook().fireTableDataChanged();
            } else {
                person = new Person();
                person.addNumber(new PhoneNumber(caller));
                Debug.msg("Found no person"); //$NON-NLS-1$
                Debug.msg("Add dummy person to database"); //$NON-NLS-1$
                JFritz.getPhonebook().addEntry(person);
                JFritz.getPhonebook().fireTableDataChanged();
            }
        }
        return name;
    }

    private String[] searchFirstAndLastNameToPhoneNumber(String caller) {
        String name[] = {"", "", ""}; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        PhoneNumber callerPhoneNumber = new PhoneNumber(caller);
        Debug.msg("Searching in local database ..."); //$NON-NLS-1$
        Person callerperson = JFritz.getPhonebook().findPerson(callerPhoneNumber);
        if (callerperson != null) {
            name[0] = callerperson.getFirstName();
            name[1] = callerperson.getLastName();
            name[2] = callerperson.getCompany();
            Debug.msg("Found in local database: " + name[1] + ", " + name[0]); //$NON-NLS-1$,  //$NON-NLS-2$
        } else {
            Debug.msg("Searching on dasoertliche.de ..."); //$NON-NLS-1$
            Person person = ReverseLookup.lookup(callerPhoneNumber);
            if (!person.getFullname().equals("")) { //$NON-NLS-1$
                name[0] = callerperson.getFirstName();
                name[1] = callerperson.getLastName();
                name[2] = callerperson.getCompany();
                Debug
                        .msg("Found on dasoertliche.de: " + name[1] + ", " + name[0]); //$NON-NLS-1$,  //$NON-NLS-2$
                Debug.msg("Add person to database"); //$NON-NLS-1$
                JFritz.getPhonebook().addEntry(person);
                JFritz.getPhonebook().fireTableDataChanged();
            } else {
                person = new Person();
                person.addNumber(new PhoneNumber(caller));
                Debug.msg("Found no person"); //$NON-NLS-1$
                Debug.msg("Add dummy person to database"); //$NON-NLS-1$
                JFritz.getPhonebook().addEntry(person);
                JFritz.getPhonebook().fireTableDataChanged();
            }
        }
        return name;
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
    public void displayCallInMsg(String callerInput, String calledInput, String name) {

        Debug.msg("Caller: " + callerInput); //$NON-NLS-1$
        Debug.msg("Called: " + calledInput); //$NON-NLS-1$
        Debug.msg("Name: " + name); //$NON-NLS-1$

        String callerstr = "", calledstr = ""; //$NON-NLS-1$,  //$NON-NLS-2$
        String firstname = "", surname = "", company = ""; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$

        callerstr = calledInput;
        if (!callerInput.startsWith("SIP")) { //$NON-NLS-1$
            PhoneNumber caller = new PhoneNumber(callerInput);
            if (caller.getIntNumber().equals("")) { //$NON-NLS-1$
                callerstr = JFritz.getMessage("unknown"); //$NON-NLS-1$
            } else
                callerstr = caller.getIntNumber();
        }

        calledstr = calledInput;
        if (calledInput.startsWith("SIP")) //$NON-NLS-1$
            calledstr = JFritz.getSIPProviderTableModel().getSipProvider(calledInput,
                    calledInput);

        if (name.equals("") && !callerstr.equals(JFritz.getMessage("unknown"))) { //$NON-NLS-1$,  //$NON-NLS-2$
            name = searchNameToPhoneNumber(callerstr);
            String[] nameArray = searchFirstAndLastNameToPhoneNumber(callerstr);
            firstname = nameArray[0];
            surname = nameArray[1];
            company = nameArray[2];
        }
        if (name.equals(""))name = JFritz.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$
        if (firstname.equals("") && surname.equals(""))surname = JFritz.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        if (company.equals(""))company = JFritz.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$

        if (callerstr.startsWith("+49"))callerstr = "0" + callerstr.substring(3); //$NON-NLS-1$,  //$NON-NLS-2$

        Debug.msg("Caller: " + callerstr); //$NON-NLS-1$
        Debug.msg("Called: " + calledstr); //$NON-NLS-1$
        Debug.msg("Name: " + name); //$NON-NLS-1$

        switch (Integer.parseInt(JFritz.getProperty("option.popuptype", "1"))) { //$NON-NLS-1$,  //$NON-NLS-2$
            case 0 : { // No Popup
                break;
            }
            default : {
                String outstring = JFritz.getMessage("incoming_call") + "\n " + JFritz.getMessage("from") //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
                        + " " + callerstr; //$NON-NLS-1$
                if (!name.equals(JFritz.getMessage("unknown"))) { //$NON-NLS-1$
                    outstring = outstring + " (" + name + ")"; //$NON-NLS-1$,  //$NON-NLS-2$
                }
                if (!calledstr.equals(JFritz.getMessage("unknown"))) { //$NON-NLS-1$
                    outstring = outstring
                            + "\n " + JFritz.getMessage("to") + " " + calledstr; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
                }
                JFritz.infoMsg(outstring);
                break;

            }
        }

        if (JFritzUtils.parseBoolean(JFritz.getProperty("option.playSounds", //$NON-NLS-1$
                "true"))) { //$NON-NLS-1$
            JFritz.playSound(JFritz.getRingSound());
        }

        if (JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.startExternProgram", "false"))) { //$NON-NLS-1$,  //$NON-NLS-2$
            String programString = JFritzUtils.deconvertSpecialChars(JFritz
                    .getProperty("option.externProgram", //$NON-NLS-1$
                            "")); //$NON-NLS-1$

            programString = programString.replaceAll("%Number", callerstr); //$NON-NLS-1$
            programString = programString.replaceAll("%Name", name); //$NON-NLS-1$
            programString = programString.replaceAll("%Called", calledstr); //$NON-NLS-1$
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
                    Debug.err("JFritz.class: UnsupportedEncodingException: " //$NON-NLS-1$
                            + uee.toString());
                }
            }

            if (programString.equals("")) { //$NON-NLS-1$
                Debug.errDlg(JFritz.getMessage("no_external_program") //$NON-NLS-1$
                        + programString);
                return;
            }
            Debug.msg("Start external Program: " + programString); //$NON-NLS-1$
            try {
                Runtime.getRuntime().exec(programString);
            } catch (IOException e) {
                Debug.errDlg(JFritz.getMessage("not_external_program_start") //$NON-NLS-1$
                        + programString);
                Debug.err(e.toString());
            }
        }

    }

    /**
     * Display call monitor message
     *
     * @param called
     *            Called number
     */
    public void displayCallOutMsg(String calledInput, String providerInput) {
        Debug.msg("Called: " + calledInput); //$NON-NLS-1$
        Debug.msg("Provider: " + providerInput); //$NON-NLS-1$

        String calledstr = "", providerstr = "", name = ""; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        String firstname = "", surname = "", company = ""; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$

        calledstr = calledInput;
        if (!calledInput.startsWith("SIP")) { //$NON-NLS-1$
            PhoneNumber called = new PhoneNumber(calledInput, JFritz
                    .getProperty("option.activateDialPrefix").toLowerCase()
                    .equals("true")
                    && (!(providerInput.indexOf("@") > 0)));
            if (!called.getIntNumber().equals("")) //$NON-NLS-1$
                calledstr = called.getIntNumber();
        }

        providerstr = providerInput;
        if (providerInput.startsWith("SIP")) //$NON-NLS-1$
            providerstr = JFritz.getSIPProviderTableModel().getSipProvider(
                    providerInput, providerInput);

        name = searchNameToPhoneNumber(calledstr);
        String[] nameArray = searchFirstAndLastNameToPhoneNumber(calledstr);
        firstname = nameArray[0];
        surname = nameArray[1];
        company = nameArray[2];

        if (name.equals(""))name = JFritz.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$
        if (firstname.equals("") && surname.equals(""))surname = JFritz.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        if (company.equals(""))company = JFritz.getMessage("unknown"); //$NON-NLS-1$,  //$NON-NLS-2$

        if (calledstr.startsWith("+49"))calledstr = "0" + calledstr.substring(3); //$NON-NLS-1$,  //$NON-NLS-2$

        String outstring = JFritz.getMessage("outgoing_call") + "\n " //$NON-NLS-1$,  //$NON-NLS-2$
                + JFritz.getMessage("to") + " " + calledstr; //$NON-NLS-1$,  //$NON-NLS-2$
        if (!name.equals(JFritz.getMessage("unknown")))outstring += " (" + name + ")\n "; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
        else
            outstring += "\n "; //$NON-NLS-1$
        outstring += JFritz.getMessage("through_provider") + " " + providerstr; //$NON-NLS-1$,  //$NON-NLS-2$

        JFritz.infoMsg(outstring);

        if (JFritzUtils.parseBoolean(JFritz.getProperty("option.playSounds", //$NON-NLS-1$
                "true"))) { //$NON-NLS-1$
            JFritz.playSound(JFritz.getCallSound());
        }

        // z.Z. noch deaktiviert
        if (false && JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.startExternProgram", "false"))) { //$NON-NLS-1$,  //$NON-NLS-2$
            String programString = JFritz.getProperty("option.externProgram", //$NON-NLS-1$
                    ""); //$NON-NLS-1$

            programString = programString.replaceAll("%Number", providerstr); //$NON-NLS-1$
            programString = programString.replaceAll("%Name", name); //$NON-NLS-1$
            programString = programString.replaceAll("%Called", calledstr); //$NON-NLS-1$
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
                        toReplace = toReplace.replaceAll("\\(", "\\\\("); //$NON-NLS-1$,  //$NON-NLS-2$
                        toReplace = toReplace.replaceAll("\\)", "\\\\)"); //$NON-NLS-1$,  //$NON-NLS-2$
                        String toEncode = m.group(1);
                        programString = programString.replaceAll(toReplace,
                                URLEncoder.encode(toEncode, "UTF-8")); //$NON-NLS-1$
                    }
                } catch (UnsupportedEncodingException uee) {
                    Debug.err("JFritz.class: UnsupportedEncodingException: " //$NON-NLS-1$
                            + uee.toString());
                }
            }

            if (programString.equals("")) { //$NON-NLS-1$
                Debug.errDlg(JFritz.getMessage("no_external_program") //$NON-NLS-1$
                        + programString);
                return;
            }
            Debug.msg("Starting external Program: " + programString); //$NON-NLS-1$
            try {
                Runtime.getRuntime().exec(programString);
            } catch (IOException e) {
                Debug.errDlg(JFritz.getMessage("not_external_program_start") //$NON-NLS-1$
                        + programString);
                Debug.err(e.toString());
            }
        }
    }
}
