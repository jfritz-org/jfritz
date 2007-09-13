package de.moonflower.jfritz.JFritzEvent.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;

import de.moonflower.jfritz.JFritzEvent.JFritzEventDispatcher;
import de.moonflower.jfritz.JFritzEvent.actions.JFritzAction;
import de.moonflower.jfritz.JFritzEvent.struct.JFritzEventAction;

public class ActionTableModel extends AbstractTableModel implements ActionListener {

	private static final long serialVersionUID = -8874564805475641703L;

	private String[] columnNames = {"action","description","editbutton","addbutton","removebutton"};

	private JFritzEventAction eventAction;

	private JDialog parent;

	public ActionTableModel(JDialog parent, JFritzEventAction eventAction) {
		super();
		this.parent = parent;
		this.eventAction = eventAction;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return eventAction.getActionListSize();
	}

	public boolean isCellEditable(int row, int col) {
		return true;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		JFritzAction action = eventAction.getAction(rowIndex);
		switch ( columnIndex ) {
			case 0: return action.getName();
			case 1: return action.getDescription();
			case 2: JButton editButton = new JButton("edit");
				editButton.setActionCommand("edit_action");
				editButton.addActionListener(this);
				editButton.setName(Integer.toString(rowIndex));
				return editButton;
			case 3:
				JButton addButton = new JButton("+");
				addButton.setActionCommand("add_new_action");
				addButton.addActionListener(this);
				addButton.setName(Integer.toString(rowIndex));
				return addButton;
			case 4:
				JButton removeButton = new JButton("-");
				removeButton.setActionCommand("remove_action");
				removeButton.addActionListener(this);
				removeButton.setName(Integer.toString(rowIndex));
				if ( eventAction.getActionListSize() == 1)
					removeButton.setEnabled(false);
				else removeButton.setEnabled(true);
				return removeButton;
		}
		return null;
	}

	public void setValueAt(Object value, int row, int col) {
 		switch ( col ) {
		case 0: // action
			eventAction.setAction(row, ((JFritzAction) value).clone());
			break;
		case 1: // description
			eventAction.getAction(row).setDescription((String) value);
			break;
		}
		fireTableDataChanged();
    }

	public void actionPerformed(ActionEvent e) {
		int row = 0;
		if ( e.getSource().getClass().equals(JButton.class)) {
			row = Integer.parseInt(((JButton) e.getSource()).getName());
		}

		if ( e.getActionCommand().equals("edit_action")) {
			// Show config dialog
			JPanel configPanel = eventAction.getAction(row).getConfigPanel();
			if ( configPanel != null) {
				JFritzAction editAction =  eventAction.getAction(row).clone();
				JFritzEditActionGUI editActionGUI = new JFritzEditActionGUI(parent, eventAction.getEvent(), editAction);
				if (editActionGUI.showDialog()) {
					eventAction.setAction(row, editAction);
				}
			}
		} else if ( e.getActionCommand().equals("add_new_action")) {
			eventAction.addAction(row+1, JFritzEventDispatcher.createNewAction());
		} else if ( e.getActionCommand().equals("remove_action")){
			eventAction.removeAction(row);
		}
		fireTableDataChanged();
	}

}