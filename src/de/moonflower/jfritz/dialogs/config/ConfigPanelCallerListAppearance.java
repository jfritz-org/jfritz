package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.callerlist.CallerTable;
import de.moonflower.jfritz.callerlist.JFritzTableColumn;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.JFritzUtils;

public class ConfigPanelCallerListAppearance extends JPanel implements ConfigPanel, ActionListener {
	private final static Logger log = Logger.getLogger(ConfigPanelCallerListAppearance.class);

	private static final long serialVersionUID = 7267124419351267208L;

	private Vector<String> columnNames = new Vector<String>();
	private JTable columnTable;
	private ColumnTableModel columnTableModel = new ColumnTableModel();
	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

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
		buttonPane.setLayout(new GridBagLayout());
		JButton upButton = new JButton(messages.getMessage("move_up"));
		upButton.setActionCommand("up");
		upButton.addActionListener(this);

		JButton downButton = new JButton(messages.getMessage("move_down"));
		downButton.setActionCommand("down");
		downButton.addActionListener(this);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets.left = 20;
		c.gridy = 0;
		buttonPane.add(upButton, c);
		c.gridy = 1;
		buttonPane.add(downButton, c);

		columnTable = new JTable(columnTableModel);
		columnTable.setRowSelectionAllowed(true);
		columnTable.setColumnSelectionAllowed(false);
		columnTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		columnTable.setAutoCreateColumnsFromModel(false);
		columnTable.setColumnSelectionAllowed(false);
		columnTable.setCellSelectionEnabled(false);
		columnTable.setRowSelectionAllowed(true);
		columnTable.getColumnModel().getColumn(0).setMinWidth(60);
		columnTable.getColumnModel().getColumn(0).setMaxWidth(60);
		columnTable.getColumnModel().getColumn(1).setMinWidth(120);
		columnTable.getColumnModel().getColumn(1).setMaxWidth(120);

		columnPanel.add(columnTable, BorderLayout.CENTER);
		columnPanel.add(columnTable.getTableHeader(), BorderLayout.NORTH);
		columnPanel.add(buttonPane, BorderLayout.EAST);

		cPane.add(columnPanel);

		JScrollPane scrollPane = new JScrollPane(cPane);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void loadSettings() {
		for (int i=0; i<CallerTable.getCallerTableColumnsCount(); i++)
		{
			String columnName = properties.getStateProperty("callerTable.column"+i+".name");
			if ( columnName != null)
			{
				JFritzTableColumn jcol = new JFritzTableColumn(columnName);
				jcol.setVisible(JFritzUtils.parseBoolean(properties.getProperty("option.showCallerListColumn." + columnName)));
				columnTableModel.addData(jcol);
			}
		}
	}

	public void saveSettings() {
		for (int i=0; i<columnTableModel.getDataSize(); i++)
		{
			properties.setStateProperty("callerTable.column"+i+".name", columnTableModel.getData(i).getName());
			properties.setProperty("option.showCallerListColumn." + columnTableModel.getData(i).getName(), columnTableModel.getData(i).isVisible());
			log.debug("CallerListTableColumn " + i + ": " + columnTableModel.getData(i).getName() + " / visible: " +columnTableModel.getData(i).isVisible());
		}
	}

	public String getPath()
	{
		return messages.getMessage("callerlist")+"::"+messages.getMessage("appearance");
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
			log.warn("Unknown command received: "+e.getActionCommand());
		}
	}

	public void cancel() {
		// TODO Auto-generated method stub

	}

	public boolean shouldRefreshJFritzWindow() {
		return false;
	}

	public boolean shouldRefreshTrayMenu() {
		return false;
	}
}
