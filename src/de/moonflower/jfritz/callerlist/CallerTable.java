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
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.cellrenderer.CallByCallCellRenderer;
import de.moonflower.jfritz.cellrenderer.CallTypeCellRenderer;
import de.moonflower.jfritz.cellrenderer.CityCellRenderer;
import de.moonflower.jfritz.cellrenderer.CommentCellRenderer;
import de.moonflower.jfritz.cellrenderer.DateCellRenderer;
import de.moonflower.jfritz.cellrenderer.DurationCellRenderer;
import de.moonflower.jfritz.cellrenderer.NumberCellRenderer;
import de.moonflower.jfritz.cellrenderer.PersonCellRenderer;
import de.moonflower.jfritz.cellrenderer.PictureCellRenderer;
import de.moonflower.jfritz.cellrenderer.PortCellRenderer;
import de.moonflower.jfritz.cellrenderer.RouteCellRenderer;
import de.moonflower.jfritz.constants.ProgramConstants;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.phonebook.PhoneBookTable;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * Creates table of callers
 *
 */
public class CallerTable extends JTable {
	private final static Logger log = Logger.getLogger(CallerTable.class);

	public static final String COLUMN_CALL_BY_CALL = "callbycall";
	public static final String COLUMN_COMMENT = "comment";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_DURATION = "duration";
	public static final String COLUMN_NUMBER = "number";
	public static final String COLUMN_PARTICIPANT = "participant";
	public static final String COLUMN_PICTURE = "picture";
	public static final String COLUMN_PORT = "port";
	public static final String COLUMN_ROUTE = "route";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_CITY = "city";

	private static final long serialVersionUID = 1;

	private ColumnHeaderToolTips headerTips;

	final CallerTable table = this;

	private CallerList callerList;

	private PhoneBookTable phoneBookTable; //to change the selected Person, if someone selects a call

	private CallerListPanel parentPanel;

	private Vector<JFritzTableColumn> allTableColumns = new Vector<JFritzTableColumn>();

	private Vector<JFritzTableColumn> sortedTableColumns = new Vector<JFritzTableColumn>();

	private PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	/**
	 * Constructs CallerTable
	 *
	 */
	public CallerTable(CallerListPanel parentPanel, CallerList list) {
		super(list);
		this.parentPanel = parentPanel;
		this.callerList = list;
		headerTips = new ColumnHeaderToolTips();
		setTableProperties();
		createColumns();
		getColumnModel().addColumnModelListener(new TableColumnModelListener(){

			TableColumnModel colModel = getColumnModel();

			public void columnAdded(TableColumnModelEvent arg0) {
				// hier stand was vor der Version 0.7.2, falls wieder notwendig, per CVS nachschauen
			}

			public void columnMarginChanged(ChangeEvent arg0) {
				for (int i = 0; i < colModel.getColumnCount(); i++) {
					TableColumn col = getColumnModel().getColumn(i);
					properties.setStateProperty("callerTable.column." + col.getIdentifier() + ".width", ""
							+ col.getPreferredWidth());
				}
			}

			public void columnMoved(TableColumnModelEvent arg0) {
				if (arg0.getFromIndex() != arg0.getToIndex())
				{
					for (int i =0; i < sortedTableColumns.size(); i++)
					{
						log.debug("Sorted columns: " + sortedTableColumns.get(i).getName() + " - visible: " + sortedTableColumns.get(i).isVisible());
					}
					moveColumn(arg0.getFromIndex(), arg0.getToIndex(), false);
					for (int i =0; i < sortedTableColumns.size(); i++)
					{
						log.debug("Sorted columns: " + sortedTableColumns.get(i).getName() + " - visible: " + sortedTableColumns.get(i).isVisible());
						properties.setStateProperty("callerTable.column" + i + ".name", sortedTableColumns.get(i).getName());
					}
				}
			}

			public void columnRemoved(TableColumnModelEvent arg0) {
				// hier stand was vor der Version 0.7.2, falls wieder notwendig, per CVS nachschauen
			}

			public void columnSelectionChanged(ListSelectionEvent arg0) {

			}

		});
	}

	public CallerTable(CallerListPanel parentPanel, PhoneBookTable phoneBookTable, CallerList list) {
		this(parentPanel, list);
		this.phoneBookTable = phoneBookTable;
	}

	public PhoneBookTable getPhoneBookTable() {
		return phoneBookTable;
	}


	public CallerListPanel getParentPanel() {
		return parentPanel;
	}

	public void setParentPanel(CallerListPanel parentPanel) {
		this.parentPanel = parentPanel;
	}

	public void setPhoneBookTable(PhoneBookTable phoneBookTable) {
		this.phoneBookTable = phoneBookTable;
	}

	/**
	 * sets some properties of the CallerTable
	 */
	private void setTableProperties() {
		setAutoResizeMode(AUTO_RESIZE_OFF); //AUTO_RESIZE_LAST_COLUMN
		setRowHeight(25);
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
					//JFritz.getJframe().setStatus();
					parentPanel.updateStatusBar(false);
//					parentPanel.getStatusBarController().fireStatusChanged(callerList.getTotalDuration());
				} else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					// Delete selected entries
					if (JOptionPane.showConfirmDialog(parentPanel, messages
							.getMessage("really_delete_entries"), //$NON-NLS-1$
							ProgramConstants.PROGRAM_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						int rows[] = getSelectedRows();
						log.info("Removing " + rows.length + " entries"); //$NON-NLS-1$
//						((CallerList) getModel()).removeEntries(getSelectedRows());
						callerList.removeEntries(rows);
					}
				}
			}
		});

		addKeyListener(keyListener);
	}

	/**
	 * Erstellt eine Spalte mit dem Namen columnName an der Position i mit dem
	 * Standard-Renderer renderer und liefert eine Referenz auf das erstellte
	 * Spalten-Objekt
	 *
	 * @param position
	 *            Spaltenposition
	 * @param columnName
	 *            Spaltenname
	 * @param renderer
	 *            Spalten-Renderer
	 * @return Referenz auf erstelltes Spalten-Objekt
	 */
	private TableColumn createColumn(int position, String columnName,
			DefaultTableCellRenderer renderer) {
		TableColumn col = getColumnModel().getColumn(position);
		col.setIdentifier(columnName);
		col.setHeaderValue(messages.getMessage(columnName));
		col.setCellRenderer(renderer);
		headerTips.setToolTip(col, messages.getMessage(columnName + "_desc")); //$NON-NLS-1$
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(properties.getStateProperty(
				"callerTable.column." + columnName + ".width"))); //$NON-NLS-1$,  //$NON-NLS-2$

		JFritzTableColumn jCol = new JFritzTableColumn(columnName);
		jCol.setColumn(col);
		jCol.setVisible(true);
		allTableColumns.insertElementAt(jCol, position);
		sortedTableColumns.insertElementAt(jCol, position);
		return col;
	}

	/**
	 * Creates the columns of the CallerTable
	 *
	 * @param messages
	 */
	private void createColumns() {

		createColumn(0, COLUMN_TYPE, new CallTypeCellRenderer());

		createColumn(1, COLUMN_DATE, new DateCellRenderer());

		createColumn(2, COLUMN_CALL_BY_CALL,
				new CallByCallCellRenderer());

		TableColumn numberColumn = createColumn(3, COLUMN_NUMBER,
				new NumberCellRenderer());
		numberColumn.setCellEditor(new CallCellEditor());

		createColumn(4, COLUMN_PARTICIPANT,
				new PersonCellRenderer());

		createColumn(5, COLUMN_PORT, new PortCellRenderer());

		createColumn(6, COLUMN_ROUTE, new RouteCellRenderer());

		createColumn(7, COLUMN_DURATION, new DurationCellRenderer());

		TableColumn commentColumn = createColumn(8, COLUMN_COMMENT, new CommentCellRenderer());
		commentColumn.setCellEditor(new CommentCellEditor());

		createColumn(9, COLUMN_PICTURE, new PictureCellRenderer());

		createColumn(10, COLUMN_CITY, new CityCellRenderer());

		reorderColumns();

		getTableHeader().addMouseMotionListener(headerTips);
	}

	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
			int vColIndex) {
		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
		if ((rowIndex % 2 == 0) && !isCellSelected(rowIndex, vColIndex)) {
			c.setBackground(new Color(255, 255, 200));
		} else if (!isCellSelected(rowIndex, vColIndex)) {
			// If not shaded, match the table's background
			c.setBackground(getBackground());
		} else {
			c.setBackground(new Color(204, 204, 255));
		}
		return c;
	}

	public void valueChanged(ListSelectionEvent e) {
		super.valueChanged(e);
		// make sure we get the last selectionEvent
		if (!e.getValueIsAdjusting()) {
			Call call = null;
			int rows[] = getSelectedRows();
			int selectedCalls = rows.length;
			double selectedCallsTotalMinutes = 0;

			for (int i = 0; i < rows.length; i++) { // sum the durations
				call = callerList.getFilteredCall(rows[i]);
				selectedCallsTotalMinutes += call.getDuration();
			}
			if ( selectedCalls > 0 )
			{
			  parentPanel.setDeleteEntriesButton(selectedCalls);
			}
			if (rows.length == 1) { // set the selection in the phonebook to the person of the selected call
				//'normaler' status
				parentPanel.setSelectedCallsInfo(selectedCalls, selectedCallsTotalMinutes);
				parentPanel.updateStatusBar(true);

			} else if (rows.length > 0) {
				// Setze Statusbar mit Infos über selektierte Anrufe
				parentPanel.setSelectedCallsInfo(selectedCalls, selectedCallsTotalMinutes);
				parentPanel.updateStatusBar(true);
			}
			parentPanel.adaptGoogleLink();
		}
	}

	public Vector<Person> getSelectedPersons()
	{
		Vector<Person> selectedPersons = new Vector<Person>();
		int rows[] = getSelectedRows();
		int selectedCalls = rows.length;

		Person currentPerson = null;
		Call currentCall = null;
		for (int i=0; i<selectedCalls; i++)
		{
			currentCall = callerList.getFilteredCall(rows[i]);
			currentPerson = JFritz.getPhonebook().findPerson(currentCall);
			if ( currentPerson != null )
			{
				selectedPersons.add(currentPerson);
			}
		}

		return selectedPersons;
	}

	/**
	 * Blendet die Spalten ein oder aus
	 *
	 */
	public void hideColumns() {
		Enumeration<String> columns = getCallerTableColumns().elements();

		String currentColumn = "";
		while (columns.hasMoreElements())
		{
			currentColumn = columns.nextElement();
			if (!JFritzUtils.parseBoolean(properties.getProperty(
					"option.showCallerListColumn."+currentColumn))) { //$NON-NLS-1$, //$NON-NLS-2$
				hideColumn(currentColumn);
			}
		}
	}

	private JFritzTableColumn getColumnByName(String columnName)
	{
		for (int i=0; i<allTableColumns.size(); i++)
		{
			JFritzTableColumn col = allTableColumns.get(i);
			if (col.getName().equals(columnName))
			{
				return col;
			}
		}
		return null;
	}

	private void hideColumn(String columnName)
	{
		JFritzTableColumn col = getColumnByName(columnName);
		if (col != null)
		{
			hideColumn(col.getColumn());
		}
	}

    public void moveColumn(int oldIndex, int newIndex, boolean moveTableColumn) {
    	if ((oldIndex < 0) || (oldIndex >= getColumnCount()) ||
    	    (newIndex < 0) || (newIndex >= getColumnCount()))
    	{
    	    throw new IllegalArgumentException("moveColumn() - Index out of range");
    	}

    	if (oldIndex != newIndex)
    	{
       		String oldColumnIdentifier = (String)getColumnModel().getColumn(oldIndex).getIdentifier();
    		String newColumnIdentifier = (String)getColumnModel().getColumn(newIndex).getIdentifier();

    		System.out.println("Moving column " + oldColumnIdentifier + " from position " + oldIndex + " to " + newIndex);

    		int sortedPositionOld = -1;
    		int sortedPositionNew = -1;
    		for (int i=0; i<sortedTableColumns.size(); i++)
    		{
    			if (sortedTableColumns.get(i).getName().equals(oldColumnIdentifier))
    			{
    				sortedPositionOld = i;
    			}
    			else if (sortedTableColumns.get(i).getName().equals(newColumnIdentifier))
    			{
    				sortedPositionNew = i;
    			}
    		}

    		if ((sortedPositionOld != -1) && (sortedPositionNew != -1))
    		{
    	    	JFritzTableColumn fromColumn = sortedTableColumns.get(sortedPositionOld);
    	    	JFritzTableColumn toColumn = sortedTableColumns.get(sortedPositionNew);

    	    	log.debug("Old sortedColumn: " + fromColumn);
        		log.debug("New sortedColumn: " + toColumn);

    	        int allColumnsOldIndex  = sortedTableColumns.indexOf(fromColumn);
    	        int allColumnsNewIndex  = sortedTableColumns.indexOf(toColumn);

    	    	log.debug("Old sortedColumnIndex: " + allColumnsOldIndex);
        		log.debug("New sortedColumnIndex: " + allColumnsNewIndex);

        		if(oldIndex != newIndex) {
    	        	sortedTableColumns.removeElementAt(allColumnsOldIndex);
    	        	sortedTableColumns.insertElementAt(fromColumn, allColumnsNewIndex);
    	        }

    	        if (moveTableColumn)
    	        {
    	        	super.moveColumn(oldIndex, newIndex);
    	        }
    			log.debug("---");

    			for (int i=0; i<getColumnCount(); i++)
    			{
    				log.debug("Table column " + i + ": " + getColumnModel().getColumn(i).getIdentifier());
    			}
    		}
    		else
    		{
    			log.warn("Could not reorder columns. Rebuild old state!");
    			reorderColumns();
    		}
    	}
    }

    public void hideColumn(TableColumn column) {
        JFritzTableColumn col = getJFritzTableColumn(column.getIdentifier().toString());
        if (col != null && col.isVisible())
        {
        	col.setVisible(false);
        }
        else
        {
        	assert col!=null;
        }
        super.removeColumn(column);
    }

    @SuppressWarnings("unused")
	public void reorderColumns()
    {
    	// remove all columns
    	while (getColumnCount() != 0)
    	{
    		TableColumn col = getColumnModel().getColumn(0);
    		getColumnModel().removeColumn(col);
    	}

    	sortedTableColumns.clear();

		for (int i = 0; i < allTableColumns.size(); i++) {
			String columnName = properties.getStateProperty("callerTable.column" + i + ".name"); //$NON-NLS-1$,  //$NON-NLS-2$
			JFritzTableColumn col = getColumnByName(columnName);
			if (col != null)
			{
				col.setVisible(true);
				log.debug("CallerTable: Adding table column " + columnName + " at position " + i);
				getColumnModel().addColumn(col.getColumn());
				sortedTableColumns.add(col);
				properties.setStateProperty("callerTable.column" + i + ".name", columnName);
			}
			else
			{
				if (col == null)
				{
					Debug.error(log, columnName + " not found");
				}
				else
				{
					log.debug(columnName + " visible:" + col.isVisible());
				}
			}
		}

		log.debug("---");

		for (int i=0; i<getColumnCount(); i++)
		{
			log.debug("Table column " + i + ": " + getColumnModel().getColumn(i).getIdentifier());
		}

		hideColumns();
	}

    public int getColumnIndex(String columnName)
    {
    	for (int i=0; i<sortedTableColumns.size(); i++)
    	{
    		JFritzTableColumn col = sortedTableColumns.get(i);
    		if (col != null && col.isVisible() && col.getName().equals(columnName))
    		{
    			return i;
    		}
    	}
    	return -1;
    }

    public int getVisibleColumnIndex(String columnName)
    {
    	int index = 0;
    	for (int i=0; i<sortedTableColumns.size(); i++)
    	{
    		JFritzTableColumn col = sortedTableColumns.get(i);
    		if (col != null && col.isVisible() && col.getName().equals(columnName))
    		{
    			return index;
    		}
    		if (col.isVisible())
    		{
    			index++;
    		}
    	}
    	return -1;
    }

    private JFritzTableColumn getJFritzTableColumn(String columnName)
    {
    	for (int i=0; i<allTableColumns.size(); i++)
    	{
    		if (allTableColumns.get(i).getName().equals(columnName))
    		{
    			return allTableColumns.get(i);
    		}
    	}
    	return null;
    }

    public static Vector<String> getCallerTableColumns()
    {
    	Vector<String> columns = new Vector<String>();

    	columns.add(COLUMN_TYPE);
    	columns.add(COLUMN_DATE);
    	columns.add(COLUMN_CALL_BY_CALL);
    	columns.add(COLUMN_NUMBER);
    	columns.add(COLUMN_PICTURE);
    	columns.add(COLUMN_PARTICIPANT);
    	columns.add(COLUMN_PORT);
    	columns.add(COLUMN_ROUTE);
    	columns.add(COLUMN_DURATION);
    	columns.add(COLUMN_COMMENT);
    	columns.add(COLUMN_CITY);

    	return columns;
    }

    public static int getCallerTableColumnsCount()
    {
    	return getCallerTableColumns().size();
    }
}
