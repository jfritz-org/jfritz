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

// Referenced classes of package de.waghoo.fbf:
//            fbfParams, fbfContactTree, fbf_tool

public class ImportOutlookContacts extends JDialog implements ActionListener, Runnable {

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public Dispatch init() {
        outlookElements.addElement("FirstName");
        outlookElements.addElement("LastName");
        outlookElements.addElement("MiddleName");
        outlookElements.addElement("FullName");
        outlookElements.addElement("HomeAddressStreet");
        outlookElements.addElement("HomeAddressPostalCode");
        outlookElements.addElement("HomeAddressCity");
        outlookElements.addElement("BusinessTelephoneNumber");
        outlookElements.addElement("HomeTelephoneNumber");
        outlookElements.addElement("MobileTelephoneNumber");
        outlookElements.addElement("OtherTelephoneNumber");
        outlookElements.addElement("Categories");
        ActiveXComponent ol = new ActiveXComponent("Outlook.Application");
        // Dispatch dsp = new Dispatch();
        Dispatch olo = ol.getObject();
        String olVersion = Dispatch.get(olo, "Version").toString();
        if (olVersion.startsWith("11")) {
            outlookElements.addElement("HasPicture");
        }
        Dispatch myNamespace = Dispatch.call(olo, "GetNamespace", "MAPI")
                .toDispatch();
        Dispatch myFolder = Dispatch.call(myNamespace, "GetDefaultFolder",
                new Integer(10)).toDispatch();
        return myFolder;
    }

    public ImportOutlookContacts(JFritz jfritz) {
        super(jfritz.getJframe(), "Aus Outlook importieren");
        outlookElements = new Vector();
//        contactPics = "resources/images/contacts/";
        this.jfritz = jfritz;
    }

    public void run() {
        toFront();
        setSize(400, 500);
        // java.awt.Toolkit tk = getToolkit();
        this.getContentPane().setLayout(null);
        setBackground(Color.white);
        JPanel jPanel = new JPanel();
        jPanel.setBounds(0, 0, 400, 500);
        jPanel.setLayout(null);
        JLabel jLab = new JLabel("Outlook-Kontakte werden importiert");
        jLab.setFont(new Font(null, 1, 22));
        jLab.setBounds(10, 0, 380, 80);
        jPanel.add(jLab);
        Dispatch myFolder = init();
        Dispatch items = Dispatch.get(myFolder, "Items").toDispatch();
        int count = Dispatch.call(items, "Count").toInt();
        JPanel jPan = new JPanel(new GridLayout(count, 1));
        jPan.setBackground(Color.white);
        JScrollPane jsp = new JScrollPane(jPan);
        jsp.setBounds(20, 80, 350, 325);
        jPanel.add(jsp);
        getContentPane().add(jPanel);
        setLocationRelativeTo(jfritz.getJframe());
        setVisible(true);
        for (int i = 1; i <= count; i++) {
            boolean hasTel = false;
            Dispatch item = Dispatch.call(items, "Item", new Integer(i))
                    .toDispatch();
            Person newContact = new Person();
            for (oElements = outlookElements.elements(); oElements
                    .hasMoreElements();)
                try {
                    String strName = oElements.nextElement().toString();
                    String strValue = Dispatch.get(item, strName).toString();
                    if (strName.equals("FullName")) {
                        jPan.add(new JLabel("  " + strValue));
                        jPan.updateUI();
                    }
                    if (!strValue.equals("")
                            && ((strName.equals("BusinessTelephoneNumber")
                                    || strName.equals("HomeTelephoneNumber")
                                    || strName.equals("MobileTelephoneNumber") || strName
                                    .equals("OtherTelephoneNumber")))) {
                        hasTel = true;
                    }
                    /**
                     * if (strName.equals("HasPicture") &&
                     * strValue.equals("-1")) contact.addContent((new
                     * Element("Picture")) .addContent(new
                     * Text(getContactPic(item))));
                     */
                    if (strName.equals("FirstName")) {
                        newContact.setFirstName(strValue);
                    } else if (strName.equals("LastName")) {
                        newContact.setLastName(strValue);
                    } else if (strName.equals("MiddleName")) {
                        newContact.setCompany(strValue);
                    } else if (strName.equals("HomeAddressStreet")) {
                        newContact.setStreet(strValue);
                    } else if (strName.equals("HomeAddressPostalCode")) {
                        newContact.setPostalCode(strValue);
                    } else if (strName.equals("HomeAddressCity")) {
                        newContact.setCity(strValue);
                    } else if (strName.equals("HomeTelephoneNumber")
                            && (!strValue.equals(""))) {
                        newContact.addNumber(new PhoneNumber(strValue, "home"));
                    } else if (strName.equals("MobileTelephoneNumber")
                            && (!strValue.equals(""))) {
                        newContact
                                .addNumber(new PhoneNumber(strValue, "mobile"));
                    } else if (strName.equals("BusinessTelephoneNumber")
                            && (!strValue.equals(""))) {
                        newContact.addNumber(new PhoneNumber(strValue,
                                "business"));
                    } else if (strName.equals("OtherTelephoneNumber")
                            && (!strValue.equals(""))) {
                        newContact
                                .addNumber(new PhoneNumber(strValue, "other"));
                    }

                } catch (Exception exception1) {
                }

            if (hasTel)
                jfritz.getPhonebook().addEntry(newContact);
        }

        JButton jButton = new JButton(" OK ");
        jButton.addActionListener(this);
        jButton.setBounds(160, 425, 80, 25);
        jPanel.add(jButton);
        jPanel.updateUI();
    }

    public String getContactPic(Dispatch item) {
        return "";
        /**
         * ROB Dispatch att = Dispatch.get(item, "Attachments").toDispatch();
         * int attCnt = Dispatch.get(att, "Count").toInt(); String AttName =
         * null; for (int x = 1; x <= attCnt; x++) { Dispatch attItem =
         * Dispatch.call(att, "Item", new Integer(x)) .toDispatch(); AttName =
         * Dispatch.get(attItem, "FileName").toString(); if
         * (AttName.equals("ContactPicture.jpg")) { //Class self = getClass();
         * Dispatch.call(attItem, "SaveasFile", (new StringBuilder(String
         * .valueOf(fPar.thisPath))).append(contactPics).append(
         * Dispatch.get(item, "FullName").toString()).append(
         * ".jpg").toString()); } }
         *
         * //Class self = getClass(); return (new
         * StringBuilder(String.valueOf(fPar.thisPath))).append(
         * contactPics).append(Dispatch.get(item, "FullName").toString())
         * .toString();
         */
    }

    public void actionPerformed(ActionEvent actionevent) {
        dispose();
    }

    private Enumeration oElements;

    private Vector outlookElements;

//    private String contactPics;

    final int olFolderContacts = 10;

    private JFritz jfritz;
}
