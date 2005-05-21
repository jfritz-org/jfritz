/*
 *
 * Created on 08.05.2005
 *
 */
package de.moonflower.jfritz;

import java.awt.Color;
import java.awt.Component;
import java.util.Date;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;



/**
 * Creates table of callers
 *
 * @author Arno Willig
 */
public class CallerTable extends JTable {

	private ResourceBundle messages;
	private JFritzProperties properties, participants;


	/**
	 * Constructs CallerTable
	 * @param callerlist
	 */
	public CallerTable(TableModel callerlist, ResourceBundle messages, JFritzProperties properties) {
		super(callerlist);
		setMessages(messages);
		setProperties(properties);
		setTableProperties();
		createColumns(messages);
	}

	/**
	 * sets some properties of the CallerTable
	 */
	private void setTableProperties() {
		setDefaultRenderer(CallType.class, new CallTypeCellRenderer());
		setDefaultRenderer(Date.class, new DateCellRenderer());
		// TODO: Create classes for Number, Port and Duration
		// setDefaultRenderer(Port.class, new PortCellRenderer());
		// setDefaultRenderer(Duration.class, new DurationCellRenderer());
		// setDefaultRenderer(Number.class, new NumberCellRenderer());

		setRowHeight(24);
		setAutoCreateColumnsFromModel(false);
		setColumnSelectionAllowed(false);
		setCellSelectionEnabled(false);
		setRowSelectionAllowed(true);
		setFocusable(false);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		getTableHeader().setReorderingAllowed(false);
		getTableHeader().setResizingAllowed(true);
		getTableHeader().addMouseListener(new ColumnHeaderListener());

		SelectionListener listener = new SelectionListener(this);
		getSelectionModel().addListSelectionListener(listener);
	}

	/**
	 * Creates the columns of the CallerTable
	 * @param messages
	 */
	private void createColumns(ResourceBundle messages) {
		ColumnHeaderToolTips headerTips = new ColumnHeaderToolTips();

		TableColumn col = getColumnModel().getColumn(0);
		col.setHeaderValue(messages.getString("type"));
		headerTips.setToolTip(col, messages.getString("type_desc"));
		col.setPreferredWidth(32);
		col.setMinWidth(32);
		col.setMaxWidth(32);

		col = getColumnModel().getColumn(1);
		col.setHeaderValue(messages.getString("date"));
		headerTips.setToolTip(col, messages.getString("date_desc"));
		col.setPreferredWidth(120);
		col.setMinWidth(120);
		col.setMaxWidth(120);

		col = getColumnModel().getColumn(2);
		col.setHeaderValue(messages.getString("number"));
		col.setCellRenderer(new NumberCellRenderer(properties,messages));
		headerTips.setToolTip(col, messages.getString("number_desc"));

		col = getColumnModel().getColumn(3);
		col.setHeaderValue(messages.getString("participant"));
		headerTips.setToolTip(col, messages.getString("participant_desc"));
		col.setCellEditor(new ParticipantCellEditor());

		col = getColumnModel().getColumn(4);
		col.setHeaderValue(messages.getString("port"));
		headerTips.setToolTip(col, messages.getString("port_desc"));
		col.setCellRenderer(new PortCellRenderer());
		col.setPreferredWidth(60);
		col.setMinWidth(60);
		col.setMaxWidth(60);

		col = getColumnModel().getColumn(5);
		col.setHeaderValue(messages.getString("route"));
		headerTips.setToolTip(col, messages.getString("route_desc"));
		col.setCellRenderer(new RouteCellRenderer());
		col.setPreferredWidth(100);
		col.setMinWidth(100);
		col.setMaxWidth(100);

		col = getColumnModel().getColumn(6);
		col.setHeaderValue(messages.getString("duration"));
		headerTips.setToolTip(col, messages.getString("duration_desc"));
		col.setCellRenderer(new DurationCellRenderer());
		col.setPreferredWidth(60);
		col.setMinWidth(60);
		col.setMaxWidth(60);


		getTableHeader().addMouseMotionListener(headerTips);
	}

	public Component prepareRenderer(TableCellRenderer renderer,
			int rowIndex, int vColIndex) {
		Component c = super.prepareRenderer(renderer, rowIndex,
				vColIndex);
		if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
			c.setBackground(new Color(255, 255, 200));
		} else if (!isCellSelected(rowIndex, vColIndex)) {
			// If not shaded, match the table's background
			c.setBackground(getBackground());
		} else {
			c.setBackground(new Color(204, 204, 255));
		}
		return c;
	}

	/**
	 * @param messages The messages to set.
	 */
	public void setMessages(ResourceBundle messages) {
		this.messages = messages;
	}

	/**
	 * @param properties The messages to set.
	 */
	public void setProperties(JFritzProperties properties) {
		this.properties = properties;
	}

	/**
	 * Find a table column by the column model index
	 *
	 * @param table
	 * @param columnModelIndex
	 * @return column
	 */
	public static TableColumn findTableColumn(JTable table, int columnModelIndex) {
		Enumeration en = table.getColumnModel().getColumns();
		for (; en.hasMoreElements();) {
			TableColumn col = (TableColumn) en.nextElement();
			if (col.getModelIndex() == columnModelIndex) {
				return col;
			}
		}
		return null;
	}


}
