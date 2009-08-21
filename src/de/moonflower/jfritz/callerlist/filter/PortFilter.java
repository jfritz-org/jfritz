/*
 * Created on 07.09.2006
 *
 */
package de.moonflower.jfritz.callerlist.filter;

import java.util.Vector;

import de.moonflower.jfritz.struct.Call;

public class PortFilter extends CallFilter {

	private Vector<String> ports = new Vector<String>();

	private static final String type = FILTER_PORT;
	private boolean allSelected = false;
	public PortFilter() {
	}

	public boolean passInternFilter(Call currentCall) {

		if (allSelected)
		{
			return true;
		}

		String port = currentCall.getPort().getName();
//			Debug.msg("route: "+route);
		if (port.equals("")) { //$NON-NLS-1$
			return true;
		}
		if (ports.contains(port))
			return true;
		else
			return false;
	}

	public String toString(){
    	String result="";
    	for(int i =0; i<ports.size();i++){
    		result +=";"+ports.elementAt(i);
    	}
    	if (result.startsWith(";"))
    	{
    		result = result.substring(1);
    	}
    	result = result.trim();
    	return result;
    }

	public void setPorts(Vector<String> ports) {
		this.ports = ports;
		allSelected = false;
	}

	public String getType(){
		return type;
	}

	public PortFilter clone(){
		PortFilter sf = new PortFilter();
		sf.setPorts(this.ports);
		sf.setEnabled(this.isEnabled());
		sf.setInvert(this.isInvert());
		return sf;
	}

	public void setAllSelected() {
		allSelected = true;
		ports.clear();
		ports.add("$ALL$");
	}

	public boolean isAllSelected() {
		return allSelected;
	}
}
