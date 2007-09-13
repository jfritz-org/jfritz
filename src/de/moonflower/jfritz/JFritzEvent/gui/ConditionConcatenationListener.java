package de.moonflower.jfritz.JFritzEvent.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;

import de.moonflower.jfritz.JFritzEvent.struct.JFritzEventAction;

class ConditionConcatenationListener implements ActionListener {

	private JTable conditionTable;
	private JFritzEventAction eventAction;

	public ConditionConcatenationListener(JTable conditionTable, JFritzEventAction eventAction) {
		this.conditionTable = conditionTable;
		this.eventAction = eventAction;
	}
	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand().equals("none_condition")) {
			conditionTable.setEnabled(false);
			eventAction.setConditionConcatenation(JFritzEventAction.NONE);
		} else if ( e.getActionCommand().equals("and_condition")) {
			conditionTable.setEnabled(true);
			eventAction.setConditionConcatenation(JFritzEventAction.AND);

		} else if ( e.getActionCommand().equals("or_condition")) {
			conditionTable.setEnabled(true);
			eventAction.setConditionConcatenation(JFritzEventAction.OR);
		}

	}

}