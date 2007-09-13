package de.moonflower.jfritz.network;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.moonflower.jfritz.callerlist.filter.CallFilter;
import de.moonflower.jfritz.network.ClientLoginsTableModel;
import de.moonflower.jfritz.network.Login;
import de.moonflower.jfritz.utils.Encryption;

public class ClientLoginsXMLHandler extends DefaultHandler{

	String chars, username, password;

	boolean allowCallListAdd, allowCallListUpdate, allowCallListRemove, allowPhoneBookAdd,
			allowPhoneBookUpdate, allowPhoneBookRemove, allowLookup, allowGetCallList;

	public ClientLoginsXMLHandler()  {
		super();
	}

	public void startDocument() throws SAXException {
	}

	public void endDocument() throws SAXException {
	}

	public void startElement(String namespaceURI, String lName, String qName,
			Attributes attrs) throws SAXException {

		String eName = lName;

		if ("".equals(eName)) //$NON-NLS-1$
			eName = qName;

		//	Important to clear buffer :)
		chars = "";  //$NON-NLS-1$


		//if we have a beginning country tag create a new reverselookupsite list
		if (eName.equals("client")) { //$NON-NLS-1$
			username = "";
			password = "";
			allowCallListAdd = false;
			allowCallListUpdate = false;
			allowCallListRemove = false;
			allowPhoneBookAdd = false;
			allowPhoneBookUpdate = false;
			allowPhoneBookRemove = false;
			allowLookup = false;
			allowGetCallList = false;

		}
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {

		if (qName.equals("user")) { //$NON-NLS-1$
			username = chars;
		} else if (qName.equals("password")) { //$NON-NLS-1$
			password = Encryption.decrypt(chars);
		}else if(qName.equals("allowCallListAdd")){
			allowCallListAdd = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowCallListUpdate")){
			allowCallListUpdate = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowCallListRemove")){
			allowCallListRemove = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowPhoneBookAdd")){
			allowPhoneBookAdd = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowPhoneBookUpdate")){
			allowPhoneBookUpdate = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowPhoneBookRemove")){
			allowPhoneBookRemove = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowDoLookup")){
			allowLookup = Boolean.parseBoolean(chars);
		}else if(qName.equals("allowGetCallList")){
			allowGetCallList = Boolean.parseBoolean(chars);
		} else if (qName.equals("client")) { //$NON-NLS-1$
			//add login settings to the list
			Login login = new Login(username, password, allowCallListAdd, allowCallListUpdate,
					allowCallListRemove, allowPhoneBookAdd, allowPhoneBookUpdate,
					allowPhoneBookRemove, allowLookup, allowGetCallList,
					new Vector<CallFilter>(), "");
			ClientLoginsTableModel.addLogin(login);
		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}
}
