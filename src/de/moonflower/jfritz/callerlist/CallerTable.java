/*
 *
 * Created on 08.05.2005
 *
 */
package de.moonflower.jfritz.callerlist;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.cellrenderer.CallByCallCellRenderer;
import de.moonflower.jfritz.cellrenderer.CallTypeCellRenderer;
import de.moonflower.jfritz.cellrenderer.CostCellRenderer;
import de.moonflower.jfritz.cellrenderer.DateCellRenderer;
import de.moonflower.jfritz.cellrenderer.DurationCellRenderer;
import de.moonflower.jfritz.cellrenderer.NumberCellRenderer;
import de.moonflower.jfritz.cellrenderer.PersonCellRenderer;
import de.moonflower.jfritz.cellrenderer.PortCellRenderer;
import de.moonflower.jfritz.cellrenderer.RouteCellRenderer;
import de.moonflower.jfritz.struct.CallType;

/**
 * Creates table of callers
 *
 * @author Arno Willig
 */
public class CallerTable extends JTable {
	private static final long serialVersionUID = 1;
	private JFritz jfritz;

	private TableColumn callByCallColumn = null;

	/**
	 * Constructs CallerTable
	 *
	 * @param jfritz JFritz object
	 */
	public CallerTable(JFritz jfritz) {
		super(jfritz.getCallerlist());
		setTableProperties();
		createColumns();
		this.jfritz = jfritz;
	}

	/**
	 * sets some properties of the CallerTable
	 */
	private void setTableProperties() {
		setDefaultRenderer(CallType.class, new CallTypeCellRenderer());
		setDefaultRenderer(Date.class, new DateCellRenderer());
		// FIXME setDefaultRenderer(Person.class, new PersonCellRenderer());
		// FIXME setDefaultRenderer(PhoneNumber.class, new
		// NumberCellRenderer(jfritz));
		// TODO: Create classes for Number, Port and Duration
		// setDefaultRenderer(Port.class, new PortCellRenderer());
		// setDefaultRenderer(Duration.class, new DurationCellRenderer());

		setRowHeight(24);
		setAutoCreateColumnsFromModel(false);
		setColumnSelectionAllowed(false);
		setCellSelectionEnabled(false);
		setRowSelectionAllowed(true);
		setFocusable(true);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		getTableHeader().setReorderingAllowed(false);
		getTableHeader().setResizingAllowed(true);
		getTableHeader().addMouseListener(new ColumnHeaderListener(getModel()));

		final CallerTable table = this;
		KeyListener keyListener = (new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					// clear selection
					table.clearSelection();
					jfritz.getJframe().setStatus();
				}
				else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					// Delete selected entries
					((CallerList)getModel()).removeEntries();
				}
			}
		});

		addKeyListener(keyListener);

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
		col.setHeaderValue(JFritz.getMessage("type"));
		headerTips.setToolTip(col, JFritz.getMessage("type_desc"));
		col.setMinWidth(32);
		col.setMaxWidth(32);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
				"column0.width", "32")));

		col = getColumnModel().getColumn(1);
		col.setHeaderValue(JFritz.getMessage("date"));
		headerTips.setToolTip(col, JFritz.getMessage("date_desc"));
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
				"column1.width", "120")));

		col = getColumnModel().getColumn(2);
		col.setHeaderValue("Call-By-Call");
		headerTips.setToolTip(col, "Benutzer Call-By-Call Anbieter");
		col.setCellRenderer(new CallByCallCellRenderer());
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
				"column2.width", "60")));
		callByCallColumn = col;

		col = getColumnModel().getColumn(3);
		col.setHeaderValue(JFritz.getMessage("number"));
		col.setCellRenderer(new NumberCellRenderer());
		headerTips.setToolTip(col, JFritz.getMessage("number_desc"));
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
				"column3.width", "128")));

		col = getColumnModel().getColumn(4);
		col.setHeaderValue(JFritz.getMessage("participant"));
		headerTips.setToolTip(col, JFritz.getMessage("participant_desc"));
		//col.setCellEditor(new TextFieldCellEditor());
		col.setCellEditor(new PersonCellEditor((CallerList) getModel()));

		col.setCellRenderer(new PersonCellRenderer());
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
				"column4.width", "120")));

		col = getColumnModel().getColumn(5);
		col.setHeaderValue(JFritz.getMessage("port"));
		headerTips.setToolTip(col, JFritz.getMessage("port_desc"));
		col.setCellRenderer(new PortCellRenderer());
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
				"column5.width", "60")));

		col = getColumnModel().getColumn(6);
		col.setHeaderValue(JFritz.getMessage("route"));
		headerTips.setToolTip(col, JFritz.getMessage("route_desc"));
		col.setCellRenderer(new RouteCellRenderer());
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
				"column6.width", "100")));

		col = getColumnModel().getColumn(7);
		col.setHeaderValue(JFritz.getMessage("duration"));
		headerTips.setToolTip(col, JFritz.getMessage("duration_desc"));
		col.setCellRenderer(new DurationCellRenderer());
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
				"column7.width", "60")));

		col = getColumnModel().getColumn(8);
		col.setHeaderValue("Kosten");
		headerTips.setToolTip(col, "Angefallene Kosten");
		col.setCellRenderer(new CostCellRenderer());
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
				"column8.width", "60")));

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

	/**
	 * @return Returns the jfritz.
	 */
	public final JFritz getJfritz() {
		return jfritz;
	}

	/**
	 * @return Returns the callByCall column
	 */
	public TableColumn getCallByCallColumn() {
		return callByCallColumn;
	}
}
