package de.moonflower.jfritz.utils.reverselookup;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.moonflower.jfritz.struct.ReverseLookupSite;
import de.moonflower.jfritz.utils.Debug;

/**
 *	This class is responsible for parsing the reverseloop.xml file
 * 	Important to note is that both url and name must be filled out!!!
 *
 * @see de.moonflower.jfritz.struct.ReverseLookupSite
 *
 *  created 06.02.07
 *
 * @author brian jensen
 *
 */
public class ReverseLookupXMLHandler extends DefaultHandler{

	String chars, url, name, prefix, country_code, pname, pstreet, pcity, pzipcode;

	Vector<ReverseLookupSite> rls_list;

	int rls_count, ac_length;

	public ReverseLookupXMLHandler() {
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
		if (eName.equals("country")) { //$NON-NLS-1$
			rls_count = 0;

			//create a new linked list for the country
			rls_list = new Vector<ReverseLookupSite>(2);

		//if we have a new website tag, clear the previous url and name attributes
		}else if (eName.equals("website")){
			url = "";
			name = "";
			prefix ="";
			ac_length = 0;

		//if we have a new entry tag, clear the previous pattern data
		}else if (eName.equals("entry")){
			pname = "";
			pstreet = "";
			pcity = "";
			pzipcode = "";

		}

		//process all attributes
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				String aName = attrs.getLocalName(i); // Attr name
				if ("".equals(aName)) //$NON-NLS-1$
					aName = attrs.getQName(i);

				if(eName.equals("reverselookup") && aName.equals("version"))
					Debug.msg("Loading reverselookup.xml version "+attrs.getValue(i));
				//this adds the country code
				if (eName.equals("country") && aName.equals("code")) { //$NON-NLS-1$,  //$NON-NLS-2$
					country_code = attrs.getValue(i);
				}else if(eName.equals("website")){
					if(aName.equals("url"))
						url = attrs.getValue(i);
					else if(aName.equals("name"))
						name = attrs.getValue(i);
					else if(aName.equals("prefix"))
						prefix = attrs.getValue(i);
					else if(aName.equals("areacode"))
						ac_length = Integer.parseInt(attrs.getValue(i));

					//add a new reverselookup site to the list
					if(i == attrs.getLength() -1){
						Debug.msg("Adding website: "+url+" for "+country_code);
						rls_list.add(new ReverseLookupSite(url, name, prefix, ac_length));

					}
				}
			}
		}
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {

		if (qName.equals("name")) { //$NON-NLS-1$
			pname = chars;
		} else if (qName.equals("street")) { //$NON-NLS-1$
			pstreet = chars;
		}else if(qName.equals("city")){
			pcity = chars;
		}else if(qName.equals("zipcode")){
			pzipcode = chars;
		} else if (qName.equals("entry")) { //$NON-NLS-1$

			//add the patterns to the new object
			rls_list.get(rls_count).addEntry(pname, pstreet, pcity, pzipcode);
			Debug.msg("adding patterns for: "+name);
			Debug.msg("Pattern name: "+ pname);
			Debug.msg("Pattern street: "+ pstreet);
			Debug.msg("Pattern city: "+pcity);
			Debug.msg("Pattern zipcode: "+pzipcode);

		}else if(qName.equals("website")){
			rls_count++;

		} else if (qName.equals("country")) { //$NON-NLS-1$
			if(!country_code.equals("")){

				Debug.msg(rls_count+" websites added for "+country_code);
				//Add the country to the hashmap with the reveselookupsite list
				ReverseLookup.addReverseLookupSites(country_code, rls_list);
			}
		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		chars += new String(buf, offset, len);
	}
}
