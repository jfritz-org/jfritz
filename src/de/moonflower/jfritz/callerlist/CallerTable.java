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


import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.cellrenderer.CallByCallCellRenderer;
import de.moonflower.jfritz.cellrenderer.CallTypeCellRenderer;
import de.moonflower.jfritz.cellrenderer.DateCellRenderer;
import de.moonflower.jfritz.cellrenderer.DurationCellRenderer;
import de.moonflower.jfritz.cellrenderer.NumberCellRenderer;
import de.moonflower.jfritz.cellrenderer.PersonCellRenderer;
import de.moonflower.jfritz.cellrenderer.PortCellRenderer;
import de.moonflower.jfritz.cellrenderer.RouteCellRenderer;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * Creates table of callers
 *
 * @author Arno Willig
 */
public class CallerTable extends JTable {
	private static final long serialVersionUID = 1;
	private JFritz jfritz;

	private TableColumn callByCallColumn = null;
	private TableColumn commentColumn = null;
	private TableColumn portColumn = null;

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

		getTableHeader().setReorderingAllowed(true);
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
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column."+JFritz.getMessage("type")+".width", "32")));

		col = getColumnModel().getColumn(1);
		col.setHeaderValue(JFritz.getMessage("date"));
		headerTips.setToolTip(col, JFritz.getMessage("date_desc"));
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column."+JFritz.getMessage("date")+".width", "80")));

		col = getColumnModel().getColumn(2);
		col.setHeaderValue("Call-By-Call");
		headerTips.setToolTip(col, "Benutzer Call-By-Call Anbieter");
		col.setCellRenderer(new CallByCallCellRenderer());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column."+"Call-By-Call"+".width", "40")));
		callByCallColumn = col;

		col = getColumnModel().getColumn(3);
		col.setHeaderValue(JFritz.getMessage("number"));
		col.setCellRenderer(new NumberCellRenderer());
		headerTips.setToolTip(col, JFritz.getMessage("number_desc"));
		col.setCellEditor(new CallCellEditor((CallerList) getModel()));
        col.setMinWidth(10);
        col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column."+JFritz.getMessage("number")+".width", "100")));

		col = getColumnModel().getColumn(4);
		col.setHeaderValue(JFritz.getMessage("participant"));
		headerTips.setToolTip(col, JFritz.getMessage("participant_desc"));
		//col.setCellEditor(new TextFieldCellEditor());
		col.setCellEditor(new PersonCellEditor((CallerList) getModel()));
		col.setCellRenderer(new PersonCellRenderer());
        col.setMinWidth(10);
        col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column."+JFritz.getMessage("participant")+".width", "100")));

		col = getColumnModel().getColumn(5);
		col.setHeaderValue(JFritz.getMessage("port"));
		headerTips.setToolTip(col, JFritz.getMessage("port_desc"));
		col.setCellRenderer(new PortCellRenderer());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column."+JFritz.getMessage("port")+".width", "60")));
		portColumn = col;

		col = getColumnModel().getColumn(6);
		col.setHeaderValue(JFritz.getMessage("route"));
		headerTips.setToolTip(col, JFritz.getMessage("route_desc"));
		col.setCellRenderer(new RouteCellRenderer());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column."+JFritz.getMessage("route")+".width", "120")));

		col = getColumnModel().getColumn(7);
		col.setHeaderValue(JFritz.getMessage("duration"));
		headerTips.setToolTip(col, JFritz.getMessage("duration_desc"));
		col.setCellRenderer(new DurationCellRenderer());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column."+JFritz.getMessage("duration")+".width", "60")));

		col = getColumnModel().getColumn(8);
		col.setHeaderValue("Kommentar");
		headerTips.setToolTip(col, "Kommentar");
		col.setCellEditor(new CommentCellEditor());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column."+"Kommentar"+".width", "60")));
		commentColumn = col;

/**
 		// Kostenanzeige entfernt, da eh zu ungenau
		col = getColumnModel().getColumn(9);
		col.setHeaderValue("Kosten");
		headerTips.setToolTip(col, "Angefallene Kosten");
		col.setCellRenderer(new CostCellRenderer());
		col.setMinWidth(10);
		col.setMaxWidth(200);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
				"column8.width", "60")));
**/
        TableColumnModel colModel = getColumnModel();
        if (!JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.showCallByCallColumn", "true"))) {
            try {
                // Try to remove Call-By-Call Column
                colModel.removeColumn(colModel.getColumn(colModel
                        .getColumnIndex("Call-By-Call")));
                Debug.msg("Hiding call-by-call column");
            } catch (IllegalArgumentException iae) { // No Call-By-Call
                                                     // column found.
            }
        }

        if (!JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.showCommentColumn", "true"))) {
            try {
                // Try to remove comment column
                colModel.removeColumn(colModel.getColumn(colModel
                        .getColumnIndex("Kommentar")));
                Debug.msg("Hiding comment column");
            } catch (IllegalArgumentException iae) { // No comment
                                                     // column found.
            }
        }

        if (!JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.showPortColumn", "true"))) {
            try {
                // Try to remove port column
                colModel.removeColumn(colModel.getColumn(colModel
                        .getColumnIndex(JFritz.getMessage("port"))));
                Debug.msg("Hiding port column");
            } catch (IllegalArgumentException iae) { // No port
                                                     // column found.
            }
        }

        for (int i = 0; i < getColumnCount(); i++) {
		    String columnName = JFritz.getProperty("column"+i+".name","");
		    if (!columnName.equals("")) {
		        Debug.msg("Moving column: " + columnName + " from " + getColumnModel().getColumnIndex(columnName) + " to " + i);
	            moveColumn(getColumnModel().getColumnIndex(columnName), i);
		    }
		}

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
	/**
	 * @return Returns the comment column
	 */
	public TableColumn getCommentColumn() {
		return commentColumn;
	}
	/**
	 * @return Returns the port column
	 */
	public TableColumn getPortColumn() {
		return portColumn;
	}
}
