package de.moonflower.jfritz.JFritzEvent.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.JFritzEvent.JFritzEventDispatcher;
import de.moonflower.jfritz.JFritzEvent.JFritzEventTableModel;
import de.moonflower.jfritz.JFritzEvent.struct.JFritzEventAction;
import de.moonflower.jfritz.dialogs.config.ConfigPanel;
import de.moonflower.jfritz.utils.Debug;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class JFritzEventGUI extends JPanel implements ConfigPanel, ActionListener {

	private static final long serialVersionUID = 2035657335276036318L;

	private JDialog configDialog;
	private JTable eventTable;
	private JFritzEventTableModel eventTableModel;

	public JFritzEventGUI(JDialog configDialog) {
		this.configDialog = configDialog;
		BorderLayout thisLayout = new BorderLayout();
		this.setLayout(thisLayout);
		this.setPreferredSize(new java.awt.Dimension(487, 720));

		eventTable = new JTable();
		eventTableModel = new JFritzEventTableModel();
		eventTable.setModel(eventTableModel);
		eventTable.setRowHeight(24);
		eventTable.setFocusable(false);
		eventTable.setAutoCreateColumnsFromModel(false);
		eventTable.setColumnSelectionAllowed(false);
		eventTable.setCellSelectionEnabled(false);
		eventTable.setRowSelectionAllowed(true);
		eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		eventTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		add(new JScrollPane(eventTable), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));

		JButton button = new JButton(Main.getMessage("new"));
		button.setActionCommand("new");
		button.addActionListener(this);
		buttonPanel.add(button);

		button = new JButton(Main.getMessage("edit"));
		button.setActionCommand("edit");
		button.addActionListener(this);
		buttonPanel.add(button);

		button = new JButton(Main.getMessage("delete"));
		button.setActionCommand("delete");
		button.addActionListener(this);
		buttonPanel.add(button);

		add(buttonPanel, BorderLayout.EAST);

	}

	public void loadSettings() {
		// TODO Auto-generated method stub

	}

	public void saveSettings() {
		JFritzEventDispatcher.clearEventList();
		// add events to JFritzEventDispatcher
		for ( int i=0; i<eventTableModel.getRowCount(); i++) {
			JFritzEventDispatcher.addEvent(eventTableModel.getEvent(i));
		}
		JFritzEventDispatcher.saveToXML();
	}

	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand().equals("new") ) {
			JFritzEventAction newEventAction = JFritzEventDispatcher.createNewEventAction();
			JFritzEventActionGUI eaGUI = new JFritzEventActionGUI(configDialog, newEventAction);
			eaGUI.setModal(true);
			if ( eaGUI.showDialog() ) {
				eventTableModel.addEvent(newEventAction);
				eventTableModel.fireTableDataChanged();
			}
		} else if ( e.getActionCommand().equals("edit")) {
			int selectedRow = eventTable.getSelectedRow();
			if ( selectedRow != -1) {
				JFritzEventAction editEventAction = eventTableModel.getEvent(selectedRow).clone();
				JFritzEventActionGUI eaGUI = new JFritzEventActionGUI(configDialog, editEventAction);
				eaGUI.setModal(true);
				if ( eaGUI.showDialog() ) {
					// SAVE SETTINGS
					eventTableModel.setEvent(selectedRow, editEventAction);
					eventTableModel.fireTableDataChanged();
				}
			}
		} else if ( e.getActionCommand().equals("delete")) {
			int selectedRow = eventTable.getSelectedRow();
			if ( selectedRow != -1) {
				eventTableModel.removeEvent(eventTableModel.getEvent(selectedRow));
				eventTableModel.fireTableDataChanged();
			}
		} else {
			Debug.msg("Not yet implemented");
		}
	}

	public String getPath() {
		return "EventAction";
	}

	public JPanel getPanel() {
		return this;
	}

	public String getHelpUrl() {
		return "";
	}
}
