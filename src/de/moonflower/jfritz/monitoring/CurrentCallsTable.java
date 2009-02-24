package de.moonflower.jfritz.monitoring;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.callmonitor.CallMonitorListener;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.utils.Debug;

public class CurrentCallsTable extends AbstractTableModel implements
	CallMonitorListener {

    private static final long serialVersionUID = 1;

    private final String columnNames[] = { Main.getMessage("type"), Main.getMessage("date"),
    	    Main.getMessage("callbycall"), Main.getMessage("number"),
    		Main.getMessage("name"),	Main.getMessage("port"),
    		Main.getMessage("route"), Main.getMessage("comment")};

    private Vector<Call> currentCalls;

    public CurrentCallsTable(){
    	currentCalls = new Vector<Call>(4);
    	JFritz.getCallMonitorList().addCallMonitorListener(this);
    }

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
	/**
	 * Auskommentiert von Rob, finde ich so schöner
	 *	if(currentCalls.size() < 5)
	 *		return currentCalls.size();
	 **/
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
    	Debug.msg("pendingCallIn was called");
    	//Nothing here for now
    	currentCalls.add(call);
    	fireTableDataChanged();

    }

    /**
     * Method part of the interface CallMonitorListener
     */
    public void establishedCallIn(Call call){
    	Debug.msg("establishedCallIn was called");

    }

    /**
     * Method part of the interface CallMonitorListener
     */
    public void pendingCallOut(Call call){
    	//Nothing here for now
    	currentCalls.add(call);
    	fireTableDataChanged();

    }

    /**
     * Method part of the interface CallMonitorListener
     */
    public void establishedCallOut(Call call){
    	Debug.msg("establishedCallOut was called");

    }

    /**
     * Method part of the interface CallMonitorListener
     */
    public void endOfCall(Call call){
    	//search through the table for the call and remove it
    	for(int i=0; i < currentCalls.size(); i++){
    		if(currentCalls.get(i).equals(call)){
    			currentCalls.remove(i);
    			fireTableDataChanged();
    			//fireTableRowsDeleted(i,i);
    			break;
    		}
    	}
    }

}
