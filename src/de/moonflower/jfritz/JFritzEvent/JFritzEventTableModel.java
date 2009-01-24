package de.moonflower.jfritz.JFritzEvent;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import de.moonflower.jfritz.JFritzEvent.struct.JFritzEventAction;

public class JFritzEventTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -2771311258279507918L;

	private String[] columnNames = {"EVENTNAME", "ACTIVE"};

	private Vector<JFritzEventAction> events;

	public JFritzEventTableModel() {
		super();
		events = new Vector<JFritzEventAction>();
		for ( int i=0; i<JFritzEventDispatcher.getEventCount(); i++) {
			if ( JFritzEventDispatcher.getEvent(i).isVisible() )
				events.add(JFritzEventDispatcher.getEvent(i));
		}
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return events.size();
	}

    public String getColumnName(int col) {
        return columnNames[col]; //FIXME: I18N
    }

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0)
		{
			return events.get(rowIndex).getDescription();
		}
		else if (columnIndex == 1)
		{
			return events.get(rowIndex).isActive();
		}
		return "";
	}

	public void addEvent(JFritzEventAction eventAction) {
		events.add(eventAction);
	}

	public void removeEvent(JFritzEventAction eventAction) {
		events.remove(eventAction);
	}

	public JFritzEventAction getEvent( int i) {
		return events.get(i);
	}
	public void setEvent(int i, JFritzEventAction eventAction) {
		events.set(i, eventAction);
	}
}
