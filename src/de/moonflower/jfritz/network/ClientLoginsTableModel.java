package de.moonflower.jfritz.network;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

import java.awt.Component;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;


import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.callerlist.filter.*;
import de.moonflower.jfritz.dialogs.config.PermissionsDialog;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;
import de.moonflower.jfritz.utils.JFritzUtils;

public class ClientLoginsTableModel extends AbstractTableModel{

	public static final long serialVersionUID = 100;

	private static Vector<Login> clientLogins = new Vector<Login>();

	public ClientLoginsTableModel(){
		super();
	}

    private final String columnNames[] = { Main.getMessage("username"), Main.getMessage("password"), //$NON-NLS-1$,  //$NON-NLS-2$
    		Main.getMessage("permissions"), Main.getMessage("callerlist_filters"),
    		Main.getMessage("phonebook_filters")}; //$NON-NLS-1$,  //$NON-NLS-2$

	public int getColumnCount(){
		return columnNames.length;
	}

	public int getRowCount(){
		return clientLogins.size();
	}

	public boolean isCellEditable(int row, int col){

		return true;
	}

	public Object getValueAt(int row, int column){

		Login login = clientLogins.elementAt(row);
		switch(column){
			case 0:
				return login.user;
			case 1:
				return login.password;
			case 2:
				return Main.getMessage("set");
			case 3:
				return Main.getMessage("set");
			case 4:
				return Main.getMessage("set");
			default:
				return "";
		}

	}

	public void setValueAt(Object value, int row, int column){
		Login login = clientLogins.elementAt(row);
		switch(column){
		case 0:
			login.user = value.toString();
			break;
		case 1:
			login.password = value.toString();
			break;
		case 2:
			if(value instanceof JDialog){
				PermissionsDialog dialog = new PermissionsDialog((JDialog) value,login);
				dialog.showConfigDialog();
				dialog.dispose();
			}
			break;
		case 3:
		case 4:
			if(value instanceof JDialog){
				JOptionPane.showMessageDialog((Component) value, "Function not yet implemented!!");
			}
		}
	}

    public String getColumnName(int column) {
        return columnNames[column];
    }

    public static Vector<Login> getClientLogins(){
    	return clientLogins;
    }

    public static void saveToXMLFile(String filename){
    	Debug.msg("Saving to file " + filename); //$NON-NLS-1$
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filename);
			PrintWriter pw = new PrintWriter(fos);
			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
			pw.println("<clientsettings>"); //$NON-NLS-1$
			for(Login login: clientLogins){
				pw.println("\t<client>");
				pw.println("\t\t<user>"+login.user+"</user>");
				pw.println("\t\t<password>"+Encryption.encrypt(login.password)+"</password>");
				pw.println("\t\t<allowCallListAdd>"+login.allowAddList+"</allowCallListAdd>");
				pw.println("\t\t<allowCallListUpdate>"+login.allowUpdateList+"</allowCallListUpdate>");
				pw.println("\t\t<allowCallListRemove>"+login.allowRemoveList+"</allowCallListRemove>");
				pw.println("\t\t<allowPhoneBookAdd>"+login.allowAddBook+"</allowPhoneBookAdd>");
				pw.println("\t\t<allowPhoneBookUpdate>"+login.allowUpdateBook+"</allowPhoneBookUpdate>");
				pw.println("\t\t<allowPhoneBookRemove>"+login.allowRemoveBook+"</allowPhoneBookRemove>");
				pw.println("\t\t<allowDoLookup>"+login.allowLookup+"</allowDoLookup>");
				pw.println("\t\t<allowGetCallList>"+login.allowGetList+"</allowGetCallList>");

				//print all the filters set for this client
				for(CallFilter callFilter: login.callFilters){

						//save the basic state of the filter
					pw.println("\t\t<callfilter type=\""+ callFilter.getType()+"\">");
					pw.println("\t\t\t<enabled>"+callFilter.isEnabled()+"</enabled>");
					pw.println("\t\t\t<inverted>"+callFilter.isInvert()+"</inverted>");

					//process the filters that use extra information
					if(callFilter.getType().equals(CallFilter.FILTER_CALLBYCALL)){
						pw.print("\t\t\t<callbycall>");
						CallByCallFilter cbcf = (CallByCallFilter) callFilter;
						Vector<String> cbc = cbcf.getCallbyCallProviders();

						for(String provider: cbc)
							pw.print(provider+" ");

						pw.println("</callbycall>");

					}else if(callFilter.getType().equals(CallFilter.FILTER_DATE)){

						DateFilter df = (DateFilter) callFilter;
						//first check if this is special date filter
						if(df.specialType.equals(CallFilter.THIS_DAY) ||
								df.specialType.equals(CallFilter.THIS_MONTH) ||
								df.specialType.equals(CallFilter.THIS_WEEK) ||
								df.specialType.equals(CallFilter.LAST_DAY) ||
								df.specialType.equals(CallFilter.LAST_MONTH) ||
								df.specialType.equals(CallFilter.LAST_WEEK)){

							pw.println("\t\t\t<special>"+df.specialType+"</special>");

						}else{

							DateFormat datef = new SimpleDateFormat("dd.MM.yy HH:mm");

							pw.println("\t\t\t<start>"+datef.format(df.getStartDate())+"</start>");
							pw.println("\t\t\t<end>"+datef.format(df.getEndDate())+"</end>");
						}

					}else if(callFilter.getType().equals(CallFilter.FILTER_SEARCH)){
						SearchFilter sf = (SearchFilter) callFilter;
						pw.println("\t\t\t<text>"+JFritzUtils.convertSpecialChars(sf.getSearchString())
								+"</text>");

					}else if(callFilter.getType().equals(CallFilter.FILTER_SIP)){
						SipFilter sf = (SipFilter) callFilter;
						pw.println("\t\t\t<providers>"+JFritzUtils.convertSpecialChars(sf.toString())
								+"</providers>");
					}

					pw.println("\t\t</callfilter>");
				}

				pw.println("\t</client>");
			}

			pw.println("</clientsettings>"); //$NON-NLS-1$
			pw.close();
		} catch (FileNotFoundException e) {
			Debug.err("Could not write " + filename + "!"); //$NON-NLS-1$,  //$NON-NLS-2$
		}
    }

    public static void loadFromXMLFile(String filename){
    	try {
			Debug.msg("loading the client settings xml file: "+filename);

			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(false);
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();

			reader.setErrorHandler(new ErrorHandler() {
				public void error(SAXParseException x) throws SAXException {
					// Debug.err(x.toString());
					throw x;
				}

				public void fatalError(SAXParseException x) throws SAXException {
					// Debug.err(x.toString());
					throw x;
				}

				public void warning(SAXParseException x) throws SAXException {
					// Debug.err(x.toString());
					throw x;
				}
			});

			reader.setContentHandler(new ClientLoginsXMLHandler());
			reader.parse(new InputSource(new FileInputStream(filename)));

		} catch (ParserConfigurationException e) {
			Debug.err("Error with ParserConfiguration!"); //$NON-NLS-1$
		} catch (SAXException e) {
			Debug.err("Error on parsing client login settings!"); //$NON-NLS-1$,  //$NON-NLS-2$
			Debug.err(e.toString());
			e.printStackTrace();

			if (e.getLocalizedMessage().startsWith("Relative URI") //$NON-NLS-1$
					|| e.getLocalizedMessage().startsWith(
							"Invalid system identifier")) { //$NON-NLS-1$
				Debug.err(e.getLocalizedMessage());
			}
		} catch (IOException e) {
			Debug.err("Could not read client login settings! No settings loaded!"); //$NON-NLS-1$,  //$NON-NLS-2$
		}
    }

    public static void addLogin(Login login){
    	clientLogins.add(login);
    }

    public static void removeLogin(int index){
    	clientLogins.remove(index);
    }

}
