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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.swing.CellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import com.toedter.calendar.JDateChooser;

import de.moonflower.jfritz.JFritz;
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
import de.moonflower.jfritz.phonebook.PhoneBookPanel;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzProperties;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.JFritzClipboard;
import de.moonflower.jfritz.utils.StatusBarController;
import de.moonflower.jfritz.utils.threeStateButton.ThreeStateButton;

/**
 * @author marc
 */
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
					&& (e.getComponent().getClass() == CallerTable.class)) {
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
//	private static final String FILTER_CALLIN ="filter_callin";
	private static final String FILTER_CALLIN_NOTHING = "filter_callin";
//	private static final String FILTER_CALLIN_SELECTED = "filter_callin_show_selected";
//	private static final String FILTER_CALLIN_INVERTED = "filter_callin_show_inverted";

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

	private static final String THIS_DAY = "date_filter.today";

	private static final String LAST_DAY = "date_filter.yesterday";

	private static final String THIS_WEEK = "date_filter.this_week";

	private static final String LAST_WEEK = "date_filter.last_week";

	private static final String THIS_MONTH = "date_filter.this_month";

	private static final String LAST_MONTH = "date_filter.last_month";

	private static final String FILTER_DATE_SPECIAL = "date_filter.special";

	private static final String PROPERTIES_FILE = "jfritz.callerlist.properties.xml";

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

	private ThreeStateButton dateFilterButton, callByCallFilterButton,
			callInFilterButton, callOutFilterButton, callInFailedFilterButton,
			anonymFilterButton, fixedFilterButton, handyFilterButton,
			sipFilterButton, commentFilterButton, searchFilterButton;

	private JButton deleteEntriesButton;

	private JDateChooser endDateChooser;

	private JTextField searchFilterTextField;

	private JLabel searchLabel;


	private JDateChooser startDateChooser;
	private PhoneBookPanel phoneBookPanel;
	private JFrame parentFrame;
	private StatusBarController statusBarController = new StatusBarController();

	private JFritzProperties properties;

	/**
	 * A callerListPanel is a view for a callerlist, it has its own
	 * resourceBundle to get the localized strings for its components. The
	 * components are a Toolbar and a table. th callerListPanel has a
	 * callerList, the model for this view
	 *
	 * @see CallerList
	 * @param callerList
	 *            the model
	 * @param parent
	 *            the parent frame to display some messages and set the status
	 *            bar
	 */

	public CallerListPanel(CallerList callerList, JFrame parent) {
		super();
		parentFrame = parent;
		properties = new JFritzProperties();
		try {
			properties.loadFromXML(Main.SAVE_DIR + PROPERTIES_FILE);
		} catch (FileNotFoundException e) {
			Debug.err("File " + PROPERTIES_FILE //$NON-NLS-1$
					+ " not found, using default values"); //$NON-NLS-1$
		} catch (Exception e) {
			Debug.err("Exception: " + e.toString()); //$NON-NLS-1$
		}
		this.callerList = callerList;
		createFilters(callerList);
		setLayout(new BorderLayout());
		add(createToolBar(), BorderLayout.NORTH);
		add(createCallerListTable(), BorderLayout.CENTER);
	}

	public void setPhoneBookPanel(PhoneBookPanel phoneBookPanel){
		this.phoneBookPanel = phoneBookPanel;
		if(phoneBookPanel==null){
			callerTable.setPhoneBookTable(null);
		}else{
			callerTable.setPhoneBookTable(phoneBookPanel.getPhoneBookTable());
		}
	}

	/**
	 * creates all filters and stores them in the array
	 *
	 * @param callerList
	 *            the model
	 */
	private void createFilters(CallerList callerList) {
		filter = new CallFilter[FILTERCOUNT];
		filter[callByCall] = new CallByCallFilter(
				getSelectedCallByCallProvider(callerList));
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
		filter[sip] = new SipFilter(getSelectedSipProvider(callerList));
		callerList.addFilter(filter[sip]);
		filter[date] = new DateFilter(new Date(), new Date());
		callerList.addFilter(filter[date]);
		filter[search] = new SearchFilter("");
		callerList.addFilter(filter[search]);
	}

	/**
	 * creates a SIPfilter using only the selected SIPProviders or <b>all</b>
	 * SIPProviders, if none is selected
	 *
	 * @param callerList
	 *            the callerlist to retrieve the SIPProviders
	 * @return the createdFilter
	 */
	private Vector getSelectedSipProvider(CallerList callerList) {
		Vector provider;
		if ((callerTable != null) && (callerTable.getSelectedRowCount() != 0)) {
			provider = callerList
					.getSelectedProviders(callerTable.getSelectedRows());
		} else {
			provider = callerList.getAllSipProviders();
		}
		return provider;
	}

	/**
	 * creates a CallByCallfilter using only the selected CallByCallProviders or
	 * all CallByCallProviders, if none is selected
	 *
	 * @param callerList
	 *            the callerlist to retrieve the CallByCallProviders
	 * @return the createdFilter
	 */

	private Vector getSelectedCallByCallProvider(CallerList callerList) {
		Vector provider;
		if ((callerTable != null) && (callerTable.getSelectedRowCount() != 0)) {
			provider = callerList
					.getCbCProviders(callerTable.getSelectedRows());
		} else {
			provider = callerList.getCbCProviders();
		}
		return provider;
	}


	/**
	 * disable all filters and hide the search and date stuff
	 */
	private void clearAllFilter() {
		callInFilterButton.setState(ThreeStateButton.NOTHING);
		callOutFilterButton.setState(ThreeStateButton.NOTHING);
		callInFailedFilterButton.setState(ThreeStateButton.NOTHING);
		anonymFilterButton.setState(ThreeStateButton.NOTHING);
		fixedFilterButton.setState(ThreeStateButton.NOTHING);
		handyFilterButton.setState(ThreeStateButton.NOTHING);
		dateFilterButton.setState(ThreeStateButton.NOTHING);
		searchFilterButton.setState(ThreeStateButton.NOTHING);
		searchFilterTextField.setVisible(false);
		startDateChooser.setVisible(false);
		endDateChooser.setVisible(false);
		searchLabel.setVisible(false);
		sipFilterButton.setState(ThreeStateButton.NOTHING);
		callByCallFilterButton.setState(ThreeStateButton.NOTHING);
		commentFilterButton.setState(ThreeStateButton.NOTHING);
		dateSpecialSaveString = " ";
		syncAllFilters();
		callerList.update();
		//((JFritzWindow) parentFrame).setStatus();
	}

	/**
	 * create gui
	 *
	 * @return a scrollPane with the callerListTable
	 */
	public JScrollPane createCallerListTable() {
		callerTable = new CallerTable(this, callerList);
		if(phoneBookPanel==null){
			callerTable.setPhoneBookTable(null);
		}else{
			callerTable.setPhoneBookTable(phoneBookPanel.getPhoneBookTable());
		}
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

	/**
	 * create gui
	 *
	 * @return the toolbar
	 */
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

		callInFilterButton = new ThreeStateButton(getImageIcon("callin.png"));
		callInFilterButton.setActionCommand(FILTER_CALLIN_NOTHING);
		callInFilterButton.addActionListener(this);
		callInFilterButton.setToolTipText(Main.getMessage(FILTER_CALLIN_NOTHING));

		callInFailedFilterButton = new ThreeStateButton(
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

		callOutFilterButton = new ThreeStateButton(getImageIcon("callout.png"));
		callOutFilterButton.setActionCommand(FILTER_CALLOUT);
		callOutFilterButton.addActionListener(this);
		callOutFilterButton.setToolTipText(Main.getMessage(FILTER_CALLOUT));

		anonymFilterButton = new ThreeStateButton(getImageIcon("mask.gif")); //$NON-NLS-1$
		anonymFilterButton.setActionCommand(FILTER_ANONYM);
		anonymFilterButton.addActionListener(this);
		anonymFilterButton.setToolTipText(Main.getMessage(FILTER_ANONYM));

		fixedFilterButton = new ThreeStateButton(getImageIcon("phone.png")); //$NON-NLS-1$
		fixedFilterButton.setActionCommand(FILTER_FIXED);
		fixedFilterButton.addActionListener(this);
		fixedFilterButton.setToolTipText(Main.getMessage(FILTER_FIXED));

		handyFilterButton = new ThreeStateButton(getImageIcon("handy.png")); //$NON-NLS-1$
		handyFilterButton.setActionCommand(FILTER_HANDY);
		handyFilterButton.addActionListener(this);
		handyFilterButton.setToolTipText(Main.getMessage(FILTER_HANDY));

		dateFilterButton = new ThreeStateButton(getImageIcon("calendar.png")); //$NON-NLS-1$

		dateFilterButton.setActionCommand(FILTER_DATE);
		dateFilterButton.addActionListener(this);
		dateFilterButton.setToolTipText(Main.getMessage(FILTER_DATE));

		JPopupMenu datePopupMenu = new JPopupMenu();
		menuItem = new JMenuItem(Main.getMessage(THIS_DAY));
		menuItem.setActionCommand(THIS_DAY);
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(Main.getMessage(LAST_DAY));
		menuItem.setActionCommand(LAST_DAY);
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(Main.getMessage(THIS_WEEK));
		menuItem.setActionCommand(THIS_WEEK);
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(Main.getMessage(LAST_WEEK));
		menuItem.setActionCommand(LAST_WEEK);
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);

		menuItem = new JMenuItem(Main.getMessage(THIS_MONTH));
		menuItem.setActionCommand(THIS_MONTH);
		menuItem.addActionListener(this);
		datePopupMenu.add(menuItem);
		menuItem = new JMenuItem(Main.getMessage(LAST_MONTH));
		menuItem.setActionCommand(LAST_MONTH);
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

		sipFilterButton = new ThreeStateButton(getImageIcon("world.png")); //$NON-NLS-1$
		sipFilterButton.setActionCommand(FILTER_SIP);
		sipFilterButton.addActionListener(this);
		sipFilterButton.setToolTipText(Main.getMessage(FILTER_SIP));

		callByCallFilterButton = new ThreeStateButton(
				getImageIcon("callbycall.png")); //$NON-NLS-1$
		callByCallFilterButton.setActionCommand(FILTER_CALLBYCALL);
		callByCallFilterButton.addActionListener(this);
		callByCallFilterButton.setToolTipText(Main
				.getMessage(FILTER_CALLBYCALL));

		commentFilterButton = new ThreeStateButton(
				getImageIcon("commentFilter.png"));
		commentFilterButton.setActionCommand(FILTER_COMMENT);
		commentFilterButton.addActionListener(this);
		commentFilterButton.setToolTipText(Main.getMessage(FILTER_COMMENT));

		searchFilterButton = new ThreeStateButton(
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
		loadButtonStatus();
		return toolbarPanel;
	}


	public CallerList getCallerList() {
		return callerList;
	}

	public CallerTable getCallerTable() {
		return callerTable;
	}

	/**
	 * get a imageIcon from a given filename
	 *
	 * @param filename
	 *            the name of the file
	 * @return the imageIcon
	 */
	public ImageIcon getImageIcon(String filename) {
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/" + filename))); //$NON-NLS-1$
	}

	/**
	 *
	 * get a image from a given filename
	 *
	 * @param filename
	 *            the name of the file
	 * @return the image
	 */

	public Image getImage(String filename) {
		return Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/" + filename)); //$NON-NLS-1$
	}
	/**
	 * some buttons are clicked.
	 *
	 * @see ActionListener
	 */
	public void actionPerformed(ActionEvent e) {
		handleAction(e.getActionCommand());
		callerList.update();
		statusBarController.fireStatusChanged(callerList.getTotalDuration());
	}


	/**
	 * the control this method is called, if the user pushed buttons or changes
	 * dates or the search filter for command use the constants
	 * <code> <br>FILTER_CALLIN, <br>FILTER_CALLOUT,<br>FILTER_COMMENT<br>...
	 * </code>
	 *
	 * @param command
	 */
	private void handleAction(String command) {
		if (callerTable != null) {
			CellEditor ce = callerTable.getCellEditor();
			if (ce != null) {
				ce.cancelCellEditing();
			}
		}
		if (command.equals(FILTER_CALLIN_NOTHING)) {
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
			if (searchFilterButton.getState() == ThreeStateButton.NOTHING) {
				searchFilterTextField.setVisible(false);
				searchLabel.setVisible(false);
				filter[search].setEnabled(false);
			} else {
				filter[search].setEnabled(true);
				searchFilterTextField.setVisible(true);
				searchLabel.setVisible(true);
				((SearchFilter) filter[search])
						.setSearchString(searchFilterTextField.getText());
				// do nothing
				if(searchFilterButton.getState()==ThreeStateButton.SELECTED){
					filter[search].setInvert(false);
				}
				if (searchFilterButton.getState() == ThreeStateButton.INVERTED) {
					filter[search].setInvert(true);
				}
			}

			return;
		}

		if (command.equals(FILTER_DATE)) {
			syncFilterWithButton(filter[date], dateFilterButton);
			if (dateFilterButton.getState() == ThreeStateButton.NOTHING) {
				startDateChooser.setVisible(false);
				endDateChooser.setVisible(false);
			} else { // selected or inverted check if some rows are selected
				if ((callerTable != null)
						&& (callerTable.getSelectedRowCount() != 0)) {
					// some rows are selected so the the date according to the
					// selected rows
					int[] rows = callerTable.getSelectedRows();
					// min und max bestimmen
					Date min = (callerList.getFilteredCallVector().get(
							rows[0])).getCalldate();
					Date max = (callerList.getFilteredCallVector().get(
							rows[0])).getCalldate();
					Date current;
					for (int i = 0; i < rows.length; i++) {
						current = (callerList.getFilteredCallVector()
								.get(rows[i])).getCalldate();
						if (current.before(min)) {
							min = current;
						}
						if (current.after(max)) {
							max = current;
						}
						Debug.msg("currentDate: " + current);
					}
					Debug.msg("minDate: " + min);
					Debug.msg("maxDate: " + max);

					startDateChooser.setDate(min);
					endDateChooser.setDate(max);
				} else {// no rows selected so we only have the days and set the
						// hours to min and max
					startDateChooser.setDate(JFritzUtils
							.setStartOfDay(startDateChooser.getDate()));
					endDateChooser.setDate(JFritzUtils
							.setEndOfDay(endDateChooser.getDate()));
				}
				((DateFilter) filter[date]).setStartDate(startDateChooser
						.getDate());
				((DateFilter) filter[date])
						.setEndDate(endDateChooser.getDate());
				if (dateFilterButton.getState() == ThreeStateButton.INVERTED) {
					filter[date].setInvert(true);
				}
				startDateChooser.setVisible(true);
				endDateChooser.setVisible(true);
			}
			dateSpecialSaveString = "";
			return;
		}

		if (command.equals(THIS_DAY)) {
			setThisDayFilter();
			return;
		}
		if (command.equals(LAST_DAY)) {
			setLastDayFilter();
			return;
		}
		if (command.equals(THIS_WEEK)) {
			setThisWeekFilter();
			return;
		}
		if (command.equals(LAST_WEEK)) {
			setLastWeekFilter();
			return;
		}

		if (command.equals(THIS_MONTH)) {
			setThisMonthFilter();
			return;
		}
		if (command.equals(LAST_MONTH)) {
			setLastMonthFilter();
			return;
		}
		if (command.equals("filter_callinfailed_allWithoutComment")) { //$NON-NLS-1$
			clearAllFilter();
			callInFailedFilterButton.setState(ThreeStateButton.SELECTED);
			commentFilterButton.setState(ThreeStateButton.INVERTED);
			syncAllFilters();
			return;
		}
		if (command.equals("filter_callinfailed_allWithoutCommentLastWeek")) { //$NON-NLS-1$
			clearAllFilter();
			callInFailedFilterButton.setState(ThreeStateButton.SELECTED);
			commentFilterButton.setState(ThreeStateButton.INVERTED);
			// dateFilter stuff for last week
			Calendar cal = Calendar.getInstance();
			Date start = cal.getTime();
			cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 7);
			Date end = cal.getTime();
			JFritzUtils.setStartOfDay(start);
			JFritzUtils.setEndOfDay(end);

			((DateFilter) filter[date]).setStartDate(start);
			((DateFilter) filter[date]).setEndDate(end);

			startDateChooser.setDate(end);
			endDateChooser.setDate(start);
			startDateChooser.setVisible(true);
			endDateChooser.setVisible(true);
			syncAllFilters();
			return;
		}
		if (command.equals(FILTER_SIP)) {
			((SipFilter) filter[sip]).setProvider(getSelectedSipProvider(callerList));
			syncFilterWithButton(filter[sip], sipFilterButton);
			return;
		}
		if (command.equals(FILTER_CALLBYCALL)) {
			((CallByCallFilter) filter[callByCall])
					.setCallbyCallProvider(getSelectedCallByCallProvider(callerList));
			syncFilterWithButton(filter[callByCall], callByCallFilterButton);
			return;
		}
		if (command.equals("clearFilter")) { //$NON-NLS-1$
			clearAllFilter();
			return;
		}
		if (command.equals(DELETE_ENTRY)) {
			if (JOptionPane.showConfirmDialog(parentFrame, Main
					.getMessage("really_delete_entries"), //$NON-NLS-1$
					Main.PROGRAM_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				Debug.msg("Removing entries"); //$NON-NLS-1$
				int rows[] = callerTable.getSelectedRows();
				callerList.removeEntries(rows);
			}
			return;
		}
		if (command.equals("reverselookup")) { //$NON-NLS-1$
			callerList.doReverseLookup(callerTable.getSelectedRows());
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
		Debug.err("unknown command: " + command);
	}

	/**
	 * sets the start and endDateChoose to the appropriate values and set em
	 * visible
	 */
	private void setLastMonthFilter() {
		dateFilterButton.setState(ThreeStateButton.SELECTED);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1); // last
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date start = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal
				.getActualMaximum(Calendar.DAY_OF_MONTH));
		Date end = cal.getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		((DateFilter) filter[date]).setStartDate(start);
		((DateFilter) filter[date]).setEndDate(end);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = LAST_MONTH;
	}

	/**
	 * sets the start and endDateChoose to the appropriate values and set em
	 * visible
	 */

	private void setThisMonthFilter() {
		dateFilterButton.setState(ThreeStateButton.SELECTED);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date start = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal
				.getActualMaximum(Calendar.DAY_OF_MONTH));
		Date end = cal.getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		((DateFilter) filter[date]).setStartDate(start);
		((DateFilter) filter[date]).setEndDate(end);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = THIS_MONTH;
	}

	/**
	 * sets the start and endDateChoose to the appropriate values and set em
	 * visible
	 */
	private void setLastDayFilter() {
		dateFilterButton.setState(ThreeStateButton.SELECTED);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
		Date end = cal.getTime();
		Date start = cal.getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		((DateFilter) filter[date]).setStartDate(start);
		((DateFilter) filter[date]).setEndDate(end);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = LAST_DAY;
	}

	/**
	 * sets the start and endDateChoose to the appropriate values and set em
	 * visible
	 */
	private void setThisWeekFilter() {
		dateFilterButton.setState(ThreeStateButton.SELECTED);
		Calendar cal = Calendar.getInstance();
		int daysPastMonday = (Calendar.DAY_OF_WEEK + (7 - Calendar.MONDAY)) % 7; //
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)
				- daysPastMonday);
		Date start = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 7);
		Date end = cal.getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		((DateFilter) filter[date]).setStartDate(start);
		((DateFilter) filter[date]).setEndDate(end);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = LAST_WEEK;
	}

	/**
	 * sets the start and endDateChoose to the appropriate values and set em
	 * visible
	 */
	private void setLastWeekFilter() {
		dateFilterButton.setState(ThreeStateButton.SELECTED);
		Calendar cal = Calendar.getInstance();
		int daysPastMonday = (Calendar.DAY_OF_WEEK + (7 - Calendar.MONDAY)) % 7; //
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)
				- daysPastMonday);
		Date end = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 7);
		Date start = cal.getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		((DateFilter) filter[date]).setStartDate(start);
		((DateFilter) filter[date]).setEndDate(end);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = LAST_WEEK;
	}

	/**
	 * sets the start and endDateChoose to the appropriate values and set em
	 * visible
	 */
	private void setThisDayFilter() {
		dateFilterButton.setState(ThreeStateButton.SELECTED);
		Date start = Calendar.getInstance().getTime();
		Date end = Calendar.getInstance().getTime();
		JFritzUtils.setStartOfDay(start);
		JFritzUtils.setEndOfDay(end);

		((DateFilter) filter[date]).setStartDate(start);
		((DateFilter) filter[date]).setEndDate(end);

		startDateChooser.setDate(start);
		endDateChooser.setDate(end);
		startDateChooser.setVisible(true);
		endDateChooser.setVisible(true);
		dateSpecialSaveString = THIS_DAY;
	}

	/**
	 * (KeyListener) for the searchButton TextField
	 */

	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			handleAction(FILTER_SEARCH);
			callerList.update();
			statusBarController.fireStatusChanged(callerList.getTotalDuration());
			return;
		}
	}

	/**
	 * (KeyListener) for the searchButton TextField
	 */

	public void keyReleased(KeyEvent arg0) {
	}

	/**
	 * (KeyListener) for the searchButton TextField
	 */
	public void keyTyped(KeyEvent arg0) {
	}

	/**
	 * to react on the change events of the Datechoosers PropertyChangedListener
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		handleAction(FILTER_DATE);
		callerList.update();
		statusBarController.fireStatusChanged(callerList.getTotalDuration());
	}

	/**
	 * syncronises all Filters with the states represented by the Buttons
	 *
	 */
	private void syncAllFilters() {
		if (searchFilterButton.getState() == ThreeStateButton.NOTHING) {
			searchFilterTextField.setVisible(false);
			searchLabel.setVisible(false);
		} else {
			searchFilterTextField.setVisible(true);
			searchLabel.setVisible(true);
		}
		if (dateFilterButton.getState() == ThreeStateButton.NOTHING) {
			startDateChooser.setVisible(false);
			endDateChooser.setVisible(false);
		} else {
			startDateChooser.setVisible(true);
			endDateChooser.setVisible(true);
		}
		((DateFilter)filter[date]).setStartDate(startDateChooser.getDate());
		((DateFilter)filter[date]).setEndDate(endDateChooser.getDate());
		((SearchFilter)filter[search]).setSearchString(searchFilterTextField.getText());
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

	}

	public void setCallerList(CallerList callerList) {
		this.callerList = callerList;
	}

	/**
	 * enables/disables the delete button and changes the tooltip
	 *
	 * @param rows
	 *            the rows, wich are selected
	 */
	public void setDeleteEntriesButton(int rows) {
		if(rows==0){
			deleteEntriesButton.setToolTipText(Main.getMessage(DELETE_ENTRIES)
					.replaceAll("%N", "")); //$NON-NLS-1$,  //$NON-NLS-2$,
			deleteEntriesButton.setEnabled(false);
		}
		else if(rows ==1){
			deleteEntriesButton.setToolTipText(Main.getMessage(DELETE_ENTRY));
			deleteEntriesButton.setEnabled(true);
		}else{
			deleteEntriesButton.setToolTipText(Main.getMessage(DELETE_ENTRIES)
					.replaceAll("%N", Integer.toString(rows))); //$NON-NLS-1$,
			deleteEntriesButton.setEnabled(true);
		}
	}

	/**
	 * syncronises a Filter with the state represented by the Button
	 *
	 * @param filter
	 *            the filter his values will be changed
	 * @param button
	 *            the button his status will be read
	 */
	private void syncFilterWithButton(CallFilter filter,
			ThreeStateButton button) {
		if (button.getState() == ThreeStateButton.SELECTED) {
			filter.setEnabled(true);
			filter.setInvert(false);
		}
		if (button.getState() == ThreeStateButton.INVERTED) {
			filter.setEnabled(true);
			filter.setInvert(true);
		}
		if (button.getState() == ThreeStateButton.NOTHING) {
			filter.setEnabled(false);
		}
	}

	/**
	 * save the Button status to the Main Properties
	 *
	 */
	private void saveButtonStatus() {
		Debug.msg("writing Buttons");
		properties.setProperty(FILTER_SEARCH_TEXT, searchFilterTextField.getText());
		properties.setProperty(FILTER_SEARCH, "" + searchFilterButton.getState());
		properties.setProperty(FILTER_COMMENT, "" + commentFilterButton.getState());
		properties.setProperty(FILTER_DATE, "" + dateFilterButton.getState());
		DateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm");
		Date start = startDateChooser.getDate();
		Date end = endDateChooser.getDate();

		properties.setProperty(FILTER_DATE_START, df.format(start));
		properties.setProperty(FILTER_DATE_END, df.format(end));
		properties.setProperty(FILTER_DATE_SPECIAL, dateSpecialSaveString);
		properties.setProperty(FILTER_SIP, "" + sipFilterButton.getState());
		properties.setProperty(FILTER_CALLBYCALL, ""
				+ callByCallFilterButton.getState());
		properties.setProperty(FILTER_CALLOUT, "" + callOutFilterButton.getState());
		properties.setProperty(FILTER_ANONYM, "" + anonymFilterButton.getState());
		properties.setProperty(FILTER_FIXED, "" + fixedFilterButton.getState());
		properties.setProperty(FILTER_HANDY, "" + handyFilterButton.getState());
		properties.setProperty(FILTER_CALLIN_NOTHING, "" + callInFilterButton.getState());
		properties.setProperty(FILTER_CALLINFAILED, ""
				+ callInFailedFilterButton.getState());
	}

	/**
	 * load the status of the Buttons from the Main Properties
	 */
	private void loadButtonStatus() {
		Debug.msg("reading Buttons");
		int state;
		state = JFritzUtils.parseInt(properties.getProperty(FILTER_COMMENT, "0"));
		commentFilterButton.setState(state);
		state = JFritzUtils.parseInt(properties.getProperty(FILTER_DATE, "0"));
		dateFilterButton.setState(state);
		state = JFritzUtils.parseInt(properties.getProperty(FILTER_SIP, "0"));
		sipFilterButton.setState(state);
		state = JFritzUtils.parseInt(properties.getProperty(FILTER_CALLBYCALL, "0"));
		callByCallFilterButton.setState(state);
		state = JFritzUtils.parseInt(properties.getProperty(FILTER_CALLOUT, "0"));
		callOutFilterButton.setState(state);
		state = JFritzUtils.parseInt(properties.getProperty(FILTER_ANONYM, "0"));
		anonymFilterButton.setState(state);
		state = JFritzUtils.parseInt(properties.getProperty(FILTER_FIXED, "0"));
		fixedFilterButton.setState(state);
		state = JFritzUtils.parseInt(properties.getProperty(FILTER_HANDY, "0"));
		handyFilterButton.setState(state);
		state = JFritzUtils.parseInt(properties.getProperty(FILTER_CALLIN_NOTHING, "0"));
		callInFilterButton.setState(state);
		state = JFritzUtils
				.parseInt(properties.getProperty(FILTER_CALLINFAILED, "0"));
		callInFailedFilterButton.setState(state);
		searchFilterTextField.setText(properties.getProperty(FILTER_SEARCH_TEXT, ""));

		state = JFritzUtils.parseInt(properties.getProperty(FILTER_SEARCH, "0"));
		searchFilterButton.setState(state);
		dateSpecialSaveString = properties.getProperty(FILTER_DATE_SPECIAL, " ");
		// Debug.msg(dateSpecialSaveString);
		if (dateSpecialSaveString.equals(THIS_DAY)) {
			setThisDayFilter();
		} else if (dateSpecialSaveString.equals(LAST_DAY)) {
			setLastDayFilter();
		} else if (dateSpecialSaveString.equals(THIS_WEEK)) {
			setThisWeekFilter();
		} else if (dateSpecialSaveString.equals(LAST_WEEK)) {
			setLastWeekFilter();
		} else if (dateSpecialSaveString.equals(THIS_MONTH)) {
			setThisMonthFilter();
		} else if (dateSpecialSaveString.equals(LAST_MONTH)) {
			setLastMonthFilter();
		} else { // read data
			DateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm");
			Date start = new Date();
			Date end = new Date();
			try {
				start = df.parse(properties.getProperty(FILTER_DATE_START,
						"11.11.11 11:11"));
				end = df.parse(properties.getProperty(FILTER_DATE_END,
						"11.11.11 11:11"));
				startDateChooser.setDate(start); // durch setDate wird über
													// den
				endDateChooser.setDate(end); // PropertyListener update
												// aufgerufen
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
		statusBarController.fireStatusChanged(callerList.getTotalDuration());
	}

	/**
	 * Speichert alle Properties, die zum CallerListPanel gehören.
	 *
	 */
	public void saveProperties() {
		saveButtonStatus();
		callerTable.saveColumnStatus();
		try {
			Debug.msg("Save other properties"); //$NON-NLS-1$
			properties.storeToXML(Main.SAVE_DIR + PROPERTIES_FILE);
		} catch (IOException e) {
			Debug.err("Couldn't save Properties"); //$NON-NLS-1$
		}
	}

	/**
	 * Zeigt bzw. versteckt die Spalten je nach Einstellung vom Benutzer
	 *
	 */
	public void showHideColumns() {
		callerTable.showHideColumns();
	}

	public StatusBarController getStatusBarController() {
		return statusBarController;
	}

	public void setStatusBarController(StatusBarController statusBarController) {
		this.statusBarController = statusBarController;
	}

}