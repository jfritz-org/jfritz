package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.callerlist.CallerTable;
import de.moonflower.jfritz.callerlist.JFritzTableColumn;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

public class ConfigPanelCallerListAppearance extends JPanel implements ConfigPanel, ActionListener {

	private static final long serialVersionUID = 7267124419351267208L;

	private Vector<String> columnNames = new Vector<String>();
	private JTable columnTable;
	private ColumnTableModel columnTableModel = new ColumnTableModel();

	public ConfigPanelCallerListAppearance() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

		JPanel cPane = new JPanel();
		cPane.setLayout(new GridBagLayout());

		columnNames.clear();
		columnNames.add("Active");
		columnNames.add("Column");

		JPanel columnPanel = new JPanel();
		columnPanel.setLayout(new BorderLayout());

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));
		JButton upButton = new JButton("up");
		upButton.setActionCommand("up");
		upButton.addActionListener(this);

		JButton downButton = new JButton("down");
		downButton.setActionCommand("down");
		downButton.addActionListener(this);

		buttonPane.add(upButton);
		buttonPane.add(downButton);

		columnTable = new JTable(columnTableModel);
		columnTable.setRowSelectionAllowed(true);
		columnTable.setColumnSelectionAllowed(false);
		columnTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		columnPanel.add(columnTable, BorderLayout.CENTER);
		columnPanel.add(buttonPane, BorderLayout.EAST);

		cPane.add(columnPanel);

		JScrollPane scrollPane = new JScrollPane(cPane);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void loadSettings() {
		for (int i=0; i<CallerTable.getCallerTableColumnsCount(); i++)
		{
			String columnName = Main.getStateProperty("callerTable.column"+i+".name");
			if ( columnName != null)
			{
				JFritzTableColumn jcol = new JFritzTableColumn(columnName);
				jcol.setVisible(JFritzUtils.parseBoolean(Main.getProperty("option.showCallerListColumn." + columnName)));
				columnTableModel.addData(jcol);
			}
		}
	}

	public void saveSettings() {
		for (int i=0; i<columnTableModel.getDataSize(); i++)
		{
			Main.setStateProperty("callerTable.column"+i+".name", columnTableModel.getData(i).getName());
			Main.setProperty("option.showCallerListColumn." + columnTableModel.getData(i).getName(), columnTableModel.getData(i).isVisible());
			Debug.debug("CallerListTableColumn " + i + ": " + columnTableModel.getData(i).getName() + " / visible: " +columnTableModel.getData(i).isVisible());
		}
	}

	public String getPath()
	{
		return Main.getMessage("callerlist")+"::Appearance---";
	}

	public JPanel getPanel() {
		return this;
	}

	public String getHelpUrl() {
		return "http://jfritz.org/wiki/JFritz_Handbuch:Deutsch#AnruflisteAussehen";
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("up"))
		{
			int newSelection = 0;
			for (int i=0; i<columnTable.getSelectedRowCount(); i++)
			{
				int currentIndex = columnTable.getSelectedRow();
				if (currentIndex > 0)
				{
					JFritzTableColumn col = columnTableModel.getData(currentIndex);
					columnTableModel.removeElementAt(currentIndex);
					columnTableModel.insertElementAt(col, currentIndex-1);
					newSelection = currentIndex-1;
				}
				else
				{
					newSelection = currentIndex;
				}
			}
			int numSelections = columnTable.getSelectedRowCount();
			for (int i=0; i<numSelections; i++)
			{
				if (i==0)
				{
					columnTable.setRowSelectionInterval(newSelection, newSelection);
				} else {
					columnTable.addRowSelectionInterval(newSelection, newSelection);
				}
			}
		} else if (e.getActionCommand().equals("down"))
		{
			int newSelection = 0;
			for (int i=0; i<columnTable.getSelectedRowCount(); i++)
			{
				int currentIndex = columnTable.getSelectedRow();
				if (currentIndex < columnTable.getRowCount()-1)
				{
					JFritzTableColumn col = columnTableModel.getData(currentIndex);
					columnTableModel.removeElementAt(currentIndex);
					columnTableModel.insertElementAt(col, currentIndex+1);
					newSelection = currentIndex+1;
				}
				else
				{
					newSelection = currentIndex;
				}
			}
			int numSelections = columnTable.getSelectedRowCount();
			for (int i=0; i<numSelections; i++)
			{
				columnTable.setRowSelectionInterval(newSelection, newSelection);
			}
		} else {
			Debug.warning("Unknown command received: "+e.getActionCommand());
		}
	}

	public void cancel() {
		// TODO Auto-generated method stub

	}
}
