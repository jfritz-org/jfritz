/*
 *
 * Created on 08.05.2005
 *
 */
package de.moonflower.jfritz.callerlist;

import java.awt.Color;
import java.awt.Component;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.JFritzProperties;
import de.moonflower.jfritz.window.cellrenderer.CallTypeCellRenderer;
import de.moonflower.jfritz.window.cellrenderer.DateCellRenderer;
import de.moonflower.jfritz.window.cellrenderer.DurationCellRenderer;
import de.moonflower.jfritz.window.cellrenderer.NumberCellRenderer;
import de.moonflower.jfritz.window.cellrenderer.PersonCellRenderer;
import de.moonflower.jfritz.window.cellrenderer.PortCellRenderer;
import de.moonflower.jfritz.window.cellrenderer.RouteCellRenderer;

/**
 * Creates table of callers
 *
 * @author Arno Willig
 */
public class CallerTable extends JTable {

	private JFritz jfritz;

	private JFritzProperties properties;

	/**
	 * Constructs CallerTable
	 *
	 * @param callerlist
	 */
	public CallerTable(JFritz jfritz) {
		super(jfritz.getCallerlist());
		this.jfritz = jfritz;
		properties = jfritz.getProperties();
		setTableProperties();
		createColumns();
	}

	/**
	 * sets some properties of the CallerTable
	 */
	private void setTableProperties() {
		setDefaultRenderer(CallType.class, new CallTypeCellRenderer());
		setDefaultRenderer(Date.class, new DateCellRenderer());
		// FIXME setDefaultRenderer(Person.class, new PersonCellRenderer());
		setDefaultRenderer(PhoneNumber.class, new NumberCellRenderer(jfritz));
		// TODO: Create classes for Number, Port and Duration
		// setDefaultRenderer(Port.class, new PortCellRenderer());
		// setDefaultRenderer(Duration.class, new DurationCellRenderer());

		setRowHeight(24);
		setAutoCreateColumnsFromModel(false);
		setColumnSelectionAllowed(false);
		setCellSelectionEnabled(false);
		setRowSelectionAllowed(true);
		setFocusable(false);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		getTableHeader().setReorderingAllowed(false);
		getTableHeader().setResizingAllowed(true);
		getTableHeader().addMouseListener(new ColumnHeaderListener(getModel()));

		SelectionListener listener = new SelectionListener(this);
		getSelectionModel().addListSelectionListener(listener);
	}

	/**
	 * Creates the columns of the CallerTable
	 *
	 * @param messages
	 */
	private void createColumns() {
		ColumnHeaderToolTips headerTips = new ColumnHeaderToolTips();

		TableColumn col = getColumnModel().getColumn(0);
		col.setHeaderValue(jfritz.getMessages().getString("type"));
		headerTips.setToolTip(col, jfritz.getMessages().getString("type_desc"));
		col.setMinWidth(32);
		col.setMaxWidth(32);
		col.setPreferredWidth(Integer.parseInt(properties.getProperty(
				"column0.width", "32")));

		col = getColumnModel().getColumn(1);
		col.setHeaderValue(jfritz.getMessages().getString("date"));
		headerTips.setToolTip(col, jfritz.getMessages().getString("date_desc"));
		col.setPreferredWidth(Integer.parseInt(properties.getProperty(
				"column1.width", "120")));

		col = getColumnModel().getColumn(2);
		col.setHeaderValue(jfritz.getMessages().getString("number"));
		//col.setCellRenderer(new NumberCellRenderer(jfritz));
		headerTips.setToolTip(col, jfritz.getMessages().getString("number_desc"));
		col.setPreferredWidth(Integer.parseInt(properties.getProperty(
				"column2.width", "128")));

		col = getColumnModel().getColumn(3);
		col.setHeaderValue(jfritz.getMessages().getString("participant"));
		headerTips.setToolTip(col, jfritz.getMessages().getString("participant_desc"));
		//col.setCellEditor(new TextFieldCellEditor());
		col.setCellEditor(new PersonCellEditor(jfritz));

		col.setCellRenderer(new PersonCellRenderer());
		col.setPreferredWidth(Integer.parseInt(properties.getProperty(
				"column3.width", "120")));

		col = getColumnModel().getColumn(4);
		col.setHeaderValue(jfritz.getMessages().getString("port"));
		headerTips.setToolTip(col, jfritz.getMessages().getString("port_desc"));
		col.setCellRenderer(new PortCellRenderer());
		col.setPreferredWidth(Integer.parseInt(properties.getProperty(
				"column4.width", "60")));

		col = getColumnModel().getColumn(5);
		col.setHeaderValue(jfritz.getMessages().getString("route"));
		headerTips.setToolTip(col, jfritz.getMessages().getString("route_desc"));
		col.setCellRenderer(new RouteCellRenderer());
		col.setPreferredWidth(Integer.parseInt(properties.getProperty(
				"column5.width", "100")));

		col = getColumnModel().getColumn(6);
		col.setHeaderValue(jfritz.getMessages().getString("duration"));
		headerTips.setToolTip(col, jfritz.getMessages().getString("duration_desc"));
		col.setCellRenderer(new DurationCellRenderer());
		col.setPreferredWidth(Integer.parseInt(properties.getProperty(
				"column6.width", "60")));

		getTableHeader().addMouseMotionListener(headerTips);
	}

	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
			int vColIndex) {
		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
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
	 * @param properties
	 *            The messages to set.
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
