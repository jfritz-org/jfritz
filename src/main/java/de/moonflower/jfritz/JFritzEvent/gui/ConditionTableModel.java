package de.moonflower.jfritz.JFritzEvent.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;

import de.moonflower.jfritz.JFritzEvent.JFritzEventDispatcher;
import de.moonflower.jfritz.JFritzEvent.struct.ConditionObject;
import de.moonflower.jfritz.JFritzEvent.struct.JFritzEventAction;
import de.moonflower.jfritz.JFritzEvent.struct.JFritzEventCondition;
import de.moonflower.jfritz.JFritzEvent.struct.JFritzEventParameter;

public class ConditionTableModel extends AbstractTableModel implements ActionListener {

	private static final long serialVersionUID = 7606494150911817254L;

	private String[] columnNames = { "parameter", "condition", "value",
			"addbutton", "deletebutton" };

	private JFritzEventAction eventAction;

	public ConditionTableModel(JFritzEventAction eventAction) {
		super();
		this.eventAction = eventAction;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return eventAction.getConditionListSize();
	}

	public boolean isCellEditable(int row, int col) {
		return true;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		JFritzEventCondition ec = eventAction.getCondition(rowIndex);
		switch (columnIndex) {
		case 0:
			return ec.getParameter();
		case 1:
			return ec.getCondition();
		case 2:
			return ec.getValue();
		case 3:
			JButton addButton = new JButton("+");
			addButton.setActionCommand("add_new_condition");
			addButton.addActionListener(this);
			addButton.setName(Integer.toString(rowIndex));
			return addButton;
		case 4:
			JButton removeButton = new JButton("-");
			removeButton.setActionCommand("remove_condition");
			removeButton.addActionListener(this);
			removeButton.setName(Integer.toString(rowIndex));
			if ( eventAction.getConditionListSize() == 1)
				removeButton.setEnabled(false);
			else removeButton.setEnabled(true);
			return removeButton;
		}
		return null;
	}

	public void setValueAt(Object value, int row, int col) {
		switch (col) {
		case 0: // parameter
			eventAction.getCondition(row).setParameter(
					((JFritzEventParameter) value));
			break;
		case 1: // condition
			eventAction.getCondition(row).setCondition(
					((ConditionObject) value).getConditionID());
			break;
		case 2:
			eventAction.getCondition(row).setValue((String) value);
			break;
		}
		fireTableDataChanged();
	}

	public void actionPerformed(ActionEvent e) {
		int row = 0;
		if ( e.getSource().getClass().equals(JButton.class)) {
			row = Integer.parseInt(((JButton) e.getSource()).getName());
		}

		if ( e.getActionCommand().equals("add_new_condition")) {
			eventAction.addCondition(row+1, JFritzEventDispatcher.createNewCondition(eventAction.getEvent()));
		} else if ( e.getActionCommand().equals("remove_condition")){
			eventAction.removeCondition(row);
		}
		fireTableDataChanged();
	}

}