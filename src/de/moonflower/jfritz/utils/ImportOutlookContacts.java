// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name: importContacts.java

package de.moonflower.jfritz.utils;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.*;


public class ImportOutlookContacts extends JDialog implements ActionListener,
        Runnable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public Dispatch init() {
        outlookElements.addElement("FirstName"); //$NON-NLS-1$
        outlookElements.addElement("LastName"); //$NON-NLS-1$
        outlookElements.addElement("MiddleName"); //$NON-NLS-1$
        outlookElements.addElement("FullName"); //$NON-NLS-1$
        outlookElements.addElement("HomeAddressStreet"); //$NON-NLS-1$
        outlookElements.addElement("HomeAddressPostalCode"); //$NON-NLS-1$
        outlookElements.addElement("HomeAddressCity"); //$NON-NLS-1$
        outlookElements.addElement("BusinessTelephoneNumber"); //$NON-NLS-1$
        outlookElements.addElement("Business2TelephoneNumber"); //$NON-NLS-1$
        outlookElements.addElement("HomeTelephoneNumber"); //$NON-NLS-1$
        outlookElements.addElement("Home2TelephoneNumber"); //$NON-NLS-1$
        outlookElements.addElement("PrimaryTelephoneNumber"); //$NON-NLS-1$
        outlookElements.addElement("MobileTelephoneNumber"); //$NON-NLS-1$
        outlookElements.addElement("CarTelephoneNumber"); //$NON-NLS-1$
        outlookElements.addElement("RadioTelephoneNumber"); //$NON-NLS-1$
        outlookElements.addElement("CallbackTelephoneNumber"); //$NON-NLS-1$
        outlookElements.addElement("AssistantTelephoneNumber"); //$NON-NLS-1$
        outlookElements.addElement("CompanyMainTelephoneNumber"); //$NON-NLS-1$
        outlookElements.addElement("OtherTelephoneNumber"); //$NON-NLS-1$
        outlookElements.addElement("Categories"); //$NON-NLS-1$
        ActiveXComponent ol = new ActiveXComponent("Outlook.Application"); //$NON-NLS-1$
        Dispatch olo = ol.getObject();
        String olVersion = Dispatch.get(olo, "Version").toString(); //$NON-NLS-1$
        if (olVersion.startsWith("11")) { //$NON-NLS-1$
            outlookElements.addElement("HasPicture"); //$NON-NLS-1$
        }
        Dispatch myNamespace = Dispatch.call(olo, "GetNamespace", "MAPI") //$NON-NLS-1$,  //$NON-NLS-2$
                .toDispatch();
        Dispatch myFolder = Dispatch.call(myNamespace, "GetDefaultFolder", //$NON-NLS-1$
                new Integer(10)).toDispatch();
        return myFolder;
    }

    public ImportOutlookContacts(JFritz jfritz) {
        super(jfritz.getJframe(), JFritz.getMessage("import_contacts_outlook")); //$NON-NLS-1$
        outlookElements = new Vector();
        // contactPics = "resources/images/contacts/";
        this.jfritz = jfritz;
    }

    public void run() {
        Debug.msg("Show Outlook-Import-Dialog"); //$NON-NLS-1$
        toFront();
        setSize(400, 500);
        this.getContentPane().setLayout(null);
        setBackground(Color.white);
        JPanel jPanel = new JPanel();
        jPanel.setBounds(0, 0, 400, 500);
        jPanel.setLayout(null);
        JLabel jLab = new JLabel(JFritz.getMessage("importing_outlook_contacts")); //$NON-NLS-1$
        jLab.setFont(new Font(null, 1, 22));
        jLab.setBounds(10, 0, 380, 80);
        jPanel.add(jLab);
        Dispatch myFolder = init();
        Dispatch items = Dispatch.get(myFolder, "Items").toDispatch(); //$NON-NLS-1$
        int count = Dispatch.call(items, "Count").toInt(); //$NON-NLS-1$
        JPanel jPan = new JPanel(new GridLayout(count, 1));
        jPan.setBackground(Color.white);
        JScrollPane jsp = new JScrollPane(jPan);
        jsp.setBounds(20, 80, 350, 325);
        jPanel.add(jsp);
        getContentPane().add(jPanel);
        setLocationRelativeTo(jfritz.getJframe());
        setVisible(true);
        Debug.msg("Importing..."); //$NON-NLS-1$
        for (int i = 1; i <= count; i++) {
            boolean hasTel = false;
            Dispatch item = Dispatch.call(items, "Item", new Integer(i)) //$NON-NLS-1$
                    .toDispatch();
            Person newContact = new Person();
            for (oElements = outlookElements.elements(); oElements
                    .hasMoreElements();)
                try {
                    String strName = oElements.nextElement().toString();
                    String strValue = Dispatch.get(item, strName).toString();
                    if (strName.equals("FullName")) { //$NON-NLS-1$
                        jPan.add(new JLabel("  " + strValue)); //$NON-NLS-1$
                        jPan.updateUI();
                    }
                    if (!strValue.equals("") //$NON-NLS-1$
                            && ((strName.equals("BusinessTelephoneNumber") 			 //$NON-NLS-1$
                                    || strName.equals("Business2TelephoneNumber") 	 //$NON-NLS-1$
                                    || strName.equals("HomeTelephoneNumber")  		 //$NON-NLS-1$
                                    || strName.equals("Home2TelephoneNumber") 		 //$NON-NLS-1$
                                    || strName.equals("MobileTelephoneNumber") 		 //$NON-NLS-1$
                                    || strName.equals("CarTelephoneNumber") 		 //$NON-NLS-1$
                                    || strName.equals("RadioTelephoneNumber") 		 //$NON-NLS-1$
                                    || strName.equals("PrimaryTelephoneNumber")  	 //$NON-NLS-1$
                                    || strName.equals("CallbackTelephoneNumber")  	 //$NON-NLS-1$
                                    || strName.equals("AssistantTelephoneNumber")  	 //$NON-NLS-1$
                                    || strName.equals("CompanyMainTelephoneNumber")  //$NON-NLS-1$
                                    || strName.equals("OtherTelephoneNumber")))) {   //$NON-NLS-1$
                        hasTel = true;
                    }
                    /**
                     * if (strName.equals("HasPicture") &&
                     * strValue.equals("-1")) contact.addContent((new
                     * Element("Picture")) .addContent(new
                     * Text(getContactPic(item))));
                     */
                    if (strName.equals("FirstName")) {  				//$NON-NLS-1$
                        newContact.setFirstName(strValue);
                    } else if (strName.equals("LastName")) {			//$NON-NLS-1$
                        newContact.setLastName(strValue);
                    } else if (strName.equals("MiddleName")) { 			//$NON-NLS-1$
                        newContact.setCompany(strValue);
                    } else if (strName.equals("HomeAddressStreet")) {   //$NON-NLS-1$
                        newContact.setStreet(strValue);
                    } else if (strName.equals("HomeAddressPostalCode")) { //$NON-NLS-1$
                        newContact.setPostalCode(strValue);
                    } else if (strName.equals("HomeAddressCity")) { //$NON-NLS-1$
                        newContact.setCity(strValue);
                    } else if (strName.equals("HomeTelephoneNumber") //$NON-NLS-1$
                            && (!strValue.equals(""))) { //$NON-NLS-1$
                        newContact.addNumber(new PhoneNumber(strValue, "home")); //$NON-NLS-1$
                    } else if (strName.equals("Home2TelephoneNumber") //$NON-NLS-1$
                            && (!strValue.equals(""))) { //$NON-NLS-1$
                        newContact.addNumber(new PhoneNumber(strValue, "home")); //$NON-NLS-1$
                    } else if (strName.equals("PrimaryTelephoneNumber") //$NON-NLS-1$
                            && (!strValue.equals(""))) { //$NON-NLS-1$
                        newContact.addNumber(new PhoneNumber(strValue, "home")); //$NON-NLS-1$
                    } else if (strName.equals("MobileTelephoneNumber") //$NON-NLS-1$
                            && (!strValue.equals(""))) { //$NON-NLS-1$
                        newContact
                                .addNumber(new PhoneNumber(strValue, "mobile")); //$NON-NLS-1$
                    } else if (strName.equals("BusinessTelephoneNumber") //$NON-NLS-1$
                            && (!strValue.equals(""))) { //$NON-NLS-1$
                        newContact.addNumber(new PhoneNumber(strValue,
                                "business")); //$NON-NLS-1$
                    } else if (strName.equals("Business2TelephoneNumber") //$NON-NLS-1$
                            && (!strValue.equals(""))) { //$NON-NLS-1$
                        newContact.addNumber(new PhoneNumber(strValue,
                                "business")); //$NON-NLS-1$
                    } else if (strName.equals("RadioTelephoneNumber") //$NON-NLS-1$
                            && (!strValue.equals(""))) { //$NON-NLS-1$
                        newContact
                                .addNumber(new PhoneNumber(strValue, "other")); //$NON-NLS-1$
                    } else if (strName.equals("CarTelephoneNumber") //$NON-NLS-1$
                            && (!strValue.equals(""))) { //$NON-NLS-1$
                        newContact
                                .addNumber(new PhoneNumber(strValue, "other")); //$NON-NLS-1$
                    } else if (strName.equals("CallbackTelephoneNumber") //$NON-NLS-1$
                            && (!strValue.equals(""))) { //$NON-NLS-1$
                        newContact
                                .addNumber(new PhoneNumber(strValue, "other")); //$NON-NLS-1$
                    } else if (strName.equals("AssistantTelephoneNumber") //$NON-NLS-1$
                            && (!strValue.equals(""))) { //$NON-NLS-1$
                        newContact
                                .addNumber(new PhoneNumber(strValue, "other")); //$NON-NLS-1$
                    } else if (strName.equals("CompanyMainTelephoneNumber") //$NON-NLS-1$
                            && (!strValue.equals(""))) { //$NON-NLS-1$
                        newContact
                                .addNumber(new PhoneNumber(strValue, "other")); //$NON-NLS-1$
                    } else if (strName.equals("OtherTelephoneNumber") //$NON-NLS-1$
                            && (!strValue.equals(""))) { //$NON-NLS-1$
                        newContact
                                .addNumber(new PhoneNumber(strValue, "other")); //$NON-NLS-1$
                    }

                } catch (Exception exception1) {
                }

            if (hasTel)
                jfritz.getPhonebook().addEntry(newContact);
        }
        Debug.msg("Import done"); //$NON-NLS-1$
        JButton jButton = new JButton(JFritz.getMessage("okay")); //$NON-NLS-1$

        //set default confirm button (Enter)
        this.getRootPane().setDefaultButton(jButton);

        jButton.addActionListener(this);
        jButton.setBounds(160, 425, 80, 25);
        jPanel.add(jButton);
        jPanel.updateUI();
    }

    public String getContactPic(Dispatch item) {
        return ""; //$NON-NLS-1$
    }

    public void actionPerformed(ActionEvent actionevent) {
        dispose();
    }

    private Enumeration oElements;

    private Vector outlookElements;

    final int olFolderContacts = 10;

    private JFritz jfritz;
}
