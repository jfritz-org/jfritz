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

	private TableColumn callByCallColumn = null;
	private TableColumn commentColumn = null;
	private TableColumn portColumn = null;
	final CallerTable table = this;

	/**
	 * Constructs CallerTable
	 *
	 */
	public CallerTable() {
		super(JFritz.getCallerlist());
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


		KeyListener keyListener = (new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					// clear selection
					table.clearSelection();
					JFritz.getJframe().setStatus();
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
		col.setIdentifier("type"); //$NON-NLS-1$
		col.setHeaderValue(JFritz.getMessage("type")); //$NON-NLS-1$
		headerTips.setToolTip(col, JFritz.getMessage("type_desc")); //$NON-NLS-1$
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column.type.width", "32")));		 //$NON-NLS-1$,  //$NON-NLS-2$

		col = getColumnModel().getColumn(1);
		col.setIdentifier("date"); //$NON-NLS-1$
		col.setHeaderValue(JFritz.getMessage("date")); //$NON-NLS-1$
		headerTips.setToolTip(col, JFritz.getMessage("date_desc")); //$NON-NLS-1$
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column.date.width", "80")));	 //$NON-NLS-1$,  //$NON-NLS-2$

		col = getColumnModel().getColumn(2);
		col.setIdentifier("callbycall"); //$NON-NLS-1$
		col.setHeaderValue(JFritz.getMessage("callbycall")); //$NON-NLS-1$
		headerTips.setToolTip(col, JFritz.getMessage("callbycall_desc")); //$NON-NLS-1$
		col.setCellRenderer(new CallByCallCellRenderer());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column.callbycall.width", "40")));		 //$NON-NLS-1$,  //$NON-NLS-2$
		callByCallColumn = col;

		col = getColumnModel().getColumn(3);
		col.setIdentifier("number"); //$NON-NLS-1$
		col.setHeaderValue(JFritz.getMessage("number")); //$NON-NLS-1$
		col.setCellRenderer(new NumberCellRenderer());
		headerTips.setToolTip(col, JFritz.getMessage("number_desc")); //$NON-NLS-1$
		col.setCellEditor(new CallCellEditor());
        col.setMinWidth(10);
        col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column.number.width", "100")));		 //$NON-NLS-1$,  //$NON-NLS-2$

		col = getColumnModel().getColumn(4);
		col.setIdentifier("participant"); //$NON-NLS-1$
		col.setHeaderValue(JFritz.getMessage("participant")); //$NON-NLS-1$
		headerTips.setToolTip(col, JFritz.getMessage("participant_desc")); //$NON-NLS-1$
		//col.setCellEditor(new TextFieldCellEditor());
		col.setCellEditor(new PersonCellEditor((CallerList) getModel()));
		col.setCellRenderer(new PersonCellRenderer());
        col.setMinWidth(10);
        col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column.participant.width", "100")));		 //$NON-NLS-1$,  //$NON-NLS-2$

		col = getColumnModel().getColumn(5);
		col.setIdentifier("port"); //$NON-NLS-1$
		col.setHeaderValue(JFritz.getMessage("port")); //$NON-NLS-1$
		headerTips.setToolTip(col, JFritz.getMessage("port_desc")); //$NON-NLS-1$
		col.setCellRenderer(new PortCellRenderer());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column.port.width", "60")));		 //$NON-NLS-1$,  //$NON-NLS-2$
		portColumn = col;

		col = getColumnModel().getColumn(6);
		col.setIdentifier("route"); //$NON-NLS-1$
		col.setHeaderValue(JFritz.getMessage("route")); //$NON-NLS-1$
		headerTips.setToolTip(col, JFritz.getMessage("route_desc")); //$NON-NLS-1$
		col.setCellRenderer(new RouteCellRenderer());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column.route.width", "120")));		 //$NON-NLS-1$,  //$NON-NLS-2$

		col = getColumnModel().getColumn(7);
		col.setIdentifier("duration"); //$NON-NLS-1$
		col.setHeaderValue(JFritz.getMessage("duration")); //$NON-NLS-1$
		headerTips.setToolTip(col, JFritz.getMessage("duration_desc")); //$NON-NLS-1$
		col.setCellRenderer(new DurationCellRenderer());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column.duration.width", "60")));		 //$NON-NLS-1$,  //$NON-NLS-2$

		col = getColumnModel().getColumn(8);
		col.setIdentifier("comment"); //$NON-NLS-1$
		col.setHeaderValue(JFritz.getMessage("comment")); //$NON-NLS-1$
		headerTips.setToolTip(col, JFritz.getMessage("comment_desc")); //$NON-NLS-1$
		col.setCellEditor(new CommentCellEditor());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(JFritz.getProperty(
		        "column.comment.width", "60")));		 //$NON-NLS-1$,  //$NON-NLS-2$
		commentColumn = col;

        TableColumnModel colModel = getColumnModel();
        if (!JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.showCallByCallColumn", "true"))) { //$NON-NLS-1$, //$NON-NLS-2$
            try {
                // Try to remove Call-By-Call Column
                colModel.removeColumn(colModel.getColumn(colModel
                        .getColumnIndex("callbycall"))); //$NON-NLS-1$
                Debug.msg("Hiding call-by-call column"); //$NON-NLS-1$
            } catch (IllegalArgumentException iae) { // No Call-By-Call
                                                     // column found.
            }
        }

        if (!JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.showCommentColumn", "true"))) { //$NON-NLS-1$,  //$NON-NLS-2$
            try {
                // Try to remove comment column
                colModel.removeColumn(colModel.getColumn(colModel
                        .getColumnIndex("comment"))); //$NON-NLS-1$
                Debug.msg("Hiding comment column"); //$NON-NLS-1$
            } catch (IllegalArgumentException iae) { // No comment
                                                     // column found.
            }
        }

        if (!JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.showPortColumn", "true"))) { //$NON-NLS-1$,  //$NON-NLS-2$
            try {
                // Try to remove port column
                colModel.removeColumn(colModel.getColumn(colModel
                        .getColumnIndex("port"))); //$NON-NLS-1$
                Debug.msg("Hiding port column"); //$NON-NLS-1$
            } catch (IllegalArgumentException iae) { // No port
                                                     // column found.
            }
        }

        for (int i = 0; i < getColumnCount(); i++) {
		    String columnName = JFritz.getProperty("column"+i+".name",""); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
		    if (!columnName.equals("")) { //$NON-NLS-1$
		    	if (getColumnIndex(columnName) != -1) {
		            moveColumn(getColumnIndex(columnName), i);
		    	}
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

	/**
	 *
	 * Bestimmt die Spaltennummer zu einer bestimmten SpaltenID
	 * SpaltenID = type, duration, port, participant etc.
	 *
	 */
	public int getColumnIndex(String columnIdentifier) {
		for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
    		TableColumn currentColumn = getColumnModel().getColumn(i);
    		if (currentColumn.getIdentifier().toString().equals(columnIdentifier)) {
    			return i;
    		}
		}
		return -1;
	}
}
