/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.callerlist;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.CellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import com.toedter.calendar.JDateChooser;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.JFritzWindow;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.callerlist.filter.CallByCallFilter;
import de.moonflower.jfritz.callerlist.filter.CallFilter;
import de.moonflower.jfritz.callerlist.filter.CallInFailedFilter;
import de.moonflower.jfritz.callerlist.filter.CallInFilter;
import de.moonflower.jfritz.callerlist.filter.CallOutFilter;
import de.moonflower.jfritz.callerlist.filter.CommentFilter;
import de.moonflower.jfritz.callerlist.filter.DateFilter;
import de.moonflower.jfritz.callerlist.filter.FixedFilter;
import de.moonflower.jfritz.callerlist.filter.HandyFilter;
import de.moonflower.jfritz.callerlist.filter.AnonymFilter;
import de.moonflower.jfritz.callerlist.filter.SearchFilter;
import de.moonflower.jfritz.callerlist.filter.SipFilter;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.JFritzClipboard;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;
import de.moonflower.jfritz.utils.threeStateButton.ThreeStateButton3;

/**
 * @author Arno Willig
 * @author marc
 */

//TODO evtl start und enddate richtig setzten, wenn man einen datefilter
//aktiviert und
//zeilen selektiert hat

//TODO delete button
public class CallerListPanel extends JPanel implements ActionListener,
KeyListener, PropertyChangeListener {

	class PopupListener extends MouseAdapter {
		JPopupMenu popupMenu;

		PopupListener(JPopupMenu popupMenu) {
			super();
			this.popupMenu = popupMenu;
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		public void mouseClicked(MouseEvent e) {
			// FIXME Listener in den table einbauen
			if ((e.getClickCount() > 1)
					&& (e.getComponent().getClass() != ThreeStateButton3.class)) {
				JFritz.getJframe().activatePhoneBook();
			}
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}
	}

	private static final String DELETE_ENTRIES = "delete_entries";

	private static final String DELETE_ENTRY = "delete_entry";

	private static final String FILTER_CALLBYCALL = "filter_callbycall";

	private static final String FILTER_CALLIN = "filter_callin";

	public static final String FILTER_CALLINFAILED = "filter_callinfailed";

	private static final String FILTER_CALLOUT = "filter_callout";

	private static final String FILTER_COMMENT = "filter_comment";

	private static final String FILTER_DATE = "filter_date";

	private static final String FILTER_FIXED = "filter_fixed";

	private static final String FILTER_HANDY = "filter_handy";

	private static final String FILTER_ANONYM = "filter_number";

	private static final String FILTER_SEARCH = "filter_search";

	private static final String FILTER_SEARCH_TEXT = "filter_search.text";

	private static final String FILTER_SIP = "filter_sip";

	private static final long serialVersionUID = 1;

	private static final String FILTER_DATE_END = "FILTER_DATE_END";

	private static final String FILTER_DATE_START = "FILTER_DATE_START";

	private static final String THIS_DAY = "date_filter_today";
	private static final String LAST_DAY = "date_filter_yesterday";
	private static final String THIS_WEEK = "date_filter_this_week";
	private static final String LAST_WEEK = "date_filter_last_week";
	private static final String THIS_MONTH = "date_filter_this_month";
	private static final String LAST_MONTH = "date_filter_last_month";

	private String dateSpecialSaveString;
	private CallerList callerList;

	private CallerTable callerTable;

	private CallFilter[] filter;

	private static final int callByCall = 0;

	private static final int callInFailed = 1;

	private static final int callIn = 2;

	private static final int callOut = 3;

	private static final int comment = 4;

	private static final int handy = 5;

	private static final int fixed = 6;

	private static final int sip = 7;

	private static final int anonym = 8;

	private static final int date = 9;

	private static final int search = 10;

	private static final int FILTERCOUNT = 11;

	private static final String FILTER_DATE_SPECIAL = "DATE_SPECIAL";



	private ThreeStateButton3 dateFilterButton, callByCallFilterButton,
	callInFilterButton, callOutFilterButton, callInFailedFilterButton,
	anonymFilterButton, fixedFilterButton, handyFilterButton,
	sipFilterButton, commentFilterButton, searchFilterButton;

	private JButton deleteEntriesButton;

	private JDateChooser endDateChooser;

	private JTextField searchFilterTextField;

	private JLabel searchLabel;

	// private FixedFilter fixedFilter;

	private JDateChooser startDateChooser;

	private WindowAdapter wl;

	private JFritzWindow parentFrame;

	public CallerListPanel(CallerList callerList, JFritzWindow parent) {
		super();
		parentFrame = parent;
		this.callerList = callerList;
		createFilters(callerList);
		setLayout(new BorderLayout());
		add(createToolBar(), BorderLayout.NORTH);
		add(createCallerListTable(), BorderLayout.CENTER);
		wl = new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				writeButtonStatus();
			}
		};
		parent.addWindowListener(wl);
	}

	private void createFilters(CallerList callerList) {
		filter = new CallFilter[FILTERCOUNT];
		filter[callByCall] = createCallByCallFilter(callerList);
		callerList.addFilter(filter[callByCall]);
		filter[callInFailed] = new CallInFailedFilter();
		callerList.addFilter(filter[callInFailed]);
		filter[callIn] = new CallInFilter();
		callerList.addFilter(filter[callIn]);
		filter[callOut] = new CallOutFilter();
		callerList.addFilter(filter[callOut]);
		filter[comment] = new CommentFilter();
		callerList.addFilter(filter[comment]);
		filter[anonym] = new AnonymFilter();
		callerList.addFilter(filter[anonym]);
		filter[fixed] = new FixedFilter();
		callerList.addFilter(filter[fixed]);
		filter[handy] = new HandyFilter();
		callerList.addFilter(filter[handy]);
		filter[sip] = createSipFilter(callerList);
		callerList.addFilter(filter[sip]);
		filter[date] = new DateFilter(new Date(), new Date());
		callerList.addFilter(filter[date]);
		filter[search] = new SearchFilter("");
		callerList.addFilter(filter[search]);
	}
	private SipFilter createSipFilter(CallerList callerList) {
		SipFilter filter;
		if (callerTable !=null && callerTable.getSelectedRowCount()!=0){
			filter = new SipFilter(callerList
				.getSipProviders(callerTable.getSelectedRows()));
			Debug.msg("callerTable.getSelectedRowCount()!=0");
		}else{
			filter = new SipFilter(callerList
					.getSipProviders());
			Debug.msg("callerTable.getSelectedRowCount()==0");
		}
		Debug.msg("filter: "+filter.toString());
		return filter;
	}

	private CallByCallFilter createCallByCallFilter(CallerList callerList) {
		CallByCallFilter filter;
		if (callerTable !=null && callerTable.getSelectedRowCount()!=0){
			filter = new CallByCallFilter(callerList
				.getCbCProviders(callerTable.getSelectedRows()));
		}else{
			filter = new CallByCallFilter(callerList
					.getCbCProviders());
		}
		Debug.msg("filter: "+filter.toString());
		return filter;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		handleAction(e.getActionCommand());
		callerList.update();
		parentFrame.setStatus();
	}

	/*
	 * disable all filters and hide the search and date stuff
	 */
	private void clearAllFilter() {
		callInFilterButton.setState(ThreeStateButton3.NOTHING);
		callOutFilterButton.setState(ThreeStateButton3.NOTHING);
		callInFailedFilterButton.setState(ThreeStateButton3.NOTHING);
		anonymFilterButton.setState(ThreeStateButton3.NOTHING);
		fixedFilterButton.setState(ThreeStateButton3.NOTHING);
		handyFilterButton.setState(ThreeStateButton3.NOTHING);
		dateFilterButton.setState(ThreeStateButton3.NOTHING);
		searchFilterButton.setState(ThreeStateButton3.NOTHING);
		searchFilterTextField.setVisible(false);
		startDateChooser.setVisible(false);
		endDateChooser.setVisible(false);
		searchLabel.setVisible(false);
		sipFilterButton.setState(ThreeStateButton3.NOTHING);
		callByCallFilterButton.setState(ThreeStateButton3.NOTHING);
		commentFilterButton.setState(ThreeStateButton3.NOTHING);
		dateSpecialSaveString = " ";
		syncAllFilters();
		callerList.update();
		parentFrame.setStatus();
	}

	public JScrollPane createCallerListTable() {
		callerTable = new CallerTable();
		JPopupMenu callerlistPopupMenu = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem(Main.getMessage("reverse_lookup")); //$NON-NLS-1$
		menuItem.setActionCommand("reverselookup"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		callerlistPopupMenu.add(menuItem);

		callerlistPopupMenu.addSeparator();

		menuItem = new JMenuItem(Main.getMessage("export_csv")); //$NON-NLS-1$
		menuItem.setActionCommand("export_csv"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		callerlistPopupMenu.add(menuItem);

		menuItem = new JMenuItem(Main.getMessage("import_callerlist_csv")); //$NON-NLS-1$
		menuItem.setActionCommand("import_callerlist_csv"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		menuItem.setEnabled(true);
		callerlistPopupMenu.add(menuItem);

		menuItem = new JMenuItem(Main.getMessage("export_xml")); //$NON-NLS-1$
		menuItem.setActionCommand("export_xml"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		callerlistPopupMenu.add(menuItem);

		menuItem = new JMenuItem(Main.getMessage("import_xml")); //$NON-NLS-1$
		menuItem.setActionCommand("import_xml"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		menuItem.setEnabled(false);
		callerlistPopupMenu.add(menuItem);

		callerlistPopupMenu.addSeparator();

		JMenu clipboardMenu = new JMenu(Main.getMessage("clipboard")); //$NON-NLS-1$
		clipboardMenu.setMnemonic(KeyEvent.VK_Z);

		JMenuItem item = new JMenuItem(Main.getMessage("number"), KeyEvent.VK_N); //$NON-NLS-1$
		item.setActionCommand("clipboard_number"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		item = new JMenuItem(Main.getMessage("address"), KeyEvent.VK_A); //$NON-NLS-1$
		item.setActionCommand("clipboard_adress"); //$NON-NLS-1$
		item.addActionListener(this);
		clipboardMenu.add(item);

		callerlistPopupMenu.add(clipboardMenu);

		MouseAdapter popupListener = new PopupListener(callerlistPopupMenu);

		callerTable.addMouseListener(popupListener);

		return new JScrollPane(callerTable);
	}

	public JPanel createToolBar() {
		JToolBar upperToolBar = new JToolBar();
		upperToolBar.setFloatable(true);
		JToolBar lowerToolbar = new JToolBar();
		lowerToolbar.setFloatable(true);

		JButton resetFiltersButton = new JButton();
		resetFiltersButton.setActionCommand("export_csv"); //$NON-NLS-1$
		resetFiltersButton.addActionListener(this);
		resetFiltersButton.setIcon(getImageIcon("csv_export.png")); //$NON-NLS-1$
		resetFiltersButton.setToolTipText(Main.getMessage("export_csv")); //$NON-NLS-1$
		upperToolBar.add(resetFiltersButton);

		resetFiltersButton = new JButton();
		resetFiltersButton.setActionCommand("import_csv"); //$NON-NLS-1$
		resetFiltersButton.addActionListener(this);
		resetFiltersButton.setIcon(getImageIcon("csv_import.png")); //$NON-NLS-1$
		resetFiltersButton.setToolTipText("CSV-Datei importieren"); //$NON-NLS-1$
		resetFiltersButton.setEnabled(false);
		upperToolBar.add(resetFiltersButton);

		resetFiltersButton = new JButton();
		resetFiltersButton.setActionCommand("export_xml"); //$NON-NLS-1$
		resetFiltersButton.addActionListener(this);
		resetFiltersButton.setIcon(getImageIcon("xml_export.png")); //$NON-NLS-1$
		resetFiltersButton.setToolTipText("XML-Datei exportieren"); //$NON-NLS-1$
		upperToolBar.add(resetFiltersButton);

		resetFiltersButton = new JButton();
		resetFiltersButton.setActionCommand("import_xml"); //$NON-NLS-1$
		resetFiltersButton.addActionListener(this);
		resetFiltersButton.setIcon(getImageIcon("xml_import.png")); //$NON-NLS-1$
		resetFiltersButton.setToolTipText("XML-Datei importieren"); //$NON-NLS-1$
		resetFiltersButton.setEnabled(false);
		upperToolBar.add(resetFiltersButton);

		upperToolBar.addSeparator();

		callInFilterButton = new ThreeStateButton3(getImageIcon("callin.png"));
		callInFilterButton.setActionCommand(FILTER_CALLIN);
		callInFilterButton.addActionListener(this);
		callInFilterButton.setToolTipText(Main.getMessage(FILTER_CALLIN));

		callInFailedFilterButton = new ThreeStateButton3(
				getImageIcon("callinfailed.png")); //$NON-NLS-1$
		callInFailedFilterButton.setActionCommand(FILTER_CALLINFAILED);
		callInFailedFilterButton.addActionListener(this);
		callInFailedFilterButton.setToolTipText(Main
				.getMessage(FILTER_CALLINFAILED));

		JPopupMenu missedPopupMenu = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem(Main
				.getMessage("missed_calls_without_comments_last_week")); //$NON-NLS-1$
		menuItem
		.setActionCommand("filter_callinfailed_allWithoutCommentLastWeek"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		missedPopupMenu.add(menuItem);
		menuItem = new JMenuItem(Main
				.getMessage("missed_calls_without_comments")); //$NON-NLS-1$
		menuItem.setActionCommand("filter_callinfailed_allWithoutComment"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		missedPopupMenu.add(menuItem);
		MouseAdapter popupListener = new PopupListener(missedPopupMenu);
		callInFailedFilterButton.addMouseListener(popupListener);

		callOutFilterButton = new ThreeStateButton3(getImageIcon("callout.png"));
		callOutFilterButton.setActionCommand(FILTER_CALLOUT);
		callOutFilterButton.addActionListener(this);
		callOutFilterButton.setToolTipText(Main.getMessage(FILTER_CALLOUT));

		anonymFilterButton = new ThreeStateButton3(
				getImageIcon("mask.gif")); //$NON-NLS-1$
		anonymFilterButton.setActionCommand(FILTER_ANONYM);
		anonymFilterButton.addActionListener(this);
		anonymFilterButton.setToolTipText(Main.getMessage(FILTER_ANONYM));

		fixedFilterButton = new ThreeStateButton3(getImageIcon("phone.png")); //$NON-NLS-1$
		fixedFilterButton.setActionCommand(FILTER_FIXED);
		fixedFilterButton.addActionListener(this);
		fixedFilterButton.setToolTipText(Main.getMessage(FILTER_FIXED));

		handyFilterButton = new ThreeStateButton3(getImageIcon("handy.png")); //$NON-NLS-1$
		handyFilterButton.setActionCommand(FILTER_HANDY);
		handyFilterButton.addActionListener(this);
		handyFilterButton.setToolTipText(Main.getMessage(FILTER_HANDY));

		dateFilterButton = new ThreeStateButton3(getImageIcon("calendar.png")); //$NON-NLS-1$

		dateFilterButton.setActionCommand(FILTER_DATE);
		dateFilterButton.addActionListener(this);
		dateFilterButton.setToolTipText(Main.getMessage(FILTER_DATE));

		JPopupMenu datePopupMenu = new JPopupMenu();
		menuItem = new JMenuItem(Main.getMessage(THIS_DAY)); //$NON-NLS-1$
		menuItem.setActionCommand(THIS_DAY); //$NON-NLS-1$
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(Main.getMessage(LAST_DAY)); //$NON-NLS-1$
		menuItem.setActionCommand(LAST_DAY); //$NON-NLS-1$
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(Main.getMessage(THIS_WEEK)); //$NON-NLS-1$
		menuItem.setActionCommand(THIS_WEEK); //$NON-NLS-1$
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(Main.getMessage(LAST_WEEK)); //$NON-NLS-1$
		menuItem.setActionCommand(LAST_WEEK); //$NON-NLS-1$
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);

		menuItem = new JMenuItem(Main.getMessage(THIS_MONTH)); //$NON-NLS-1$
		menuItem.setActionCommand(THIS_MONTH); //$NON-NLS-1$
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(Main.getMessage(LAST_MONTH)); //$NON-NLS-1$
		menuItem.setActionCommand(LAST_MONTH); //$NON-NLS-1$
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		popupListener = new PopupListener(datePopupMenu);

		dateFilterButton.addMouseListener(popupListener);

		startDateChooser = new JDateChooser();
		startDateChooser.setVisible(false);
		startDateChooser.addPropertyChangeListener("date", this);
		endDateChooser = new JDateChooser();
		endDateChooser.setDate(Calendar.getInstance().getTime());
		endDateChooser.setVisible(false);
		endDateChooser.addPropertyChangeListener("date", this);

		sipFilterButton = new ThreeStateButton3(getImageIcon("world.png")); //$NON-NLS-1$
		sipFilterButton.setActionCommand(FILTER_SIP);
		sipFilterButton.addActionListener(this);
		sipFilterButton.setToolTipText(Main.getMessage(FILTER_SIP));

		callByCallFilterButton = new ThreeStateButton3(
				getImageIcon("callbycall.png")); //$NON-NLS-1$
		callByCallFilterButton.setActionCommand(FILTER_CALLBYCALL);
		callByCallFilterButton.addActionListener(this);
		callByCallFilterButton.setToolTipText(Main
				.getMessage(FILTER_CALLBYCALL));

		commentFilterButton = new ThreeStateButton3(
				getImageIcon("commentFilter.png"));
		commentFilterButton.setActionCommand(FILTER_COMMENT);
		commentFilterButton.addActionListener(this);
		commentFilterButton.setToolTipText(Main.getMessage(FILTER_COMMENT));

		searchFilterButton = new ThreeStateButton3(
				getImageIcon("searchfilter.png"));
		searchFilterButton.setActionCommand(FILTER_SEARCH);
		searchFilterButton.addActionListener(this);
		searchFilterButton.setToolTipText(Main.getMessage(FILTER_SEARCH));
		searchFilterTextField = new JTextField(10);

		deleteEntriesButton = new JButton();
		deleteEntriesButton.setToolTipText(Main.getMessage(DELETE_ENTRIES)
				.replaceAll("%N", "")); //$NON-NLS-1$,  //$NON-NLS-2$,
		deleteEntriesButton.setActionCommand(DELETE_ENTRY);
		deleteEntriesButton.addActionListener(this);
		deleteEntriesButton.setIcon(getImageIcon("delete.png")); //$NON-NLS-1$
		deleteEntriesButton.setFocusPainted(false);
		deleteEntriesButton.setEnabled(false);

		searchLabel = new JLabel(Main.getMessage("search") + ": ");//$NON-NLS-1$,  //$NON-NLS-2$
		searchLabel.setVisible(false);
		searchFilterTextField.addKeyListener(this);
		searchFilterTextField.setVisible(false);

		resetFiltersButton = new JButton(Main.getMessage("clear")); //$NON-NLS-1$
		resetFiltersButton.setActionCommand("clearFilter"); //$NON-NLS-1$
		resetFiltersButton.addActionListener(this);

		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setLayout(new BorderLayout());
		// Icons sind noch zu groß, deshalb erst einmal auskommentiert
		// toolbarPanel.add(upperToolBar, BorderLayout.NORTH);

		/** **********add all Buttons and stuff to the lowerToolbar************** */
		lowerToolbar.add(callInFilterButton);
		lowerToolbar.add(callInFailedFilterButton);
		lowerToolbar.add(callOutFilterButton);
		lowerToolbar.add(anonymFilterButton);
		lowerToolbar.add(fixedFilterButton);
		lowerToolbar.add(handyFilterButton);
		lowerToolbar.add(sipFilterButton);
		lowerToolbar.add(callByCallFilterButton);
		lowerToolbar.add(commentFilterButton);
		lowerToolbar.addSeparator();
		lowerToolbar.add(dateFilterButton);
		lowerToolbar.add(startDateChooser);
		lowerToolbar.add(endDateChooser);
		lowerToolbar.addSeparator();
		lowerToolbar.add(searchFilterButton);
		lowerToolbar.add(searchLabel);
		lowerToolbar.add(searchFilterTextField);
		lowerToolbar.addSeparator();
		lowerToolbar.add(resetFiltersButton);
		lowerToolbar.addSeparator();
		lowerToolbar.addSeparator();
		lowerToolbar.add(deleteEntriesButton);
		toolbarPanel.add(lowerToolbar, BorderLayout.SOUTH);
		readButtonStatus();
		return toolbarPanel;
	}

	public void disableDeleteEntriesButton() {
		deleteEntriesButton.setToolTipText(Main.getMessage(DELETE_ENTRIES)
				.replaceAll("%N", "")); //$NON-NLS-1$,  //$NON-NLS-2$,
		deleteEntriesButton.setEnabled(false);
	}

	// TODO reverseLookup verschieben
	private void doReverseLookup(int[] rows) {
		if (rows.length > 0) { // nur für markierte Einträge ReverseLookup
			// durchführen
			for (int i = 0; i < rows.length; i++) {
				Call call = (Call) callerList.getFilteredCallVector().get(
						rows[i]);
				Person newPerson = ReverseLookup.lookup(call.getPhoneNumber());
				if (newPerson != null) {
					JFritz.getPhonebook().addEntry(newPerson);
					JFritz.getPhonebook().fireTableDataChanged();
					callerList.fireTableDataChanged();
				}
			}
		} else { // Für alle Einträge ReverseLookup durchführen
			JFritz.getJframe().reverseLookup();
		}
	}

	public CallerList getCallerList() {
		return callerList;
	}

	public CallerTable getCallerTable() {
		return callerTable;
	}

	public ImageIcon getImageIcon(String filename) {
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/" + filename))); //$NON-NLS-1$
	}

	public Image getImage(String filename) {
		return Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/" + filename)); //$NON-NLS-1$
	}

	private void handleAction(String command) {
		if(callerTable!=null){
			CellEditor ce = callerTable.getCellEditor();
			if(ce!=null){
				ce.cancelCellEditing();
			}
		}
		if (command.equals(FILTER_CALLIN)) {
			syncFilterWithButton(filter[callIn], callInFilterButton);
			return;
		}
		if (command.equals(FILTER_CALLINFAILED)) {
			syncFilterWithButton(filter[callInFailed], callInFailedFilterButton);
			return;
		}
		if (command.equals(FILTER_CALLOUT)) {
			syncFilterWithButton(filter[callOut], callOutFilterButton);
			return;
		}
		if (command.equals(FILTER_COMMENT)) {
			syncFilterWithButton(filter[comment], commentFilterButton);
			return;
		}
		if (command.equals(FILTER_ANONYM)) {
			syncFilterWithButton(filter[anonym], anonymFilterButton);
			return;
		}
		if (command.equals(FILTER_FIXED)) {
			syncFilterWithButton(filter[fixed], fixedFilterButton);
			return;
		}
		if (command.equals(FILTER_HANDY)) {
			syncFilterWithButton(filter[handy], handyFilterButton);
			return;
		}

		if (command.equals(FILTER_SEARCH)) {
			if (searchFilterButton.getState() == ThreeStateButton3.NOTHING) {
				searchFilterTextField.setVisible(false);
				searchLabel.setVisible(false);
				filter[search].setEnabled(false);
			} else {
				searchFilterTextField.setVisible(true);
				searchLabel.setVisible(true);
				callerList.removeFilter(filter[search]);
				filter[search] = new SearchFilter(searchFilterTextField.getText());
				callerList.addFilter(filter[search]);
				// do nothing
				// if(searchFilterButton.getState()==ThreeStateButton.SELECTED)
				if (searchFilterButton.getState() == ThreeStateButton3.INVERTED) {
					filter[search].setInvert(true);
				}
			}

			return;
		}

		if (command.equals(FILTER_DATE)) {
			syncFilterWithButton(filter[date], dateFilterButton);
			if (dateFilterButton.getState() == ThreeStateButton3.NOTHING) {
				startDateChooser.setVisible(false);
				endDateChooser.setVisible(false);
			} else {
				//es sind Zeilen selektiert, also start und endDatum aus ihnen bestimmen
				if(callerTable != null && callerTable.getSelectedRowCount()!=0){
					int[] rows = callerTable.getSelectedRows();
					// min und max bestimmen
					Date min =  ((Call) callerList.getFilteredCallVector().get(rows[0])).getCalldate();
					Date max =  ((Call) callerList.getFilteredCallVector().get(rows[0])).getCalldate();
					Date current;
					for(int i =0; i< rows.length; i++){
						current = ((Call) callerList.getFilteredCallVector().get(rows[i])).getCalldate();
						if(current.before(min)){min = current;}
						if(current.after(max)){max = current;}
					}

					startDateChooser.setDate(min);
					endDateChooser.setDate(max);
				}else{//no rows selected so we only have the days and set the hours to min and max
					startDateChooser.setDate(JFritzUtils.setStartOfDay(startDateChooser.getDate()));
					endDateChooser.setDate(JFritzUtils.setEndOfDay(endDateChooser.getDate()));
				}
				callerList.removeFilter(filter[date]);
				filter[date] = new DateFilter(startDateChooser.getDate(),
					endDateChooser.getDate());
				callerList.addFilter(filter[date]);

				if (dateFilterButton.getState() == ThreeStateButton3.INVERTED) {
					filter[date].setInvert(true);
				}
				startDateChooser.setVisible(true);
				endDateChooser.setVisible(true);
			}
			dateSpecialSaveString ="";
			return;
		}

		if (command.equals(THIS_DAY)) { //$NON-NLS-1$
			setThisDayFilter();
			return;
		}
		if (command.equals(LAST_DAY)) { //$NON-NLS-1$
			setLastDayFilter();
			return;
		}
		if (command.equals(THIS_WEEK)) { //$NON-NLS-1$
			setThisWeekFilter();
			return;
		}
		if (command.equals(LAST_WEEK)) { //$NON-NLS-1$
			setLastWeekFilter();
			return;
		}

		if (command.equals(THIS_MONTH)) { //$NON-NLS-1$
			setThisMonthFilter();
			return;
		}
		if (command.equals(LAST_MONTH)) { //$NON-NLS-1$
			setLastMonthFilter();
			return;
		}
		if (command.equals("filter_callinfailed_allWithoutComment")) { //$NON-NLS-1$
			clearAllFilter();
			callInFailedFilterButton.setState(ThreeStateButton3.SELECTED);
			commentFilterButton.setState(ThreeStateButton3.INVERTED);
			syncAllFilters();
			return;
		}
		if (command.equals("filter_callinfailed_allWithoutCommentLastWeek")) { //$NON-NLS-1$
			clearAllFilter();
			callInFailedFilterButton.setState(ThreeStateButton3.SELECTED);
			commentFilterButton.setState(ThreeStateButton3.INVERTED);
			// dateFilter stuff for last week
			Calendar cal = Calendar.getInstance();
			Date start = cal.getTime();
			cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 7);
			Date end = cal.getTime();
			JFritzUtils.setStartOfDay(start);
			JFritzUtils.setEndOfDay(end);

			callerList.removeFilter(filter[date]);
			filter[date] = new DateFilter(end, start);
			callerList.addFilter(filter[date]);

			startDateChooser.setDate(end);
			endDateChooser.setDate(start);
			startDateChooser.setVisible(true);
			endDateChooser.setVisible(true);
			syncAllFilters();
			return;
		}
		if (command.equals(FILTER_SIP)) {
			callerList.removeFilter(filter[sip]);
			filter[sip] = createSipFilter(callerList);
			callerList.addFilter(filter[sip]);

			syncFilterWithButton(filter[sip], sipFilterButton);
			return;
		}
		if (command.equals(FILTER_CALLBYCALL)) {
			callerList.removeFilter(filter[callByCall]);
			filter[callByCall] = createCallByCallFilter(callerList);
			callerList.addFilter(filter[callByCall]);
			syncFilterWithButton(filter[callByCall], callByCallFilterButton);
			return;
		}
		if (command.equals("clearFilter")) { //$NON-NLS-1$
			clearAllFilter();
			return;
		}
		if (command.equals(DELETE_ENTRY)) {
			callerList.removeEntries();
			return;
		}
		if (command.equals("reverselookup")) { //$NON-NLS-1$
			doReverseLookup(callerTable.getSelectedRows());
			return;
		}
		if (command.equals("export_csv")) { //$NON-NLS-1$
			JFritz.getJframe().exportCallerListToCSV();
			return;
		}
		if (command.equals("export_xml")) { //$NON-NLS-1$
			JFritz.getJframe().exportCallerListToXML();
			return;
		}
		if (command.equals("import_callerlist_csv")) { //$NON-NLS-1$
			JFritz.getJframe().importCallerlistCSV();
			return;
		}
		if (command.equals("clipboard_number")) { //$NON-NLS-1$
			Call call = callerList.getSelectedCall();
			if (call != null) {
				PhoneNumber number = call.getPhoneNumber();
				if ((number != null) && (call != null)) {
					JFritzClipboard.copy(number.convertToNationalNumber());
				}
			}
			// JFritz.getJframe().copyNumberToClipboard();
			return;
		}
		if (command.equals("clipboard_adress")) { //$NON-NLS-1$
			Call call = callerList.getSelectedCall();
			if (call != null) {
				Person person = call.getPerson();
				if (person != null) {
					JFritzClipboard.copy(person.getAddress());
				}
			}
			// JFritz.getJframe().copyAddressToClipboard();

		}
		Debug.err("unknown command: "+command);
	}
	/**
	 * sets the start and endDateChoose to the appropriate values
	 * and set em visible
	 */
	private void setLastMonthFilter() {
		dateFilterButton.setState(ThreeStateButton3.SELECTED);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1); // last
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date start = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal
				.getActualMaximum(Calendar.DAY_OF_MONTH));
		Date end = cal.getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		callerList.removeFilter(filter[date]);
		filter[date] = new DateFilter(start, end);
		callerList.addFilter(filter[date]);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = LAST_MONTH;
	}
	/**
	 * sets the start and endDateChoose to the appropriate values
	 * and set em visible
	 */

	private void setThisMonthFilter() {
		dateFilterButton.setState(ThreeStateButton3.SELECTED);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date start = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal
				.getActualMaximum(Calendar.DAY_OF_MONTH));
		Date end = cal.getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		callerList.removeFilter(filter[date]);
		filter[date] = new DateFilter(start, end);
		callerList.addFilter(filter[date]);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = THIS_MONTH;
	}

	/**
	 * sets the start and endDateChoose to the appropriate values
	 * and set em visible
	 */
	private void setLastDayFilter() {
		dateFilterButton.setState(ThreeStateButton3.SELECTED);
		Calendar cal = Calendar.getInstance();
		Date end = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		Date start = cal.getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		callerList.removeFilter(filter[date]);
		filter[date] = new DateFilter(start, end);
		callerList.addFilter(filter[date]);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = LAST_DAY;
	}

	/**
	 * sets the start and endDateChoose to the appropriate values
	 * and set em visible
	 */
	private void setThisWeekFilter() {
		dateFilterButton.setState(ThreeStateButton3.SELECTED);
		Calendar cal = Calendar.getInstance();
		int daysPastMonday = (Calendar.DAY_OF_WEEK +(7-Calendar.MONDAY) )%7; //
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - daysPastMonday);
		Date start = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) +7);
		Date end = cal.getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		callerList.removeFilter(filter[date]);
		filter[date] = new DateFilter(start, end);
		callerList.addFilter(filter[date]);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = LAST_WEEK;
	}
	/**
	 * sets the start and endDateChoose to the appropriate values
	 * and set em visible
	 */
	private void setLastWeekFilter() {
		dateFilterButton.setState(ThreeStateButton3.SELECTED);
		Calendar cal = Calendar.getInstance();
		int daysPastMonday = (Calendar.DAY_OF_WEEK +(7-Calendar.MONDAY) )%7; //
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - daysPastMonday);
		Date end = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 7);
		Date start = cal.getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		callerList.removeFilter(filter[date]);
		filter[date] = new DateFilter(start, end);
		callerList.addFilter(filter[date]);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = LAST_WEEK;
	}

	/**
	 * sets the start and endDateChoose to the appropriate values
	 * and set em visible
	 */
	private void setThisDayFilter() {
		dateFilterButton.setState(ThreeStateButton3.SELECTED);
		Date start = Calendar.getInstance().getTime();
		Date end = Calendar.getInstance().getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		callerList.removeFilter(filter[date]);
		filter[date] = new DateFilter(start, end);
		callerList.addFilter(filter[date]);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = THIS_DAY;
	}

	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			handleAction(FILTER_SEARCH);
			callerList.update();
			parentFrame.setStatus();
			return;

		}

	}

	public void keyReleased(KeyEvent arg0) {
		// unnötig

	}

	public void keyTyped(KeyEvent arg0) {
		// unnötig

	}

	/**
	 * to react on the change events of the Datechoosers
	 *
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		handleAction(FILTER_DATE);
		callerList.update();
		parentFrame.setStatus();
	}


	/**
	 * syncronises all Filters with the states represented by the Buttons
	 *
	 */
	private void syncAllFilters() {
		syncFilterWithButton(filter[callIn], callInFilterButton);
		syncFilterWithButton(filter[callInFailed], callInFailedFilterButton);
		syncFilterWithButton(filter[callOut], callOutFilterButton);
		syncFilterWithButton(filter[comment], commentFilterButton);
		syncFilterWithButton(filter[anonym], anonymFilterButton);
		syncFilterWithButton(filter[fixed], fixedFilterButton);
		syncFilterWithButton(filter[handy], handyFilterButton);
		syncFilterWithButton(filter[date], dateFilterButton);
		syncFilterWithButton(filter[sip], sipFilterButton);
		syncFilterWithButton(filter[callByCall], callByCallFilterButton);
		syncFilterWithButton(filter[search], searchFilterButton);

		if (searchFilterButton.getState() == ThreeStateButton3.NOTHING) {
			searchFilterTextField.setVisible(false);
			searchLabel.setVisible(false);
		} else {
			searchFilterTextField.setVisible(true);
			searchLabel.setVisible(true);
		}
		if (dateFilterButton.getState() == ThreeStateButton3.NOTHING) {
			startDateChooser.setVisible(false);
			endDateChooser.setVisible(false);
		} else {
			startDateChooser.setVisible(true);
			endDateChooser.setVisible(true);
		}
	}

	public void setCallerList(CallerList callerList) {
		this.callerList = callerList;
	}

	public void setDeleteEntriesButton(int rows) {
		deleteEntriesButton.setToolTipText(Main.getMessage(DELETE_ENTRIES)
				.replaceAll("%N", Integer.toString(rows))); //$NON-NLS-1$,
		deleteEntriesButton.setEnabled(true);
	}

	public void setDeleteEntryButton() {
		deleteEntriesButton.setToolTipText(Main.getMessage(DELETE_ENTRY));
		deleteEntriesButton.setEnabled(true);
	}

	public void setDeleteListButton() {
		deleteEntriesButton.setToolTipText(Main.getMessage("delete_list")); //$NON-NLS-1$
		// clearList-Icon to big, so use std. delete.png
		// deleteEntriesButton.setIcon(getImage("clearList.png"));
		deleteEntriesButton.setEnabled(true);
	}

	/**
	 * syncronises a Filter with the state represented by the Button
	 *
	 * @param filter
	 *            the filter his values will be changed
	 * @param button
	 *            the button his status will be read
	 */
	private void syncFilterWithButton(CallFilter filter, ThreeStateButton3 button) {
		if (button.getState() == ThreeStateButton3.SELECTED) {
			filter.setEnabled(true);
			filter.setInvert(false);
//			Debug.msg("sel");
		}
		if (button.getState() == ThreeStateButton3.INVERTED) {
			filter.setEnabled(true);
			filter.setInvert(true);
//			Debug.msg("sel not");
		}
		if (button.getState() == ThreeStateButton3.NOTHING) {
			filter.setEnabled(false);
//			Debug.msg("nothing");
		}
	}

	/**
	 * writes the Button status to the Main Properties
	 *
	 */
	public void writeButtonStatus() {
		Debug.msg("writing Buttons");
		Main.setProperty(FILTER_SEARCH_TEXT, searchFilterTextField.getText());
		Main.setProperty(FILTER_SEARCH, "" + searchFilterButton.getState());
		Main.setProperty(FILTER_COMMENT, "" + commentFilterButton.getState());
		Main.setProperty(FILTER_DATE, "" + dateFilterButton.getState());
		DateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm");
		Date start = startDateChooser.getDate();
		Date end = endDateChooser.getDate();

		Main.setProperty(FILTER_DATE_START, df.format(start));
		Main.setProperty(FILTER_DATE_END, df.format(end));
		Main.setProperty(FILTER_DATE_SPECIAL, dateSpecialSaveString);
		Main.setProperty(FILTER_SIP, "" + sipFilterButton.getState());
		Main.setProperty(FILTER_CALLBYCALL, ""
				+ callByCallFilterButton.getState());
		Main.setProperty(FILTER_CALLOUT, "" + callOutFilterButton.getState());
		Main.setProperty(FILTER_ANONYM, "" + anonymFilterButton.getState());
		Main.setProperty(FILTER_FIXED, "" + fixedFilterButton.getState());
		Main.setProperty(FILTER_HANDY, "" + handyFilterButton.getState());
		Main.setProperty(FILTER_CALLIN, "" + callInFilterButton.getState());
		Main.setProperty(FILTER_CALLINFAILED, ""
				+ callInFailedFilterButton.getState());
	}
	/**
	 * read the status of the Buttons from the Main Properties
	 */
	private void readButtonStatus() {
		Debug.msg("reading Buttons");
		int state;
		state = JFritzUtils.parseInt(Main.getProperty(FILTER_COMMENT, "0"));
		commentFilterButton.setState(state);
		state = JFritzUtils.parseInt(Main.getProperty(FILTER_DATE, "0"));
		dateFilterButton.setState(state);
		state = JFritzUtils.parseInt(Main.getProperty(FILTER_SIP, "0"));
		sipFilterButton.setState(state);
		state = JFritzUtils.parseInt(Main.getProperty(FILTER_CALLBYCALL, "0"));
		callByCallFilterButton.setState(state);
		state = JFritzUtils.parseInt(Main.getProperty(FILTER_CALLOUT, "0"));
		callOutFilterButton.setState(state);
		state = JFritzUtils.parseInt(Main.getProperty(FILTER_ANONYM, "0"));
		anonymFilterButton.setState(state);
		state = JFritzUtils.parseInt(Main.getProperty(FILTER_FIXED, "0"));
		fixedFilterButton.setState(state);
		state = JFritzUtils.parseInt(Main.getProperty(FILTER_HANDY, "0"));
		handyFilterButton.setState(state);
		state = JFritzUtils.parseInt(Main.getProperty(FILTER_CALLIN, "0"));
		callInFilterButton.setState(state);
		state = JFritzUtils
		.parseInt(Main.getProperty(FILTER_CALLINFAILED, "0"));
		callInFailedFilterButton.setState(state);
		searchFilterTextField.setText(Main.getProperty(FILTER_SEARCH_TEXT, ""));
		state = JFritzUtils.parseInt(Main.getProperty(FILTER_SEARCH, "0"));
		searchFilterButton.setState(state);
		dateSpecialSaveString = Main.getProperty(FILTER_DATE_SPECIAL," ");
//		Debug.msg(dateSpecialSaveString);
		if(dateSpecialSaveString.equals(THIS_DAY)){
			setThisDayFilter();
		}else if(dateSpecialSaveString.equals(LAST_DAY)){
			setLastDayFilter();
		}else if(dateSpecialSaveString.equals(THIS_WEEK)){
			setThisWeekFilter();
		}else if(dateSpecialSaveString.equals(LAST_WEEK)){
			setLastWeekFilter();
		}else if(dateSpecialSaveString.equals(THIS_MONTH)){
			setThisMonthFilter();
		}else if(dateSpecialSaveString.equals(LAST_MONTH)){
			setLastMonthFilter();
		}else{ // read data
			DateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm");
			Date start = new Date();
			Date end = new Date();
			try {
				start = df.parse(Main.getProperty(FILTER_DATE_START,
				"11.11.11 11:11"));
				end = df.parse(Main.getProperty(FILTER_DATE_END, "11.11.11 11:11"));
				startDateChooser.setDate(start); // durch setDate wird über den
				endDateChooser.setDate(end); // PropertyListener update aufgerufen
			} catch (ParseException e) {
				startDateChooser.setDate(Calendar.getInstance().getTime());
				endDateChooser.setDate(Calendar.getInstance().getTime());
				Debug
				.err("error parsing date while loading dates from main properties "
						+ e.toString());
			}
		}
		syncAllFilters();
		callerList.update();
		parentFrame.setStatus();
	}

}
