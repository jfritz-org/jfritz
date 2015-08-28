package de.moonflower.jfritz.monitoring;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.callmonitor.CallMonitorListener;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class CurrentCallsTable extends AbstractTableModel implements
	CallMonitorListener {
	private final static Logger log = Logger.getLogger(CurrentCallsTable.class);

    private static final long serialVersionUID = 1;
	protected MessageProvider messages = MessageProvider.getInstance();

    private final String columnNames[] = { messages.getMessage("type"), messages.getMessage("date"),
    	    messages.getMessage("callbycall"), messages.getMessage("number"),
    		messages.getMessage("name"),	messages.getMessage("port"),
    		messages.getMessage("route"), messages.getMessage("comment")};

    private Vector<Call> currentCalls;

    public CurrentCallsTable(){
    	currentCalls = new Vector<Call>(4);
    	JFritz.getCallMonitorList().addCallMonitorListener(this);
    }

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return currentCalls.size();
	}

	public Object getValueAt(int arg0, int arg1) {
		if(currentCalls.size() <= arg0)
			return null;
		Call call = currentCalls.get(arg0);
		switch (arg1){
			case 0: return call.getCalltype();
			case 1: return call.getCalldate();
			case 2: if(call.getPhoneNumber() != null)
						return call.getPhoneNumber().getCallByCall();
					return null;
			case 3: return call.getPhoneNumber();
			case 4: return JFritz.getPhonebook().findPerson(call);
			case 5: return call.getPort();
			case 6: return call.getRoute();
			case 7: return null;
		}

		//default junk, so code compiles cleanly
		return null;
	}

    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     * Method part of the interface CallMonitorListener
     */
    public void pendingCallIn(Call call){
    	if (JFritzUtils.parseBoolean(
    			PropertyProvider.getInstance().getProperty(
    					"option.callmonitor.monitorTableIncomingCalls"))) {
    		currentCalls.add(call);
    		fireTableDataChanged();
    	}
    }

    /**
     * Method part of the interface CallMonitorListener
     */
    public void establishedCallIn(Call call){
    	log.info("establishedCallIn was called");

    }

    /**
     * Method part of the interface CallMonitorListener
     */
    public void pendingCallOut(Call call){
    	if (JFritzUtils.parseBoolean(
    			PropertyProvider.getInstance().getProperty(
    					"option.callmonitor.monitorTableOutgoingCalls"))) {
	    	currentCalls.add(call);
	    	fireTableDataChanged();
    	}
    }

    /**
     * Method part of the interface CallMonitorListener
     */
    public void establishedCallOut(Call call){
    	log.info("establishedCallOut was called");

    }

    /**
     * Method part of the interface CallMonitorListener
     */
    public void endOfCall(Call call){
    	if (call != null) {
	    	Vector<Call> callsToDelete = new Vector<Call>();
	    	//search through the table for the call
	    	for(int i=0; i < currentCalls.size(); i++){
	    		if(call.equals(currentCalls.get(i))){
	    			callsToDelete.add(currentCalls.get(i));
	    		}
	    	}
	    	// remove calls
	    	for (int i=0; i<callsToDelete.size(); i++) {
	    		currentCalls.remove(callsToDelete.get(i));
	    	}
	    	fireTableDataChanged();
    	}
    }

    public String toString() {
    	return getClass().getSimpleName();
    }
}
